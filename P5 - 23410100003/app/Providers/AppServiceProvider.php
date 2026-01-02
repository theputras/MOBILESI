<?php

namespace App\Providers;

use Illuminate\Support\ServiceProvider;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     */
    public function register(): void
    {
        //
    }

    /**
     * Bootstrap any application services.
     */
    public function boot(): void
    {
        
    }
    // app/Providers/EventServiceProvider.php
protected $listen = [
    \App\Events\RentalStarted::class => [
        \App\Listeners\UpdateTvStatusListener::class,
    ],
];

}
