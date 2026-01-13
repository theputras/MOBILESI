<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class DetailTransaksi extends Model
{
    protected $table = 'detail_transaksi';
    
    protected $fillable = [
        'id_transaksi',
        'tv_id',
        'id_paket',
        'id_console', // <--- TAMBAHAN: Masukkan ini kalau kolomnya dibuat
        'id_item',    // Buat jaga-jaga kalau nanti jualan snack
        'nama_item_snapshot', 
        'harga_satuan',
        'qty',
        'subtotal'
    ];

    // Relasi balik (Opsional, buat display history di frontend)
    public function tv() {
        return $this->belongsTo(Tv::class, 'tv_id');
    }
    
    // Relasi ke Console (Opsional, buat shortcut ambil nama console)
    public function jenisConsole() {
        return $this->belongsTo(JenisConsole::class, 'id_console');
    }
    public function paket() {
        // Relasi ke model PaketSewa, foreign key 'id_paket', owner key 'id_paket'
        return $this->belongsTo(PaketSewa::class, 'id_paket', 'id_paket');
    }
}