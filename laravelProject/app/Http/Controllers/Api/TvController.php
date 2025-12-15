<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Tv;
use Illuminate\Http\Request;

class TvController extends Controller
{
    // ==========================================
    // BAGIAN 1: API UNTUK KASIR (TRANSAKSI)
    // ==========================================

    /**
     * Mengambil daftar TV yang AVAILABLE saja.
     * Method: GET /api/tvs/available
     */
    public function getAvailableTvs(Request $request)
    {
        // Query data TV + relasi jenis console + filter status available
        $query = Tv::with('jenisConsole')->where('status', 'available');

        // Filter opsional by console (misal kasir cuma mau liat PS5)
        if ($request->has('id_console')) {
            $query->where('id_console', $request->id_console);
        }

        $tvs = $query->get();

        return response()->json([
            'success' => true,
            'message' => 'List TV Available',
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
            'nomor_tv'   => 'required|unique:tvs,nomor_tv',
            'id_console' => 'required|exists:jenis_console,id_console',
        ]);

        $tv = Tv::create([
            'nomor_tv'   => $request->nomor_tv,
            'id_console' => $request->id_console,
            'status'     => 'available', // Default available
        ]);

        return response()->json([
            'success' => true,
            'message' => 'TV berhasil ditambahkan',
            'data'    => $tv
        ], 201); // 201 = Created
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