<?php

namespace App\Models;

use Illuminate\Support\Facades\Http;
use Illuminate\Support\Collection;

class Tv
{
    // Properti sesuai data JSON dari Backend
    public $id;
    public $nomor_tv;
    public $status;
    public $rental_end_time;
    public $jenis_console; // Ini nanti jadi object juga atau array

    // Constructor buat ngisi data
    public function __construct($data = [])
    {
        $this->id = $data['id'] ?? null;
        $this->nomor_tv = $data['nomor_tv'] ?? '-';
        $this->status = $data['status'] ?? 'maintenance';
        $this->rental_end_time = $data['rental_end_time'] ?? null;
        $this->jenis_console = $data['jenis_console'] ?? [];
    }

    // MIRIP ELOQUENT: Method all() buat ambil semua data
    public static function all(): Collection
    {
        $token = session('api_token');
        $url = env('BACKEND_URL') . '/api/tvs'; // http://mobile.theputras.my.id/api/tvs
        
        // Tembak API
        $response = Http::withToken($token)->get($url);

        $tvs = [];
        if ($response->successful()) {
            // Loop data JSON dan ubah jadi Object Tv
            foreach ($response->json()['data'] as $item) {
                $tvs[] = new self($item);
            }
        }
        
        // Return sebagai Collection biar bisa di-loop di Blade
        return collect($tvs);
    }

    // Helper: Cek apakah available
    public function isAvailable()
    {
        return $this->status === 'available';
    }
}