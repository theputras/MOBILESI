<?php
namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Tv;
use App\Models\JenisConsole;
use Illuminate\Http\Request;

class TvController extends Controller
{
    // ==========================================
    // BAGIAN 1: API UNTUK HALAMAN TRANSAKSI (KASIR)
    // ==========================================

    /**
     * Mengambil daftar TV yang BISA DIPILIH saja.
     * Logic: Status 'available' & dikelompokkan per Console (PS4/PS5)
     */
    public function getAvailableTvs(Request $request)
    {
        // Ambil data TV, sertakan info jenis consolenya
        // Filter hanya yang statusnya 'available'
        $query = Tv::with('jenisConsole')->where('status', 'available');

        // (Opsional) Jika kasir memilih filter "Hanya PS5" dari UI
        if ($request->has('id_console')) {
            $query->where('id_console', $request->id_console);
        }

        $tvs = $query->get();

        // Return JSON biar enak diolah Frontend (Vue/React/Blade+JS)
        return response()->json([
            'message' => 'List TV Available',
            'data' => $tvs
        ]);
    }

    // ==========================================
    // BAGIAN 2: CRUD UNTUK ADMIN (MANAJEMEN TV)
    // ==========================================

    public function index()
    {
        // Admin butuh lihat SEMUA status (booked/maintenance/available)
        $tvs = Tv::with('jenisConsole')->get();
        return view('admin.tvs.index', compact('tvs'));
    }

    public function store(Request $request)
    {
        // Validasi input
        $request->validate([
            'nomor_tv' => 'required|unique:tvs,nomor_tv',
            'id_console' => 'required|exists:jenis_console,id_console',
        ]);

        Tv::create([
            'nomor_tv' => $request->nomor_tv,
            'id_console' => $request->id_console,
            'status' => 'available', // Default available
        ]);

        return redirect()->back()->with('success', 'TV berhasil ditambahkan');
    }

    public function update(Request $request, $id)
    {
        $tv = Tv::find($id);
        
        // Bisa update status manual jika TV rusak (Maintenance)
        $tv->update($request->all());

        return redirect()->back()->with('success', 'Data TV diupdate');
    }
    
    public function destroy($id)
    {
        Tv::destroy($id);
        return redirect()->back()->with('success', 'TV dihapus');
    }
}