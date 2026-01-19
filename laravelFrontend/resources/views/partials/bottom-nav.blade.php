{{-- Bottom Navigation --}}
<nav class="bottom-nav">
    <ul class="nav">
        <li class="nav-item">
            <a class="nav-link {{ request()->routeIs('dashboard') ? 'active' : '' }}" href="{{ route('dashboard') }}">
                <i class="bi bi-house-door-fill"></i>
                <span>Home</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link {{ request()->routeIs('history*') ? 'active' : '' }}" href="{{ route('history') }}">
                <i class="bi bi-clock-history"></i>
                <span>Riwayat</span>
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link {{ request()->routeIs('account') ? 'active' : '' }}" href="{{ route('account') }}">
                <i class="bi bi-person-fill"></i>
                <span>Akun</span>
            </a>
        </li>
    </ul>
</nav>
