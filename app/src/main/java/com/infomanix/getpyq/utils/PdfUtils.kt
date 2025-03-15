package com.infomanix.getpyq.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.core.content.FileProvider
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
            val pdfDocument = PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY))
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
        val fileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r") ?: return emptyList()
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
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val getPYQFolder = File(downloadsDir, "GetPYQ")

        if (!getPYQFolder.exists()) getPYQFolder.mkdirs()

        // Ensure unique filename if it already exists
        var outputPdf = File(getPYQFolder, "$pdfFileName.pdf")
        var fileIndex = 1
        while (outputPdf.exists()) {
            outputPdf = File(getPYQFolder, "${pdfFileName}_$fileIndex.pdf")
            fileIndex++
        }

        val pdfDocument = PdfDocument()

        try {
            imageFiles.forEachIndexed { index, file ->
                val originalBitmap = BitmapFactory.decodeFile(file.path)

                // Scale down image to reduce file size
                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 1000, 1400, true)

                val pageInfo = PdfDocument.PageInfo.Builder(scaledBitmap.width, scaledBitmap.height, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)

                page.canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
                pdfDocument.finishPage(page)

                originalBitmap.recycle()
                scaledBitmap.recycle()
            }

            FileOutputStream(outputPdf).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }

            pdfDocument.close() // ✅ Close the document before returning

            // ✅ Show Toast with PDF Name
            Toast.makeText(context, "PDF Saved: ${outputPdf.name}", Toast.LENGTH_LONG).show()

            // ✅ Prompt User to Open the PDF
            openPdfWithExternalApp(context, outputPdf)

            return outputPdf // ✅ Return the generated PDF file

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error Saving PDF!", Toast.LENGTH_LONG).show()
            pdfDocument.close()
        }

        return null // If something goes wrong, return null
    }
    /**
     * Opens the compiled PDF with the default PDF reader on the device.
     */
    private fun openPdfWithExternalApp2(context: Context, pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read access
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Start as a new task
            }

            context.startActivity(Intent.createChooser(intent, "Open PDF with"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No PDF viewer found!", Toast.LENGTH_LONG).show()
        }
    }
    /**
     * Open a PDF file using an external PDF viewer.
     */
    fun openPdfWithExternalApp(context: Context, pdfFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, "Open PDF with"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Delete a given PDF file.
     */
    fun deletePdf(pdfFile: File): Boolean {
        return if (pdfFile.exists()) pdfFile.delete() else false
    }
    /*
        Extract a single page from an existing PDF and save it as a new PDF file.
    */
    /*fun extractPageFromPdf(context: Context, pdfUri: Uri, pageNumber: Int, outputPdfName: String): File? {
        val fileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r") ?: return null
        val pdfRenderer = PdfRenderer(fileDescriptor)

        if (pageNumber < 1 || pageNumber > pdfRenderer.pageCount) {
            pdfRenderer.close()
            return null
        }

        val page = pdfRenderer.openPage(pageNumber - 1)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        pdfRenderer.close()

        val extractedPdf = compileImagesToPdf(context, listOf(bitmapToFile(context, bitmap, outputPdfName)), outputPdfName)
        bitmap.recycle()

        return extractedPdf
    }*/
    /**
     * Convert a Bitmap to a File and store it in temporary storage.
     */
    private fun bitmapToFile(context: Context, bitmap: Bitmap, fileName: String): File {
        val file = File(context.cacheDir, "$fileName.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Compress with 80% quality
        }
        return file
    }
}

/**
 * Data classes to represent PDFs and pages.
 */
data class PdfFile(val name: String, val uri: String, val pageCount: Int, val size: Long)
data class PdfPage(val bitmap: Bitmap, val pageNumber: Int)
