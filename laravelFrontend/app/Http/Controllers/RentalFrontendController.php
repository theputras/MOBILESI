<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Tv;           // <-- Panggil Model Frontend tadi
use App\Models\PaketSewa;
use App\Models\Transaksi;

class RentalFrontendController extends Controller
{
    public function index()
    {
        // 1. Panggil data pake gaya Eloquent
        $tvs = Tv::all(); 
        $pakets = PaketSewa::all();

        // 2. Lempar ke View
        return view('dashboard', compact('tvs', 'pakets'));
    }

    public function store(Request $request)
    {
        // Susun data sesuai request Backend
        $payload = [
            'nama_penyewa' => $request->nama_penyewa,
            'uang_bayar'   => $request->uang_bayar,
            'metode_pembayaran' => 'TUNAI',
            'items' => [
                [
                    'tv_id'    => $request->tv_id,
                    'id_paket' => $request->id_paket
                ]
            ]
        ];

        // Panggil Model Transaksi buat create
        $response = Transaksi::create($payload);

        if ($response->successful()) {
            return redirect()->back()->with('success', "Sewa Berhasil!");
        } else {
            return redirect()->back()->with('error', 'Gagal: ' . $response->json()['message'] ?? 'Error');
        }
    }

    public function stop($id)
    {
        // Panggil Model Transaksi buat stop
        Transaksi::stop($id);
        return redirect()->back()->with('success', 'TV berhasil di-reset.');
    }
}