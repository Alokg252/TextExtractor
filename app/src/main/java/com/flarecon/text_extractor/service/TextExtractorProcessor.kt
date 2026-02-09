package com.flarecon.text_extractor.service

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object TextExtractorProcessor {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.Builder().build())
    }

    fun extractText(context: Context, bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text.trim()
                
                Handler(Looper.getMainLooper()).post {
                    if (extractedText.isNotEmpty()) {
                        ResultHandler.handleResult(context, extractedText)
                    } else {
                        Toast.makeText(context, "No text detected", Toast.LENGTH_SHORT).show()
                    }
                }
                
                // Recycle bitmap after processing
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
            .addOnFailureListener { e ->
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context, 
                        "Failed to extract text: ${e.localizedMessage}", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
    }
}
