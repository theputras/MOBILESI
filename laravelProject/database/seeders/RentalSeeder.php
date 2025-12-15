<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB; // <--- Wajib di-import

class RentalSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        // 1. Insert Jenis Console (PS5 & PS4)
        // Kita pakai array biar rapi
        $consoles = [
            [
                'id_console' => 1, 
                'nama_console' => 'PlayStation 5',
                'created_at' => now(), 'updated_at' => now()
            ],
            [
                'id_console' => 2, 
                'nama_console' => 'PlayStation 4',
                'created_at' => now(), 'updated_at' => now()
            ]
        ];

        // Masukkan ke tabel 'jenis_console'
        DB::table('jenis_console')->insert($consoles);


        // 2. Insert Paket Harga (Sesuai Gambar)
        $pakets = [
            // --- PAKET PS 5 (ID Console = 1) ---
            ['id_console' => 1, 'nama_paket' => '15 Menit', 'durasi_menit' => 15,  'harga' => 20000,  'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 1, 'nama_paket' => '30 Menit', 'durasi_menit' => 30,  'harga' => 30000,  'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 1, 'nama_paket' => '1 Jam',    'durasi_menit' => 60,  'harga' => 50000,  'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 1, 'nama_paket' => '3 Jam',    'durasi_menit' => 180, 'harga' => 140000, 'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 1, 'nama_paket' => '5 Jam',    'durasi_menit' => 300, 'harga' => 220000, 'created_at' => now(), 'updated_at' => now()],

            // --- PAKET PS 4 (ID Console = 2) ---
            ['id_console' => 2, 'nama_paket' => '15 Menit', 'durasi_menit' => 15,  'harga' => 10000,  'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 2, 'nama_paket' => '30 Menit', 'durasi_menit' => 30,  'harga' => 18000,  'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 2, 'nama_paket' => '1 Jam',    'durasi_menit' => 60,  'harga' => 30000,  'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 2, 'nama_paket' => '3 Jam',    'durasi_menit' => 180, 'harga' => 84000,  'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 2, 'nama_paket' => '5 Jam',    'durasi_menit' => 300, 'harga' => 132000, 'created_at' => now(), 'updated_at' => now()],
        ];

        // Masukkan ke tabel 'paket_sewa'
        DB::table('paket_sewa')->insert($pakets);
    }
}