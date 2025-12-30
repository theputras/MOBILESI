<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
   public function up()
{
    Schema::create('tvs', function (Blueprint $table) {
        $table->id();
        $table->string('nomor_tv')->unique(); 
        
        // PERBAIKAN: Arahkan ke tabel 'jenis_console' dan kolom 'id_console'
        $table->foreignId('id_console') 
              ->constrained('jenis_console', 'id_console') 
              ->onDelete('cascade'); 
        
        $table->enum('status', ['available', 'booked', 'maintenance'])->default('available');
        $table->timestamp('rental_end_time')->nullable();
        $table->timestamps();
    });
}

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('tvs');
    }
};
