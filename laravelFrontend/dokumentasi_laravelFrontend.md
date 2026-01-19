# Dokumentasi Lengkap: Laravel Frontend SSR (Rental PS)

Dokumen ini menjelaskan secara **DETAIL** seluruh kode di project `laravelFrontend` - sebuah aplikasi **Server-Side Rendering (SSR)** yang terhubung ke Backend API.

---

## 1. ARSITEKTUR SISTEM

```
┌─────────────────┐         HTTP Request        ┌─────────────────┐
│                 │  ────────────────────────►  │                 │
│    BROWSER      │                             │  laravelFrontend│
│    (User)       │  ◄────────────────────────  │  (SSR Server)   │
│                 │         HTML Response       │  Port: 8000     │
└─────────────────┘                             └────────┬────────┘
                                                         │
                                                         │ HTTP API
                                                         │ (JSON)
                                                         ▼
                                                ┌─────────────────┐
                                                │  laravelProject │
                                                │  (Backend API)  │
                                                │  mobile.theputras│
                                                │     .my.id      │
                                                └─────────────────┘
```

### Penjelasan:
- **Browser** → Mengirim request ke Frontend (localhost:8000)
- **laravelFrontend** → Menerima request, fetch data dari Backend API, lalu render HTML
- **laravelProject** → Backend murni, hanya return JSON (RESTful API)

**Kenapa pakai SSR?**
- Lebih mudah untuk tampilan web (tidak perlu React/Vue)
- SEO friendly
- Session management lebih simpel

---

## 2. STRUKTUR DIREKTORI

```
laravelFrontend/
├── app/
│   ├── Http/
│   │   ├── Controllers/          # Logic bisnis
│   │   │   ├── AccountController.php
│   │   │   ├── FrontendAuthController.php
│   │   │   ├── HistoryController.php
│   │   │   └── RentalFrontendController.php
│   │   └── Middleware/
│   │       └── CekTokenBackend.php   # Proteksi route
│   └── Models/                   # "Pseudo-Eloquent" - fetch dari API
│       ├── PaketSewa.php
│       ├── Transaksi.php
│       └── Tv.php
├── config/
│   └── app.php                   # Timezone: Asia/Jakarta
├── public/
│   └── css/
│       └── app.css               # Custom styling
├── resources/
│   └── views/
│       ├── layouts/
│       │   └── app.blade.php     # Master layout
│       ├── partials/
│       │   ├── bottom-nav.blade.php
│       │   ├── modal-checkout.blade.php
│       │   └── modal-paket.blade.php
│       ├── account.blade.php
│       ├── dashboard.blade.php
│       ├── history.blade.php
│       ├── login.blade.php
│       ├── register.blade.php
│       └── struk.blade.php
├── routes/
│   └── web.php                   # Definisi semua route
└── .env                          # BACKEND_URL config
```

---

## 3. ALUR KERJA APLIKASI

### 3.1 Alur Login

```
[User buka /login]
        │
        ▼
┌───────────────────────────────────────────────────────────────┐
│ FrontendAuthController@showLoginForm                          │
│ → return view('login')                                        │
└───────────────────────────────────────────────────────────────┘
        │
        ▼
[User isi form, klik LOGIN]
        │
        ▼
┌───────────────────────────────────────────────────────────────┐
│ FrontendAuthController@login                                  │
│ 1. Http::post($backendUrl/api/login, [email, password])       │
│ 2. Jika sukses:                                               │
│    - Simpan 'api_token' ke SESSION                            │
│    - Simpan 'user_name' ke SESSION                            │
│    - Redirect ke dashboard                                    │
│ 3. Jika gagal: back()->with('error', 'Login gagal')           │
└───────────────────────────────────────────────────────────────┘
```

### 3.2 Alur Dashboard (Pilih TV & Paket)

