@extends('layouts.app')

@section('title', 'Bukti Transaksi')

@section('content')
<div class="struk-container">
    {{-- Page Title --}}
    <h4 class="text-center mb-4 no-print">Bukti Transaksi</h4>
    
    {{-- Struk Card --}}
    <div class="struk-card" id="strukContent">
        {{-- Header --}}
        <div class="struk-header">
            <div class="struk-logo">🎮</div>
            <div class="struk-store-name">RENTAL PS SANTUY</div>
            <div class="struk-address">Jl. Low Effort No. 1, Surabaya</div>
        </div>
        
        <hr class="struk-divider">
        
        {{-- Info Transaksi --}}
        @php
            // Handle different data formats
            $idTrx = $transaksi->id_transaksi ?? $transaksi->id ?? 0;
            $tanggal = $transaksi->tanggal_transaksi ?? $transaksi->created_at ?? now();
            $namaPenyewa = $transaksi->nama_penyewa ?? 'Guest';
            $total = $transaksi->total_tagihan ?? $transaksi->total ?? 0;
            $uangBayar = $transaksi->uang_bayar ?? 0;
            $kembalian = $transaksi->uang_kembalian ?? ($uangBayar - $total);
            $metodePembayaran = $transaksi->metode_pembayaran ?? 'TUNAI';
            $items = $transaksi->details ?? $transaksi->items ?? [];
        @endphp
        
        <div class="struk-info">
            <div class="row">
                <div class="col-4">No:</div>
                <div class="col-8 text-end">TRX-{{ $idTrx }}</div>
            </div>
            <div class="row">
                <div class="col-4">Tgl:</div>
                <div class="col-8 text-end">{{ \Carbon\Carbon::parse($tanggal)->format('d/m/Y H:i') }}</div>
            </div>
            <div class="row">
                <div class="col-4">Cust:</div>
                <div class="col-8 text-end fw-bold">{{ $namaPenyewa }}</div>
            </div>
        </div>
        
        <hr class="struk-divider">
        
        {{-- Items --}}
        <div class="struk-items">
            @if(is_array($items) || is_object($items))
                @foreach($items as $item)
                    @php
                        // Handle array or object
                        $itemData = is_array($item) ? $item : (array)$item;
                        
                        // Get item name - support multiple formats
                        $itemName = $itemData['nama_item_snapshot'] 
                            ?? ($itemData['tv_name'] ?? 'TV') . ' - ' . ($itemData['paket_name'] ?? ($itemData['paket']['nama_paket'] ?? 'Paket'))
                            ?? 'Item';
                        
                        // Get price
                        $itemPrice = $itemData['harga'] ?? $itemData['subtotal'] ?? $itemData['harga_satuan'] ?? 0;
                    @endphp
                    <div class="struk-item">
                        <span>{{ $itemName }}</span>
                        <span>Rp {{ number_format($itemPrice) }}</span>
                    </div>
                @endforeach
            @endif
        </div>
        
        <hr class="struk-divider">
        
        {{-- Total --}}
        <div class="struk-info">
            <div class="row struk-total">
                <div class="col-6">TOTAL</div>
                <div class="col-6 text-end">Rp {{ number_format($total) }}</div>
            </div>
            <div class="row">
                <div class="col-6">{{ strtoupper($metodePembayaran) }}</div>
                <div class="col-6 text-end">Rp {{ number_format($uangBayar) }}</div>
            </div>
            @if(strtoupper($metodePembayaran) === 'TUNAI')
            <div class="row">
                <div class="col-6">KEMBALI</div>
                <div class="col-6 text-end">Rp {{ number_format($kembalian) }}</div>
            </div>
            @endif
        </div>
        
        {{-- Footer --}}
        <div class="struk-footer">
            <p class="mb-0">TERIMA KASIH</p>
            <p class="mb-0">Selamat Bermain!</p>
        </div>
    </div>
    
    {{-- Buttons --}}
    <div class="mt-4 no-print">
        <button class="btn btn-secondary w-100 mb-2 btn-print" onclick="window.print()">
            <i class="bi bi-printer me-2"></i> CETAK STRUK
        </button>
        <a href="{{ route('dashboard') }}" class="btn btn-primary w-100 btn-back">
            <i class="bi bi-house me-2"></i> KEMBALI KE MENU UTAMA
        </a>
    </div>
</div>
@endsection

@push('styles')
<style>
@media print {
    .struk-container {
        max-width: 100% !important;
    }
}
</style>
@endpush
