<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Tv extends Model
{
    use HasFactory;

    protected $table = 'tvs';
    protected $fillable = ['nomor_tv', 'id_console', 'status', 'rental_end_time'];

    // PERBAIKAN DISINI:
    public function jenisConsole()
    {
        // Parameter 2: Foreign Key di tabel 'tvs' (id_console)
        // Parameter 3: Primary Key di tabel 'jenis_console' (id_console)
        return $this->belongsTo(JenisConsole::class, 'id_console', 'id_console');
    }
}