import time
import io
import base64
import socket
import os
from threading import Thread
from flask import Flask, render_template
from flask_socketio import SocketIO, emit
from PIL import Image 

# --- KONFIGURASI ---
# Di Linux dengan Socket, kita tidak butuh port '/dev/rfcomm0'
# Kita pakai Channel 1 (Standar Serial Port Profile)
RFCOMM_CHANNEL = 1 

app = Flask(__name__, template_folder='public', static_folder='public', static_url_path='')
app.config['SECRET_KEY'] = 'rahasia'
socketio = SocketIO(app, async_mode='threading')

# Variable Global
client_socket = None
is_connected = False

@app.route('/')
def index():
    return render_template('printer.html')

@socketio.on('get_status')
def handle_get_status():
    global is_connected
    status = 'connected' if is_connected else 'disconnected'
    emit('status_update', {'status': status})

@socketio.on('force_disconnect')
def handle_force_disconnect():
    global client_socket, is_connected
    print("⚠️ Memutus koneksi paksa...")
    if client_socket:
        try:
            client_socket.close()
        except: pass
    client_socket = None
    is_connected = False
    emit('status_update', {'status': 'disconnected'})

# --- ENGINE 1: IMAGE PROCESSING (TETAP SAMA) ---
def process_raster_image(data_buffer):
    try:
        header_index = data_buffer.find(b'\x1d\x76\x30')
        if header_index != -1:
            if len(data_buffer) < header_index + 8:
                return None, data_buffer 

            idx = header_index + 4 
            xL, xH = data_buffer[idx], data_buffer[idx+1]
            yL, yH = data_buffer[idx+2], data_buffer[idx+3]
            
            width_bytes = xL + (xH * 256)
            height_dots = yL + (yH * 256)
            width_pixels = width_bytes * 8
            total_data_size = width_bytes * height_dots
            
            start_data = idx + 4
            end_data = start_data + total_data_size
            
            if len(data_buffer) < end_data:
                return None, data_buffer 
            
            img_data = data_buffer[start_data:end_data]
            try:
                img = Image.frombytes('1', (width_pixels, height_dots), img_data)
                if width_pixels > 380:
                    ratio = 380 / float(width_pixels)
                    new_height = int(height_dots * ratio)
                    img = img.resize((380, new_height))
                
                buffered = io.BytesIO()
                img.save(buffered, format="PNG")
                img_str = base64.b64encode(buffered.getvalue()).decode("utf-8")
                return img_str, data_buffer[end_data:]
            except:
                return None, data_buffer[header_index+3:]
    except:
        pass
    return None, data_buffer

# --- ENGINE 2: SMART TEXT PARSER (TETAP SAMA) ---
def parse_esc_pos_chunk(raw_bytes):
    clean_output = []
    i = 0
    length = len(raw_bytes)
    current_align = 'left' 
    is_bold = False
    
    while i < length:
        byte = raw_bytes[i]
        if byte == 0x1B: 
            if i + 1 < length:
                cmd = raw_bytes[i+1]
                if cmd == 0x61: 
                    if i + 2 < length:
                        val = raw_bytes[i+2]
                        if val in [0, 48]: current_align = 'left'
                        elif val in [1, 49]: current_align = 'center'
                        elif val in [2, 50]: current_align = 'right'
                        i += 3; continue
                elif cmd == 0x45: 
                    if i + 2 < length:
                        is_bold = (raw_bytes[i+2] in [1, 49])
                        i += 3; continue
                elif cmd in [0x21, 0x40]:
                    i += 2; continue
            i += 1; continue
        elif 32 <= byte <= 126:
            clean_output.append(chr(byte))
            i += 1
        elif byte == 0x0A:
            text = "".join(clean_output).strip()
            if text: return {"text": text, "align": current_align, "bold": is_bold}
            clean_output = []; i += 1
        else:
            i += 1
    text = "".join(clean_output).strip()
    if text: return {"text": text, "align": current_align, "bold": is_bold}
    return None

# --- THREAD UTAMA (VERSI LINUX SOCKET) ---
def start_bluetooth_server():
    global client_socket, is_connected
    
    # Membuat Socket Bluetooth RFCOMM
    server_sock = socket.socket(socket.AF_BLUETOOTH, socket.SOCK_STREAM, socket.BTPROTO_RFCOMM)
    server_sock.bind(("", RFCOMM_CHANNEL))
    server_sock.listen(1)

    print(f"🚀 Bluetooth Service Started on Channel {RFCOMM_CHANNEL}")
    print("👉 Silakan Scan & Connect dari HP sekarang...")

    while True:
        try:
            # Menunggu Koneksi Masuk
            if client_socket is None:
                socketio.emit('status_update', {'status': 'disconnected'})
                client_sock, address = server_sock.accept()
                print(f"✅ TERHUBUNG dengan {address}")
                
                client_socket = client_sock
                is_connected = True
                socketio.emit('status_update', {'status': 'connected'})
                
                master_buffer = b''

            # Membaca Data
            if client_socket:
                try:
                    data = client_socket.recv(1024)
                    if not data: 
                        # Jika data kosong, artinya client putus
                        raise Exception("Connection closed by peer")
                    
                    master_buffer += data
                    
                    # A. Cek Gambar
                    base64_img, remaining_buffer = process_raster_image(master_buffer)
                    if base64_img:
                        print("🖼️ Mencetak Gambar...")
                        socketio.emit('print-image', {'image': base64_img})
                        master_buffer = remaining_buffer
                        continue 

                    # B. Cek Teks
                    if b'\n' in master_buffer or len(master_buffer) > 200:
                        if b'\x1d\x76' in master_buffer and len(master_buffer) < 1000:
                            pass 
                        else:
                            lines = master_buffer.split(b'\n')
                            for line in lines:
                                if not line: continue
                                result = parse_esc_pos_chunk(line)
                                if result:
                                    print(f"🖨️ Teks: {result['text']}")
                                    socketio.emit('print-output', result)
                            master_buffer = b''
                            
                except Exception as e:
                    print(f"⚠️ Koneksi Putus: {e}")
                    client_socket.close()
                    client_socket = None
                    is_connected = False
                    socketio.emit('status_update', {'status': 'disconnected'})
                    master_buffer = b''
                    
        except Exception as e:
            print(f"❌ Server Error: {e}")
            time.sleep(1)

if __name__ == '__main__':
    # Pastikan dijalankan dengan sudo di Linux
    if os.geteuid() != 0:
        print("⚠️ PERINGATAN: Jalankan script ini dengan 'sudo' agar Bluetooth bisa diakses!")
    
    bt_thread = Thread(target=start_bluetooth_server)
    bt_thread.daemon = True
    bt_thread.start()
    
    print("🌐 Web Server di http://localhost:5000")
    socketio.run(app, host='0.0.0.0', port=5000, debug=False)