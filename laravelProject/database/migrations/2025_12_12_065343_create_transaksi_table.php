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
    $table->id('id_transaksi');
    $table->dateTime('tanggal_transaksi')->useCurrent();
    $table->string('nama_penyewa', 100);
    
    // Kita pakai ID, bukan String Nomor TV biar berelasi
    $table->foreignId('tv_id')->constrained('tvs')->onDelete('cascade');
    
    $table->foreignId('id_paket')->constrained('paket_sewa', 'id_paket')->onDelete('restrict');
    
    $table->integer('total_tagihan');
    $table->integer('uang_bayar');
    $table->integer('uang_kembalian');
    $table->string('metode_pembayaran', 20)->default('TUNAI'); // Nanti bisa QRIS
    $table->string('status_pembayaran', 15)->default('LUNAS');
    $table->timestamps();
});
}

public function down(): void
{
    Schema::dropIfExists('transaksi');
}
};