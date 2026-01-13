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
        Schema::create('detail_transaksi', function (Blueprint $table) {
            $table->id(); // ID internal

            // LINK KE MASTER TRANSAKSI
            $table->string('id_transaksi', 20);
            $table->foreign('id_transaksi')
                  ->references('id_transaksi')->on('transaksi')
                  ->onDelete('cascade'); // Kalau master dihapus, detail ikut hilang

            // JENIS ITEM (Bisa Sewa, Bisa Jajan)
            // Kita bikin nullable semua FK-nya biar fleksibel
            
            // 1. Kalo ini item SEWA RENTAL
            $table->foreignId('tv_id')->nullable()->constrained('tvs');
            $table->foreignId('id_paket')->nullable()->constrained('paket_sewa', 'id_paket');
            
            // 2. Kalo ini item JAJANAN/MINUMAN (dari tabel items)
            $table->foreignId('id_item')->nullable()->constrained('items');
            $table->integer('id_console')->nullable();
            // SNAPSHOT DATA (PENTING!)
            // Nama & Harga disimpan teks/angka di sini. 
            // Jadi kalo harga master naik, riwayat transaksi gak berubah.
            $table->string('nama_item_snapshot'); // Contoh: "Paket 1 Jam (PS5)" atau "Teh Botol"
            $table->integer('harga_satuan');
            $table->integer('qty');
            $table->integer('subtotal'); // harga_satuan * qty

            $table->timestamps();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('detail_transaksi');
    }
};
