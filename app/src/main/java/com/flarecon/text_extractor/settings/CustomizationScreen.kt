package com.flarecon.text_extractor.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationScreen(
    onBack: () -> Unit,
    onSettingsChanged: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // State for all customization options
    var buttonText by remember { mutableStateOf(FloatingButtonPreferences.getText(context)) }
    var textColor by remember { mutableIntStateOf(FloatingButtonPreferences.getTextColor(context)) }
    var bgColor by remember { mutableIntStateOf(FloatingButtonPreferences.getBackgroundColor(context)) }
    var borderColor by remember { mutableIntStateOf(FloatingButtonPreferences.getBorderColor(context)) }
    var borderWidth by remember { mutableFloatStateOf(FloatingButtonPreferences.getBorderWidth(context)) }
    var opacity by remember { mutableFloatStateOf(FloatingButtonPreferences.getOpacity(context)) }
    var size by remember { mutableIntStateOf(FloatingButtonPreferences.getSize(context)) }
    var textSize by remember { mutableFloatStateOf(FloatingButtonPreferences.getTextSize(context)) }
    
    // Color picker dialog states
    var showTextColorPicker by remember { mutableStateOf(false) }
    var showBgColorPicker by remember { mutableStateOf(false) }
    var showBorderColorPicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customize Floating Button") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        FloatingButtonPreferences.resetToDefaults(context)
                        buttonText = FloatingButtonPreferences.DEFAULT_TEXT
                        textColor = FloatingButtonPreferences.DEFAULT_TEXT_COLOR
                        bgColor = FloatingButtonPreferences.DEFAULT_BG_COLOR
                        borderColor = FloatingButtonPreferences.DEFAULT_BORDER_COLOR
                        borderWidth = FloatingButtonPreferences.DEFAULT_BORDER_WIDTH
                        opacity = FloatingButtonPreferences.DEFAULT_OPACITY
                        size = FloatingButtonPreferences.DEFAULT_SIZE
                        textSize = FloatingButtonPreferences.DEFAULT_TEXT_SIZE
                        onSettingsChanged()
                    }) {
                        Text("Reset")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Preview
            Text(
                text = "Preview",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .alpha(opacity)
                    .clip(CircleShape)
                    .background(Color(bgColor))
                    .border(borderWidth.dp, Color(borderColor), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonText,
                    color = Color(textColor),
                    fontSize = textSize.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    
                    // Button Text
                    Text("Button Text", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = buttonText,
                        onValueChange = { 
                            if (it.length <= 4) {
                                buttonText = it
                                FloatingButtonPreferences.setText(context, it)
                                onSettingsChanged()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Max 4 characters") }
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Opacity
                    Text("Opacity: ${(opacity * 100).toInt()}%", fontWeight = FontWeight.Medium)
                    Slider(
                        value = opacity,
                        onValueChange = { 
                            opacity = it
                            FloatingButtonPreferences.setOpacity(context, it)
                            onSettingsChanged()
                        },
                        valueRange = 0.2f..1f
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Button Size
                    Text("Button Size: ${size}dp", fontWeight = FontWeight.Medium)
                    Slider(
                        value = size.toFloat(),
                        onValueChange = { 
                            size = it.toInt()
                            FloatingButtonPreferences.setSize(context, size)
                            onSettingsChanged()
                        },
                        valueRange = 30f..80f
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Text Size
                    Text("Text Size: ${textSize.toInt()}sp", fontWeight = FontWeight.Medium)
                    Slider(
                        value = textSize,
                        onValueChange = { 
                            textSize = it
                            FloatingButtonPreferences.setTextSize(context, it)
                            onSettingsChanged()
                        },
                        valueRange = 10f..28f
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Border Width
                    Text("Border Width: ${borderWidth.toInt()}dp", fontWeight = FontWeight.Medium)
                    Slider(
                        value = borderWidth,
                        onValueChange = { 
                            borderWidth = it
                            FloatingButtonPreferences.setBorderWidth(context, it)
                            onSettingsChanged()
                        },
                        valueRange = 0f..6f
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Colors Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Colors", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Text Color
                    ColorPickerRow(
                        label = "Text Color",
                        color = Color(textColor),
                        onClick = { showTextColorPicker = true }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Background Color
                    ColorPickerRow(
                        label = "Background Color",
                        color = Color(bgColor),
                        onClick = { showBgColorPicker = true }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Border Color
                    ColorPickerRow(
                        label = "Border Color",
                        color = Color(borderColor),
                        onClick = { showBorderColorPicker = true }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    
    // Color Picker Dialogs
    if (showTextColorPicker) {
        ColorPickerDialog(
            currentColor = Color(textColor),
            onColorSelected = { 
                textColor = it.toArgb()
                FloatingButtonPreferences.setTextColor(context, textColor)
                onSettingsChanged()
            },
            onDismiss = { showTextColorPicker = false }
        )
    }
    
    if (showBgColorPicker) {
        ColorPickerDialog(
            currentColor = Color(bgColor),
            onColorSelected = { 
                bgColor = it.toArgb()
                FloatingButtonPreferences.setBackgroundColor(context, bgColor)
                onSettingsChanged()
            },
            onDismiss = { showBgColorPicker = false }
        )
    }
    
    if (showBorderColorPicker) {
        ColorPickerDialog(
            currentColor = Color(borderColor),
            onColorSelected = { 
                borderColor = it.toArgb()
                FloatingButtonPreferences.setBorderColor(context, borderColor)
                onSettingsChanged()
            },
            onDismiss = { showBorderColorPicker = false }
        )
    }
}

@Composable
fun ColorPickerRow(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f)
        )
        
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
    }
}

@Composable
fun ColorPickerDialog(
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val presetColors = listOf(
        // Row 1 - Primary colors
        Color(0xFF00BFFF), // Cyan (default)
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF4CAF50), // Green
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF2196F3), // Blue
        
        // Row 2 - More colors
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFFF9800), // Orange
        Color(0xFF00BCD4), // Teal
        Color(0xFF673AB7), // Deep Purple
        Color(0xFF3F51B5), // Indigo
        Color(0xFF009688), // Teal
        
        // Row 3 - Neutrals
        Color(0xFFFFFFFF), // White
        Color(0xFF000000), // Black
        Color(0xFF9E9E9E), // Gray
        Color(0xFF607D8B), // Blue Gray
        Color(0xFF795548), // Brown
        Color(0xFFF44336), // Red
    )
    
    var customHex by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Color") },
        text = {
            Column {
                // Preset colors grid
                for (row in presetColors.chunked(6)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        if (color == currentColor) 3.dp else 1.dp,
                                        if (color == currentColor) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.outline,
                                        CircleShape
                                    )
                                    .clickable {
                                        onColorSelected(color)
                                        onDismiss()
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Custom hex input
                OutlinedTextField(
                    value = customHex,
                    onValueChange = { 
                        if (it.length <= 6 && it.all { c -> c.isDigit() || c in 'A'..'F' || c in 'a'..'f' }) {
                            customHex = it.uppercase()
                        }
                    },
                    label = { Text("Custom Hex (e.g., FF5722)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    prefix = { Text("#") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
                )
                
                if (customHex.length == 6) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor("#$customHex")))
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            onColorSelected(Color(android.graphics.Color.parseColor("#$customHex")))
                            onDismiss()
                        }) {
                            Text("Apply Custom Color")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
