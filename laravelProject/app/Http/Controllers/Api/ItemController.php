<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Item;
use Illuminate\Http\Request;
use Illuminate\Http\Response;

class ItemController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        $items = Item::all();
        return response()->json($items, Response::HTTP_OK);
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        $validatedData = $request->validate([
            'kode_item' => 'nullable|string|max:15|unique:items,kode_item',
            'nama_item' => 'nullable|string|max:100',
            'satuan' => 'nullable|string|max:15',
            'hargabeli' => 'nullable|numeric|min:0',
            'hargajual' => 'nullable|numeric|min:0',
        ]);
        $item = Item::create($validatedData);
        return response()->json($item, Response::HTTP_CREATED);
    }

    /**
     * Display the specified resource.
     */
    public function show(Item $id)
    {
            $item = Item::find($id);
        return response()-> json($item, Response::HTTP_OK);
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, $id)
    {
        $item = Item::find($id);
        $validatedData = $request->validate([
            'kode_item' => 'nullable|string|max:15|unique:items,kode_item,' . $item->id,
            'nama_item' => 'nullable|string|max:100',
            'satuan' => 'nullable|string|max:15',
            'hargabeli' => 'nullable|numeric|min:0',
            'hargajual' => 'nullable|numeric|min:0',
        ]);
        $item->update($validatedData);
        return response()->json($item, Response::HTTP_OK);
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(Item $item)
    {
        $item->delete();
        return response()->json(null, Response::HTTP_NO_CONTENT);
    }
}
