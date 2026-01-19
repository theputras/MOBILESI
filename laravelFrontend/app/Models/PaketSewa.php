<?php

namespace App\Models;

use Illuminate\Support\Facades\Http;
use Illuminate\Support\Collection;

class PaketSewa
{
    public $id_paket;
    public $id_console;
    public $nama_paket;
    public $harga;
    public $durasi_menit; // Added field
    public $nama_console;

    public function __construct($data = [])
    {
        $this->id_paket = $data['id_paket'] ?? null;
        $this->id_console = $data['id_console'] ?? null;
        $this->nama_paket = $data['nama_paket'] ?? '-';
        $this->harga = $data['harga'] ?? 0;
        $this->durasi_menit = $data['durasi_menit'] ?? 0; // Added assignment
        $this->nama_console = $data['nama_console'] ?? '-';
    }

    public static function all(): Collection
    {
        $token = session('api_token');
        $url = env('BACKEND_URL') . '/api/master/pakets';

        $response = Http::withToken($token)->get($url);

        $pakets = [];
        if ($response->successful()) {
            foreach ($response->json() as $item) {
                $pakets[] = new self($item);
            }
        }

        return collect($pakets);
    }
}