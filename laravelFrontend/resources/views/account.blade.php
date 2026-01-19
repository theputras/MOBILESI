@extends('layouts.app')

@section('title', 'Akun Saya')

@section('content')
{{-- Account Header --}}
<div class="account-header">
    <div class="account-avatar">
        <i class="bi bi-person-fill"></i>
    </div>
    <div class="account-name">{{ session('user_name', 'Admin Rental') }}</div>
    <div class="account-role">Kasir - Shift Aktif</div>
</div>

{{-- Account Menu --}}
<div class="account-menu">
    <div class="mb-3">
        <small class="text-muted fw-bold">PENGATURAN UMUM</small>
    </div>
    
    <div class="account-menu-item">
        <i class="bi bi-printer"></i>
        <span>Atur Printer</span>
        <span class="menu-value text-danger">Belum Terhubung</span>
    </div>
    
    <div class="account-menu-item">
        <i class="bi bi-file-earmark-text"></i>
        <span>Ukuran Kertas</span>
        <span class="menu-value">58mm (Kecil)</span>
    </div>
    
    <hr class="my-3">
    
    <div class="mb-3">
        <small class="text-muted fw-bold">TENTANG APLIKASI</small>
    </div>
    
    <div class="account-menu-item">
        <i class="bi bi-info-circle"></i>
        <span>Versi Aplikasi</span>
        <span class="menu-value">v1.0.0</span>
    </div>
    
    <a href="{{ route('logout') }}" class="account-menu-item danger text-decoration-none">
        <i class="bi bi-box-arrow-right"></i>
        <span>Keluar Aplikasi</span>
    </a>
</div>
@endsection