```
[User buka / (dashboard)]
        │
        ▼
┌───────────────────────────────────────────────────────────────┐
│ CekTokenBackend Middleware                                    │
│ → Cek session('api_token')                                    │
│ → Jika kosong: redirect ke /login                             │
└───────────────────────────────────────────────────────────────┘
        │ (Token valid)
        ▼
┌───────────────────────────────────────────────────────────────┐
│ RentalFrontendController@index                                │
│ 1. $tvs = Tv::all()           → Hit API /api/tvs              │
│ 2. $pakets = PaketSewa::all() → Hit API /api/master/pakets    │
│ 3. Sort pakets by durasi_menit                                │
│ 4. return view('dashboard', compact('tvs', 'pakets'))         │
└───────────────────────────────────────────────────────────────┘
        │
        ▼
[Browser render TV cards grid]
        │
[User klik TV yang AVAILABLE]
        │
        ▼
┌───────────────────────────────────────────────────────────────┐
│ JavaScript: openPaketModal(tvId, tvName, consoleId)           │
│ → Filter pakets sesuai consoleId                              │
│ → Tampilkan modal bottom sheet "Pilih Paket"                  │
└───────────────────────────────────────────────────────────────┘
        │
[User pilih paket]
        │
        ▼
┌───────────────────────────────────────────────────────────────┐
│ JavaScript: addToCart(paketId, paketName, harga)              │
│ → AJAX POST ke /cart/add                                      │
│ → RentalFrontendController@addToCart                          │
│   - Cek apakah TV sudah di cart                               │
│   - Tambah ke session('cart')                                 │
│   - Return JSON { success, cart_count }                       │
└───────────────────────────────────────────────────────────────┘
```

### 3.3 Alur Checkout & Pembayaran

```
[User klik FAB Cart (tombol keranjang)]
        │
        ▼
┌───────────────────────────────────────────────────────────────┐
│ Modal Checkout muncul (modal-checkout.blade.php)              │
│ → Tampilkan list cart items dari session('cart')              │
│ → Form: nama_penyewa, metode_pembayaran, uang_bayar           │
└───────────────────────────────────────────────────────────────┘
        │
[User isi form, klik BAYAR]
        │
        ▼
┌───────────────────────────────────────────────────────────────┐
│ POST /checkout → RentalFrontendController@checkout            │
│                                                               │
│ 1. Validate request                                           │
│ 2. Ambil cart dari session                                    │
│ 3. Hitung total                                               │
│ 4. Validasi uang bayar (untuk TUNAI)                          │
│ 5. Siapkan payload untuk API:                                 │
│    {                                                          │
│      nama_penyewa, uang_bayar, metode_pembayaran,             │
│      items: [ {tv_id, id_paket}, ... ]                        │
│    }                                                          │
│ 6. Http::post($backendUrl/api/transaksi, $payload)            │
│ 7. Jika sukses:                                               │
│    - Clear session cart                                       │
│    - Simpan transaksi ke session untuk struk                  │
│    - Redirect ke /struk/{id_transaksi}                        │
│ 8. Jika gagal: back()->with('error', $message)                │
└───────────────────────────────────────────────────────────────┘
```

### 3.4 Alur Lihat Struk & Print

```
[Redirect ke /struk/{id}]
        │
        ▼
┌───────────────────────────────────────────────────────────────┐
│ RentalFrontendController@showStruk($id)                       │
│ 1. Cek session('last_transaksi') (transaksi baru dibuat)      │
│ 2. Jika tidak ada, fetch dari API /api/transaksi/{id}         │
│ 3. return view('struk', compact('transaksi'))                 │
└───────────────────────────────────────────────────────────────┘
        │
        ▼
[Browser render struk.blade.php]
│
├── Tampilkan detail transaksi (no, tanggal, customer)
├── Tampilkan list items
├── Tampilkan total, uang bayar, kembalian
├── Tombol "CETAK STRUK" → window.print()
└── Tombol "KEMBALI" → redirect ke dashboard
```

### 3.5 Alur Stop Rental (Reset TV)

```
[Di dashboard, TV status BOOKED ada tombol STOP]
        │
[User klik STOP]
        │
        ▼
┌───────────────────────────────────────────────────────────────┐
│ JavaScript: confirm('Stop rental TV XX?')                     │
└───────────────────────────────────────────────────────────────┘
        │ (OK)
        ▼
┌───────────────────────────────────────────────────────────────┐
│ POST /stop/{tv_id} → RentalFrontendController@stop            │
│ 1. Transaksi::stop($id)                                       │
│    → Http::put($backendUrl/api/tvs/{id}, {                    │
│         status: 'available',                                  │
│         rental_end_time: null                                 │
│       })                                                      │
│ 2. redirect()->back()->with('success', 'TV berhasil di-reset')│
└───────────────────────────────────────────────────────────────┘
```

---

## 4. PENJELASAN DETAIL SETIAP FILE

### 4.1 ROUTES (`routes/web.php`)

