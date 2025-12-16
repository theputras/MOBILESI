<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Transaksi;
use App\Models\JenisConsole;
use App\Models\PaketSewa;
use App\Models\Tv;

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
        // 1. Validasi Input (HARUS MATCH DENGAN ANDROID)
        // Android kirim: nama_penyewa, tv_id, id_paket, uang_bayar
        $request->validate([
            'nama_penyewa' => 'required|string',
            'tv_id'        => 'required|exists:tvs,id', // Android kirim 'tv_id', Server lama minta 'nomor_tv' (SALAH)
            'id_paket'     => 'required|exists:paket_sewa,id_paket', 
            'uang_bayar'   => 'required|integer|min:0',
        ]);

        // 2. Ambil Data Paket
        $paket = PaketSewa::findOrFail($request->id_paket);
        
        // 3. Cek Status TV
        $tv = Tv::findOrFail($request->tv_id);
        if($tv->status != 'available') {
             // Return JSON agar Android tidak error Malformed
             return response()->json(['message' => 'TV ini baru saja dipesan orang lain!'], 409);
        }

        // 4. Hitung Keuangan (Otomatis Server)
        $totalTagihan = $paket->harga;
        $uangKembalian = $request->uang_bayar - $totalTagihan;

        if ($uangKembalian < 0) {
            return response()->json([
                'message' => 'Uang pembayaran kurang!',
                'kurang' => abs($uangKembalian)
            ], 400);
        }

        // 5. Simpan Transaksi
        $transaksi = Transaksi::create([
            'nama_penyewa'      => $request->nama_penyewa,
            'tv_id'             => $request->tv_id,
            'id_paket'          => $paket->id_paket,
            'total_tagihan'     => $totalTagihan,
            'uang_bayar'        => $request->uang_bayar,
            'uang_kembalian'    => $uangKembalian,
            'metode_pembayaran' => $request->metode_pembayaran ?? 'TUNAI',
            'status_pembayaran' => 'LUNAS',
        ]);

        // 6. Update Status TV jadi Booked
        $tv->update(['status' => 'booked']);

        return response()->json([
            'message' => 'Transaksi berhasil & TV aktif',
            'data' => $transaksi
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