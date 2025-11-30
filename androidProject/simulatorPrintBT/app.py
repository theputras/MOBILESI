import serial
import time
import io
import base64
from threading import Thread
from flask import Flask, render_template
from flask_socketio import SocketIO, emit
from PIL import Image 

# --- KONFIGURASI ---
BLUETOOTH_PORT = 'COM4'  # Sesuaikan port kamu
BAUD_RATE = 9600

# app = Flask(__name__, template_folder='public', static_folder='public')
app = Flask(__name__, template_folder='public', static_folder='public', static_url_path='')
app.config['SECRET_KEY'] = 'rahasia'
socketio = SocketIO(app, async_mode='threading')

# Variable Global
ser = None 
is_connected = False

@app.route('/')
def index():
    return render_template('printer.html')

# --- EVENT HANDLER (BAGIAN INI YANG HILANG DI KODE KAMU) ---
@socketio.on('get_status')
def handle_get_status():
    global is_connected
    # Jawab pertanyaan browser: Connected atau Disconnected?
    status = 'connected' if is_connected else 'disconnected'
    emit('status_update', {'status': status})

@socketio.on('force_disconnect')
def handle_force_disconnect():
    global ser, is_connected
    print("⚠️ Memutus koneksi paksa...")
    if ser is not None:
        try:
            ser.close()
        except: pass
    ser = None
    is_connected = False
    emit('status_update', {'status': 'disconnected'})

# --- ENGINE 1: IMAGE PROCESSING ---
def process_raster_image(data_buffer):
    """ Mencari perintah GS v 0 dan convert jadi Base64 """
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

# --- ENGINE 2: SMART TEXT PARSER (PENGGANTI REGEX) ---
def parse_esc_pos_chunk(raw_bytes):
    """ Membaca byte per byte untuk hindari sampah !a !E """
    clean_output = []
    i = 0
    length = len(raw_bytes)
    current_align = 'left' 
    is_bold = False
    
    while i < length:
        byte = raw_bytes[i]
        
        # Deteksi ESC (0x1B)
        if byte == 0x1B: 
            if i + 1 < length:
                cmd = raw_bytes[i+1]
                # ESC a n (Alignment)
                if cmd == 0x61: 
                    if i + 2 < length:
                        val = raw_bytes[i+2]
                        if val in [0, 48]: current_align = 'left'
                        elif val in [1, 49]: current_align = 'center'
                        elif val in [2, 50]: current_align = 'right'
                        i += 3; continue
                # ESC E n (Bold)
                elif cmd == 0x45: 
                    if i + 2 < length:
                        is_bold = (raw_bytes[i+2] in [1, 49])
                        i += 3; continue
                elif cmd in [0x21, 0x40]: # Print mode / Init
                    i += 2; continue
            i += 1; continue
            
        # Karakter Biasa
        elif 32 <= byte <= 126:
            clean_output.append(chr(byte))
            i += 1
        elif byte == 0x0A: # Newline
            text = "".join(clean_output).strip()
            if text: return {"text": text, "align": current_align, "bold": is_bold}
            clean_output = []; i += 1
        else:
            i += 1
            
    text = "".join(clean_output).strip()
    if text: return {"text": text, "align": current_align, "bold": is_bold}
    return None

# --- THREAD UTAMA ---
def read_bluetooth_serial():
    global ser, is_connected
    print(f"🔄 Menunggu koneksi Bluetooth di {BLUETOOTH_PORT}...")
    
    master_buffer = b''

    while True:
        try:
            # 1. LOGIKA KONEKSI
            if ser is None:
                try:
                    ser = serial.Serial(BLUETOOTH_PORT, BAUD_RATE, timeout=0.1)
                    is_connected = True
                    print(f"✅ TERHUBUNG ke {BLUETOOTH_PORT}!")
                    socketio.emit('status_update', {'status': 'connected'})
                except:
                    time.sleep(1)
                    continue

            # 2. LOGIKA MEMBACA DATA
            if ser is not None and ser.is_open:
                if ser.in_waiting > 0:
                    chunk = ser.read(ser.in_waiting)
                    master_buffer += chunk
                    
                    # A. Cek Gambar
                    base64_img, remaining_buffer = process_raster_image(master_buffer)
                    if base64_img:
                        print("🖼️ Mencetak Gambar...")
                        socketio.emit('print-image', {'image': base64_img})
                        master_buffer = remaining_buffer
                        continue 

                    # B. Cek Teks (jika ada newline atau buffer penuh)
                    if b'\n' in master_buffer or len(master_buffer) > 200:
                        # Cek jangan sampai memotong header gambar
                        if b'\x1d\x76' in master_buffer and len(master_buffer) < 1000:
                            pass 
                        else:
                            # Pecah per baris untuk parser
                            lines = master_buffer.split(b'\n')
                            # Simpan sisa potongan terakhir yang belum lengkap (opsional, disimplekan disini)
                            
                            for line in lines:
                                if not line: continue
                                result = parse_esc_pos_chunk(line)
                                if result:
                                    print(f"🖨️ Teks: {result['text']}")
                                    socketio.emit('print-output', result)
                            
                            master_buffer = b'' # Reset buffer setelah diproses

            time.sleep(0.05)

        except Exception as e:
            print(f"Error: {e}")
            ser = None
            is_connected = False
            socketio.emit('status_update', {'status': 'disconnected'})
            time.sleep(1)

if __name__ == '__main__':
    bt_thread = Thread(target=read_bluetooth_serial)
    bt_thread.daemon = True
    bt_thread.start()
    print("🚀 Server berjalan di http://localhost:5000")
    socketio.run(app, debug=False, port=5000)