```php
// PUBLIC ROUTES - Tidak perlu login
Route::get('/login', ...);      // Tampilkan form login
Route::post('/login', ...);     // Proses login
Route::get('/logout', ...);     // Logout (clear session)
Route::get('/register', ...);   // Form register
Route::post('/register', ...);  // Proses register

// PROTECTED ROUTES - Harus login (middleware CekTokenBackend)
Route::middleware([CekTokenBackend::class])->group(function () {
    Route::get('/', ...);              // Dashboard
    Route::post('/cart/add', ...);     // AJAX add to cart
    Route::delete('/cart/remove/{i}'); // AJAX remove from cart
    Route::post('/checkout', ...);     // Proses pembayaran
    Route::get('/struk/{id}', ...);    // Tampilkan struk
    Route::get('/history', ...);       // List riwayat
    Route::get('/history/{id}', ...);  // Detail riwayat
    Route::get('/account', ...);       // Halaman akun
    Route::post('/stop/{id}', ...);    // Stop/reset rental
});
```

---

### 4.2 MIDDLEWARE (`CekTokenBackend.php`)

**Fungsi:** Mengecek apakah user sudah login dengan melihat session `api_token`.

```php
public function handle(Request $request, Closure $next)
{
    // Cek apakah ada token di session
    if (!session()->has('api_token')) {
        // Belum login, redirect ke halaman login
        return redirect()->route('login');
    }
    
    // Token ada, lanjutkan request
    return $next($request);
}
```

**Kapan dipanggil:** Setiap kali user akses route yang ada di dalam group middleware.

---

### 4.3 CONTROLLERS

#### A. `FrontendAuthController.php`

**Fungsi:** Handle login, logout, register.

| Method | Penjelasan |
|--------|------------|
| `showLoginForm()` | Return view login.blade.php |
| `login(Request $r)` | POST ke backend /api/login, simpan token ke session |
| `logout()` | Hapus semua session, redirect ke login |
| `showRegisterForm()` | Return view register.blade.php |
| `register(Request $r)` | POST ke backend /api/register |

**Kode penting (login):**
```php
$response = Http::post($this->backendUrl . '/api/login', [
    'email'    => $request->email,
    'password' => $request->password,
]);

if ($response->successful()) {
    $data = $response->json();
    session([
        'api_token' => $data['access_token'],  // PENTING: disimpan untuk API calls
        'user_name' => $data['user']['name'],
        'is_logged_in' => true
    ]);
    return redirect()->route('dashboard');
}
```

---

#### B. `RentalFrontendController.php`

**Fungsi:** Controller utama untuk fitur rental.

| Method | Penjelasan |
|--------|------------|
| `index()` | Ambil TVs dan Pakets, tampilkan dashboard |
| `addToCart(Request)` | Tambah item ke session cart (AJAX) |
| `removeFromCart($i)` | Hapus item dari session cart (AJAX) |
| `checkout(Request)` | Validasi, POST ke API transaksi, redirect struk |
| `showStruk($id)` | Tampilkan bukti transaksi |
| `stop($id)` | Reset status TV ke available |

**Kode penting (checkout):**
```php
// Siapkan data untuk dikirim ke backend
$payload = [
    'nama_penyewa' => $request->nama_penyewa,
    'uang_bayar' => $uangBayar,
    'metode_pembayaran' => $request->metode_pembayaran,
    'items' => $items  // Array of {tv_id, id_paket}
];

// Kirim ke backend API
$response = Transaksi::create($payload);

if ($response->successful()) {
    session()->forget('cart');  // Kosongkan keranjang
    // ... redirect ke struk
}
```

---

#### C. `HistoryController.php`

**Fungsi:** Menampilkan riwayat transaksi.

```php
public function index()
{
    $transactions = Transaksi::all();  // Fetch dari API
    return view('history', compact('transactions'));
}

public function show($id)
{
    $transaksi = Transaksi::find($id);  // Fetch detail dari API
    return view('struk', compact('transaksi'));  // Sama view dengan struk baru
}
```

---

#### D. `AccountController.php`

**Fungsi:** Menampilkan halaman profil user.

```php
public function index()
{
    return view('account');  // Data user diambil dari session
}
```

---

### 4.4 MODELS (Pseudo-Eloquent)

Ini **BUKAN** model database biasa. Ini adalah class yang **fetch data dari API** dan berperilaku seperti Eloquent.

#### A. `Tv.php`

**Fungsi:** Mengambil data TV dari backend API.

