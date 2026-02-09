package com.flarecon.text_extractor.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.flarecon.text_extractor.capture.ScreenCaptureHelper
import com.flarecon.text_extractor.service.FloatingButtonService

@SuppressLint("ViewConstructor")
class RegionSelectionOverlay(
    private val context: Context,
    private val onRegionSelected: (Bitmap?) -> Unit
) : View(context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var layoutParams: WindowManager.LayoutParams? = null
    
    private val selectionRect = RectF()
    private var startX = 0f
    private var startY = 0f
    private var isDrawing = false

    private val dimPaint = Paint().apply {
        color = Color.parseColor("#80000000")
        style = Paint.Style.FILL
    }

    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val borderPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val cornerPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val instructionPaint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun show() {
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        windowManager.addView(this, layoutParams)
    }

    fun dismiss() {
        try {
            windowManager.removeView(this)
        } catch (e: Exception) {
            // View might already be removed
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw semi-transparent overlay
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), dimPaint)

        if (isDrawing && !selectionRect.isEmpty) {
            // Clear the selection area
            canvas.drawRect(selectionRect, clearPaint)
            // Draw border around selection
            canvas.drawRect(selectionRect, borderPaint)
            // Draw corner handles
            drawCornerHandles(canvas)
        } else {
            // Draw instruction text
            canvas.drawText(
                "Draw a box around the text",
                width / 2f,
                height / 2f,
                instructionPaint
            )
        }
    }

    private fun drawCornerHandles(canvas: Canvas) {
        val handleSize = 16f
        val corners = listOf(
            selectionRect.left to selectionRect.top,
            selectionRect.right to selectionRect.top,
            selectionRect.left to selectionRect.bottom,
            selectionRect.right to selectionRect.bottom
        )
        
        corners.forEach { (x, y) ->
            canvas.drawCircle(x, y, handleSize, cornerPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                selectionRect.set(startX, startY, startX, startY)
                isDrawing = true
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                selectionRect.set(
                    minOf(startX, event.x),
                    minOf(startY, event.y),
                    maxOf(startX, event.x),
                    maxOf(startY, event.y)
                )
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                isDrawing = false
                if (selectionRect.width() > 50 && selectionRect.height() > 50) {
                    captureRegion()
                } else {
                    // Selection too small, cancel
                    onRegionSelected(null)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun captureRegion() {
        // Get the view's location on screen to convert view coordinates to screen coordinates
        val locationOnScreen = IntArray(2)
        getLocationOnScreen(locationOnScreen)
        
        // Create adjusted rectangle with absolute screen coordinates
        val screenRect = RectF(
            selectionRect.left + locationOnScreen[0],
            selectionRect.top + locationOnScreen[1],
            selectionRect.right + locationOnScreen[0],
            selectionRect.bottom + locationOnScreen[1]
        )
        
        // Hide overlay before capture
        visibility = INVISIBLE
        
        // Small delay to ensure overlay is hidden
        Handler(Looper.getMainLooper()).postDelayed({
            val mediaProjectionIntent = FloatingButtonService.mediaProjectionIntent
            val resultCode = FloatingButtonService.mediaProjectionResultCode
            
            if (mediaProjectionIntent != null && resultCode != 0) {
                ScreenCaptureHelper.captureRegion(
                    context,
                    resultCode,
                    mediaProjectionIntent,
                    screenRect  // Use screen-adjusted coordinates
                ) { bitmap ->
                    onRegionSelected(bitmap)
                }
            } else {
                // No media projection permission, need to request it
                onRegionSelected(null)
            }
        }, 100)
    }
}
