<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\DB;

class RentalSeeder extends Seeder
{
    /**
     * Run the database seeds.
     */
    public function run(): void
    {
        // ------------------------------------------
        // 1. Insert Jenis Console (Master Data)
        // ------------------------------------------
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

        // Hapus data lama biar gak duplikat (opsional)
        // DB::table('jenis_console')->truncate(); 
        DB::table('jenis_console')->insert($consoles);


        // ------------------------------------------
        // 2. Insert Paket Harga (Sesuai Data Kamu)
        // ------------------------------------------
        $pakets = [
            // --- PAKET PS 5 (ID = 1) ---
            ['id_console' => 1, 'nama_paket' => '15 Menit', 'durasi_menit' => 15,  'harga' => 20000,  'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 1, 'nama_paket' => '30 Menit', 'durasi_menit' => 30,  'harga' => 30000,  'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 1, 'nama_paket' => '1 Jam',    'durasi_menit' => 60,  'harga' => 50000,  'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 1, 'nama_paket' => '3 Jam',    'durasi_menit' => 180, 'harga' => 140000, 'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 1, 'nama_paket' => '5 Jam',    'durasi_menit' => 300, 'harga' => 220000, 'created_at' => now(), 'updated_at' => now()],

            // --- PAKET PS 4 (ID = 2) ---
            ['id_console' => 2, 'nama_paket' => '15 Menit', 'durasi_menit' => 15,  'harga' => 10000,  'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 2, 'nama_paket' => '30 Menit', 'durasi_menit' => 30,  'harga' => 18000,  'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 2, 'nama_paket' => '1 Jam',    'durasi_menit' => 60,  'harga' => 30000,  'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 2, 'nama_paket' => '3 Jam',    'durasi_menit' => 180, 'harga' => 84000,  'created_at' => now(), 'updated_at' => now()],
            ['id_console' => 2, 'nama_paket' => '5 Jam',    'durasi_menit' => 300, 'harga' => 132000, 'created_at' => now(), 'updated_at' => now()],
        ];

        DB::table('paket_sewa')->insert($pakets);


        // ------------------------------------------
        // 3. Insert Data TV (Pemetaan Console ke TV)
        // ------------------------------------------
        // Ingat: ID 1 = PS5, ID 2 = PS4
        $tvs = [
            // TV 1 Pakai PS4
            [
                'nomor_tv' => 'TV 01', 
                'id_console' => 2, 
                'status' => 'booked', // Ceritanya lagi dipakai
                'created_at' => now(), 'updated_at' => now()
            ],
            // TV 2 Pakai PS5
            [
                'nomor_tv' => 'TV 02', 
                'id_console' => 1, 
                'status' => 'available', 
                'created_at' => now(), 'updated_at' => now()
            ],
            // TV 3 Pakai PS4
            [
                'nomor_tv' => 'TV 03', 
                'id_console' => 2, 
                'status' => 'available', 
                'created_at' => now(), 'updated_at' => now()
            ],
            // TV 4 Pakai PS5
            [
                'nomor_tv' => 'TV 04', 
                'id_console' => 1, 
                'status' => 'available', 
                'created_at' => now(), 'updated_at' => now()
            ],
             // TV 5 Pakai PS5 (Maintenance)
             [
                'nomor_tv' => 'TV 05', 
                'id_console' => 1, 
                'status' => 'maintenance', 
                'created_at' => now(), 'updated_at' => now()
            ],
        ];

        DB::table('tvs')->insert($tvs);
    }
}