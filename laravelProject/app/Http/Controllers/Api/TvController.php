<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Tv;
use App\Models\Transaksi;
use App\Models\PaketSewa;
use Illuminate\Http\Request;
use App\Events\RentalStarted;
use Carbon\Carbon;



class TvController extends Controller
{
    // ==========================================
    // BAGIAN 1: API UNTUK KASIR (TRANSAKSI)
    // ==========================================

    /**
     * Mengambil daftar TV yang AVAILABLE saja.
     * Method: GET /api/tvs/available
     */
public function getAvailableTvs()
{
    // Cari TV yang statusnya 'available'
    $tvs = Tv::where('status', 'available')
             ->select('id', 'nomor_tv', 'status', 'rental_end_time')  // Pilih kolom yang dibutuhkan
             ->get();

    return response()->json([
        'success' => true,
        'message' => 'List TV Available',
        'data'    => $tvs
    ], 200);
}


public function getBookedTvs()
{
    // Cari TV yang statusnya 'booked'
    $tvs = Tv::where('status', 'booked')
             ->select('id', 'nomor_tv', 'status', 'rental_end_time')  // Pilih kolom yang dibutuhkan
             ->get();

    return response()->json([
        'success' => true,
        'message' => 'List TV Booked dan Durasi Sewa',
        'data'    => $tvs
    ], 200);
}


public function getMaintenanceTvs()
{
    // Cari TV yang statusnya 'maintenance'
    $tvs = Tv::where('status', 'maintenance')
             ->select('id', 'nomor_tv', 'status', 'rental_end_time')  // Pilih kolom yang dibutuhkan
             ->get();

    return response()->json([
        'success' => true,
        'message' => 'List TV Maintenance',
        'data'    => $tvs
    ], 200);
}


    // ==========================================
    // BAGIAN 2: CRUD UNTUK ADMIN (MANAJEMEN TV)
    // ==========================================

    /**
     * Menampilkan SEMUA TV (termasuk yang booked/rusak)
     * Method: GET /api/tvs
     */
    public function index()
    {
        // Kita return JSON, bukan View
        $tvs = Tv::with('jenisConsole')->get();

        return response()->json([
            'success' => true,
            'message' => 'List Semua Data TV',
            'data'    => $tvs
        ], 200);
    }

    /**
     * Menambah TV Baru
     * Method: POST /api/tvs
     */
    public function store(Request $request)
{
    $validator = $request->validate([
        'nomor_tv'   => 'required|exists:tvs,nomor_tv',
        'id_console' => 'required|exists:jenis_console,id_console',
        'id_paket'   => 'required|exists:paket_sewa,id_paket',
    ]);

    // Ambil TV berdasarkan nomor_tv
    $tv = Tv::where('nomor_tv', $request->nomor_tv)->first();

    if ($tv && $tv->status == 'available') {
        // Ambil paket sewa
        $paket = PaketSewa::findOrFail($request->id_paket);

        // Hitung waktu selesai sewa
        $rentalEndTime = Carbon::now()->addMinutes($paket->durasi_menit);  // Waktu sewa berakhir berdasarkan durasi paket

        // Update status TV dan waktu sewa berakhir
        $tv->update([
            'status' => 'booked',
            'rental_end_time' => $rentalEndTime,  // Menyimpan waktu selesai sewa
        ]);

        return response()->json([
            'message' => 'TV berhasil disewa',
            'data' => $tv
        ], 201);
    }

    return response()->json(['message' => 'TV tidak tersedia'], 400);
}



    /**
     * Update Data TV (Misal: Ganti status jadi maintenance, atau ganti console)
     * Method: PUT /api/tvs/{id}
     */
    public function update(Request $request, $id)
    {
        // Cari TV, kalau gak ketemu otomatis return 404 (Not Found)
        $tv = Tv::findOrFail($id);

        // Update data sesuai input (bisa update status, nomor_tv, dll)
        $tv->update($request->all());

        return response()->json([
            'success' => true,
            'message' => 'Data TV berhasil diupdate',
            'data'    => $tv
        ], 200);
    }

    /**
     * Hapus TV
     * Method: DELETE /api/tvs/{id}
     */
    public function destroy($id)
    {
        $tv = Tv::findOrFail($id);
        $tv->delete();

        return response()->json([
            'success' => true,
            'message' => 'Data TV berhasil dihapus'
        ], 200);
    }
    
    /**
     * (Opsional) Detail Satu TV
     * Method: GET /api/tvs/{id}
     */
    public function show($id)
    {
        $tv = Tv::with('jenisConsole')->findOrFail($id);
        
        return response()->json([
            'success' => true,
            'message' => 'Detail TV',
            'data'    => $tv
        ], 200);
    }





}