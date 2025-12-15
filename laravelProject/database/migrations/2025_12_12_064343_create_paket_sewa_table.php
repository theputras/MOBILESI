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
    Schema::create('paket_sewa', function (Blueprint $table) {
        $table->id('id_paket');
        
        // Relasi: Paket ini milik console apa? (PS4/PS5)
        $table->foreignId('id_console')
              ->constrained('jenis_console', 'id_console')
              ->onDelete('cascade');

        $table->string('nama_paket', 50); // Contoh: "15 Menit", "Paket 3 Jam"
        $table->integer('durasi_menit');  // Contoh: 15, 60, 180 (Simpan dalam menit biar gampang)
        $table->integer('harga');         // Contoh: 20000, 140000
        
        $table->timestamps();
    });
}

    /**
     * Reverse the migrations.
     */
public function down(): void
{
    Schema::dropIfExists('paket_sewa');
}
};
