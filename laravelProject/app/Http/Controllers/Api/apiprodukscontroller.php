<?php
namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;
use App\Models\produks;

class apiprodukscontroller extends Controller
{
    // ===========================================
    // STYLE 1: ORM (Eloquent)
    // ===========================================

    public function index_orm()
    {
        $data = produks::orderBy('nama', 'asc')->get();
        return response()->json(['style' => 'ORM', 'message' => 'Data Produk', 'data' => $data], 200);
    }

    public function store_orm(Request $request)
    {
        $validasi = Validator::make($request->all(), [
            'nama' => 'required',
            'satuan' => 'nullable',
            'harga' => 'required|numeric'
        ]);

        if ($validasi->fails()) {
            return response()->json(['style' => 'ORM', 'message' => 'Gagal validasi', 'errors' => $validasi->errors()], 400);
        }

        $data = produks::create($request->all());
        return response()->json(['style' => 'ORM', 'message' => 'Berhasil input data', 'data' => $data], 201);
    }

    public function show_orm(string $id)
    {
        $data = produks::find($id);
        if ($data) {
            return response()->json(['style' => 'ORM', 'message' => 'Data ditemukan', 'data' => $data], 200);
        }
        return response()->json(['style' => 'ORM', 'message' => 'Data tidak ditemukan'], 404);
    }

    public function update_orm(Request $request, string $id)
    {
        $validasi = Validator::make($request->all(), [
            'nama' => 'required',
            'satuan' => 'nullable',
            'harga' => 'required|numeric'
        ]);

        if ($validasi->fails()) {
            return response()->json(['style' => 'ORM', 'message' => 'Gagal validasi', 'errors' => $validasi->errors()], 400);
        }

        $data = produks::find($id);
        if ($data) {
            $data->update($request->all());
            return response()->json(['style' => 'ORM', 'message' => 'Data berhasil diupdate', 'data' => $data], 200);
        }
        return response()->json(['style' => 'ORM', 'message' => 'Data tidak ditemukan'], 404);
    }

    public function destroy_orm(string $id)
    {
        $data = produks::find($id);
        if ($data) {
            $data->delete();
            return response()->json(['style' => 'ORM', 'message' => 'Data berhasil dihapus'], 200);
        }
        return response()->json(['style' => 'ORM', 'message' => 'Data tidak ditemukan'], 404);
    }

    // ===========================================
    // STYLE 2: QUERY BUILDER (Fluent)
    // ===========================================

    public function index_qb()
    {
        $data = DB::table('produks')->orderBy('nama', 'asc')->get();
        return response()->json(['style' => 'Query Builder', 'message' => 'Data Produk', 'data' => $data], 200);
    }

    public function store_qb(Request $request)
    {
        $validasi = Validator::make($request->all(), [
            'nama' => 'required',
            'satuan' => 'nullable',
            'harga' => 'required|numeric'
        ]);

        if ($validasi->fails()) {
            return response()->json(['style' => 'Query Builder', 'message' => 'Gagal validasi', 'errors' => $validasi->errors()], 400);
        }

        $data = [
            'nama' => $request->nama,
            'satuan' => $request->satuan,
            'harga' => $request->harga,
            'created_at' => now(),
            'updated_at' => now()
        ];

        $id = DB::table('produks')->insertGetId($data);
        $savedData = DB::table('produks')->where('id', $id)->first();

        return response()->json(['style' => 'Query Builder', 'message' => 'Berhasil input data', 'data' => $savedData], 201);
    }

    public function show_qb(string $id)
    {
        $data = DB::table('produks')->where('id', $id)->first();
        if ($data) {
            return response()->json(['style' => 'Query Builder', 'message' => 'Data ditemukan', 'data' => $data], 200);
        }
        return response()->json(['style' => 'Query Builder', 'message' => 'Data tidak ditemukan'], 404);
    }

    public function update_qb(Request $request, string $id)
    {
        $validasi = Validator::make($request->all(), [
            'nama' => 'required',
            'satuan' => 'nullable',
            'harga' => 'required|numeric'
        ]);

        if ($validasi->fails()) {
            return response()->json(['style' => 'Query Builder', 'message' => 'Gagal validasi', 'errors' => $validasi->errors()], 400);
        }

        $exists = DB::table('produks')->where('id', $id)->exists();
        if ($exists) {
            DB::table('produks')->where('id', $id)->update([
                'nama' => $request->nama,
                'satuan' => $request->satuan,
                'harga' => $request->harga,
                'updated_at' => now()
            ]);
            $data = DB::table('produks')->where('id', $id)->first();
            return response()->json(['style' => 'Query Builder', 'message' => 'Data berhasil diupdate', 'data' => $data], 200);
        }
        return response()->json(['style' => 'Query Builder', 'message' => 'Data tidak ditemukan'], 404);
    }

    public function destroy_qb(string $id)
    {
        $deleted = DB::table('produks')->where('id', $id)->delete();
        if ($deleted) {
            return response()->json(['style' => 'Query Builder', 'message' => 'Data berhasil dihapus'], 200);
        }
        return response()->json(['style' => 'Query Builder', 'message' => 'Data tidak ditemukan'], 404);
    }

    // Legacy/Helper methods
    public function test()
    {
        echo "hello api";
    }
    public function bacaqb()
    {
        return $this->index_qb();
    }
    public function bacaorm()
    {
        return $this->index_orm();
    }
    // Using ORM as default 'index', 'store', etc for compatibility
    public function index()
    {
        return $this->index_orm();
    }
    public function store(Request $request)
    {
        return $this->store_orm($request);
    }
    public function show(string $id)
    {
        return $this->show_orm($id);
    }
    public function update(Request $request, string $id)
    {
        return $this->update_orm($request, $id);
    }
    public function destroy(string $id)
    {
        return $this->destroy_orm($id);
    }
}
