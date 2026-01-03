<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Transaksi;
use App\Models\DetailTransaksi; // <--- Pake Model Baru
use App\Models\PaketSewa;
use App\Models\Tv;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;

class TransaksiController extends Controller
{
  public function index()
    {
        // Ambil data mentah dengan relasi
        $data = Transaksi::with(['details.tv.jenisConsole', 'details.paket']) 
                    ->orderBy('tanggal_transaksi', 'desc')
                    ->get();
        
        // KITA FORMAT ULANG (MAPPING) BIAR BERSIH
        $formattedData = $data->map(function ($tr) {
            return [
                'id_transaksi'      => $tr->id_transaksi,
                'tanggal_transaksi' => $tr->tanggal_transaksi,
                'nama_penyewa'      => $tr->nama_penyewa,
                'total_tagihan'     => $tr->total_tagihan, // Pastikan key ini sama dengan Model Android
                'uang_bayar'        => $tr->uang_bayar,
                'uang_kembalian'    => $tr->uang_kembalian,
                'metode_pembayaran' => $tr->metode_pembayaran,
                'status_pembayaran' => $tr->status_pembayaran,
                // Loop details agar bersih juga
                'details'           => $tr->details->map(function ($dt) {
                    return [
                        'id'                 => $dt->id,
                        'nama_item_snapshot' => $dt->nama_item_snapshot, // PENTING: Ini yang ditampilkan di list utama
                        'harga_satuan'       => $dt->harga_satuan,
                        'qty'                => $dt->qty,
                        'subtotal'           => $dt->subtotal,
                        
                        // Masih kirim object TV tapi ambil yang penting aja
                        'tv' => $dt->tv ? [
                            'nomor_tv' => $dt->tv->nomor_tv,
                            'status'   => $dt->tv->status,
                            'jenis_console' => $dt->tv->jenisConsole ? [
                                'nama_console' => $dt->tv->jenisConsole->nama_console
                            ] : null
                        ] : null,

                        // Masih kirim object Paket tapi ambil yang penting aja
                        'paket' => $dt->paket ? [
                            'nama_paket'   => $dt->paket->nama_paket,
                            'durasi_menit' => $dt->paket->durasi_menit
                        ] : null
                    ];
                })
            ];
        });
        
        return response()->json([
            'success' => true,
            'message' => 'Data riwayat berhasil diambil',
            'data'    => $formattedData
        ], 200);
    }

