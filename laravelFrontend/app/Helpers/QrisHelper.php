<?php

namespace App\Helpers;

use chillerlan\QRCode\QRCode;
use chillerlan\QRCode\QROptions;

class QrisHelper
{
    /**
     * QRIS Statis dari THE PUTRAS, TAMBAKSARI
     * NMID: ID1025403479328
     * 
     * Format EMV QRIS:
     * - 00: Payload Format Indicator
     * - 01: Point of Initiation Method (11=static, 12=dynamic)
     * - 26-45: Merchant Account Information
     * - 52: Merchant Category Code
     * - 53: Transaction Currency (360 = IDR)
     * - 54: Transaction Amount (HANYA ADA DI DYNAMIC QRIS)
     * - 58: Country Code
     * - 59: Merchant Name
     * - 60: Merchant City
     * - 63: CRC (Checksum)
     */
    
    // QRIS String statis (decode dari gambar asli THE PUTRAS)
    private static $staticQrisBase = '00020101021126610014COM.GO-JEK.WWW01189360091433175824250210G3175824250303UKE51440014ID.CO.QRIS.WWW0215ID10254034793280303UKE5204581253033605802ID5922The Putras, Tambaksari6008SURABAYA61056013462070703A016304627B';

    /**
     * Parse QRIS EMV string ke array TLV
     */
    public static function parseQris(string $qris): array
    {
        $data = [];
        $pos = 0;
        $len = strlen($qris);
        
        while ($pos < $len) {
            // Minimal butuh 4 karakter (ID 2 + Length 2)
            if ($pos + 4 > $len) break;
            
            $id = substr($qris, $pos, 2);
            $length = (int)substr($qris, $pos + 2, 2);
            
            // Pastikan value tidak melampaui string
            if ($pos + 4 + $length > $len) break;
            
            $value = substr($qris, $pos + 4, $length);
            $data[$id] = $value;
            $pos += 4 + $length;
        }
        
        return $data;
    }

    /**
     * Build QRIS string dari array TLV
     */
    public static function buildQris(array $data): string
    {
        $qris = '';
        
        // Urutkan key secara ascending sesuai standar EMV (00, 01, ... 62)
        ksort($data);
        
        foreach ($data as $id => $value) {
            $length = str_pad(strlen($value), 2, '0', STR_PAD_LEFT);
            $qris .= $id . $length . $value;
        }
        
        // Add CRC placeholder (6304)
        $qris .= '6304';
        
        // Calculate CRC16-CCITT
        $crc = self::calculateCRC16($qris);
        $qris .= strtoupper(str_pad(dechex($crc), 4, '0', STR_PAD_LEFT));
        
        return $qris;
    }

    /**
     * Calculate CRC16-CCITT untuk QRIS
     */
    public static function calculateCRC16(string $data): int
    {
        $crc = 0xFFFF;
        
        for ($i = 0; $i < strlen($data); $i++) {
            $crc ^= ord($data[$i]) << 8;
            for ($j = 0; $j < 8; $j++) {
                if ($crc & 0x8000) {
                    $crc = ($crc << 1) ^ 0x1021;
                } else {
                    $crc <<= 1;
                }
            }
            $crc &= 0xFFFF;
        }
        
        return $crc;
    }

    /**
     * Convert QRIS statis ke dinamis dengan menambahkan nominal
     * 
     * @param int $amount Nominal dalam rupiah (contoh: 15000)
     * @return string QRIS string dengan nominal
     */
    public static function addAmount(int $amount): string
    {
        $qris = self::$staticQrisBase;
        
        // Parse QRIS
        $data = self::parseQris($qris);
        
        // Ubah Point of Initiation dari 11 (static) ke 12 (dynamic)
        $data['01'] = '12';
        
        // Tambahkan Transaction Amount (field 54)
        $data['54'] = (string)$amount;
        
        // Rebuild QRIS dengan CRC baru
        return self::buildQris($data);
    }

    /**
     * Generate QR Code SVG dari QRIS string (NO GD REQUIRED!)
     * Returns SVG string
     * 
     * @param string $qrisString QRIS EMV string
     * @return string SVG image string
     */
    public static function generateQrCode(string $qrisString): string
    {
        $options = new QROptions([
            'version'         => QRCode::VERSION_AUTO,
            'outputType'      => QRCode::OUTPUT_MARKUP_SVG,
            'eccLevel'        => QRCode::ECC_M,
            'scale'           => 10,
            'addQuietzone'    => true,
            'svgViewBoxSize'  => 300,
        ]);

        $qrcode = new QRCode($options);
        return $qrcode->render($qrisString);
    }

    /**
     * Generate QRIS Dinamis dengan nominal dan return sebagai SVG
     * 
     * @param int $amount Nominal dalam rupiah
     * @return array ['qris_string' => '...', 'qr_svg' => 'base64...', 'qr_data_uri' => '...']
     */
    public static function generateDynamicQris(int $amount): array
    {
        $qrisString = self::addAmount($amount);
        $qrSvg = self::generateQrCode($qrisString);
        
        return [
            'qris_string' => $qrisString,
            'qr_svg' => $qrSvg,
            'qr_data_uri' => 'data:image/svg+xml;base64,' . base64_encode($qrSvg),
            'amount' => $amount,
            'amount_formatted' => 'Rp ' . number_format($amount, 0, ',', '.')
        ];
    }

    /**
     * Set QRIS string statis (jika ingin update dari source lain)
     */
    public static function setStaticQris(string $qris): void
    {
        self::$staticQrisBase = $qris;
    }

    /**
     * Get QRIS string statis saat ini
     */
    public static function getStaticQris(): string
    {
        return self::$staticQrisBase;
    }
}
