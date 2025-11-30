import serial
import time
from threading import Thread
from flask import Flask, render_template
from flask_socketio import SocketIO, emit
import re

# --- KONFIGURASI ---
BLUETOOTH_PORT = 'COM4'  # Sesuaikan port kamu
BAUD_RATE = 9600

app = Flask(__name__, template_folder='public', static_folder='public')
app.config['SECRET_KEY'] = 'rahasia'
socketio = SocketIO(app, async_mode='threading')

# --- VARIABLE GLOBAL ---
# Kita taruh di luar supaya bisa diakses dari mana saja
ser = None 
is_connected = False

@app.route('/')
def index():
    return render_template('printer.html')

# --- EVENT HANDLER: DISCONNECT ---
@socketio.on('force_disconnect')
def handle_force_disconnect():
    global ser, is_connected
    print("\n⚠️  Permintaan Disconnect diterima dari Browser...")
    
    if ser is not None and ser.is_open:
        try:
            ser.close()
            print("✅ Koneksi Bluetooth diputus paksa.")
        except Exception as e:
            print(f"Error saat menutup: {e}")
    
    # Reset variabel supaya loop utama mulai mencari koneksi baru lagi
    ser = None
    is_connected = False
    
    # Kabari browser bahwa sudah putus
    socketio.emit('status_update', {'status': 'disconnected'})
@socketio.on('get_status')
def handle_get_status():
    global is_connected
    current_status = 'connected' if is_connected else 'disconnected'
    # Kirim balik status ke browser yang bertanya saja
    emit('status_update', {'status': current_status})
# --- THREAD UTAMA: BACA BLUETOOTH ---
def read_bluetooth_serial():
    global ser, is_connected
    print(f"🔄 Menunggu koneksi Bluetooth di {BLUETOOTH_PORT}...")
    
    while True:
        try:
            # 1. Jika belum connect, coba buka port
            if ser is None:
                try:
                    ser = serial.Serial(BLUETOOTH_PORT, BAUD_RATE, timeout=1)
                    is_connected = True
                    print(f"✅ TERHUBUNG ke {BLUETOOTH_PORT}!")
                    socketio.emit('status_update', {'status': 'connected'})
                except serial.SerialException:
                    time.sleep(1)
                    continue

            # 2. Jika sudah connect, baca data
            if ser is not None and ser.is_open:
                if ser.in_waiting > 0:
                    try:
                        # Baca raw bytes dulu
                        raw_data = ser.readline()
                        
                        # --- PERBAIKAN UTAMA DISINI ---
                        # Gunakan 'replace' supaya kalau ada karakter aneh, diganti tanda tanya 
                        # Jangan biarkan error decode memutus koneksi!
                        try:
                            # Coba decode UTF-8 (standar web)
                            text_data = raw_data.decode('utf-8', errors='replace').strip()
                        except:
                            # Kalau gagal total, paksa decode pake CP437 (standar printer jadul)
                            text_data = raw_data.decode('cp437', errors='ignore').strip()

                        if text_data:
                            # Filter karakter sampah (ESC/POS commands)
                            clean_text = re.sub(r'[\x00-\x1F\x7F]', '', text_data)
                            clean_text = clean_text.replace('@', '').replace('!', '') if len(clean_text) < 3 else clean_text
                            
                            if clean_text.strip():
                                print(f"🖨️  Mencetak: {clean_text}")
                                socketio.emit('print-output', {
                                    'text': clean_text,
                                    'align': 'left', 
                                    'bold': False 
                                })
                                
                    except serial.SerialException as e:
                        # HANYA putuskan koneksi kalau errornya dari SERIAL (Hardware)
                        print(f"⚠️ Koneksi fisik terputus: {e}")
                        ser.close()
                        ser = None
                        is_connected = False
                        socketio.emit('status_update', {'status': 'disconnected'})
                        
                    except Exception as e:
                        # Kalau error coding/data, jangan putus koneksi, cukup lapor aja
                        print(f"⚠️ Error data (diabaikan): {e}")

            time.sleep(0.1)

        except Exception as e:
            # Error global loop
            print(f"Error Loop: {e}")
            ser = None
            is_connected = False
            time.sleep(1)


if __name__ == '__main__':
    bt_thread = Thread(target=read_bluetooth_serial)
    bt_thread.daemon = True
    bt_thread.start()

    print("🚀 Server berjalan di http://localhost:5000")
    socketio.run(app, debug=False, port=5000)