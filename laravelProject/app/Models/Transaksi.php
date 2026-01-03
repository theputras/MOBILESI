<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Transaksi extends Model
{
    protected $table = 'transaksi';
    
    // Kita set Primary Key manual (String)
    protected $primaryKey = 'id_transaksi';
    public $incrementing = false; 
    protected $keyType = 'string';

    // Hapus 'tv_id', 'id_paket', 'id_inv' dari sini.
    protected $fillable = [
        'id_transaksi', // PK kita input manual
        'nama_penyewa', 
        'total_tagihan', 
        'uang_bayar', 
        'uang_kembalian', 
        'metode_pembayaran', 
        'status_pembayaran', 
        'tanggal_transaksi'
    ];

    // Relasi ke Detail (One to Many)
    public function details(): HasMany
    {
        return $this->hasMany(DetailTransaksi::class, 'id_transaksi', 'id_transaksi');
    }
}