<?php

use App\Http\Controllers\FrontendAuthController;
use App\Http\Controllers\RentalFrontendController;
use App\Http\Middleware\CekTokenBackend;
use Illuminate\Support\Facades\Route;

// Halaman Login
Route::get('/login', [FrontendAuthController::class, 'showLoginForm'])->name('login');
Route::post('/login', [FrontendAuthController::class, 'login']);
Route::get('/logout', [FrontendAuthController::class, 'logout'])->name('logout');

// Halaman Dashboard (Diproteksi Middleware)
Route::middleware([CekTokenBackend::class])->group(function () {
    Route::get('/', [RentalFrontendController::class, 'index'])->name('dashboard');
    Route::post('/sewa', [RentalFrontendController::class, 'store'])->name('sewa.store');
    Route::post('/stop/{id}', [RentalFrontendController::class, 'stop'])->name('sewa.stop');
});