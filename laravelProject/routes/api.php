<?php
use App\Http\Controllers\Api\ItemController;
use App\Http\Controllers\Api\LoginController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\KonsumenController;
use App\Http\Controllers\Api\TransaksiController;
use App\Http\Controllers\Api\MasterDataController;
use App\Http\Controllers\Api\TvController;

Route::get ('/user', function (Request $request) {
return $request->user () ;
})->middleware ('auth:sanctum') ;

// // Route untuk "Read" semua item
// Route::get ('/items', [ItemController::class, 'index']);

// // Route untuk "Insert" item baru
// Route:: post ('/items', [ItemController::class, 'store']);

// // Route untuk "Read" item berdasarkan ID
// Route::get ('/items/{id}', [ItemController::class, 'show']);

// // Route untuk "Update" item berdasarkan ID
// // Route:: put ('/items/{id}', [ItemController::class, 'update']);

// // Route:: patch ('/items/{id} ', [ItemController::class, 'update']);

// Route::put('/items/{item}', [ItemController::class, 'update']);
// Route::patch('/items/{item}', [ItemController::class, 'update']);


// // Route untuk "Delete" item berdasarkan ID
// Route:: delete ('/items/{item}', [ItemController::class, 'destroy']);


// // Route untuk "Read" semua konsumen
// Route::get('/konsumens', [KonsumenController::class, 'index']);

// // Route untuk "Insert" konsumen baru
// Route::post('/konsumens', [KonsumenController::class, 'store']);

// // Route untuk "Read" konsumen berdasarkan ID
// Route::get('/konsumens/{id}', [KonsumenController::class, 'show']);

// // Route untuk "Update" konsumen berdasarkan ID
// Route::put('/konsumens/{konsumen}', [KonsumenController::class, 'update']);
// Route::patch('/konsumens/{konsumen}', [KonsumenController::class, 'update']);

// // Route untuk "Delete" konsumen berdasarkan ID
// Route::delete('/konsumens/{id}', [KonsumenController::class, 'destroy']);



// Route untuk "Registrasi"
Route::post('/register', [LoginController::class, 'register']);

// Route untuk "Login"
Route::post('/login', [LoginController::class, 'login']);



// Opsional
// Route::post('/logout-all', [LoginController::class, 'logoutAll']);

// Route::post('/logout-all', [LoginController::class, 'logoutAll']);

/*
|--------------------------------------------------------------------------
| 1. AUTHENTICATION ROUTES (Biar bisa Login/Register)
|--------------------------------------------------------------------------
*/
Route::post('/register', [LoginController::class, 'register']);
Route::post('/login', [LoginController::class, 'login']);

/*
|--------------------------------------------------------------------------
| 2. PROTECTED ROUTES (Harus Login Dulu)
|--------------------------------------------------------------------------
*/
// Route::middleware('auth:sanctum')->group(function () {
    
    // Route untuk "Read" semua item
Route::get('/items', [ItemController::class, 'index']);

// Route untuk "Insert" item baru
Route::post('/items', [ItemController::class, 'store']);

// Route untuk "Read" item berdasarkan ID
Route::get('/items/{id}', [ItemController::class, 'show']);

// Route untuk "Update" item berdasarkan ID
Route::put('/items/{id}', [ItemController::class, 'update']);
Route::patch('/items/{item}', [ItemController::class, 'update']);


// Route untuk "Delete" item berdasarkan ID
Route::delete('/items/{item}', [ItemController::class, 'destroy']);

// Route untuk "Read" semua konsumen
Route::get('/konsumens', [KonsumenController::class, 'index']);

// Route untuk "Insert" konsumen baru

Route::post('/konsumens', [KonsumenController::class, 'store']);

// Route untuk "Read" konsumen berdasarkan ID
Route::get('/konsumens/{id}', [KonsumenController::class, 'show']);

// Route untuk "Update" konsumen berdasarkan ID
Route::put('/konsumens/{konsumen}', [KonsumenController::class, 'update']);
Route::patch('/konsumens/{konsumen}', [KonsumenController::class, 'update']);

// Route untuk "Delete" konsumen berdasarkan ID
Route::delete('/konsumens/{id}', [KonsumenController::class, 'destroy']);
    
    // --- FITUR AUTH ---
    Route::post('/logout', [LoginController::class, 'logout']);
    Route::post('/logout-all', [LoginController::class, 'logoutAll']);
    
    // --- FITUR UTAMA (TRANSAKSI) ---
    // Ini yang kamu tulis tadi
    Route::get('/transaksi', [TransaksiController::class, 'index']);      // List Riwayat
    Route::post('/transaksi', [TransaksiController::class, 'store']);     // Simpan & Bayar
    Route::get('/transaksi/{id}', [TransaksiController::class, 'show']);  // Detail Struk
    
    // --- FITUR PENDUKUNG (DATA DROPDOWN) ---
    // Android butuh ini buat ngisi Pilihan Console & Pilihan Paket
    Route::get('/master/consoles', [MasterDataController::class, 'getConsoles']); 
    Route::get('/master/pakets', [MasterDataController::class, 'getPakets']);
    
    
    // Menampilkan halaman daftar TV (Admin)
    Route::get('/tvs', [TvController::class, 'index']);

    // Menyimpan data TV baru
    Route::post('/tvs', [TvController::class, 'store']);

    // Update data TV (Edit Status / Console)
    // Pakai {id} karena di controller kamu menangkap $id
    Route::put('/tvs/{id}', [TvController::class, 'update']);

    // Menghapus data TV
    Route::delete('/tvs/{id}', [TvController::class, 'destroy']);
    
    // Route Khusus API untuk Dropdown Kasir (Yang tadi kita bahas)
    Route::get('/tvs-available', [TvController::class, 'getAvailableTvs']);
// });