<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
public function up(): void
    {
        Schema::create('transaksi', function (Blueprint $table) {
            // PRIMARY KEY STRING (Format: 20260112092001)
            $table->string('id_transaksi', 20)->primary(); 
            
            $table->dateTime('tanggal_transaksi')->useCurrent();
            $table->string('nama_penyewa', 100);
            
            // Kolom Keuangan Global
            $table->integer('total_tagihan');
            $table->integer('uang_bayar')->nullable(); // Nullable jaga2 kalau bayar nanti
            $table->integer('uang_kembalian')->nullable();
            
            $table->string('metode_pembayaran', 20)->default('TUNAI');
            $table->string('status_pembayaran', 15)->default('LUNAS');

            // Opsional: Siapa kasir yang input (Relasi ke tabel users)
            // $table->foreignId('user_id')->nullable()->constrained('users'); 

            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('transaksi');
    }
};