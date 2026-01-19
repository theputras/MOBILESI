<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;

class FrontendAuthController extends Controller
{
    private $backendUrl;

    public function __construct()
    {
        $this->backendUrl = env('BACKEND_URL'); // http://mobile.theputras.my.id
    }

    // Tampilkan Form Login
    public function showLoginForm()
    {
        return view('login');
    }

    // Proses Login ke Backend
    public function login(Request $request)
    {
        // 1. Tembak API Login di Backend
        // Endpoint ini ada di backend kamu: app/Http/Controllers/Api/LoginController.php
        $response = Http::post($this->backendUrl . '/api/login', [
            'email'    => $request->email,
            'password' => $request->password,
        ]);

        // 2. Cek apakah login sukses
        if ($response->successful()) {
            $data = $response->json();
            $token = $data['access_token']; // Backend kirim 'access_token'

            // 3. Simpan Token & User Info ke Session Frontend
            // Kita pakai session bawaan Laravel Frontend
            session([
                'api_token' => $token,
                'user_name' => $data['user']['name'] ?? 'Kasir',
                'is_logged_in' => true
            ]);

            return redirect()->route('dashboard');
        }

        // Kalau gagal
        return back()->with('error', 'Email atau Password salah!');
    }

    public function logout()
    {
        // Hapus session
        session()->flush();
        return redirect()->route('login');
    }
}