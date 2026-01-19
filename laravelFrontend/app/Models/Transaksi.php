<?php

namespace App\Models;

use Illuminate\Support\Facades\Http;

class Transaksi
{
    // MIRIP ELOQUENT: Method create() buat simpan data
    public static function create($data)
    {
        $token = session('api_token');
        $url = env('BACKEND_URL') . '/api/transaksi';

        // Kirim POST ke Backend
        $response = Http::withToken($token)->post($url, $data);

        // Return object response biar controller bisa cek success/fail
        return $response;
    }

    // Tambahan: Method stop() buat stop timer
    public static function stop($tvId)
    {
        $token = session('api_token');
        $url = env('BACKEND_URL') . '/api/tvs/' . $tvId;

        // Kirim PUT ke Backend
        return Http::withToken($token)->put($url, [
            'status' => 'available',
            'rental_end_time' => null
        ]);
    }
}