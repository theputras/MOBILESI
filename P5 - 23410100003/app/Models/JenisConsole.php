<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class JenisConsole extends Model
{
    use HasFactory;
    
    protected $table = 'jenis_console';
    protected $primaryKey = 'id_console';
    protected $fillable = ['nama_console', 'harga_per_jam'];
}