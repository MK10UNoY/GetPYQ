package com.infomanix.getpyq.ui.screen

import com.infomanix.getpyq.camera.CameraXHelper
import com.infomanix.getpyq.storage.FileStorage
import com.infomanix.getpyq.utils.OCRUtils
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun ScannerScreen() {
    val context = LocalContext.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val scannedText = remember { mutableStateOf("") }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Camera permission required!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Button(onClick = {
            CameraXHelper.captureImage(imageCapture, context, cameraExecutor) { file ->
                OCRUtils.processImage(context, file) { extractedText ->
                    scannedText.value = extractedText
                    val fileName = OCRUtils.generateFileName(extractedText)
                    FileStorage.saveFile(file, fileName, context)
                }
            }
        }) {
            Text("Capture")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(scannedText.value)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScannerScreen() {
    ScannerScreen()
}
