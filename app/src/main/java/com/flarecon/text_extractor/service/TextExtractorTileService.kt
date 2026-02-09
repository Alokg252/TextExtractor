package com.flarecon.text_extractor.service

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.flarecon.text_extractor.R
import com.flarecon.text_extractor.ScreenCapturePermissionActivity

class TextExtractorTileService : TileService() {

    companion object {
        private var isServiceActive = false
        
        fun updateTileState(active: Boolean) {
            isServiceActive = active
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        
        if (isServiceActive) {
            // Stop the service
            FloatingButtonService.stopService(this)
            isServiceActive = false
        } else {
            // Check if we have media projection permission
            if (FloatingButtonService.mediaProjectionIntent == null) {
                // Need to request permission first - open the app
                val intent = Intent(this, ScreenCapturePermissionActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val pendingIntent = PendingIntent.getActivity(
                        this, 0, intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    startActivityAndCollapse(pendingIntent)
                } else {
                    @Suppress("DEPRECATION")
                    startActivityAndCollapse(intent)
                }
            } else {
                // Start the service directly
                FloatingButtonService.startService(this)
                isServiceActive = true
            }
        }
        
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        
        if (isServiceActive) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Text Extract"
            tile.subtitle = "Active"
            tile.icon = Icon.createWithResource(this, R.drawable.ic_tile_active)
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Text Extract"
            tile.subtitle = "Tap to start"
            tile.icon = Icon.createWithResource(this, R.drawable.ic_tile_inactive)
        }
        
        tile.updateTile()
    }
}
