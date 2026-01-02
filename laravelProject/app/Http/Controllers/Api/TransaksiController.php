<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Transaksi;
use App\Models\JenisConsole;
use App\Models\PaketSewa;
use App\Models\Tv;
use Carbon\Carbon;

class TransaksiController extends Controller
{
    /**
     * GET: Ambil Semua Riwayat Transaksi (Untuk Halaman List)
     */
    public function index()
    {
        // Ambil data, urutkan dari yang terbaru, dan sertakan info console-nya
        $transaksi = Transaksi::with(['tv.jenisConsole'])
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
    // Validasi input transaksi
    $validator = $request->validate([
        'tv_id'          => 'required|exists:tvs,id',
        'id_paket'       => 'required|exists:paket_sewa,id_paket',  // Pastikan paket valid
        'nama_penyewa'   => 'required|string',
        'uang_bayar'     => 'required|integer|min:0',
        'metode_pembayaran' => 'required|string',
    ]);

    // Ambil data TV berdasarkan id
    $tv = Tv::find($request->tv_id);

    // Cek apakah TV sudah disewa
    if ($tv->status !== 'available') {
        return response()->json(['message' => 'TV tidak tersedia'], 409);
    }

    // Ambil paket sewa berdasarkan id_paket
    $paket = PaketSewa::find($request->id_paket);

    // Cek apakah paket ditemukan
    if (!$paket) {
        return response()->json(['message' => 'Paket tidak ditemukan'], 404);
    }

    // Hitung rental_end_time berdasarkan waktu sekarang dan durasi sewa
    $rentalEndTime = Carbon::now()->addMinutes($paket->durasi_menit);  // Durasi sewa dalam menit

    // Menghitung total tagihan dan uang kembalian
    $totalTagihan = $paket->harga;
    $uangKembalian = $request->uang_bayar - $totalTagihan;

    // Validasi uang pembayaran
    if ($uangKembalian < 0) {
        return response()->json(['message' => 'Uang pembayaran kurang!', 'kurang' => abs($uangKembalian)], 400);
    }

    // Simpan transaksi
    $transaksi = Transaksi::create([
        'tv_id'          => $tv->id,
        'id_paket'       => $paket->id_paket,
        'nama_penyewa'   => $request->nama_penyewa,
        'total_tagihan'  => $totalTagihan,
        'uang_bayar'     => $request->uang_bayar,
        'uang_kembalian' => $uangKembalian,
        'metode_pembayaran' => $request->metode_pembayaran,
        'status_pembayaran' => 'LUNAS',
    ]);

    // Perbarui status TV menjadi 'booked' dan set rental_end_time
    $tv->update([
        'status' => 'booked',
        'rental_end_time' => $rentalEndTime  // Set waktu berakhirnya penyewaan
    ]);

    // Kirim respons sukses
    return response()->json([
        'message' => 'Transaksi berhasil',
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