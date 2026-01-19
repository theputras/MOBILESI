{{-- Modal Bottom Sheet: Pilih Paket --}}
<div class="modal fade bottom-sheet" id="modalPaket" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Pilih Paket Sewa</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" id="selectedTvId" value="">
                <input type="hidden" id="selectedTvName" value="">
                <input type="hidden" id="selectedConsoleId" value="">
                
                <div class="paket-grid" id="paketGrid">
                    {{-- Paket akan di-render via JavaScript --}}
                </div>
            </div>
        </div>
    </div>
</div>

@push('scripts')
<script>
// Data paket dari backend (di-pass dari controller)
const allPakets = @json($pakets ?? []);

// Ketika TV card diklik
function openPaketModal(tvId, tvName, consoleId) {
    document.getElementById('selectedTvId').value = tvId;
    document.getElementById('selectedTvName').value = tvName;
    document.getElementById('selectedConsoleId').value = consoleId;
    
    // Filter paket sesuai console
    const filteredPakets = allPakets.filter(p => p.id_console == consoleId);
    
    // Render paket cards
    const grid = document.getElementById('paketGrid');
    grid.innerHTML = filteredPakets.map(paket => `
        <div class="paket-card" onclick="addToCart(${paket.id_paket}, '${paket.nama_paket}', ${paket.harga})">
            <div class="paket-header">
                <div class="paket-name">${paket.nama_paket}</div>
            </div>
            <div class="paket-body">
                <div class="paket-label">Harga</div>
                <div class="paket-price">Rp ${numberFormat(paket.harga)}</div>
            </div>
        </div>
    `).join('');
    
    // Show modal
    new bootstrap.Modal(document.getElementById('modalPaket')).show();
}

// Add to cart via AJAX
function addToCart(paketId, paketName, harga) {
    const tvId = document.getElementById('selectedTvId').value;
    const tvName = document.getElementById('selectedTvName').value;
    
    fetch('{{ route("cart.add") }}', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="csrf-token"]').content
        },
        body: JSON.stringify({
            tv_id: tvId,
            tv_name: tvName,
            paket_id: paketId,
            paket_name: paketName,
            harga: harga
        })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            // Close modal paket
            bootstrap.Modal.getInstance(document.getElementById('modalPaket')).hide();
            
            // Update cart count
            updateCartBadge(data.cart_count);
            
            // Show toast
            showToast('success', `${tvName} - ${paketName} ditambahkan ke keranjang`);
            
            // Reload page to update TV status
            setTimeout(() => location.reload(), 500);
        }
    });
}

function numberFormat(num) {
    return new Intl.NumberFormat('id-ID').format(num);
}
</script>
@endpush