    public function store(Request $request)
    {
        // 1. GENERATE ID (Format: YYYYMMDDHHmm + Random)
        // Contoh: 20260112092045
        $now = Carbon::now();
        $id_transaksi = $now->format('YmdHi') . rand(10, 99); 

        // 2. START DB TRANSACTION
        DB::beginTransaction();
        try {
            // Validasi Input
            $request->validate([
                'nama_penyewa' => 'required|string',
                'uang_bayar'   => 'required|numeric|min:0', // Numeric biar aman
                'metode_pembayaran' => 'required|string',
                'items'        => 'required|array|min:1', // Harus ada isinya
                
                // Validasi tiap item dalam array
                'items.*.tv_id'    => 'required|exists:tvs,id',
                'items.*.id_paket' => 'required|exists:paket_sewa,id_paket',
            ]);

            $grandTotal = 0;
            $dataDetails = []; // Nampung data untuk di-insert nanti

            // 3. LOGIC LOOPING ITEMS (Kalkulasi dulu, jangan insert Master dulu)
            foreach ($request->items as $itemRequest) {
                
                // A. Ambil Data Paket
                $paket = PaketSewa::find($itemRequest['id_paket']);
                // Safety check: Harusnya lolos validasi, tapi jaga2 kalau data master dihapus saat transaksi
                if (!$paket) {
                    throw new \Exception("Paket tidak ditemukan ID: " . $itemRequest['id_paket']);
                }

                // B. Ambil Data TV & Lock Row (PENTING biar gak rebutan)
                // lockForUpdate memastikan user lain gak bisa booking TV ini sampai transaksi ini selesai
                $tv = Tv::lockForUpdate()->find($itemRequest['tv_id']);

                // Cek Status TV Realtime
                if ($tv->status !== 'available') {
                    // Rollback otomatis terjadi karena masuk catch block nanti
                    throw new \Exception("TV " . $tv->nomor_tv . " statusnya " . $tv->status . " (Tidak Available)");
                }

                // C. Hitung Subtotal per Item
                $qty = 1; // Default rental pasti 1, kalau snack nanti bisa beda
                $hargaSatuan = $paket->harga;
                $subtotal = $hargaSatuan * $qty;
                $grandTotal += $subtotal;

                // D. Update Status TV
                $rentalEndTime = $now->copy()->addMinutes($paket->durasi_menit);
                $tv->update([
                    'status' => 'booked',
                    'rental_end_time' => $rentalEndTime
                ]);

                // E. Siapkan Data Detail (Masukin array dulu)
                $dataDetails[] = [
                    'id_transaksi' => $id_transaksi,
                    'tv_id'        => $tv->id,
                    'id_paket'     => $paket->id_paket,
                    'id_item'      => null, // Null karena ini sewa, bukan beli snack
                    'id_console'   => $tv->id_console,
                    'nama_item_snapshot' => "Sewa " . $tv->nomor_tv . " - " . $paket->nama_paket,
                    'harga_satuan' => $hargaSatuan,
                    'qty'          => $qty,
                    'subtotal'     => $subtotal,
                    'created_at'   => $now,
                    'updated_at'   => $now,
                ];
            }

            // 4. CEK PEMBAYARAN
            $uangKembalian = $request->uang_bayar - $grandTotal;
            if ($uangKembalian < 0) {
                throw new \Exception("Uang pembayaran kurang! Total: $grandTotal, Bayar: " . $request->uang_bayar);
            }

            // 5. INSERT MASTER TRANSAKSI (Sekali saja)
            $transaksi = Transaksi::create([
                'id_transaksi'      => $id_transaksi,
                'tanggal_transaksi' => $now,
                'nama_penyewa'      => $request->nama_penyewa,
                'total_tagihan'     => $grandTotal,
                'uang_bayar'        => $request->uang_bayar,
                'uang_kembalian'    => $uangKembalian,
                'metode_pembayaran' => $request->metode_pembayaran,
                'status_pembayaran' => 'LUNAS', // Asumsi rental bayar dimuka
            ]);

            // 6. INSERT DETAIL TRANSAKSI (Bulk Insert biar cepat)
            DetailTransaksi::insert($dataDetails);

            // 7. COMMIT DATABASE (Simpan Permanen)
            DB::commit();

            return response()->json([
                'success' => true,
                'message' => 'Transaksi Berhasil',
                'data' => [
                    'id_transaksi'      => $id_transaksi,
                    'tanggal_transaksi' => $now->toDateTimeString(), // Tambahkan ini
                    'nama_penyewa'      => $request->nama_penyewa,   // Tambahkan ini
                    'total_tagihan'     => $grandTotal,      // UBAH DARI 'total_bayar' JADI 'total_tagihan'
                    'uang_bayar'        => $request->uang_bayar, // Tambahkan ini biar lengkap
                    'uang_kembalian'    => $uangKembalian,   // UBAH DARI 'kembalian' JADI 'uang_kembalian'
                    'metode_pembayaran' => $request->metode_pembayaran,
                    'status_pembayaran' => 'LUNAS',
                    'details'           => $dataDetails
                ]
            ], 201);

        } catch (\Exception $e) {
            // Kalau ada error APAPUN di atas, batalkan semua perubahan DB
            DB::rollback();
            return response()->json([
                'success' => false,
                'message' => 'Transaksi Gagal: ' . $e->getMessage()
            ], 400); // 400 Bad Request
        }
    }

    public function show($id)
    {
        $transaksi = Transaksi::with(['details.tv', 'details.paket'])->find($id);

        if (!$transaksi) {
            return response()->json(['message' => 'Transaksi tidak ditemukan'], 404);
        }

        return response()->json([
            'success' => true,
            'data' => $transaksi
        ], 200);
    }
}