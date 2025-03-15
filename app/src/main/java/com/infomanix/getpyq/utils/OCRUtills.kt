package com.infomanix.getpyq.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

object OCRUtils {
    fun processImage(context: Context, file: File, onTextExtracted: (String) -> Unit) {
        val image = InputImage.fromFilePath(context, Uri.fromFile(file))
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                onTextExtracted(extractedText)
            }
            .addOnFailureListener { e ->
                Log.e("OCR", "Failed to recognize text", e)
            }
    }

    fun generateFileName(text: String): String {
        val subjectCode = Regex("CODE[:\\-]?\\s*(\\w+)").find(text)?.groupValues?.get(1) ?: "Unknown"
        val year = Regex("\\b(19|20)\\d{2}\\b").find(text)?.groupValues?.get(0) ?: "Unknown"

        return "${subjectCode}_Exam_${year}.jpg"
    }
}
