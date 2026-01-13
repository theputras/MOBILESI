<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class LoginController extends Controller
{
    // REGISTER (Sudah Oke)
    public function register(Request $request)
    {
        $request->validate([
            'name' => 'required|string|max:255',
            'email' => 'required|string|email|max:255|unique:users', // Tambahkan unique biar ga dobel
            'password' => 'required|string|min:8',
        ]);
        
        $user = User::create([
            'name' => $request->name,
            'email' => $request->email,
            'password' => Hash::make($request->password),
        ]);
        
        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'message' => 'User registered successfully',
            'token' => $token,
        ], 201);
    }
    
    // LOGIN (Sudah Oke)
    public function login(Request $request)
    {
        $request->validate([
            'email' => 'required|string|email',
            'password' => 'required|string',
        ]);
        
        $user = User::where('email', $request->email)->first();

        // Cek user & password sekaligus biar lebih aman
        if (!$user || !Hash::check($request->password, $user->password)) {
            return response()->json([
                'message' => 'Invalid login credentials'
            ], 401);
        }

        $token = $user->createToken('auth_token')->plainTextToken;

        return response()->json([
            'message' => 'Login successful',
            'user'    => $user,
            'access_token' => $token,
            'token_type' => 'Bearer',
        ]);
    }
    
    // LOGOUT (Hanya hapus token device ini saja)
    // REVISI: Pakai currentAccessToken()->delete()
    public function logout(Request $request)
    {
        // Hanya menghapus token yang dipakai saat request ini
        $request->user()->currentAccessToken()->delete(); 

        return response()->json(['message' => 'Successfully logged out']);
    }
    
    // LOGOUT ALL (Hapus token di semua device)
    // REVISI: Pakai tokens()->delete()
    public function logoutAll(Request $request)
    {
        // Menghapus SEMUA token milik user ini (HP, Laptop, Tablet, dll log out semua)
        $request->user()->tokens()->delete();

        return response()->json(['message' => 'Successfully logged out from all devices']);
    }
}