```php
public static function all(): Collection
{
    $token = session('api_token');               // Ambil token dari session
    $url = env('BACKEND_URL') . '/api/tvs';      // URL endpoint backend
    
    $response = Http::withToken($token)->get($url);  // GET request dengan Bearer token
    
    $tvs = [];
    if ($response->successful()) {
        foreach ($response->json() as $item) {
            $tvs[] = new self($item);  // Convert JSON ke object Tv
        }
    }
    
    return collect($tvs);  // Return sebagai Laravel Collection
}
```

**Properties:**
- `id` - ID TV
- `nomor_tv` - Nama TV (TV 01, TV 02, ...)
- `status` - available / booked / maintenance
- `rental_end_time` - Waktu selesai sewa (jika booked)
- `jenis_console` - Array {id_console, nama_console}

---

#### B. `PaketSewa.php`

**Fungsi:** Mengambil data paket sewa dari backend API.

```php
public static function all(): Collection
{
    $token = session('api_token');
    $url = env('BACKEND_URL') . '/api/master/pakets';
    
    $response = Http::withToken($token)->get($url);
    // ... return collection of PaketSewa objects
}
```

**Properties:**
- `id_paket` - ID paket
- `id_console` - ID console (untuk filter)
- `nama_paket` - Nama paket (30 Menit, 1 Jam, ...)
- `harga` - Harga paket
- `durasi_menit` - Durasi dalam menit (untuk sorting)

---

#### C. `Transaksi.php`

**Fungsi:** CRUD transaksi via API.

| Method | HTTP | Endpoint | Penjelasan |
|--------|------|----------|------------|
| `all()` | GET | /api/transaksi | List semua transaksi |
| `find($id)` | GET | /api/transaksi/{id} | Detail 1 transaksi |
| `create($data)` | POST | /api/transaksi | Buat transaksi baru |
| `stop($tvId)` | PUT | /api/tvs/{id} | Reset status TV |

**Properties:**
- `id_transaksi` / `id`
- `nama_penyewa`
- `total_tagihan` / `total`
- `uang_bayar`, `uang_kembalian`
- `metode_pembayaran` (TUNAI / QRIS)
- `details` / `items` - Array item transaksi
- `tanggal_transaksi` / `created_at`

---

### 4.5 VIEWS (Blade Templates)

#### A. `layouts/app.blade.php`

**Fungsi:** Master layout yang dipakai semua halaman.

```html
<!DOCTYPE html>
<html>
<head>
    <!-- Bootstrap 5 CSS -->
    <!-- Bootstrap Icons -->
    <!-- Custom CSS (app.css) -->
</head>
<body>
    <main class="main-content">
        @yield('content')  <!-- Konten halaman dimasukkan disini -->
    </main>
    
    @include('partials.bottom-nav')  <!-- Bottom navigation -->
    
    <!-- Bootstrap JS -->
    @stack('scripts')  <!-- Scripts dari child view -->
</body>
</html>
```

---

#### B. `dashboard.blade.php`

**Fungsi:** Halaman utama menampilkan grid TV.

**Struktur:**
1. Header (Selamat Datang, RENTAL PS SANTUY)
2. Search box untuk filter TV
3. Alert messages (success/error)
4. Grid TV cards:
   - Jika AVAILABLE → bisa diklik, buka modal pilih paket
   - Jika BOOKED → tampil waktu selesai + tombol STOP
   - Jika DI KERANJANG → tampil badge biru, tidak bisa diklik
   - Jika MAINTENANCE → greyed out
5. FAB (Floating Action Button) keranjang
6. Include modal-paket dan modal-checkout

**Kode penting (TV card logic):**
```blade
@php
    $isInCart = in_array($tv->id, $cartTvIds);
    
    if ($isInCart) {
        $statusClass = 'status-in-cart';
        $statusText = 'DI KERANJANG';
        $isClickable = false;
    } else {
        $statusClass = match($tv->status) {...};
        $isClickable = $tv->status === 'available';
    }
@endphp

<div class="tv-card" 
     @if($isClickable) onclick="openPaketModal(...)" @endif>
    ...
</div>
```

---

#### C. `partials/modal-paket.blade.php`

**Fungsi:** Modal bottom sheet untuk pilih paket sewa.

