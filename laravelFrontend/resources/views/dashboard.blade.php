@extends('layouts.app')

@section('title', 'Dashboard')

@section('content')
{{-- Header --}}
<div class="page-header">
    <p class="greeting">Selamat Datang,</p>
    <h1 class="title">RENTAL PS SANTUY</h1>
    <p class="subtitle">Pilih TV untuk mulai sewa</p>
</div>

{{-- Search Box --}}
<div class="search-box d-flex align-items-center">
    <i class="bi bi-search text-muted me-2"></i>
    <input type="text" id="searchTv" placeholder="Cari Nomor TV / Console..." onkeyup="filterTv()">
</div>

{{-- Alert Messages --}}
@if(session('success'))
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        {{ session('success') }}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
@endif

@if(session('error'))
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ session('error') }}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
@endif

{{-- Validation Errors --}}
@if($errors->any())
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <ul class="mb-0">
            @foreach ($errors->all() as $error)
                <li>{{ $error }}</li>
            @endforeach
        </ul>
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
@endif

{{-- Get cart TV IDs --}}
@php 
    $cart = session('cart', []);
    $cartTvIds = collect($cart)->pluck('tv_id')->toArray();
@endphp

{{-- TV Grid --}}
<div class="tv-grid" id="tvGrid">
    @forelse($tvs as $tv)
        @php
            // Check if TV is in cart
            $isInCart = in_array($tv->id, $cartTvIds);
            
            // Determine status and styling
            if ($isInCart) {
                $statusClass = 'status-in-cart';
                $statusText = 'DI KERANJANG';
                $isClickable = false;
            } else {
                $statusClass = match($tv->status) {
                    'available' => 'status-available',
                    'booked' => 'status-booked',
                    default => 'status-maintenance'
                };
                $statusText = strtoupper($tv->status);
                $isClickable = $tv->status === 'available';
            }
            
            $consoleName = $tv->jenis_console['nama_console'] ?? 'Unknown';
            $consoleId = $tv->jenis_console['id_console'] ?? 0;
        @endphp
        
        <div class="tv-card {{ !$isClickable ? 'opacity-75' : '' }} {{ $isInCart ? 'in-cart' : '' }}" 
             data-tv="{{ strtolower($tv->nomor_tv) }}" 
             data-console="{{ strtolower($consoleName) }}"
             data-tv-id="{{ $tv->id }}"
             @if($isClickable)
                onclick="openPaketModal({{ $tv->id }}, '{{ $tv->nomor_tv }}', {{ $consoleId }})"
             @endif>
            
            {{-- Cart indicator badge --}}
            @if($isInCart)
                <div class="cart-indicator">
                    <i class="bi bi-cart-check-fill"></i>
                </div>
            @endif
            
            <div class="tv-icon">
                <i class="bi bi-tv"></i>
            </div>
            <div class="tv-number">{{ $tv->nomor_tv }}</div>
            <div class="tv-console">{{ $consoleName }}</div>
            <span class="status-badge {{ $statusClass }}">
                {{ $statusText }}
            </span>
            
            @if($tv->status === 'booked')
                <div class="mt-2">
                    <div class="small text-muted mb-2">
                        <i class="bi bi-clock"></i> 
                        Selesai: {{ \Carbon\Carbon::parse($tv->rental_end_time)->setTimezone('Asia/Jakarta')->format('H:i') }}
                    </div>
                    <form action="{{ route('sewa.stop', $tv->id) }}" method="POST" onsubmit="return confirm('Stop rental TV {{ $tv->nomor_tv }}?')">
                        @csrf
                        <button type="submit" class="btn btn-sm btn-outline-danger w-100" style="font-size: 12px; padding: 4px 8px;">
                            <i class="bi bi-stop-circle me-1"></i> STOP / RESET
                        </button>
                    </form>
                </div>
            @endif
        </div>
    @empty
        <div class="col-12">
            <div class="empty-state">
                <i class="bi bi-tv"></i>
                <h5>Data TV Tidak Ditemukan</h5>
                <p>Belum ada TV yang terdaftar di sistem</p>
            </div>
        </div>
    @endforelse
</div>

{{-- FAB Cart Button --}}
@php $cartCount = count($cart); @endphp
<button class="fab-cart" id="fabCart" 
        style="{{ $cartCount > 0 ? '' : 'display: none;' }}"
        data-bs-toggle="modal" data-bs-target="#modalCheckout">
    <i class="bi bi-cart3"></i>
    <span class="badge" id="cartBadge" style="{{ $cartCount > 0 ? '' : 'display: none;' }}">
        {{ $cartCount }}
    </span>
</button>

{{-- Include Modals --}}
@include('partials.modal-paket')
@include('partials.modal-checkout')
@endsection

@push('scripts')
<script>
// Filter TV by search
function filterTv() {
    const query = document.getElementById('searchTv').value.toLowerCase();
    const cards = document.querySelectorAll('.tv-card');
    
    cards.forEach(card => {
        const tvName = card.dataset.tv;
        const console = card.dataset.console;
        
        if (tvName.includes(query) || console.includes(query)) {
            card.style.display = '';
        } else {
            card.style.display = 'none';
        }
    });
}
</script>
@endpush