package com.infomanix.getpyq.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object PdfUtils {
    /**
     * Get all scanned PDFs stored in the app's external directory.
     */
    fun getScannedPdfs(context: Context): List<PdfFile> {
        val pdfList = mutableListOf<PdfFile>()
        val pdfDir = File(context.getExternalFilesDir(null), "PDFs")

        if (!pdfDir.exists()) return emptyList()

        pdfDir.listFiles { file -> file.extension == "pdf" }?.forEach { file ->
            val pdfDocument =
                PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
            val pageCount = pdfDocument.pageCount
            val fileSize = file.length() / 1024 // KB
            pdfList.add(PdfFile(file.nameWithoutExtension, file.absolutePath, pageCount, fileSize))
            pdfDocument.close()
        }
        return pdfList
    }

    /**
     * Extract all pages from a given PDF and return them as Bitmaps.
     */
    fun getPdfPages(context: Context, pdfUri: Uri): List<PdfPage> {
        val pages = mutableListOf<PdfPage>()
        val fileDescriptor =
            context.contentResolver.openFileDescriptor(pdfUri, "r") ?: return emptyList()
        val pdfRenderer = PdfRenderer(fileDescriptor)

        for (i in 0 until pdfRenderer.pageCount) {
            val page = pdfRenderer.openPage(i)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            pages.add(PdfPage(bitmap, i + 1))
            page.close()
        }

        pdfRenderer.close()
        return pages
    }

    /**
     * Compile images into a single PDF and store it in the "GetPYQ" folder in Downloads.
     */
    fun compileImagesToPdf(context: Context, imageFiles: List<File>, pdfFileName: String): File? {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val getPYQFolder = File(downloadsDir, "GetPYQ").apply { if (!exists()) mkdirs() }

        val outputPdf = generateUniqueFileName(getPYQFolder, pdfFileName)
        val pdfDocument = PdfDocument()

        try {
            imageFiles.forEachIndexed { index, file ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                    BitmapFactory.decodeFile(file.path, this) // Get dimensions
                    inJustDecodeBounds = false
                    inSampleSize =
                        calculateInSampleSize(outWidth, outHeight, 800, 1000) // Aggressive scaling
                    inPreferredConfig = Bitmap.Config.RGB_565 // Lower memory format
                }
                val scaledBitmap = BitmapFactory.decodeFile(file.path, options)

                scaledBitmap?.let {
                    val pageInfo =
                        PdfDocument.PageInfo.Builder(it.width, it.height, index + 1).create()
                    val page = pdfDocument.startPage(pageInfo)

                    page.canvas.drawBitmap(it, 0f, 0f, null)
                    pdfDocument.finishPage(page)
                    it.recycle() // Free memory
                }
            }

            BufferedOutputStream(FileOutputStream(outputPdf)).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            Toast.makeText(context, "PDF Saved: ${outputPdf.name}", Toast.LENGTH_LONG).show()

            return outputPdf

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error Saving PDF!", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }

        return null
    }

    fun compileImagesToPdf4Upload(imageFiles: List<File>, pdfFile: String): File? {
        if (imageFiles.isEmpty()) return null

        val outputPdf = File(pdfFile)
        val pdfDocument = PdfDocument()

        try {
            imageFiles.forEachIndexed { index, file ->
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                    BitmapFactory.decodeFile(file.path, this)
                    inJustDecodeBounds = false
                    inSampleSize = calculateInSampleSize(outWidth, outHeight, 800, 1000)
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                val scaledBitmap = BitmapFactory.decodeFile(file.path, options)

                scaledBitmap?.let {
                    val pageInfo =
                        PdfDocument.PageInfo.Builder(it.width, it.height, index + 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    page.canvas.drawBitmap(it, 0f, 0f, null)
                    pdfDocument.finishPage(page)
                    it.recycle()
                }
            }

            BufferedOutputStream(FileOutputStream(outputPdf)).use { pdfDocument.writeTo(it) }

            return outputPdf // ✅ Return PDF file without deleting it

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }

        return null
    }
    /**
     * Helper to generate a unique file name*/
    private fun generateUniqueFileName(directory: File, baseName: String): File {
        var file = File(directory, "$baseName.pdf")
        var index = 1
        while (file.exists()) {
            file = File(directory, "${baseName}_$index.pdf")
            index++
        }
        return file
    }

    /**
     * Calculate optimal inSampleSize for scaling
     */
    private fun calculateInSampleSize(
        origWidth: Int,
        origHeight: Int,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        var inSampleSize = 1
        if (origHeight > reqHeight || origWidth > reqWidth) {
            val halfHeight = origHeight / 2
            val halfWidth = origWidth / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Opens the compiled PDF with the default PDF reader on the device.
     */
    fun openPdfWithExternalApp2(context: Context, pdfFile: File) {
        if (!pdfFile.exists()) {
            Log.e("Pdf", "PDF file does not exist at: ${pdfFile.absolutePath}")
            return
        } else {
            Log.d("Pdf", "PDF file exists at: ${pdfFile.absolutePath}")
        }

        try {
            val uri =
                FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Try opening with the default PDF viewer first
            val packageManager = context.packageManager
            val resolvedActivities = packageManager.queryIntentActivities(intent, 0)

            if (resolvedActivities.isNotEmpty()) {
                context.startActivity(intent) // Open with default app
            } else {
                // No default app found, show chooser
                val chooserIntent = Intent.createChooser(intent, "Open PDF with")
                context.startActivity(chooserIntent)
            }
        } catch (e: Exception) {
            Log.e("Pdf", "Failed to open PDF", e)
            Toast.makeText(context, "No PDF viewer found!", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Delete a given PDF file.
     */
    fun deletePdf(pdfFile: File): Boolean {
        return if (pdfFile.exists()) pdfFile.delete() else false
    }

    /**
     * Convert a Bitmap to a File and store it in temporary storage.
     */
    private fun bitmapToFile(context: Context, bitmap: Bitmap, fileName: String): File {
        val file = File(context.cacheDir, "$fileName.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                80,
                outputStream
            ) // Compress with 80% quality
        }
        return file
    }
}

/**
 * Data classes to represent PDFs and pages.
 */
data class PdfFile(val name: String, val uri: String, val pageCount: Int, val size: Long)
data class PdfPage(val bitmap: Bitmap, val pageNumber: Int)
