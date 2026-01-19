<?php

namespace App\Models;

use Illuminate\Support\Facades\Http;
use Illuminate\Support\Collection;

class Transaksi
{
    // Properties - matching backend response
    public $id;
    public $id_transaksi;
    public $nama_penyewa;
    public $total;
    public $total_tagihan;
    public $uang_bayar;
    public $uang_kembalian;
    public $metode_pembayaran;
    public $status_pembayaran;
    public $items;
    public $details;
    public $items_count;
    public $created_at;
    public $tanggal_transaksi;

    public function __construct($data = [])
    {
        // Handle both backend format (id_transaksi) and internal format (id)
        $this->id = $data['id'] ?? $data['id_transaksi'] ?? null;
        $this->id_transaksi = $data['id_transaksi'] ?? $data['id'] ?? null;
        
        $this->nama_penyewa = $data['nama_penyewa'] ?? 'Guest';
        
        // Handle both total formats
        $this->total = $data['total'] ?? $data['total_tagihan'] ?? 0;
        $this->total_tagihan = $data['total_tagihan'] ?? $data['total'] ?? 0;
        
        $this->uang_bayar = $data['uang_bayar'] ?? 0;
        $this->uang_kembalian = $data['uang_kembalian'] ?? 0;
        $this->metode_pembayaran = $data['metode_pembayaran'] ?? 'TUNAI';
        $this->status_pembayaran = $data['status_pembayaran'] ?? 'LUNAS';
        
        // Handle both items and details
        $this->items = $data['items'] ?? $data['details'] ?? [];
        $this->details = $data['details'] ?? $data['items'] ?? [];
        $this->items_count = count($this->items);
        
        // Handle both date formats
        $this->created_at = $data['created_at'] ?? $data['tanggal_transaksi'] ?? now();
        $this->tanggal_transaksi = $data['tanggal_transaksi'] ?? $data['created_at'] ?? now();
    }

    /**
     * Get all transactions from API
     */
    public static function all(): Collection
    {
        $token = session('api_token');
        $url = env('BACKEND_URL') . '/api/transaksi';

        try {
            $response = Http::withToken($token)->timeout(10)->get($url);

            $transactions = [];
            if ($response->successful()) {
                $data = $response->json();
                // Handle 'data' wrapped response from backend
                $items = $data['data'] ?? $data;
                
                if (is_array($items)) {
                    foreach ($items as $item) {
                        $transactions[] = new self($item);
                    }
                }
            }

            return collect($transactions);
        } catch (\Exception $e) {
            // Return empty collection on error
            return collect([]);
        }
    }

    /**
     * Find transaction by ID
     */
    public static function find($id): ?self
    {
        $token = session('api_token');
        $url = env('BACKEND_URL') . '/api/transaksi/' . $id;

        try {
            $response = Http::withToken($token)->timeout(10)->get($url);

            if ($response->successful()) {
                $data = $response->json();
                // Handle 'data' wrapped response from backend
                $item = $data['data'] ?? $data;
                return new self($item);
            }

            return null;
        } catch (\Exception $e) {
            return null;
        }
    }

    /**
     * Create new transaction (POST to API)
     */
    public static function create($data)
    {
        $token = session('api_token');
        $url = env('BACKEND_URL') . '/api/transaksi';

        $response = Http::withToken($token)->post($url, $data);

        return $response;
    }

    /**
     * Stop rental / reset TV
     */
    public static function stop($tvId)
    {
        $token = session('api_token');
        $url = env('BACKEND_URL') . '/api/tvs/' . $tvId;

        return Http::withToken($token)->put($url, [
            'status' => 'available',
            'rental_end_time' => null
        ]);
    }
}