**JavaScript Logic:**
```javascript
// Data paket di-pass dari controller (sudah sorted)
const allPakets = @json($pakets);

function openPaketModal(tvId, tvName, consoleId) {
    // Simpan TV yang dipilih
    document.getElementById('selectedTvId').value = tvId;
    
    // Filter paket sesuai console
    const filteredPakets = allPakets.filter(p => p.id_console == consoleId);
    
    // Render grid paket
    grid.innerHTML = filteredPakets.map(paket => `
        <div class="paket-card" onclick="addToCart(...)">
            ${paket.nama_paket} - Rp ${paket.harga}
        </div>
    `).join('');
    
    // Show modal
    new bootstrap.Modal(...).show();
}

function addToCart(paketId, paketName, harga) {
    // AJAX POST ke /cart/add
    fetch('/cart/add', {
        method: 'POST',
        body: JSON.stringify({tv_id, tv_name, paket_id, paket_name, harga})
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            // Update badge, close modal, reload page
        }
    });
}
```

---

#### D. `partials/modal-checkout.blade.php`

**Fungsi:** Modal untuk checkout dan pembayaran.

**Struktur Form:**
1. List cart items (dari session)
2. Input nama penyewa
3. Radio button metode pembayaran (TUNAI / QRIS)
4. Input uang diterima (hanya untuk TUNAI)
5. Total tagihan
6. Tombol BAYAR

**Kode penting (payment method toggle):**
```javascript
document.querySelectorAll('input[name="metode_pembayaran"]').forEach(radio => {
    radio.addEventListener('change', function() {
        if (this.value === 'QRIS') {
            cashContainer.style.display = 'none';  // Sembunyikan input uang
        } else {
            cashContainer.style.display = 'block';  // Tampilkan input uang
        }
    });
});
```

---

#### E. `struk.blade.php`

**Fungsi:** Menampilkan bukti transaksi (receipt).

**Features:**
- Layout monospace seperti struk kasir
- Print-friendly (CSS @media print)
- Tombol CETAK → `window.print()`
- Tombol KEMBALI → redirect dashboard

---

#### F. `history.blade.php`

**Fungsi:** Menampilkan daftar riwayat transaksi.

**Struktur:**
- Search box
- List transaksi (clickable → ke detail struk)

---

#### G. `account.blade.php`

**Fungsi:** Halaman profil user.

**Isi:**
- Avatar & nama user (dari session)
- Menu settings (disabled untuk sekarang)
- Tombol Logout

---

### 4.6 CSS (`public/css/app.css`)

**Fungsi:** Custom styling untuk seluruh aplikasi.

**Highlights:**
- CSS Variables untuk warna brand
- Bottom navigation dengan blur effect
- TV card grid dengan status badges
- FAB (Floating Action Button) untuk cart
- Bottom sheet modal animation
- Struk styling (monospace, print-ready)
- Cart indicator badge (biru)

---

---
---

## 5. DYNAMIC QRIS IMPLEMENTATION (FITUR BARU)

Kode ini memungkinkan aplikasi untuk generate QRIS yang **otomatis berisi nominal pembayaran**, sehingga user tidak perlu input manual.

### 5.1 Logic di `QrisHelper.php`

**Lokasi:** `app/Helpers/QrisHelper.php`
**Fungsi:** Memanipulasi string QRIS EMV (Europay Mastercard Visa) standard.

#### A. Struktur QRIS
QRIS itu sebenarnya hanya string panjang dengan format **TLV (Tag-Length-Value)**.
Contoh: `000201010211`
- Tag `00`: Format Indicator
- Length `02`: Panjang value
- Value `01`: Versi
- Tag `01`: Initial Method
- Length `02`: Panjang value
- Value `11`: **Static** (kalau 12 = **Dynamic**)

#### B. Proses Konversi (Method `addAmount`)

```php
public static function addAmount(int $amount): string
{
    // 1. Ambil QRIS Statis Base (dari file gambar asli owner)
    $qris = self::$staticQrisBase;
    
    // 2. Parse string jadi Array (Key-Value)
    $data = self::parseQris($qris);
    
    // 3. Ubah Tipe QR jadi DYNAMIC
    // Field '01' nilainya diganti dari '11' jadi '12'
    $data['01'] = '12';
    
    // 4. Tambahkan Nominal (Field '54')
    // Field 54 = Transaction Amount
    $data['54'] = (string)$amount;
    
    // 5. Susun Ulang String (Rebuild)
    // Menggabungkan semua keys + hitung ulang CRC16 Checksum
    return self::buildQris($data);
}
```

