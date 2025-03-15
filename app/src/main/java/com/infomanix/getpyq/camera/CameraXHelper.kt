package com.infomanix.getpyq.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import java.io.File
import java.util.concurrent.ExecutorService

object CameraXHelper {
    fun captureImage(imageCapture: ImageCapture, context: Context, executor: ExecutorService, onImageCaptured: (File) -> Unit) {
        val file = File(context.filesDir, "scanned_image.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture.takePicture(outputOptions, executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onImageCaptured(file)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraX", "Capture failed", exception)
                }
            })
    }
}
