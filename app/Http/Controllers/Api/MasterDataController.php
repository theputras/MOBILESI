<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Support\Facades\DB;

class MasterDataController extends Controller
{
    // Android panggil ini buat ngisi Spinner "Pilih Console"
    public function getConsoles()
    {
        $data = DB::table('jenis_console')->get();
        return response()->json($data);
    }

    // Android panggil ini buat ngisi Spinner "Pilih Paket"
    public function getPakets()
    {
        // Ambil semua paket, atau bisa difilter by console_id nanti
        $data = DB::table('paket_sewa')
                  ->join('jenis_console', 'paket_sewa.id_console', '=', 'jenis_console.id_console')
                  ->select('paket_sewa.*', 'jenis_console.nama_console')
                  ->get();
                  
        return response()->json($data);
    }
}