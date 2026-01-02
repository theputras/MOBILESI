<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Transaksi extends Model
{
    use HasFactory;

    protected $table = 'transaksi';
    protected $primaryKey = 'id_transaksi';
    
    // Pastikan fillable sudah benar
    protected $fillable = [
        'nama_penyewa', 'tv_id', 'id_paket', 'total_tagihan', 
        'uang_bayar', 'uang_kembalian', 'metode_pembayaran', 
        'status_pembayaran', 'tanggal_transaksi'
    ];

    // Otomatis load relasi 'tv' dan append attribute 'console' setiap query
    protected $with = ['tv']; 
    protected $appends = ['console']; 

    // Relasi ke TV
    public function tv()
    {
        return $this->belongsTo(Tv::class, 'tv_id', 'id'); // Pastikan 'id' adalah PK di tabel tvs
    }

    // Relasi ke Paket (Opsional)
    public function paket()
    {
        return $this->belongsTo(PaketSewa::class, 'id_paket', 'id_paket');
    }

    // --- MAGIC ACCESSOR: Ini yang bikin field "console" terisi ---
    public function getConsoleAttribute()
    {
        // Cek apakah ada data TV, lalu ambil relasi jenisConsole dari TV
        return $this->tv ? $this->tv->jenisConsole : null;
    }
}