<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\Models\Transaksi;

class HistoryController extends Controller
{
    /**
     * List all transactions
     */
    public function index()
    {
        $transactions = Transaksi::all();
        
        return view('history', compact('transactions'));
    }

    /**
     * Show transaction detail (struk) - supports both id and id_transaksi
     */
    public function show($id)
    {
        $transaksi = Transaksi::find($id);

        if (!$transaksi) {
            return redirect()->route('history')->with('error', 'Transaksi tidak ditemukan');
        }

        return view('struk', compact('transaksi'));
    }
}
