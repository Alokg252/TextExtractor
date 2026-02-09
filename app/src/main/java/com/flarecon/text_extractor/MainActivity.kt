package com.flarecon.text_extractor

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flarecon.text_extractor.service.FloatingButtonService
import com.flarecon.text_extractor.settings.CustomizationScreen
import com.flarecon.text_extractor.ui.theme.TextExtractorTheme

class MainActivity : ComponentActivity() {
    
    private var isServiceRunning by mutableStateOf(false)
    private var hasOverlayPermission by mutableStateOf(false)
    private var hasNotificationPermission by mutableStateOf(false)
    private var showCustomization by mutableStateOf(false)
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
        if (granted) {
            checkAndStartService()
        }
    }
    
    private val mediaProjectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            FloatingButtonService.mediaProjectionResultCode = result.resultCode
            FloatingButtonService.mediaProjectionIntent = result.data?.clone() as Intent
            Toast.makeText(this, "Screen capture permission granted!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            TextExtractorTheme {
                if (showCustomization) {
                    CustomizationScreen(
                        onBack = { showCustomization = false },
                        onSettingsChanged = {
                            // If service is running, restart it to apply new settings
                            if (isServiceRunning) {
                                FloatingButtonService.stopService(this@MainActivity)
                                FloatingButtonService.startService(this@MainActivity)
                            }
                        }
                    )
                } else {
                    MainScreen()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        checkPermissions()
    }
    
    private fun checkPermissions() {
        hasOverlayPermission = Settings.canDrawOverlays(this)
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    
    private fun requestMediaProjectionPermission() {
        val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as android.media.projection.MediaProjectionManager
        mediaProjectionLauncher.launch(manager.createScreenCaptureIntent())
    }
    
    private fun checkAndStartService() {
        if (!hasOverlayPermission) {
            Toast.makeText(this, "Please grant overlay permission first", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
            return
        }
        
        // Request media projection permission if not already granted
        if (FloatingButtonService.mediaProjectionIntent == null) {
            requestMediaProjectionPermission()
        }
        
        FloatingButtonService.startService(this)
        isServiceRunning = true
        Toast.makeText(this, "Floating button activated!", Toast.LENGTH_SHORT).show()
    }
    
    private fun stopService() {
        FloatingButtonService.stopService(this)
        isServiceRunning = false
        Toast.makeText(this, "Floating button deactivated", Toast.LENGTH_SHORT).show()
    }
    
    @Composable
    fun MainScreen() {
        val primaryColor = Color(0xFF00BFFF)
        val surfaceColor = MaterialTheme.colorScheme.surface
        
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = surfaceColor
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // App Icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tx",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Text Extractor",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Extract text from any screen with a single tap",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Permissions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Required Permissions",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        PermissionItem(
                            title = "Display over other apps",
                            isGranted = hasOverlayPermission,
                            onClick = { requestOverlayPermission() }
                        )
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Spacer(modifier = Modifier.height(12.dp))
                            PermissionItem(
                                title = "Notifications",
                                isGranted = hasNotificationPermission,
                                onClick = { requestNotificationPermission() }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        PermissionItem(
                            title = "Screen capture",
                            isGranted = FloatingButtonService.mediaProjectionIntent != null,
                            onClick = { requestMediaProjectionPermission() }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Instructions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "How to use:",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Tap the floating button\n" +
                                   "2. Draw a box around text\n" +
                                   "3. Text is copied automatically!\n" +
                                   "4. Tap Share pill to share",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            lineHeight = 22.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Main Action Button
                Button(
                    onClick = {
                        if (isServiceRunning) {
                            stopService()
                        } else {
                            checkAndStartService()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isServiceRunning) Color(0xFFE53935) else primaryColor
                    )
                ) {
                    Text(
                        text = if (isServiceRunning) "Stop Floating Button" else "Start Floating Button",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Customize Button
                OutlinedButton(
                    onClick = { showCustomization = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor
                    )
                ) {
                    Text(
                        text = "Customize Floating Button",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    @Composable
    fun PermissionItem(
        title: String,
        isGranted: Boolean,
        onClick: () -> Unit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) Color(0xFF4CAF50) else Color(0xFFFF9800)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isGranted) "✓" else "!",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontSize = 15.sp
            )
            
            if (!isGranted) {
                TextButton(onClick = onClick) {
                    Text("Grant")
                }
            }
        }
    }
}