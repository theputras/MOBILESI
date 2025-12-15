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
    Schema::create('jenis_console', function (Blueprint $table) {
        $table->id('id_console');
        $table->string('nama_console', 50); // PS4 atau PS5
        $table->timestamps();
    });
}

    /**
     * Reverse the migrations.
     */
   public function down(): void
{
    Schema::dropIfExists('jenis_console');
}
};