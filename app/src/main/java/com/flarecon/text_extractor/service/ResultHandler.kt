package com.flarecon.text_extractor.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.flarecon.text_extractor.R

object ResultHandler {

    private var shareButton: View? = null
    private var windowManager: WindowManager? = null
    private var extractedText: String = ""

    fun handleResult(context: Context, text: String) {
        extractedText = text
        
        // Copy to clipboard
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Extracted Text", text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(context, "✓ Text copied!", Toast.LENGTH_SHORT).show()

        // Show share pill
        showSharePill(context)
    }

    private fun showSharePill(context: Context) {
        // Remove existing share button if any
        dismissSharePill()
        
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        shareButton = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(R.drawable.share_pill_bg)
            setPadding(dpToPx(context, 16), dpToPx(context, 10), dpToPx(context, 16), dpToPx(context, 10))
            elevation = 12f
            
            addView(TextView(context).apply {
                text = "Share"
                setTextColor(0xFFFFFFFF.toInt())
                textSize = 14f
                setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_share, 0, 0, 0)
                compoundDrawablePadding = dpToPx(context, 8)
            })
            
            setOnClickListener {
                shareText(context)
                dismissSharePill()
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = dpToPx(context, 100)
        }

        windowManager?.addView(shareButton, params)

        // Auto-dismiss after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            dismissSharePill()
        }, 3000)
    }

    private fun shareText(context: Context) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, extractedText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        val chooser = Intent.createChooser(shareIntent, "Share extracted text")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun dismissSharePill() {
        try {
            shareButton?.let { view ->
                windowManager?.removeView(view)
            }
        } catch (e: Exception) {
            // View might already be removed
        }
        shareButton = null
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
