package com.infomanix.getpyq.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {

    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir("ScannedDocs")
        return File.createTempFile("IMG_$timeStamp", ".jpg", storageDir)
    }

    fun getScannedImages(context: Context): List<Uri> {
        val storageDir = context.getExternalFilesDir("ScannedDocs") ?: return emptyList()
        return storageDir.listFiles()?.map { file ->
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } ?: emptyList()
    }

    fun deleteImage(context: Context, uri: Uri) {
        val file = File(uri.path ?: return)
        if (file.exists()) file.delete()
    }

    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun rotateAndSaveImage(file: File, degrees: Float) {
        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val matrix = Matrix().apply { postRotate(degrees) }

            val rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            FileOutputStream(file).use { out ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            bitmap.recycle() // Free memory
            rotatedBitmap.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun rotateAndSaveImage2(file: File, degrees: Float) {
        try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: return
            val matrix = Matrix().apply { postRotate(degrees) }

            val rotatedBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            // Preserve EXIF metadata
            val exif = ExifInterface(file.absolutePath)
            val tempFile = File(file.parent, "temp_${file.name}")

            FileOutputStream(tempFile).use { out ->
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            // Copy EXIF data to the new file
            val newExif = ExifInterface(tempFile.absolutePath)
            copyTo(exif, newExif)
            newExif.saveAttributes()

            // Replace original file with rotated one
            tempFile.renameTo(file)

            bitmap.recycle() // Free memory
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun compressImage2(inputFile: File, maxSizeKB: Int = 1500) {
        val bitmap = BitmapFactory.decodeFile(inputFile.absolutePath)

        var quality = 90
        val outputStream = ByteArrayOutputStream()

        do {
            outputStream.reset()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            quality -= 5 // Decrease by 5% per iteration
        } while (outputStream.size() / 1024 > maxSizeKB && quality > 10)

        FileOutputStream(inputFile).use { it.write(outputStream.toByteArray()) }
    }

    fun getCorrectlyOrientedBitmap(file: File): Bitmap {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        val exif = ExifInterface(file.absolutePath)

        val rotation = when (exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        return if (rotation != 0f) {
            val matrix = Matrix().apply { postRotate(rotation) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }
}

// Function to manually copy EXIF attributes
private fun copyTo(source: ExifInterface, destination: ExifInterface) {
    val attributes = listOf(
        ExifInterface.TAG_DATETIME,
        ExifInterface.TAG_MAKE,
        ExifInterface.TAG_MODEL,
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.TAG_F_NUMBER,
        ExifInterface.TAG_ISO_SPEED_RATINGS,
        ExifInterface.TAG_EXPOSURE_TIME,
        ExifInterface.TAG_GPS_LATITUDE,
        ExifInterface.TAG_GPS_LONGITUDE
    )

    for (tag in attributes) {
        source.getAttribute(tag)?.let { value ->
            destination.setAttribute(tag, value)
        }
    }
}
