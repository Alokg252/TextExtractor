package com.flarecon.text_extractor.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.graphics.Color
import android.graphics.Typeface
import androidx.core.app.NotificationCompat
import com.flarecon.text_extractor.MainActivity
import com.flarecon.text_extractor.R
import com.flarecon.text_extractor.overlay.RegionSelectionOverlay

class FloatingButtonService : Service() {

    companion object {
        private const val CHANNEL_ID = "text_extractor_channel"
        private const val NOTIFICATION_ID = 1001
        private const val PREFS_NAME = "floating_button_prefs"
        private const val KEY_X = "button_x"
        private const val KEY_Y = "button_y"
        
        var mediaProjectionIntent: Intent? = null
        var mediaProjectionResultCode: Int = 0
        
        fun startService(context: Context) {
            val intent = Intent(context, FloatingButtonService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            context.stopService(Intent(context, FloatingButtonService::class.java))
        }
    }

    private lateinit var windowManager: WindowManager
    private lateinit var floatingButton: TextView
    private lateinit var prefs: SharedPreferences
    private var regionOverlay: RegionSelectionOverlay? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        createFloatingButton()
        
        // Notify tile that service is active
        TextExtractorTileService.updateTileState(true)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Text Extractor Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the floating button active"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Text Extractor Active")
        .setContentText("Tap the floating button to extract text")
        .setSmallIcon(R.drawable.ic_text_extract)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    @SuppressLint("ClickableViewAccessibility")
    private fun createFloatingButton() {
        floatingButton = TextView(this).apply {
            text = "Tx"
            setTextColor(Color.parseColor("#00bfff"))
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setBackgroundResource(R.drawable.floating_button_bg)
            elevation = 8f
        }

        val savedX = prefs.getInt(KEY_X, 0)
        val savedY = prefs.getInt(KEY_Y, 200)

        val params = WindowManager.LayoutParams(
            dpToPx(40),
            dpToPx(40),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = savedY
        }

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        val clickThreshold = dpToPx(10)

        floatingButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    
                    if (kotlin.math.abs(deltaX) > clickThreshold || kotlin.math.abs(deltaY) > clickThreshold) {
                        isDragging = true
                    }
                    
                    params.x = initialX + deltaX
                    params.y = initialY + deltaY
                    windowManager.updateViewLayout(floatingButton, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        onFloatingButtonClicked()
                    } else {
                        // Save position
                        prefs.edit()
                            .putInt(KEY_X, params.x)
                            .putInt(KEY_Y, params.y)
                            .apply()
                    }
                    true
                }
                else -> false
            }
        }

        windowManager.addView(floatingButton, params)
    }

    private fun onFloatingButtonClicked() {
        // Hide floating button temporarily
        floatingButton.visibility = View.GONE
        
        // Show region selection overlay
        showRegionSelectionOverlay()
    }

    private fun showRegionSelectionOverlay() {
        regionOverlay = RegionSelectionOverlay(this) { bitmap ->
            // Region selected, process the bitmap
            regionOverlay?.dismiss()
            regionOverlay = null
            floatingButton.visibility = View.VISIBLE
            
            if (bitmap != null) {
                TextExtractorProcessor.extractText(this, bitmap)
            }
        }
        regionOverlay?.show()
    }

    fun showFloatingButton() {
        floatingButton.visibility = View.VISIBLE
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // Notify tile that service is inactive
        TextExtractorTileService.updateTileState(false)
        
        try {
            windowManager.removeView(floatingButton)
            regionOverlay?.dismiss()
        } catch (e: Exception) {
            // View might already be removed
        }
    }
}
