<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Konsumen extends Model
{
    /** @use HasFactory<\Database\Factories\KonsumenFactory> */
    use HasFactory;
    
        protected $fillable = [
        'kodekonsumen',
        'namakonsumen',
        'telp',
        'alamat',
        'perusahaan',
        'keterangan'
    ];
}
