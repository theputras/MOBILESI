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
        Schema::create('konsumens', function (Blueprint $table) {
            $table->id();
            $table->string('kodekonsumen', 50)->unique();
            $table->string('namakonsumen', 100);
            $table->string('telp', 30)->nullable();
            $table->text('alamat')->nullable();
            $table->string('perusahaan', 150)->nullable();
            $table->text('keterangan')->nullable();
            $table->timestamps(); // otomatis bikin created_at & updated_at
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('konsumens');
    }
};
