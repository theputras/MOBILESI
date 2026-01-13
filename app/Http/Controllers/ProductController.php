<?php

namespace App\Http\Controllers;

use Illuminate\Support\Facades\Http;
use GuzzleHttp\Promise\PromiseInterface;

class ProductController extends Controller
{
    public function tampil()
    {
        $response = Http::timeout(5)
            ->retry(3, 100)
            ->get('http://172.16.3.3:60600/api/bacaqb');
        // Handle async promise if returned
        if ($response instanceof PromiseInterface) {
            $response = $response->wait();
        }

        if ($response->successful()) {
            // Return JSON response for the browser
            return $response->json();
        } else {
            return response()->json([
                'error' => 'Failed to fetch data',
                'status' => $response->status()
            ], $response->status());
        }
    }
}

