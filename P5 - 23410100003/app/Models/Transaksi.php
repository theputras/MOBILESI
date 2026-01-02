<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Transaksi extends Model
{
    use HasFactory;
    
    protected $table = 'transaksi';
    protected $primaryKey = 'id_transaksi';
protected $fillable = [
        'nama_penyewa',
        'tv_id',        // <--- INI SEBELUMNYA HILANG
        'id_paket',     // <--- INI JUGA
        'total_tagihan',
        'uang_bayar',
        'uang_kembalian',
        'metode_pembayaran',
        'status_pembayaran'
    ];

    // Relasi biar bisa ambil nama console
    public function console()
    {
        return $this->belongsTo(JenisConsole::class, 'id_console', 'id_console');
    }
}