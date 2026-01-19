<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Tv;
use App\Models\PaketSewa;
use App\Models\Transaksi;

class RentalFrontendController extends Controller
{
    /**
     * Dashboard - List semua TV
     */
    public function index()
    {
        $tvs = Tv::all(); 
        $pakets = PaketSewa::all()->sortBy('durasi_menit')->values(); // Sort by duration ascending
        
        // Generate QRIS untuk checkout (SSR approach)
        $cart = session('cart', []);
        $total = collect($cart)->sum('harga');
        $qrisSvg = null;
        
        if ($total > 0) {
            try {
                $qrisData = \App\Helpers\QrisHelper::generateDynamicQris($total);
                $qrisSvg = $qrisData['qr_svg'];
            } catch (\Exception $e) {
                // Fallback: use static QRIS
                $qrisSvg = null;
            }
        }

        return view('dashboard', compact('tvs', 'pakets', 'qrisSvg', 'total'));
    }

    /**
     * Add item to cart (AJAX)
     */
    public function addToCart(Request $request)
    {
        $cart = session('cart', []);
        
        // Check if TV already in cart
        foreach ($cart as $item) {
            if ($item['tv_id'] == $request->tv_id) {
                return response()->json([
                    'success' => false,
                    'message' => 'TV ini sudah ada di keranjang!'
                ]);
            }
        }
        
        // Add to cart
        $cart[] = [
            'tv_id' => $request->tv_id,
            'tv_name' => $request->tv_name,
            'paket_id' => $request->paket_id,
            'paket_name' => $request->paket_name,
            'harga' => $request->harga
        ];
        
        session(['cart' => $cart]);
        
        return response()->json([
            'success' => true,
            'message' => 'Item ditambahkan ke keranjang',
            'cart_count' => count($cart)
        ]);
    }

    /**
     * Remove item from cart (AJAX)
     */
    public function removeFromCart($index)
    {
        $cart = session('cart', []);
        
        if (isset($cart[$index])) {
            unset($cart[$index]);
            $cart = array_values($cart); // Re-index array
        }
        
        session(['cart' => $cart]);
        
        return response()->json([
            'success' => true,
            'cart_count' => count($cart)
        ]);
    }

    /**
     * Process checkout - hit API transaksi
     */
    public function checkout(Request $request)
    {
        $request->validate([
            'nama_penyewa' => 'required|string',
            'metode_pembayaran' => 'required|in:TUNAI,QRIS',
        ]);

        $cart = session('cart', []);
        
        if (empty($cart)) {
            return back()->with('error', 'Keranjang kosong!');
        }

        $total = collect($cart)->sum('harga');
        $uangBayar = $request->metode_pembayaran === 'QRIS' ? $total : $request->uang_bayar;

        // Validate uang bayar for TUNAI
        if ($request->metode_pembayaran === 'TUNAI' && $uangBayar < $total) {
            return back()->with('error', 'Uang bayar kurang dari total!');
        }

        // Prepare items for API
        $items = [];
        foreach ($cart as $item) {
            $items[] = [
                'tv_id' => $item['tv_id'],
                'id_paket' => $item['paket_id']
            ];
        }

        // Hit API transaksi
        $payload = [
            'nama_penyewa' => $request->nama_penyewa,
            'uang_bayar' => $uangBayar,
            'metode_pembayaran' => $request->metode_pembayaran,
            'items' => $items
        ];

        $response = Transaksi::create($payload);

        if ($response->successful()) {
            // Clear cart
            session()->forget('cart');
            
            // Get transaction ID from response - backend uses id_transaksi
            $data = $response->json();
            $transaksiId = $data['data']['id_transaksi'] ?? $data['data']['id'] ?? $data['id_transaksi'] ?? $data['id'] ?? 0;
            
            // Store transaction data in session for struk page
            session(['last_transaksi' => [
                'id' => $transaksiId,
                'id_transaksi' => $transaksiId,
                'nama_penyewa' => $request->nama_penyewa,
                'total' => $total,
                'total_tagihan' => $total,
                'uang_bayar' => $uangBayar,
                'uang_kembalian' => $uangBayar - $total,
                'metode_pembayaran' => $request->metode_pembayaran,
                'items' => $cart,
                'details' => $cart,
                'created_at' => now(),
                'tanggal_transaksi' => now()
            ]]);

            return redirect()->route('struk', $transaksiId);
        }

        return back()->with('error', 'Gagal memproses transaksi: ' . ($response->json()['message'] ?? 'Error'));
    }

    /**
     * Show struk/receipt
     */
    public function showStruk($id)
    {
        // Try to get from session first (just created transaction)
        $transaksi = session('last_transaksi');
        
        if (!$transaksi || ($transaksi['id'] ?? 0) != $id) {
            // Fetch from API
            $transaksi = Transaksi::find($id);
        } else {
            // Convert array to object for view consistency
            $transaksi = (object) $transaksi;
        }

        if (!$transaksi) {
            return redirect()->route('dashboard')->with('error', 'Transaksi tidak ditemukan');
        }

        return view('struk', compact('transaksi'));
    }

    /**
     * Legacy store method (kept for backward compatibility)
     */
    public function store(Request $request)
    {
        $payload = [
            'nama_penyewa' => $request->nama_penyewa,
            'uang_bayar' => $request->uang_bayar,
            'metode_pembayaran' => 'TUNAI',
            'items' => [
                [
                    'tv_id' => $request->tv_id,
                    'id_paket' => $request->id_paket
                ]
            ]
        ];

        $response = Transaksi::create($payload);

        if ($response->successful()) {
            return redirect()->back()->with('success', "Sewa Berhasil!");
        } else {
            return redirect()->back()->with('error', 'Gagal: ' . $response->json()['message'] ?? 'Error');
        }
    }

    /**
     * Stop rental (reset TV)
     */
    public function stop($id)
    {
        Transaksi::stop($id);
        return redirect()->back()->with('success', 'TV berhasil di-reset.');
    }

    /**
     * Generate Dynamic QRIS with amount (AJAX)
     */
    public function generateQris(Request $request)
    {
        $amount = (int) $request->input('amount', 0);
        
        if ($amount <= 0) {
            return response()->json([
                'success' => false,
                'message' => 'Amount harus lebih dari 0'
            ], 400);
        }

        try {
            $result = \App\Helpers\QrisHelper::generateDynamicQris($amount);
            
            return response()->json([
                'success' => true,
                'data' => $result
            ]);
        } catch (\Exception $e) {
            return response()->json([
                'success' => false,
                'message' => 'Gagal generate QRIS: ' . $e->getMessage()
            ], 500);
        }
    }
}