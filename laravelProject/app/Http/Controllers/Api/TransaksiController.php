<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Transaksi;
use App\Models\JenisConsole;

class TransaksiController extends Controller
{
    /**
     * GET: Ambil Semua Riwayat Transaksi (Untuk Halaman List)
     */
    public function index()
    {
        // Ambil data, urutkan dari yang terbaru, dan sertakan info console-nya
        $transaksi = Transaksi::with('console')
                    ->orderBy('tanggal_transaksi', 'desc')
                    ->get();
        
        return response()->json([
            'message' => 'Data riwayat berhasil diambil',
            'data' => $transaksi
        ], 200);
    }

    /**
     * POST: Simpan Transaksi Baru (Untuk Tombol Bayar/Simpan)
     */
    public function store(Request $request)
    {
        // 1. Validasi Input dari Android
        $request->validate([
            'nama_penyewa' => 'required|string',
            'nomor_tv'     => 'required|integer',
            'id_console'   => 'required|exists:jenis_console,id_console', // Harus ada di tabel master
            'durasi_jam'   => 'required|integer|min:1',
            'uang_bayar'   => 'required|integer|min:0',
        ]);

        // 2. Ambil Harga Console dari Database (Biar Akurat)
        $console = JenisConsole::findOrFail($request->id_console);
        $hargaPerJam = $console->harga_per_jam;

        // 3. Hitung Otomatis (Logika Kasir)
        $totalTagihan = $hargaPerJam * $request->durasi_jam;
        $uangKembalian = $request->uang_bayar - $totalTagihan;

        // Cek kalau uang kurang
        if ($uangKembalian < 0) {
            return response()->json([
                'message' => 'Uang pembayaran kurang!',
                'kurang' => abs($uangKembalian)
            ], 400);
        }

        // 4. Simpan ke Database
        $transaksi = Transaksi::create([
            'nama_penyewa'      => $request->nama_penyewa,
            'nomor_tv'          => $request->nomor_tv,
            'id_console'        => $request->id_console,
            'durasi_jam'        => $request->durasi_jam,
            'total_tagihan'     => $totalTagihan,     // Hasil hitung server
            'uang_bayar'        => $request->uang_bayar,
            'uang_kembalian'    => $uangKembalian,    // Hasil hitung server
            'metode_pembayaran' => 'TUNAI',           // Default
            'status_pembayaran' => 'LUNAS',
        ]);

        // 5. Kembalikan Respon ke Android (Termasuk data hitungan tadi buat dicetak)
        return response()->json([
            'message' => 'Transaksi berhasil disimpan',
            'data' => $transaksi->load('console') // Load nama console biar lengkap
        ], 201);
    }

    /**
     * GET: Ambil Detail 1 Transaksi (Untuk Halaman Struk/Detail)
     */
    public function show($id)
    {
        $transaksi = Transaksi::with('console')->find($id);

        if (!$transaksi) {
            return response()->json(['message' => 'Transaksi tidak ditemukan'], 404);
        }

        return response()->json([
            'data' => $transaksi
        ], 200);
    }
}