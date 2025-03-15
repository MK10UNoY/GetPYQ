package com.infomanix.getpyq.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object OCRUtils {
    fun extractTextFromImage(imageFile: File, context: Context, onResult: (String) -> Unit) {
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer: TextRecognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text.trim()
                Log.d("OCRUtils", "Extracted text: $extractedText")

                // Extract useful info (e.g., Subject, Year, Semester)
                val documentTitle = generateDocumentTitle(extractedText)
                onResult(documentTitle) // Pass the generated name back
            }
            .addOnFailureListener { e ->
                Log.e("OCRUtils", "Text recognition failed", e)
                onResult("Untitled_Document")
            }
    }

    private fun generateDocumentTitle(text: String): String {
        val courseCodeRegex = Regex("\\b[A-Z]{2,3}\\s*\\d{3}\\b") // Matches EE 210, CS101, etc.
        val subjectRegex = Regex("(?i)Subject:\\s*([A-Za-z &]+)")
        val semesterRegex = Regex("\\bSemester[:\\s]+(\\d+)\\b", RegexOption.IGNORE_CASE)
        val examTypeRegex = Regex("(?i)(Mid-Semester|End-Semester|Quiz)")
        val monthYearRegex = Regex("\\b(January|February|March|April|May|June|July|August|September|October|November|December)\\s*(20\\d{2})\\b")

        val courseCode = courseCodeRegex.find(text)?.value ?: "UnknownCode"
        val subject = subjectRegex.find(text)?.groupValues?.get(1)?.trim() ?: "UnknownSubject"

        // Extract and clean semester (handle cases like "4th")
        val semester = semesterRegex.find(text)?.groupValues?.get(1)?.replace(Regex("\\D"), "")?.let { "Sem$it" } ?: "UnknownSem"

        val examType = examTypeRegex.find(text)?.value ?: "Exam"

        // Extract month-year and remove duplicates
        val monthYearMatches = monthYearRegex.findAll(text).map { it.groupValues[1] + it.groupValues[2] }.toList()
        val monthYear = if (monthYearMatches.isNotEmpty()) monthYearMatches.distinct().joinToString("_") else "UnknownDate"

        Log.d("OCRUtils", "Extracted: $courseCode, $subject, $semester, $examType, $monthYear")
        Log.d("Extracted", "${courseCode}_${subject.replace(" ", "")}_${semester}_${examType}_$monthYear")

        return "${courseCode}_${subject.replace(" ", "")}_${semester}_${examType}_$monthYear"
    }
    fun saveToPdf(bitmap: Bitmap, fileName: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        pdfDocument.finishPage(page)

        // Define the custom directory path (Downloads/GetPYQ)
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val getPyqDir = File(downloadsDir, "GetPYQ")

        if (!getPyqDir.exists()) {
            getPyqDir.mkdirs() // Create directory if it doesn’t exist
        }

        val filePath = File(getPyqDir, "GPQ_$fileName.pdf")
        try {
            FileOutputStream(filePath).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            Log.d("OCRUtils", "PDF saved: ${filePath.absolutePath}")
        } catch (e: IOException) {
            Log.e("OCRUtils", "Error saving PDF", e)
        } finally {
            pdfDocument.close()
        }
    }
}
