{{-- Modal Bottom Sheet: Checkout / Payment --}}
<div class="modal fade bottom-sheet" id="modalCheckout" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Checkout Pembayaran</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                {{-- Cart Items --}}
                <div id="cartItemsContainer">
                    @php $cart = session('cart', []); @endphp
                    @forelse($cart as $index => $item)
                        <div class="cart-item" id="cartItem-{{ $index }}">
                            <div class="item-icon">
                                <i class="bi bi-tv"></i>
                            </div>
                            <div class="item-info">
                                <div class="item-name">{{ $item['tv_name'] }} - {{ $item['paket_name'] }}</div>
                                <div class="item-price">Rp {{ number_format($item['harga']) }}</div>
                            </div>
                            <button type="button" class="btn-remove" onclick="removeFromCart({{ $index }})">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>
                    @empty
                        <div class="empty-state py-4">
                            <i class="bi bi-cart-x"></i>
                            <p>Keranjang kosong</p>
                        </div>
                    @endforelse
                </div>
                
                @if(count($cart) > 0)
                <hr>
                
                {{-- Form Checkout --}}
                <form action="{{ route('checkout') }}" method="POST" id="formCheckout">
                    @csrf
                    
                    {{-- Nama Penyewa --}}
                    <div class="mb-3" id="namaPenyewaContainer">
                        <label class="form-label">Nama Penyewa</label>
                        <input type="text" class="form-control" name="nama_penyewa" id="inputNamaPenyewa" required 
                               placeholder="Masukkan nama penyewa">
                    </div>
                    
                    {{-- Metode Pembayaran --}}
                    <div class="mb-3" id="metodePembayaranContainer">
                        <label class="form-label">Metode Pembayaran</label>
                        <div class="payment-method">
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="metode_pembayaran" 
                                       id="payTunai" value="TUNAI" checked>
                                <label class="form-check-label" for="payTunai">
                                    <i class="bi bi-cash-stack"></i> Tunai
                                </label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="metode_pembayaran" 
                                       id="payQris" value="QRIS">
                                <label class="form-check-label" for="payQris">
                                    <i class="bi bi-qr-code"></i> QRIS
                                </label>
                            </div>
                        </div>
                    </div>
                    
                    {{-- Uang Bayar (Tunai) --}}
                    <div class="mb-3" id="cashInputContainer">
                        <label class="form-label">Uang Diterima (Rp)</label>
                        <input type="number" class="form-control" name="uang_bayar" id="inputUangBayar"
                               placeholder="Masukkan nominal uang">
                    </div>
                    
                    {{-- QRIS Section (Hidden by default) - SSR Generated --}}
                    @php $cartTotal = collect($cart)->sum('harga'); @endphp
                    <div id="qrisContainer" style="display: none;">
                        <div class="text-center mb-3">
                            <div class="qris-amount-display">
                                <small class="text-muted d-block mb-1">Total yang harus dibayar</small>
                                <h2 class="text-success fw-bold mb-0">Rp {{ number_format($cartTotal) }}</h2>
                            </div>
                        </div>
                        
                        {{-- SSR Generated QR Code --}}
                        <div class="qris-image-container text-center mb-3">
                            @if(isset($qrisSvg) && $qrisSvg)
                                {{-- Render QR - $qrisSvg already is a data URI --}}
                                <img src="{{ $qrisSvg }}" 
                                     alt="QRIS Dynamic Payment" 
                                     class="img-fluid"
                                     style="max-width: 280px; border-radius: 12px; box-shadow: 0 4px 16px rgba(0,0,0,0.1); background: white; padding: 10px;">
                            @else
                                {{-- Fallback: Static QRIS image --}}
                                <img src="{{ asset('images/qris.png') }}" alt="QRIS Payment" class="img-fluid" 
                                     style="max-width: 280px; border-radius: 12px; box-shadow: 0 4px 16px rgba(0,0,0,0.1);">
                                <p class="text-warning small mt-2 mb-0">
                                    <i class="bi bi-exclamation-triangle me-1"></i>
                                    QRIS Statis - Masukkan nominal manual
                                </p>
                            @endif
                        </div>
                        
                        <div class="text-center mb-3">
                            <p class="text-muted small mb-0">
                                <i class="bi bi-info-circle me-1"></i>
                                Scan QR code di atas menggunakan aplikasi e-wallet atau mobile banking
                            </p>
                            @if(isset($qrisSvg) && $qrisSvg)
                            <p class="text-success small mt-1 mb-0">
                                <i class="bi bi-check-circle me-1"></i>
                                <strong>Nominal sudah terisi otomatis!</strong>
                            </p>
                            @endif
                        </div>
                        
                        <div class="alert alert-info py-2 mb-3">
                            <div class="d-flex align-items-center">
                                <i class="bi bi-exclamation-circle me-2"></i>
                                <small>Setelah pembayaran berhasil, tekan tombol <strong>"Selesai Dibayar"</strong></small>
                            </div>
                        </div>
                    </div>
                    
                    {{-- Total (untuk TUNAI) --}}
                    <div class="total-section" id="totalSectionTunai">
                        <span class="total-label">Total Tagihan</span>
                        <span class="total-amount">Rp {{ number_format($cartTotal) }}</span>
                    </div>
                    
                    <input type="hidden" name="total" value="{{ $cartTotal }}">
                    
                    {{-- Button Bayar TUNAI --}}
                    <button type="submit" class="btn btn-primary btn-pay mt-3" id="btnBayarTunai">
                        <i class="bi bi-credit-card me-2"></i> BAYAR SEKARANG
                    </button>
                    
                    {{-- Button Selesai Dibayar QRIS --}}
                    <button type="submit" class="btn btn-success btn-pay mt-3" id="btnSelesaiQris" style="display: none;">
                        <i class="bi bi-check-circle me-2"></i> SELESAI DIBAYAR
                    </button>
                </form>
                @endif
            </div>
        </div>
    </div>
