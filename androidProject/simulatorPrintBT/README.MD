# 🖨️ Bluetooth Thermal Printer Simulator

**Ubah laptop kamu menjadi Printer Thermal Bluetooth Virtual!**

Project ini adalah simulator printer thermal berbasis web yang berjalan di atas Python. Aplikasi ini memungkinkan kamu untuk mengirim perintah cetak dari HP (Aplikasi Kasir/POS) via Bluetooth ke Laptop, dan melihat hasil "cetakan"-nya secara real-time di layar browser tanpa menghabiskan kertas asli.

---

## ✨ Fitur Utama

* **Real-time Printing:** Teks muncul seketika saat dikirim dari HP menggunakan teknologi WebSockets.
* **Bluetooth SPP Support:** Mendukung profil Serial Port Profile (SPP) standar yang digunakan oleh hampir semua printer thermal murah.
* **ESC/POS Command Filter:** Secara cerdas membersihkan kode-kode perintah mesin (ESC/POS commands) agar yang tampil di layar adalah teks yang bisa dibaca.
* **Robust Encoding:** Tahan terhadap karakter asing/biner yang biasanya membuat error (menggunakan logic `decode error='replace'`).
* **Interactive UI:**
  * Simulasi kertas gulung (auto-scroll).
  * Status Indikator (Online/Offline/Connected).
  * Tombol **Disconnect** untuk memutus koneksi bluetooth yang macet.
  * Tombol **Clear Paper** untuk membersihkan layar.

---

## 🛠️ Arsitektur Sistem

Aplikasi ini bekerja dengan menjembatani Hardware Bluetooth Laptop dengan Web Browser:

**Cuplikan kode**

```
graph LR
    A[HP / Aplikasi Kasir] -- Bluetooth SPP --> B((Laptop Bluetooth Hardware));
    B -- COM Port (Serial) --> C[Python Backend (app.py)];
    C -- WebSockets --> D[Web Frontend (printer.html)];
    D -- Visual Output --> E[Browser User];
```

1. **HP** mengirim data string ke Laptop via Bluetooth.
2. **Python** membaca data tersebut dari **COM Port Incoming**.
3. **Python** membersihkan data dari karakter sampah (hex code).
4. **Flask-SocketIO** mengirim data bersih ke Browser.
5. **Browser** menampilkan teks pada div yang didesain mirip kertas struk.

---

## ⚙️ Prasyarat (Requirements)

Sebelum memulai, pastikan kamu memiliki:

1. **Laptop/PC** dengan fitur Bluetooth.
2. **Python 3.x** terinstall.
3. **OS:** Windows 10/11 (Disarankan) atau Linux.

---

## 🚀 Instalasi & Setup

### 1. Clone/Download Project

Simpan folder project ini di komputer kamu. Struktur foldernya harus seperti ini:

**Plaintext**

```
/my-thermal-sim
  ├── app.py             # Script utama Python
  ├── requirements.txt   # Daftar library (opsional)
  └── public
       └── printer.html  # Tampilan Web Simulator
```

### 2. Install Library Python

Buka Terminal/CMD di folder project, lalu jalankan:

**Bash**

```
pip install pyserial flask flask-socketio eventlet
```

### 3. Konfigurasi Bluetooth (PENTING!)

Agar Python bisa membaca Bluetooth, kita perlu tahu **COM Port** mana yang digunakan.

1. Nyalakan Bluetooth di **HP** dan **Laptop**.
2. Lakukan **Pairing** antara HP dan Laptop.
3. Di Windows, buka **Settings > Bluetooth & devices > Devices > More Bluetooth settings**.
4. Pilih tab **COM Ports**.
5. Cari port dengan arah **"Incoming"** (Misal: `COM4`, `COM5`, dll).
6. Buka file `app.py` di text editor, lalu ubah baris ini sesuai port kamu:

**Python**

```
# app.py
BLUETOOTH_PORT = 'COM4'  # <-- Ganti dengan port Incoming kamu
```

---

## ▶️ Cara Menjalankan

1. Buka terminal di folder project.
2. Jalankan perintah:
   **Bash**

   ```
   python app.py
   ```
3. Jika berhasil, akan muncul pesan:
   🚀 Server berjalan di http://localhost:5000
4. Buka browser (Chrome/Edge), akses alamat:
   http://localhost:5000
5. Pastikan status di pojok kanan atas web adalah **DEVICE CONNECTED** (Jika HP sudah connect) atau **WAITING CONNECTION**.

---

## 📱 Cara Connect dari HP (Aplikasi Kasir/Terminal)

Simulator ini bertindak sebagai **Generic Printer**.

### Opsi A: Menggunakan Aplikasi "Serial Bluetooth Terminal" (Disarankan untuk Test)

1. Download **Serial Bluetooth Terminal** di PlayStore.
2. Buka menu **Devices**, pilih Laptop kamu.
3. Klik icon **Connect**.
4. Ketik pesan dan kirim. Teks akan muncul di layar laptop.

### Opsi B: Menggunakan Aplikasi Kasir (Moka, Pawoon, RawBT, dll)

1. Masuk ke menu **Settings > Printer** di aplikasi kasir.
2. Tambah Printer Baru.
3. **Interface:** Bluetooth.
4. **Model/Driver:** Pilih **"Generic ESC/POS"**, **"Generic Text"**, atau **"Epson TM-T88"**.
5. **Paper Width:** 58mm.
6. Coba **Print Test Struk**.

> **Catatan:** Jangan kaget jika logo gambar tidak muncul atau muncul sebagai kode aneh. Simulator ini didesain untuk menangkap **Teks**, bukan gambar bitmap.

---

## ❓ Troubleshooting (Masalah Umum)

### 1. Error `Access is denied` pada Terminal Python

* **Sebab:** COM Port sedang digunakan aplikasi lain, atau HP belum menginisiasi koneksi dengan benar.
* **Solusi:** Tutup aplikasi lain yang menggunakan Bluetooth. Coba connect dari HP dulu baru jalankan script Python, atau sebaliknya.

### 2. Status di Web tetap "OFFLINE"

* **Sebab:** Browser gagal terhubung ke WebSocket Python.
* **Solusi:** Refresh browser (Ctrl + F5). Pastikan tidak ada firewall yang memblokir port 5000.

### 3. Muncul karakter kotak-kotak atau simbol aneh

* **Sebab:** Itu adalah kode ESC/POS (Print command) seperti perintah *Bold*, *Cut Paper*, dll.
* **Solusi:** Script `app.py` sudah dilengkapi filter Regex, tapi beberapa command kompleks mungkin masih lolos. Ini wajar untuk simulator teks.

### 4. Error `'utf-8' codec can't decode byte...`

* **Solusi:** Kode terbaru sudah menggunakan `errors='replace'` dan `decode('cp437')` sebagai cadangan. Pastikan kamu menggunakan kode `app.py` versi terakhir.

---

## 📜 License

Project ini dibuat untuk tujuan edukasi dan testing. Silakan dimodifikasi sesuai kebutuhan.

**Selamat Mencoba! 🖨️**
