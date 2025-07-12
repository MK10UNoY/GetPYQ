package com.infomanix.getpyq.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LibraryAddCheck
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.infomanix.getpyq.ui.viewmodels.FileViewModel
import com.infomanix.getpyq.utils.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

@RequiresApi(Build.VERSION_CODES.R)
@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScannerScreen(navController: NavController, fileViewModel: FileViewModel) {
    val context = LocalContext.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val outputDirectory = remember {
        val sessionFolder = fileViewModel.getSessionFolderName()?.let { File(it) }

        if (fileViewModel.isViewSessionActive && sessionFolder != null && sessionFolder.exists()) {
            sessionFolder
        } else {
            val newDir = getOutputDirectory(context)
            if (!newDir.exists()) newDir.mkdirs() // Ensure folder exists
            newDir
        }
    }

    val capturedImages = remember {
        mutableStateListOf<File>().apply {
            if (!fileViewModel.isViewSessionActive) {
                // 🔹 Fresh Scan Session → Start with an empty list
                clear()
            }
        }
    }

    val imageCapture = remember {
        ImageCapture.Builder().setTargetRotation(
            context.display.rotation
        ).build()
    }
    var showExitDialog by remember { mutableStateOf(false) }
    var proceedClicked by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    var isGridEnabled by rememberSaveable { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> if (!granted) Log.e("CameraX", "Camera permission denied!") }
    )
    // Launch the image picker for multiple image selection
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            uris.let {
                // Copy selected images to output directory and add to the capturedImages list
                it.forEach { uri ->
                    val imageFile = File(outputDirectory, "image_${System.currentTimeMillis()}.jpg")
                    copyImageToFile(context, uri, imageFile)
                    capturedImages.add(imageFile)
                }
            }
        }
    )
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                try {
                    val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                    val pdfRenderer = PdfRenderer(fileDescriptor!!)
                    val pageCount = pdfRenderer.pageCount

                    val images = mutableListOf<File>()
                    val outputDir = File(getOutputDirectory(context), "pdf_images")
                    if (!outputDir.exists()) outputDir.mkdirs()

                    // Loop through each page and convert to image
                    for (pageIndex in 0 until pageCount) {
                        val page = pdfRenderer.openPage(pageIndex)
                        val bitmap =
                            Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                        // Save bitmap as a file in outputDir
                        val imageFile = File(outputDir, "page_${pageIndex + 1}.jpg")
                        val outputStream = FileOutputStream(imageFile)
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

                        // Add image file to the list
                        images.add(imageFile)

                        // Close the page after rendering
                        page.close()
                    }

                    // Once all images are generated, add them to capturedImages
                    capturedImages.addAll(images)
                    pdfRenderer.close()
                } catch (e: Exception) {
                    Log.e("PDFConversion", "Error converting PDF to images", e)
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        Log.d("AddPage", "${fileViewModel.getSessionFolderName()}")
        Log.d("AddPage", "$outputDirectory")
        Log.d("AddPage", "$capturedImages")
        val cameFromGridPreview =
            navController.previousBackStackEntry?.destination?.route == "gridPreview"
        Log.d("SessionCheck", "Came from GridPreview: $cameFromGridPreview")

        if (!cameFromGridPreview) {
            fileViewModel.endViewSession()
            fileViewModel.endScanSession()
            Log.d("SessionCheck", "Reset View and Scan Sessions!")
        }

    }

    BackHandler(enabled = true) {
        if (capturedImages.isNotEmpty()) {
            showExitDialog = true  // Ask before exit
        } else {
            if (!fileViewModel.isScanSessionActive) { // Cleanup only if session is not active
                outputDirectory.deleteRecursively()
            }
            navController.popBackStack()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(2.dp, 0.dp),
        floatingActionButton = {
            if (capturedImages.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f) // Takes 95% of screen width (adjust as needed)
                        .wrapContentWidth(Alignment.Start) // Align to left
                ) {
                    ImagePreviewRow(
                        imageUris = capturedImages.map { Uri.fromFile(it) },
                        onDelete = { file ->
                            capturedImages.remove(file)
                            file.delete()
                        }
                    )
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                    ) {
                        IconButton(onClick = {
                            if (capturedImages.isNotEmpty()) {
                                showExitDialog = true  // Ask before exit
                            } else {
                                if (!fileViewModel.isScanSessionActive) { // Cleanup only if session is not active
                                    outputDirectory.deleteRecursively()
                                }
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                modifier = Modifier.size(35.dp),
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit Camera",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarColors(
                    containerColor = Color.DarkGray,
                    scrolledContainerColor = Color.Transparent,
                    navigationIconContentColor = Color.Transparent,
                    titleContentColor = Color.Transparent,
                    actionIconContentColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = {
                        isTorchOn = !isTorchOn
                        imageCapture.camera?.cameraControl?.enableTorch(isTorchOn)
                    }) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = "Toggle Flash",
                            tint = if (isTorchOn) Color.Yellow else Color.White
                        )
                    }
                    // Grid Toggle Button
                    IconButton(onClick = { isGridEnabled = !isGridEnabled }) {
                        Icon(
                            imageVector = if (isGridEnabled) Icons.Filled.GridOn else Icons.Filled.GridOff,
                            contentDescription = "Toggle Grid",
                            tint = if (isGridEnabled) Color.Green else Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .padding(0.dp)
                ) {
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .background(Color.DarkGray)
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.DarkGray),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionBtn(onClick = {
                            galleryLauncher.launch("image/*") // Open the gallery and allow image selection
                        }, text = "Gallery", size = 35)
                        ActionBtn(onClick = {
                            pdfLauncher.launch("application/pdf")
                            /* Open Pdf Selector */
                        }, text = "PDF", size = 35)
                    }
                    CaptureButton {
                        capturePhoto(imageCapture, outputDirectory, capturedImages, context)
                    }
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center) {
                        ActionBtn(
                            onClick = {
                                if (capturedImages.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "No images to proceed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // 🔹 Only add new images to the session
                                    fileViewModel.addScannedImages(capturedImages)
                                    fileViewModel.setSessionFolderName(outputDirectory.absolutePath)
                                    fileViewModel.startScanSession()
                                    proceedClicked = true
                                    showExitDialog = false
                                    navController.navigate("edit") // ✅ Move to Edit Screen
                                }
                            },
                            text = "Proceed", size = 50
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Avoids overlap with topBar and bottomBar
                .border(2.dp, Color.Black)
        ) {
            CameraPreviewScreen(imageCapture = imageCapture, isGridEnabled = isGridEnabled)
        }
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirmExit = {
                capturedImages.forEach { it.delete() }
                outputDirectory.deleteRecursively()
                fileViewModel.endScanSession()
                showExitDialog = false
                // Ensure state update before navigation
                navController.popBackStack()
            },
            onDismiss = { showExitDialog = false }
        )
    }
}

