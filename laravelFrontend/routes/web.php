<?php

use App\Http\Controllers\FrontendAuthController;
use App\Http\Controllers\RentalFrontendController;
use App\Http\Controllers\HistoryController;
use App\Http\Controllers\AccountController;
use App\Http\Middleware\CekTokenBackend;
use Illuminate\Support\Facades\Route;

// ============================================
// PUBLIC ROUTES (Tanpa Login)
// ============================================

// Halaman Login
Route::get('/login', [FrontendAuthController::class, 'showLoginForm'])->name('login');
Route::post('/login', [FrontendAuthController::class, 'login']);
Route::get('/logout', [FrontendAuthController::class, 'logout'])->name('logout');

// Halaman Register
Route::get('/register', [FrontendAuthController::class, 'showRegisterForm'])->name('register');
Route::post('/register', [FrontendAuthController::class, 'register']);

// ============================================
// PROTECTED ROUTES (Harus Login)
// ============================================

Route::middleware([CekTokenBackend::class])->group(function () {
    
    // Dashboard (Home)
    Route::get('/', [RentalFrontendController::class, 'index'])->name('dashboard');
    
    // Cart Management (AJAX)
    Route::post('/cart/add', [RentalFrontendController::class, 'addToCart'])->name('cart.add');
    Route::delete('/cart/remove/{index}', [RentalFrontendController::class, 'removeFromCart'])->name('cart.remove');
    
    // Checkout & Struk
    Route::post('/checkout', [RentalFrontendController::class, 'checkout'])->name('checkout');
    Route::get('/struk/{id}', [RentalFrontendController::class, 'showStruk'])->name('struk');
    
    // History (Riwayat Transaksi)
    Route::get('/history', [HistoryController::class, 'index'])->name('history');
    Route::get('/history/{id}', [HistoryController::class, 'show'])->name('history.show');
    
    // Account (Akun)
    Route::get('/account', [AccountController::class, 'index'])->name('account');
    
    // QRIS Dynamic Generation (AJAX)
    Route::post('/generate-qris', [RentalFrontendController::class, 'generateQris'])->name('qris.generate');
    
    // Legacy routes (backward compatibility)
    Route::post('/sewa', [RentalFrontendController::class, 'store'])->name('sewa.store');
    Route::post('/stop/{id}', [RentalFrontendController::class, 'stop'])->name('sewa.stop');
});