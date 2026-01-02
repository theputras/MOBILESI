<?php

// app/Listeners/UpdateTvStatusListener.php
namespace App\Listeners;

use App\Events\RentalStarted;
use App\Models\Tv;
use Carbon\Carbon;

class UpdateTvStatusListener
{
    public function handle(RentalStarted $event)
    {
        // Ambil TV berdasarkan ID
        $tv = Tv::find($event->tvId);
        $elapsedTime = Carbon::now()->diffInMinutes($tv->rental_start_time);

        // Cek apakah durasi sewa sudah berakhir
        if ($elapsedTime >= $event->rentalDuration) {
            // Update status menjadi available
            $tv->update(['status' => 'available']);
        }
    }
}