#### C. Generate SVG (Tanpa GD Extension)

Karena server kadang tidak punya extension `gd` atau `imagick`, kita pakai library `chillerlan/php-qrcode` untuk output **SVG (Scalable Vector Graphics)**. SVG itu teks XML, jadi sangat ringan dan pasti support di semua browser.

```php
public static function generateQrCode(string $qrisString): string
{
    // Output type: MARKUP_SVG
    $options = new QROptions([
        'outputType' => QRCode::OUTPUT_MARKUP_SVG,
        // ...
    ]);
    
    // Return string SVG
    return (new QRCode($options))->render($qrisString);
}
```

---

### 5.2 Implementasi di Frontend (SSR Approach)

Awalnya kita pakai AJAX, tapi itu bikin _flicker_ dan kadang error CORS atau loading lambat. Sekarang kita pakai **Server Side Rendering (SSR)** agar QR langsung muncul saat halaman dimuat.

#### Controller (`RentalFrontendController@index`)

```php
// 1. Ambil total dari session cart
$total = collect(session('cart'))->sum('harga');

// 2. Generate QR Code saat itu juga
$qrisSvg = null;
if ($total > 0) {
    $qrisData = \App\Helpers\QrisHelper::generateDynamicQris($total);
    // Kita cuma butuh SVG string-nya
    $qrisSvg = $qrisData['qr_svg'];
}

// 3. Lempar ke View
return view('dashboard', compact(..., 'qrisSvg'));
```

#### View (`modal-checkout.blade.php`)

Disini kita render SVG sebagai gambar menggunakan Data URI.

```blade
@if(isset($qrisSvg))
    <!-- Render SVG Base64 -->
    <img src="{{ $qrisSvg }}" alt="QRIS">
    <!-- $qrisSvg isinya sudah: "data:image/svg+xml;base64,..." -->
@else
    <!-- Fallback ke QRIS Statis (tanpa nominal) -->
    <img src="{{ asset('images/qris.png') }}">
@endif
```

---

## 6. SESSION MANAGEMENT

| Key | Isi | Kapan Diset |
|-----|-----|-------------|
| `api_token` | Bearer token dari backend | Setelah login sukses |
| `user_name` | Nama user | Setelah login sukses |
| `is_logged_in` | Boolean | Setelah login sukses |
| `cart` | Array items keranjang | Saat add to cart |
| `last_transaksi` | Data transaksi terakhir | Setelah checkout sukses |

---

## 6. ENVIRONMENT VARIABLES (`.env`)

```env
BACKEND_URL=http://mobile.theputras.my.id   # URL backend API
APP_TIMEZONE=Asia/Jakarta                    # Timezone (di config/app.php)
SESSION_DRIVER=file                          # Session disimpan di file
```

---

## 7. FLOW DIAGRAM RINGKAS

```
[LOGIN] ──► [DASHBOARD] ──► [PILIH TV] ──► [PILIH PAKET]
                                              │
                                              ▼
                                         [ADD TO CART]
                                              │
                                              ▼
                                        [CART (FAB)]
                                              │
                                              ▼
                                         [CHECKOUT]
                                              │
            ┌─────────────────────────────────┴─────────────────────────────────┐
            │                                                                   │
            ▼                                                                   ▼
       [TUNAI]                                                              [QRIS]
       Input uang                                                        Auto-pay
            │                                                                   │
            └─────────────────────────────────┬─────────────────────────────────┘
                                              │
                                              ▼
                                      [HIT API TRANSAKSI]
                                              │
                                              ▼
                                         [STRUK PAGE]
                                              │
                                              ▼
                                    [PRINT / KEMBALI]
```

---

## 8. RINGKASAN

**laravelFrontend** adalah aplikasi **SSR (Server-Side Rendering)** yang:

1. **Tidak punya database sendiri** - Semua data diambil dari Backend API
2. **Menggunakan Session** - Untuk menyimpan token & cart
3. **Bootstrap 5** - Untuk styling responsive
4. **Pseudo-Eloquent Models** - Class yang fetch data via HTTP, bukan database
5. **Middleware Protection** - Route dilindungi dengan pengecekan token
6. **Real-time API Integration** - Setiap action memerlukan komunikasi dengan backend

**Tech Stack:**
- Laravel 11 (SSR)
- Bootstrap 5 + Bootstrap Icons
- JavaScript (Vanilla) untuk interaktivitas
- HTTP Client untuk API calls
