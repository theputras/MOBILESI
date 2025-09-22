<?php
use App\Http\Controllers\Api\ItemController;
use App\Http\Controllers\LoginController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\KonsumenController;

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

// Route untuk "Read" semua item
Route::get('/items', [ItemController::class, 'index'])->middleware('auth:sanctum');

// Route untuk "Insert" item baru
Route::post('/items', [ItemController::class, 'store'])->middleware('auth:sanctum');

// Route untuk "Read" item berdasarkan ID
Route::get('/items/{id}', [ItemController::class, 'show'])->middleware('auth:sanctum');

// Route untuk "Update" item berdasarkan ID
Route::put('/items/{item}', [ItemController::class, 'update'])->middleware('auth:sanctum');
Route::patch('/items/{item}', [ItemController::class, 'update'])->middleware('auth:sanctum');


// Route untuk "Delete" item berdasarkan ID
Route::delete('/items/{item}', [ItemController::class, 'destroy'])->middleware('auth:sanctum');

// Route untuk "Read" semua konsumen
Route::get('/konsumens', [KonsumenController::class, 'index'])->middleware('auth:sanctum');

// Route untuk "Insert" konsumen baru
Route::post('/konsumens', [KonsumenController::class, 'store'])->middleware('auth:sanctum');

// Route untuk "Read" konsumen berdasarkan ID
Route::get('/konsumens/{id}', [KonsumenController::class, 'show'])->middleware('auth:sanctum');

// Route untuk "Update" konsumen berdasarkan ID
Route::put('/konsumens/{konsumen}', [KonsumenController::class, 'update'])->middleware('auth:sanctum');
Route::patch('/konsumens/{konsumen}', [KonsumenController::class, 'update'])->middleware('auth:sanctum');

// Route untuk "Delete" konsumen berdasarkan ID
Route::delete('/konsumens/{id}', [KonsumenController::class, 'destroy'])->middleware('auth:sanctum');

Route::post('/register', [LoginController::class, 'register']);
Route::post('/login', [LoginController::class, 'login'])->middleware('throttle:5,1');
Route::post('/logout', [LoginController::class, 'logout'])->middleware('auth:sanctum');

// Opsional
// Route::post('/logout-all', [LoginController::class, 'logoutAll'])->middleware('auth:sanctum');