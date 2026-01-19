<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;

class AccountController extends Controller
{
    /**
     * Show account page
     */
    public function index()
    {
        return view('account');
    }
}
