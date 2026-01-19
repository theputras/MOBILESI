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

### 4.1 ROUTES ([routes/web.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelProject/routes/web.php))

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

### 4.2 MIDDLEWARE ([CekTokenBackend.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Http/Middleware/CekTokenBackend.php))

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

#### A. [FrontendAuthController.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Http/Controllers/FrontendAuthController.php)

**Fungsi:** Handle login, logout, register.

| Method | Penjelasan |
|--------|------------|
| [showLoginForm()](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Http/Controllers/FrontendAuthController.php#17-22) | Return view login.blade.php |
| [login(Request $r)](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Http/Controllers/FrontendAuthController.php#23-52) | POST ke backend /api/login, simpan token ke session |
| [logout()](file:///media/theputras/Data%20D/Github/MOBILESI/laravelProject/app/Http/Controllers/Api/LoginController.php#62-71) | Hapus semua session, redirect ke login |
| [showRegisterForm()](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Http/Controllers/FrontendAuthController.php#60-65) | Return view register.blade.php |
| [register(Request $r)](file:///media/theputras/Data%20D/Github/MOBILESI/laravelProject/app/Http/Controllers/Api/LoginController.php#12-34) | POST ke backend /api/register |

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

#### B. [RentalFrontendController.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Http/Controllers/RentalFrontendController.php)

**Fungsi:** Controller utama untuk fitur rental.

| Method | Penjelasan |
|--------|------------|
| [index()](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Http/Controllers/HistoryController.php#10-19) | Ambil TVs dan Pakets, tampilkan dashboard |
| [addToCart(Request)](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Http/Controllers/RentalFrontendController.php#23-57) | Tambah item ke session cart (AJAX) |
| [removeFromCart($i)](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Http/Controllers/RentalFrontendController.php#58-77) | Hapus item dari session cart (AJAX) |
| [checkout(Request)](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Http/Controllers/RentalFrontendController.php#78-150) | Validasi, POST ke API transaksi, redirect struk |
| [showStruk($id)](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Http/Controllers/RentalFrontendController.php#151-173) | Tampilkan bukti transaksi |
| [stop($id)](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Models/Transaksi.php#121-134) | Reset status TV ke available |

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

#### C. [HistoryController.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Http/Controllers/HistoryController.php)

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

#### D. [AccountController.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Http/Controllers/AccountController.php)

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

#### A. [Tv.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Models/Tv.php)

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

#### B. [PaketSewa.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Models/PaketSewa.php)

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

#### C. [Transaksi.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Models/Transaksi.php)

**Fungsi:** CRUD transaksi via API.

| Method | HTTP | Endpoint | Penjelasan |
|--------|------|----------|------------|
| [all()](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Models/Tv.php#27-47) | GET | /api/transaksi | List semua transaksi |
| [find($id)](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Models/Transaksi.php#84-107) | GET | /api/transaksi/{id} | Detail 1 transaksi |
| [create($data)](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Models/Transaksi.php#108-120) | POST | /api/transaksi | Buat transaksi baru |
| [stop($tvId)](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/app/Models/Transaksi.php#121-134) | PUT | /api/tvs/{id} | Reset status TV |

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

#### A. [layouts/app.blade.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/resources/views/layouts/app.blade.php)

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

#### B. [dashboard.blade.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/resources/views/dashboard.blade.php)

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

#### C. [partials/modal-paket.blade.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/resources/views/partials/modal-paket.blade.php)

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

#### D. [partials/modal-checkout.blade.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/resources/views/partials/modal-checkout.blade.php)

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

#### E. [struk.blade.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/resources/views/struk.blade.php)

**Fungsi:** Menampilkan bukti transaksi (receipt).

**Features:**
- Layout monospace seperti struk kasir
- Print-friendly (CSS @media print)
- Tombol CETAK → `window.print()`
- Tombol KEMBALI → redirect dashboard

---

#### F. [history.blade.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/resources/views/history.blade.php)

**Fungsi:** Menampilkan daftar riwayat transaksi.

**Struktur:**
- Search box
- List transaksi (clickable → ke detail struk)

---

#### G. [account.blade.php](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/resources/views/account.blade.php)

**Fungsi:** Halaman profil user.

**Isi:**
- Avatar & nama user (dari session)
- Menu settings (disabled untuk sekarang)
- Tombol Logout

---

### 4.6 CSS ([public/css/app.css](file:///media/theputras/Data%20D/Github/MOBILESI/laravelFrontend/public/css/app.css))

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

## 5. SESSION MANAGEMENT

| Key | Isi | Kapan Diset |
|-----|-----|-------------|
| `api_token` | Bearer token dari backend | Setelah login sukses |
| `user_name` | Nama user | Setelah login sukses |
| `is_logged_in` | Boolean | Setelah login sukses |
| `cart` | Array items keranjang | Saat add to cart |
| `last_transaksi` | Data transaksi terakhir | Setelah checkout sukses |

---

## 6. ENVIRONMENT VARIABLES ([.env](file:///media/theputras/Data%20D/Github/MOBILESI/laravelProject/.env))

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
