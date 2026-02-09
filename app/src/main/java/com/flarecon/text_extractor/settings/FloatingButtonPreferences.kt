package com.flarecon.text_extractor.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit

object FloatingButtonPreferences {
    private const val PREFS_NAME = "floating_button_customization"
    
    // Keys
    private const val KEY_TEXT = "button_text"
    private const val KEY_TEXT_COLOR = "text_color"
    private const val KEY_BG_COLOR = "bg_color"
    private const val KEY_BORDER_COLOR = "border_color"
    private const val KEY_BORDER_WIDTH = "border_width"
    private const val KEY_OPACITY = "opacity"
    private const val KEY_SIZE = "size"
    private const val KEY_TEXT_SIZE = "text_size"
    
    // Defaults
    const val DEFAULT_TEXT = "Tx"
    const val DEFAULT_TEXT_COLOR = 0xFF00BFFF.toInt()  // Cyan
    const val DEFAULT_BG_COLOR = 0xFF000000.toInt()    // Black
    const val DEFAULT_BORDER_COLOR = 0xFF00BFFF.toInt() // Cyan
    const val DEFAULT_BORDER_WIDTH = 2f
    const val DEFAULT_OPACITY = 1f
    const val DEFAULT_SIZE = 40
    const val DEFAULT_TEXT_SIZE = 16f
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun getText(context: Context): String {
        return getPrefs(context).getString(KEY_TEXT, DEFAULT_TEXT) ?: DEFAULT_TEXT
    }
    
    fun setText(context: Context, text: String) {
        getPrefs(context).edit { putString(KEY_TEXT, text) }
    }
    
    fun getTextColor(context: Context): Int {
        return getPrefs(context).getInt(KEY_TEXT_COLOR, DEFAULT_TEXT_COLOR)
    }
    
    fun setTextColor(context: Context, color: Int) {
        getPrefs(context).edit { putInt(KEY_TEXT_COLOR, color) }
    }
    
    fun getBackgroundColor(context: Context): Int {
        return getPrefs(context).getInt(KEY_BG_COLOR, DEFAULT_BG_COLOR)
    }
    
    fun setBackgroundColor(context: Context, color: Int) {
        getPrefs(context).edit { putInt(KEY_BG_COLOR, color) }
    }
    
    fun getBorderColor(context: Context): Int {
        return getPrefs(context).getInt(KEY_BORDER_COLOR, DEFAULT_BORDER_COLOR)
    }
    
    fun setBorderColor(context: Context, color: Int) {
        getPrefs(context).edit { putInt(KEY_BORDER_COLOR, color) }
    }
    
    fun getBorderWidth(context: Context): Float {
        return getPrefs(context).getFloat(KEY_BORDER_WIDTH, DEFAULT_BORDER_WIDTH)
    }
    
    fun setBorderWidth(context: Context, width: Float) {
        getPrefs(context).edit { putFloat(KEY_BORDER_WIDTH, width) }
    }
    
    fun getOpacity(context: Context): Float {
        return getPrefs(context).getFloat(KEY_OPACITY, DEFAULT_OPACITY)
    }
    
    fun setOpacity(context: Context, opacity: Float) {
        getPrefs(context).edit { putFloat(KEY_OPACITY, opacity) }
    }
    
    fun getSize(context: Context): Int {
        return getPrefs(context).getInt(KEY_SIZE, DEFAULT_SIZE)
    }
    
    fun setSize(context: Context, size: Int) {
        getPrefs(context).edit { putInt(KEY_SIZE, size) }
    }
    
    fun getTextSize(context: Context): Float {
        return getPrefs(context).getFloat(KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE)
    }
    
    fun setTextSize(context: Context, size: Float) {
        getPrefs(context).edit { putFloat(KEY_TEXT_SIZE, size) }
    }
    
    fun resetToDefaults(context: Context) {
        getPrefs(context).edit {
            clear()
        }
    }
}