</div>

@push('scripts')
<script>
// Toggle payment method views
document.querySelectorAll('input[name="metode_pembayaran"]').forEach(radio => {
    radio.addEventListener('change', function() {
        const cashContainer = document.getElementById('cashInputContainer');
        const cashInput = document.getElementById('inputUangBayar');
        const qrisContainer = document.getElementById('qrisContainer');
        const totalSectionTunai = document.getElementById('totalSectionTunai');
        const btnBayarTunai = document.getElementById('btnBayarTunai');
        const btnSelesaiQris = document.getElementById('btnSelesaiQris');
        
        if (this.value === 'QRIS') {
            // Show QRIS, hide TUNAI
            cashContainer.style.display = 'none';
            cashInput.removeAttribute('required');
            qrisContainer.style.display = 'block';
            totalSectionTunai.style.display = 'none';
            btnBayarTunai.style.display = 'none';
            btnSelesaiQris.style.display = 'block';
        } else {
            // Show TUNAI, hide QRIS
            cashContainer.style.display = 'block';
            cashInput.setAttribute('required', 'required');
            qrisContainer.style.display = 'none';
            totalSectionTunai.style.display = 'flex';
            btnBayarTunai.style.display = 'block';
            btnSelesaiQris.style.display = 'none';
        }
    });
});

// Remove item from cart
function removeFromCart(index) {
    fetch(`{{ url('cart/remove') }}/${index}`, {
        method: 'DELETE',
        headers: {
            'X-CSRF-TOKEN': document.querySelector('meta[name="csrf-token"]').content
        }
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            // Reload to update (will regenerate QRIS with new total)
            location.reload();
        }
    });
}

// Update cart badge
function updateCartBadge(count) {
    const badge = document.getElementById('cartBadge');
    const fab = document.getElementById('fabCart');
    
    if (count > 0) {
        badge.textContent = count;
        badge.style.display = 'flex';
        fab.style.display = 'flex';
    } else {
        fab.style.display = 'none';
    }
}

// Show toast notification
function showToast(type, message) {
    const toast = document.createElement('div');
    toast.className = `alert alert-${type === 'success' ? 'success' : 'danger'} alert-float`;
    toast.innerHTML = message;
    document.body.appendChild(toast);
    
    setTimeout(() => toast.remove(), 3000);
}
</script>
@endpush

@push('styles')
<style>
.qris-amount-display {
    background: linear-gradient(135deg, #E8F5E9, #C8E6C9);
    padding: 16px 24px;
    border-radius: 12px;
    margin-bottom: 16px;
}

.qris-amount-display h2 {
    font-size: 28px;
}

.qris-image-container {
    background: #f8f9fa;
    padding: 16px;
    border-radius: 16px;
}

.qris-svg-wrapper svg {
    width: 100%;
    height: auto;
    max-width: 260px;
}
</style>
@endpush
