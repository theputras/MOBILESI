<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Tv;
use App\Models\Transaksi;
use App\Models\PaketSewa;
use Illuminate\Http\Request;
use Carbon\Carbon;

class TvController extends Controller
{
    /**
     * FUNGSI TAMBAHAN (HELPER):
     * Otomatis cek apakah durasi sewa sudah habis.
     * Jika rental_end_time sudah lewat dari waktu sekarang, ubah status jadi available.
     */
    private function updateStatusTV()
    {
        // 1. Ambil semua TV yang statusnya 'booked' DAN memiliki rental_end_time
        $tvs = Tv::where('status', 'booked')
                 ->whereNotNull('rental_end_time')
                 ->get();

        $now = Carbon::now();

        foreach ($tvs as $tv) {
            // 2. Cek apakah waktu sekarang sudah melewati rental_end_time
            if ($now->greaterThan($tv->rental_end_time)) {
                // 3. Jika ya, ubah status jadi available dan reset waktunya
                $tv->update([
                    'status' => 'available',
                    'rental_end_time' => null 
                ]);
            }
        }
    }

    // ==========================================
    // BAGIAN 1: API UNTUK KASIR (TRANSAKSI)
    // ==========================================

    /**
     * Mengambil daftar TV yang AVAILABLE saja.
     * Method: GET /api/tvs/available
     */
    public function getAvailableTvs()
    {
        // CEK DULU: Apakah ada TV yang durasinya habis?
        $this->updateStatusTV(); 

        // Baru ambil datanya
        $tvs = Tv::where('status', 'available')
                 ->select('id', 'nomor_tv', 'status', 'rental_end_time')
                 ->get();

        return response()->json([
            'success' => true,
            'message' => 'List TV Available',
            'data'    => $tvs
        ], 200);
    }

    public function getBookedTvs()
    {
        // CEK DULU: Biar data yang tampil realtime, kalau habis langsung hilang dari list booked
        $this->updateStatusTV();

        $tvs = Tv::where('status', 'booked')
                 ->select('id', 'nomor_tv', 'status', 'rental_end_time')
                 ->get();

        return response()->json([
            'success' => true,
            'message' => 'List TV Booked dan Durasi Sewa',
            'data'    => $tvs
        ], 200);
    }

    public function getMaintenanceTvs()
    {
        // Maintenance tidak perlu dicek waktunya
        $tvs = Tv::where('status', 'maintenance')
                 ->select('id', 'nomor_tv', 'status', 'rental_end_time')
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
        // CEK DULU JUGA DI SINI
        $this->updateStatusTV();

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

        // Pastikan update status dulu sebelum validasi available
        $this->updateStatusTV(); 
        $tv->refresh(); // Refresh data model agar status terbaru terambil

        if ($tv && $tv->status == 'available') {
            $paket = PaketSewa::findOrFail($request->id_paket);

            // Hitung waktu selesai
            $rentalEndTime = Carbon::now()->addMinutes($paket->durasi_menit);

            $tv->update([
                'status' => 'booked',
                'rental_end_time' => $rentalEndTime,
            ]);

            return response()->json([
                'message' => 'TV berhasil disewa',
                'data' => $tv
            ], 201);
        }

        return response()->json(['message' => 'TV tidak tersedia'], 400);
    }

    /**
     * Update Data TV
     * Method: PUT /api/tvs/{id}
     */
    public function update(Request $request, $id)
    {
        $tv = Tv::findOrFail($id);
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
     * Detail Satu TV
     * Method: GET /api/tvs/{id}
     */
    public function show($id)
    {
        $this->updateStatusTV(); // Cek dulu
        
        $tv = Tv::with('jenisConsole')->findOrFail($id);
        
        return response()->json([
            'success' => true,
            'message' => 'Detail TV',
            'data'    => $tv
        ], 200);
    }
}