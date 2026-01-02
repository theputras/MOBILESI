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
        Schema::create('items', function (Blueprint $table) {
            $table->id();
            $table-> string('kode_item', 15)-> nullable()->unique();
            $table-> string('nama_item', 100)->nullable();
            $table-> string('satuan', 15)->nullable();
            $table-> double('hargabeli')->nullable();
            $table-> double('hargajual')->nullable();
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('items');
    }
};
