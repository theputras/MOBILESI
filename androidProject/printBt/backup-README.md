# 🖨️ PrintBt - Android Bluetooth Thermal Printer App

Aplikasi Android sederhana namun *powerful* untuk mencetak teks ke **Printer Thermal Bluetooth** (ESC/POS). Dibangun menggunakan **Java** dengan dukungan penuh untuk Android versi terbaru (Android 12+ / SDK 34).

## ✨ Fitur Utama

* **Koneksi Bluetooth Stabil:** Menggunakan *Singleton Pattern* untuk menjaga koneksi tetap hidup antar-activity.
* **Dukungan Android 12+:** Menangani *Runtime Permissions* (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`) dengan benar.
* **Scan & Pairing:** Memindai perangkat di sekitar dan menampilkan daftar perangkat yang sudah *paired*.
* **Direct Printing:** Mencetak teks input pengguna secara *real-time*.
* **Smart Feeding:** Mencegah *buffer stuck* (teks tidak keluar) dengan perintah *feed lines* otomatis.
* **Status Monitor:** Indikator status koneksi *real-time* di menu utama.

## 



## 🛠️ Tech Stack

* **Language:** Java
* **Minimum SDK:** 21 (Android 5.0)
* **Target SDK:** 34 (Android 14)
* **Architecture:** MVC (Model-View-Controller)
* **Communication:** Bluetooth Serial Port Profile (SPP) / RFCOMM

## 📂 Struktur Kode & File Sumber

Kode sumber lengkap (Java Classes & XML Layouts) dapat dilihat langsung di repositori GitHub kami:

👉 **[Lihat Kode Sumber di GitHub (Java & Res)](https://github.com/theputras/MOBILESI/tree/main/androidProject/printBt)**

### Gambaran Struktur Project:

* **`MainActivity`**: Halaman dashboard untuk cek status & navigasi.
* **`DeviceListActivity`**: Logic scanning, handling permission, dan koneksi ke printer.
* **`PrintTextActivity`**: Interface input teks dan eksekusi perintah print.
* **`utils/BluetoothHelper`**: *The Brain*. Class singleton yang menangani socket bluetooth, stream data, dan perintah ESC/POS.

## 🚀 Cara Instalasi

1. Clone repositori ini atau download ZIP.
2. Buka project menggunakan **Android Studio**.
3. Biarkan Gradle melakukan sinkronisasi (*Sync Project*).
4. Hubungkan HP Android fisik (Emulator tidak mendukung Bluetooth).
5. Run aplikasi (▶).

## 📖 Cara Penggunaan

1. **Nyalakan Printer Thermal** dan pastikan bluetooth HP aktif.
2. Buka Aplikasi, klik tombol **"Koneksi Printer"**.
3. Klik **"Scan Bluetooth Device"** atau pilih dari daftar *Paired Devices*.
4. Pilih nama printer Anda. Tunggu hingga muncul pesan *"Berhasil Terkoneksi"*.
5. (Opsional) Klik tombol **"Test Print"** untuk memastikan printer merespon.
6. Kembali ke menu awal, pilih **"Cetak Teks Biasa"**.
7. Ketik pesan Anda dan tekan **"PRINT SEKARANG"**.

## 🔧 Troubleshooting (Masalah Umum)

Jika Anda mengalami kendala saat pengembangan, berikut solusinya:

### 1. Aplikasi Crash saat Scan (Android 12+)

* **Penyebab:** Izin `BLUETOOTH_SCAN` atau `BLUETOOTH_CONNECT` belum diberikan.
* **Solusi:** Aplikasi ini sudah dilengkapi `ActivityCompat.requestPermissions`. Pastikan Anda mengizinkan akses "Nearby Devices" saat pop-up muncul. Cek `AndroidManifest.xml` untuk detail permission.

### 2. Status "Terkoneksi" tapi Printer Diam saat di-Print (Buffer Stuck)

* **Penyebab:** Data terkirim tapi tertahan di *buffer* printer karena kurang perintah *feed* (gulung kertas).
* **Solusi:** Kode di `BluetoothHelper` sudah kami optimalkan dengan menambahkan:
  ```java
  outputStream.write("\n\n\n".getBytes()); // Feed 3 baris
  outputStream.flush(); // Paksa dorong data keluar
  Thread.sleep(300); // Beri jeda printer memproses
  ```

### 3. Error `socket might closed or timeout`

* **Penyebab:** Mencoba konek ke device yang bukan printer (misal: HP lain) atau printer mati.
* **Solusi:** Pastikan target adalah Printer Thermal yang mendukung protokol SPP. Restart printer jika perlu.

## 🤝 Kontribusi

Project ini dibuat oleh **The Putra's**. Silakan fork dan pull request jika ingin menambahkan fitur seperti cetak gambar atau QR Code!

---

*Dibuat dengan ❤️ untuk komunitas Android Developer Indonesia.*
