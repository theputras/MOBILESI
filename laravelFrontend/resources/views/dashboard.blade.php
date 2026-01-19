<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kasir Frontend SSR</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/flowbite/2.2.1/flowbite.min.js"></script>
</head>
<body class="bg-gray-100 p-6">

    <div class="container mx-auto">
        <h1 class="text-3xl font-bold mb-6 text-center">Rental PS - Frontend SSR</h1>

        @if(session('success'))
            <div class="p-4 mb-4 text-green-800 bg-green-100 rounded">{{ session('success') }}</div>
        @endif
        @if(session('error'))
            <div class="p-4 mb-4 text-red-800 bg-red-100 rounded">{{ session('error') }}</div>
        @endif

       <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
    @foreach($tvs as $tv)
        @php
            // SEKARANG PAKAI PANAH (->) KARENA SUDAH JADI OBJECT MODEL
            $bgClass = $tv->status == 'available' ? 'bg-white border-green-500' : 
                      ($tv->status == 'booked' ? 'bg-red-50 border-red-500' : 'bg-gray-200 border-gray-400');
            
            // Akses nested object array
            $namaConsole = $tv->jenis_console['nama_console'] ?? '-';
        @endphp

        <div class="border-l-4 rounded shadow p-4 {{ $bgClass }} relative">
            <div class="flex justify-between items-center mb-2">
                <h2 class="text-2xl font-bold">{{ $tv->nomor_tv }}</h2>
                <span class="text-xs font-semibold px-2 py-1 rounded {{ $tv->status == 'available' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800' }}">
                    {{ strtoupper($tv->status) }}
                </span>
            </div>
            
            <p class="text-gray-600 mb-4">{{ $namaConsole }}</p>

            @if($tv->status == 'available')
                <button data-modal-target="modal-{{ $tv->id }}" data-modal-toggle="modal-{{ $tv->id }}" 
                        class="w-full bg-blue-600 text-white py-2 rounded hover:bg-blue-700">
                    Sewa Sekarang
                </button>
            @elseif($tv->status == 'booked')
                <div class="text-center mb-4">
                    <p class="text-sm text-gray-500">Selesai jam:</p>
                    <p class="text-xl font-mono font-bold">
                        {{ \Carbon\Carbon::parse($tv->rental_end_time)->format('H:i') }}
                    </p>
                </div>
                <form action="{{ route('sewa.stop', $tv->id) }}" method="POST">
                    @csrf
                    <button class="w-full bg-gray-500 text-white py-2 rounded hover:bg-gray-600">Stop Paksa</button>
                </form>
            @else
                <button disabled class="w-full bg-gray-300 cursor-not-allowed py-2 rounded">Maintenance</button>
            @endif
        </div>

        @if($tv->status == 'available')
        <div id="modal-{{ $tv->id }}" tabindex="-1" aria-hidden="true" class="hidden overflow-y-auto overflow-x-hidden fixed top-0 right-0 left-0 z-50 justify-center items-center w-full md:inset-0 h-[calc(100%-1rem)] max-h-full">
            <div class="relative p-4 w-full max-w-md max-h-full">
                <div class="relative bg-white rounded-lg shadow">
                    <div class="flex items-center justify-between p-4 border-b rounded-t">
                        <h3 class="text-xl font-semibold text-gray-900">Sewa {{ $tv->nomor_tv }}</h3>
                        <button type="button" class="text-gray-400 bg-transparent hover:bg-gray-200 rounded-lg text-sm w-8 h-8 ms-auto inline-flex justify-center items-center" data-modal-toggle="modal-{{ $tv->id }}">X</button>
                    </div>
                    
                    <form action="{{ route('sewa.store') }}" method="POST" class="p-4">
                        @csrf
                        <input type="hidden" name="tv_id" value="{{ $tv->id }}">

                        <div class="mb-4">
                            <label class="block mb-2 text-sm font-medium text-gray-900">Nama Penyewa</label>
                            <input type="text" name="nama_penyewa" required class="bg-gray-50 border border-gray-300 text-sm rounded-lg block w-full p-2.5">
                        </div>

                        <div class="mb-4">
                            <label class="block mb-2 text-sm font-medium text-gray-900">Pilih Paket</label>
                            <select name="id_paket" required class="bg-gray-50 border border-gray-300 text-sm rounded-lg block w-full p-2.5">
                                @foreach($pakets as $paket)
                                    {{-- Filter paket sesuai console TV ini (PAKAI PANAH ->) --}}
                                    @if($paket->id_console == ($tv->jenis_console['id_console'] ?? 0))
                                        <option value="{{ $paket->id_paket }}">
                                            {{ $paket->nama_paket }} - Rp {{ number_format($paket->harga) }}
                                        </option>
                                    @endif
                                @endforeach
                            </select>
                        </div>

                        <div class="mb-4">
                            <label class="block mb-2 text-sm font-medium text-gray-900">Uang Bayar</label>
                            <input type="number" name="uang_bayar" required class="bg-gray-50 border border-gray-300 text-sm rounded-lg block w-full p-2.5">
                        </div>

                        <button type="submit" class="w-full text-white bg-blue-700 hover:bg-blue-800 font-medium rounded-lg text-sm px-5 py-2.5">
                            Proses Sewa
                        </button>
                    </form>
                </div>
            </div>
        </div>
        @endif
    @endforeach
</div>
    </div>

</body>
</html> 