@extends('layouts.app')

@section('title', 'Riwayat Transaksi')

@section('content')
{{-- Header --}}
<div class="page-header">
    <h1 class="title">Riwayat Transaksi</h1>
</div>

{{-- Search Box --}}
<div class="search-box d-flex align-items-center">
    <i class="bi bi-search text-muted me-2"></i>
    <input type="text" id="searchHistory" placeholder="Cari Nama / TV..." onkeyup="filterHistory()">
</div>

{{-- Alert Messages --}}
@if(session('error'))
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ session('error') }}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
@endif

{{-- History List --}}
<div id="historyList">
    @forelse($transactions as $trx)
        @php
            // Handle different field names from backend
            $trxId = $trx->id_transaksi ?? $trx->id ?? 0;
            $tanggal = $trx->tanggal_transaksi ?? $trx->created_at ?? now();
            $nama = $trx->nama_penyewa ?? 'Guest';
            $total = $trx->total_tagihan ?? $trx->total ?? 0;
            $itemCount = count($trx->details ?? $trx->items ?? []);
        @endphp
        <a href="{{ route('history.show', $trxId) }}" class="history-item text-decoration-none">
            <div class="history-icon">
                <i class="bi bi-receipt"></i>
            </div>
            <div class="history-info">
                <div class="history-name text-dark" data-name="{{ strtolower($nama) }}">
                    {{ $nama }}
                </div>
                <div class="history-date">
                    {{ \Carbon\Carbon::parse($tanggal)->format('d M Y, H:i') }}
                    • {{ $itemCount }} item
                </div>
            </div>
            <div class="history-amount">
                Rp {{ number_format($total) }}
            </div>
        </a>
    @empty
        <div class="empty-state">
            <i class="bi bi-clock-history"></i>
            <h5>Belum ada riwayat transaksi</h5>
            <p>Transaksi anda akan muncul disini</p>
        </div>
    @endforelse
</div>
@endsection

@push('scripts')
<script>
function filterHistory() {
    const query = document.getElementById('searchHistory').value.toLowerCase();
    const items = document.querySelectorAll('.history-item');
    
    items.forEach(item => {
        const name = item.querySelector('.history-name').dataset.name;
        if (name.includes(query)) {
            item.style.display = '';
        } else {
            item.style.display = 'none';
        }
    });
}
</script>
@endpush
