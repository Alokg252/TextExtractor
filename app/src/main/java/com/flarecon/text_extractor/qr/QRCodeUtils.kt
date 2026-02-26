package com.flarecon.text_extractor.qr

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Utility object for QR code generation using ZXing library
 */
object QRCodeUtils {
    
    /**
     * Generate a QR code bitmap from the given text
     * @param text The text to encode in QR code
     * @param size The width and height of the QR code in pixels
     * @param foregroundColor The color of the QR code patterns (default: black)
     * @param backgroundColor The color of the QR code background (default: white)
     * @return Bitmap of the QR code, or null if generation fails
     */
    fun generateQRCode(
        text: String,
        size: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        if (text.isBlank()) return null
        
        return try {
            val hints = hashMapOf<EncodeHintType, Any>(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 2
            )
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints)
            
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            
            for (y in 0 until height) {
                for (x in 0 until width) {
                    pixels[y * width + x] = if (bitMatrix[x, y]) foregroundColor else backgroundColor
                }
            }
            
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Generate a QR code with custom styling (cyan on black theme)
     */
    fun generateThemedQRCode(text: String, size: Int = 512): Bitmap? {
        return generateQRCode(
            text = text,
            size = size,
            foregroundColor = 0xFF00BFFF.toInt(), // Cyan
            backgroundColor = Color.BLACK
        )
    }
}