fun


        convertPdfToImages(context: Context, uri: Uri) {

}

fun copyImageToFile(context: Context, uri: Uri, destinationFile: File) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(destinationFile)
        inputStream?.copyTo(outputStream)
        outputStream.close()
        inputStream?.close()
    } catch (e: Exception) {
        Log.e("GallerySelection", "Error copying image", e)
    }
}

@Composable
fun ExitConfirmationDialog(onConfirmExit: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exit without saving?") },
        text = { Text("You have unsaved images. Do you want to discard them?") },
        confirmButton = {
            Button(onClick = onConfirmExit) {
                Text("Yes, Discard")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CameraPreviewScreen(imageCapture: ImageCapture, isGridEnabled: Boolean) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    Log.d("CameraX", "Camera provider loaded")

                    val preview = Preview.Builder().build().also { preview: Preview ->
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                    Log.d("CameraX", "Camera bound successfully")
                } catch (e: Exception) {
                    Log.e("CameraX", "Failed to bind camera", e)
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
    if (isGridEnabled) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val thirdX = width / 3
            val thirdY = height / 3

            drawLine(
                Color.White,
                Offset(thirdX, 0f),
                Offset(thirdX, height),
                strokeWidth = 2f
            )
            drawLine(
                Color.White,
                Offset(thirdX * 2, 0f),
                Offset(thirdX * 2, height),
                strokeWidth = 2f
            )
            drawLine(
                Color.White,
                Offset(0f, thirdY),
                Offset(width, thirdY),
                strokeWidth = 2f
            )
            drawLine(
                Color.White,
                Offset(0f, thirdY * 2),
                Offset(width, thirdY * 2),
                strokeWidth = 2f
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
fun capturePhoto(
    imageCapture: ImageCapture,
    outputDirectory: File,
    capturedImages: MutableList<File>,
    context: Context,
) {
    if (!outputDirectory.exists()) outputDirectory.mkdirs()

    val photoFile = File(outputDirectory, "IMG_${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    val executor = ContextCompat.getMainExecutor(context)

    imageCapture.takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            CoroutineScope(Dispatchers.IO).launch {
                val correctedBitmap = ImageUtils.getCorrectlyOrientedBitmap(photoFile)

                FileOutputStream(photoFile).use { out ->
                    correctedBitmap.compress(Bitmap.CompressFormat.JPEG, 67, out) // Change to JPEG
                }

                correctedBitmap.recycle()

                withContext(Dispatchers.Main) { capturedImages.add(photoFile) }
            }
            Log.d("CameraX", "✅ Image saved: ${photoFile.absolutePath}")
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("CameraX", "❌ Image capture failed", exception)
        }
    })
}

@Composable
fun CaptureButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .background(Color(0xFFE3F2FD), shape = CircleShape)
            .border(3.dp, Color.Gray, shape = CircleShape)
            .clickable { onClick() }
    )
}

@Composable
fun ImagePreviewRow(imageUris: List<Uri>, onDelete: (File) -> Unit) {
    if (imageUris.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .background(Color.Transparent)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            imageUris.asReversed().forEachIndexed { index, uri ->
                Box(
                    modifier = Modifier
                        .height(90.dp)
                        .width(80.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Image(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .border(2.dp, Color.White, shape = RoundedCornerShape(8.dp))
                            .clip(
                                RoundedCornerShape(8.dp)
                            ),
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )

                    // 🔹 Image Numbering (Top Left)
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .background(Color.Red, shape = CircleShape)
                            .border(1.dp, Color.White, shape = CircleShape)
                            .align(Alignment.TopStart),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${imageUris.size - index}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 🔹 Delete Button (Top Right)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black, shape = CircleShape)
                            .border(1.dp, Color.White, shape = CircleShape)
                            .clickable { onDelete(File(uri.path!!)) }
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "X",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

fun getOutputDirectory(context: Context): File {
    val mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val sessionFolder =
        File(
            mediaDir,
            "GetPYQ_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}"
        )
    if (!sessionFolder.exists()) sessionFolder.mkdirs()
    return sessionFolder
}

fun deleteSessionFolder(folder: File) {
    folder.listFiles()?.forEach { it.delete() }
    folder.delete()
    Log.d("CameraX", "Deleted session folder: ${folder.absolutePath}")
}

@Composable
fun ActionBtn(text: String, onClick: () -> Unit, size: Int) {
    val sizer = size.dp
    val icon = when (text.lowercase()) {
        "pdf" -> Icons.Filled.PictureAsPdf
        "gallery" -> Icons.Filled.Image
        "proceed" -> Icons.Filled.LibraryAddCheck
        else -> Icons.Filled.Air // Default icon
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, // Align elements naturally
        modifier = Modifier.padding(0.dp)
    )
    {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(sizer + 12.dp),
            colors = IconButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.White
            )// Adjust size as needed
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(sizer)
            )
        }
        Text(
            text,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
