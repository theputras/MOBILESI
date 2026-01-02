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
    // Sesuaikan 'jenis_console_id' dengan nama kolom foreign key di tabel 'tvs'
    return $this->belongsTo(JenisConsole::class, 'jenis_console_id', 'id_console');
}
    
    // Scope biar coding di controller lebih pendek (Opsional)
    public function scopeAvailable($query)
    {
        return $query->where('status', 'available');
    }
}