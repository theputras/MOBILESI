<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class PaketSewa extends Model
{
    use HasFactory;

    // Karena nama tabelnya bukan 'paket_sewas' (default laravel), kita harus define
    protected $table = 'paket_sewa';
    
    // Karena primary key-nya 'id_paket' (bukan 'id'), kita harus define
    protected $primaryKey = 'id_paket';

    protected $fillable = [
        'id_console',
        'nama_paket',
        'durasi_menit',
        'harga'
    ];

    // Relasi ke Jenis Console (Opsional, tapi bagus buat validasi)
    public function console()
    {
        return $this->belongsTo(JenisConsole::class, 'id_console', 'id_console');
    }
}