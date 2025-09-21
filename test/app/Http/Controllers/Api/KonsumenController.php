<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Konsumen;
use Illuminate\Http\Request;
use Illuminate\Http\Response;
use Illuminate\Validation\Rule;

class KonsumenController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $konsumens = Konsumen::all();
        return response()->json($konsumens, Response::HTTP_OK);
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        $validatedData = $request->validate([
            'kodekonsumen' => 'required|string|max:50|unique:konsumens,kodekonsumen',
            'namakonsumen' => 'required|string|max:150',
            'telp'        => 'nullable|string|max:30',
            'alamat'      => 'nullable|string',
            'perusahaan'  => 'nullable|string|max:150',
            'keterangan'  => 'nullable|string',
        ]);

        $konsumen = Konsumen::create($validatedData);
        return response()->json($konsumen, Response::HTTP_CREATED);
    }

    /**
     * Display the specified resource.
     */
    public function show(Konsumen $konsumen)
    {
        return response()->json($konsumen, Response::HTTP_OK);
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, Konsumen $konsumen)
    {
        $validatedData = $request->validate([
            'kodekonsumen' => [
                'nullable',
                'string',
                'max:50',
                Rule::unique('konsumens', 'kodekonsumen')->ignore($konsumen->id),
            ],
            'namakonsumen' => 'nullable|string|max:150',
            'telp'        => 'nullable|string|max:30',
            'alamat'      => 'nullable|string',
            'perusahaan'  => 'nullable|string|max:150',
            'keterangan'  => 'nullable|string',
        ]);

        $konsumen->update($validatedData);
        return response()->json($konsumen, Response::HTTP_OK);
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Konsumen $id)
    {
        $id->delete();
        return response()->json(null, Response::HTTP_NO_CONTENT);
    }
}
