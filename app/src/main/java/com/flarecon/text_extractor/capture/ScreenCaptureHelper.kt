package com.flarecon.text_extractor.capture

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.RectF
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager

object ScreenCaptureHelper {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    fun captureRegion(
        context: Context,
        resultCode: Int,
        data: Intent,
        region: RectF,
        callback: (Bitmap?) -> Unit
    ) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)
        
        val screenWidth = metrics.widthPixels
        val screenHeight = metrics.heightPixels
        val screenDensity = metrics.densityDpi

        val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        
        try {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data)
            
            if (mediaProjection == null) {
                callback(null)
                return
            }

            imageReader = ImageReader.newInstance(
                screenWidth,
                screenHeight,
                PixelFormat.RGBA_8888,
                2
            )

            virtualDisplay = mediaProjection?.createVirtualDisplay(
                "ScreenCapture",
                screenWidth,
                screenHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                Handler(Looper.getMainLooper())
            )

            // Allow time for the display to be ready
            Handler(Looper.getMainLooper()).postDelayed({
                var image: Image? = null
                var bitmap: Bitmap? = null
                
                try {
                    image = imageReader?.acquireLatestImage()
                    
                    if (image != null) {
                        val planes = image.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * screenWidth

                        // Create full screen bitmap
                        val fullBitmap = Bitmap.createBitmap(
                            screenWidth + rowPadding / pixelStride,
                            screenHeight,
                            Bitmap.Config.ARGB_8888
                        )
                        fullBitmap.copyPixelsFromBuffer(buffer)

                        // Crop to region
                        val cropLeft = region.left.toInt().coerceIn(0, screenWidth - 1)
                        val cropTop = region.top.toInt().coerceIn(0, screenHeight - 1)
                        val cropWidth = region.width().toInt().coerceIn(1, screenWidth - cropLeft)
                        val cropHeight = region.height().toInt().coerceIn(1, screenHeight - cropTop)

                        bitmap = Bitmap.createBitmap(
                            fullBitmap,
                            cropLeft,
                            cropTop,
                            cropWidth,
                            cropHeight
                        )
                        
                        fullBitmap.recycle()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    image?.close()
                    cleanup()
                }
                
                callback(bitmap)
            }, 150)
            
        } catch (e: Exception) {
            e.printStackTrace()
            cleanup()
            callback(null)
        }
    }

    private fun cleanup() {
        virtualDisplay?.release()
        virtualDisplay = null
        
        imageReader?.close()
        imageReader = null
        
        mediaProjection?.stop()
        mediaProjection = null
    }
}
