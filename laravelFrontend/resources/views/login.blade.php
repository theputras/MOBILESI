<!DOCTYPE html>
<html>
<head>
    <title>Login Kasir</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="flex items-center justify-center min-h-screen bg-gradient-to-br from-blue-600 to-purple-700">
    <div class="bg-white/95 backdrop-blur p-8 rounded-2xl shadow-2xl w-96">
        <div class="text-center mb-6">
            <h2 class="text-3xl font-bold text-gray-800">Login Kasir</h2>
            <p class="text-gray-500 mt-2">Masuk ke sistem rental PS</p>
        </div>
        
        @if(session('error'))
            <div class="bg-red-100 border border-red-400 text-red-700 p-3 rounded-lg mb-4">
                {{ session('error') }}
            </div>
        @endif

        @if(session('success'))
            <div class="bg-green-100 border border-green-400 text-green-700 p-3 rounded-lg mb-4">
                {{ session('success') }}
            </div>
        @endif

        <form action="{{ route('login') }}" method="POST">
            @csrf
            <div class="mb-4">
                <label class="block text-sm font-semibold text-gray-700 mb-2">Email</label>
                <input type="email" name="email" 
                       class="w-full border border-gray-300 p-3 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition" 
                       placeholder="contoh@email.com" required>
            </div>
            <div class="mb-6">
                <label class="block text-sm font-semibold text-gray-700 mb-2">Password</label>
                <input type="password" name="password" 
                       class="w-full border border-gray-300 p-3 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition" 
                       placeholder="Masukkan password" required>
            </div>
            <button type="submit" 
                    class="w-full bg-gradient-to-r from-blue-600 to-purple-600 text-white p-3 rounded-lg font-bold hover:from-blue-700 hover:to-purple-700 transition transform hover:scale-[1.02] shadow-lg">
                MASUK
            </button>
        </form>

        <div class="mt-6 text-center">
            <p class="text-gray-600">Belum punya akun? 
                <a href="{{ route('register') }}" class="text-blue-600 font-semibold hover:underline">Daftar di sini</a>
            </p>
        </div>
    </div>
</body>
</html>