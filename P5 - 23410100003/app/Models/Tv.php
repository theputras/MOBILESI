<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Tv extends Model
{
    protected $table = 'tvs';
    protected $guarded = ['id'];

    // Relasi: Setiap TV memiliki 1 Jenis Console
    public function jenisConsole()
    {
        // Parameter: (ModelTujuan, Foreign_Key_di_tabel_ini, Primary_key_di_tabel_tujuan)
        return $this->belongsTo(JenisConsole::class, 'id_console', 'id_console');
    }
    
    // Scope biar coding di controller lebih pendek (Opsional)
    public function scopeAvailable($query)
    {
        return $query->where('status', 'available');
    }
}