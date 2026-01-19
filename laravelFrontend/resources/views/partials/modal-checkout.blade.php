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
                    <div class="mb-3">
                        <label class="form-label">Nama Penyewa</label>
                        <input type="text" class="form-control" name="nama_penyewa" required 
                               placeholder="Masukkan nama penyewa">
                    </div>
                    
                    {{-- Metode Pembayaran --}}
                    <div class="mb-3">
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
                    
                    {{-- Uang Bayar --}}
                    <div class="mb-3" id="cashInputContainer">
                        <label class="form-label">Uang Diterima (Rp)</label>
                        <input type="number" class="form-control" name="uang_bayar" id="inputUangBayar"
                               placeholder="Masukkan nominal uang">
                    </div>
                    
                    {{-- Total --}}
                    @php $total = collect($cart)->sum('harga'); @endphp
                    <div class="total-section">
                        <span class="total-label">Total Tagihan</span>
                        <span class="total-amount">Rp {{ number_format($total) }}</span>
                    </div>
                    
                    <input type="hidden" name="total" value="{{ $total }}">
                    
                    {{-- Button Bayar --}}
                    <button type="submit" class="btn btn-primary btn-pay mt-3">
                        <i class="bi bi-credit-card me-2"></i> BAYAR SEKARANG
                    </button>
                </form>
                @endif
            </div>
        </div>
    </div>
</div>

@push('scripts')
<script>
// Toggle cash input based on payment method
document.querySelectorAll('input[name="metode_pembayaran"]').forEach(radio => {
    radio.addEventListener('change', function() {
        const cashContainer = document.getElementById('cashInputContainer');
        const cashInput = document.getElementById('inputUangBayar');
        
        if (this.value === 'QRIS') {
            cashContainer.style.display = 'none';
            cashInput.removeAttribute('required');
        } else {
            cashContainer.style.display = 'block';
            cashInput.setAttribute('required', 'required');
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
            // Reload to update
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
