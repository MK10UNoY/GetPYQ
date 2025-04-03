package com.infomanix.getpyq.ui.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowCircleLeft
import androidx.compose.material.icons.filled.ArrowCircleRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.infomanix.getpyq.ui.viewmodels.FileViewModel
import com.infomanix.getpyq.utils.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EditImageScreen(navController: NavController, fileViewModel: FileViewModel) {
    Log.d("CameraX", fileViewModel.getSessionFolderName().toString().substringAfterLast("/"))
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    val imageList = remember {
        if (fileViewModel.isViewSessionActive) fileViewModel.newlyScannedImages
        else fileViewModel.imageFiles
    }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf<Bitmap.() -> Bitmap>({ this }) }

    if (imageList.isEmpty()) return

    val imageFile = imageList[currentIndex]
    var bitmap by remember { mutableStateOf(loadBitmap(imageFile.path)) }
    var filteredBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Update bitmap when currentIndex changes
    LaunchedEffect(currentIndex) {
        rotationAngle = 0f // Reset rotation
        bitmap = loadBitmap(imageList[currentIndex].path)
        filteredBitmap = null // Reset filter when changing images
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(Color(0xFF222222)),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Edit Image",
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp),
            color = Color.White
        )

        // **Filter Preview Row**
        FilterPreviewRow(bitmap) { filter ->
            selectedFilter = filter
            bitmap?.let { originalBitmap ->
                filteredBitmap = selectedFilter(originalBitmap) // 🔹 Apply filter in memory
            }
        }

        // **Display Image with Selected Filter**
        AnimatedContent(
            targetState = currentIndex to rotationAngle,
            transitionSpec = { slideInHorizontally { it } togetherWith slideOutHorizontally { -it } },
            label = "Image Transition"
        ) { (_, angle) ->
            (filteredBitmap ?: bitmap)?.asImageBitmap()?.let { imageBitmap ->
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Editing Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction = 0.7f)
//                        .weight(1f)
                        .padding(16.dp)
                        .graphicsLayer(rotationZ = angle)
                )
            }

        }

        // **Crop & Navigation Controls**
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                ActionButton("No Crop") { /* Implement No Crop */ }
                ActionButton("Auto Crop") { /* Implement Auto Crop */ }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .padding(12.dp)
            ) {
                ActionButton("Previous") {
                    if (currentIndex > 0) {
                        currentIndex--
                    } else {
                        navController.popBackStack()
                    }
                }
                ActionButton("Left") {
                    ImageUtils.rotateAndSaveImage2(imageFile, -90f)
                    bitmap = loadBitmap(imageFile.path)
                }
                ActionButton("Right") {
                    ImageUtils.rotateAndSaveImage2(imageFile, 90f)
                    bitmap = loadBitmap(imageFile.path)
                }
                ActionButton("Next") {
                    // 🔹 Save the filter **only** when moving to the next image
                    filteredBitmap?.let { saveFilteredImage(imageFile, it) }
                    if (currentIndex < imageList.size - 1) {
                        currentIndex++
                    } else {
                        navController.navigate("gridPreview") {
                            fileViewModel.isScanSessionActive = false
                            popUpTo("camera") { inclusive = true }
                        }
                    }
                }
            }
        }
    }
}

// **Helper function to load a bitmap from file**
private fun loadBitmap(path: String): Bitmap? {
    return BitmapFactory.decodeFile(path)
}

// **Filter Preview Row**
@Composable
fun FilterPreviewRow(originalBitmap: Bitmap?, onFilterSelected: (Bitmap.() -> Bitmap) -> Unit) {
    val filters = listOf(
        "None" to { bitmap: Bitmap -> bitmap },
        "Gray" to { bitmap: Bitmap -> bitmap.toGrayscale() },
        "Sepia" to { bitmap: Bitmap -> bitmap.toSepia() },
        "Magic 1" to { bitmap: Bitmap -> bitmap.toMagicWhite1() },
        "Magic 2" to { bitmap: Bitmap -> bitmap.toMagicWhite2() }
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        items(filters) { (name, filter) ->
            originalBitmap?.let { bmp ->
                val filteredBitmap = remember(filter) { filter(bmp) }
                FilterThumbnail(name, filteredBitmap) { onFilterSelected(filter) }
            }
        }
    }
}

// **Thumbnail for Each Filter**
@Composable
fun FilterThumbnail(name: String, bitmap: Bitmap, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick() }
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = name,
            modifier = Modifier
                .size(60.dp)
                .background(Color.Gray)
        )
        Text(text = name, fontSize = 12.sp, color = Color.White)
    }
}

// **Image Filter Functions**
fun Bitmap.toGrayscale(): Bitmap {
    val bmp = copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(bmp)
    val paint = Paint()
    val cm = ColorMatrix()
    cm.setSaturation(0f) // Convert to grayscale
    paint.colorFilter = ColorMatrixColorFilter(cm)
    canvas.drawBitmap(this, 0f, 0f, paint)
    return bmp
}

fun Bitmap.toSepia(): Bitmap {
    val bmp = copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(bmp)
    val paint = Paint()
    val cm = ColorMatrix()
    cm.set(floatArrayOf(
        0.393f, 0.769f, 0.189f, 0f, 0f,
        0.349f, 0.686f, 0.168f, 0f, 0f,
        0.272f, 0.534f, 0.131f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f
    ))
    paint.colorFilter = ColorMatrixColorFilter(cm)
    canvas.drawBitmap(this, 0f, 0f, paint)
    return bmp
}
// **Save the filtered image when "Next" is pressed**
private fun saveFilteredImage(imageFile: File, filteredBitmap: Bitmap) {
    CoroutineScope(Dispatchers.IO).launch {
        FileOutputStream(imageFile).use { out ->
            filteredBitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
        }
    }
}

fun Bitmap.invertColors(): Bitmap {
    val bmp = copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(bmp)
    val paint = Paint()
    val cm = ColorMatrix()
    cm.set(floatArrayOf(
        -1f, 0f, 0f, 0f, 255f,
        0f, -1f, 0f, 0f, 255f,
        0f, 0f, -1f, 0f, 255f,
        0f, 0f, 0f, 1f, 0f
    ))
    paint.colorFilter = ColorMatrixColorFilter(cm)
    canvas.drawBitmap(this, 0f, 0f, paint)
    return bmp
}
fun Bitmap.toMagicWhite1(): Bitmap {
    val bmp = copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(bmp)
    val paint = Paint()
    val cm = ColorMatrix()

    // Increase contrast & brightness while keeping text sharp
    cm.set(floatArrayOf(
        1.5f, 0f, 0f, 0f, 40f,  // Red
        0f, 1.5f, 0f, 0f, 40f,  // Green
        0f, 0f, 1.5f, 0f, 40f,  // Blue
        0f, 0f, 0f, 1f, 0f      // Alpha
    ))

    paint.colorFilter = ColorMatrixColorFilter(cm)
    canvas.drawBitmap(this, 0f, 0f, paint)

    return bmp
}

fun Bitmap.toMagicWhite2(): Bitmap {
    val bmp = copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(bmp)
    val paint = Paint()
    val cm = ColorMatrix()

    // High contrast B&W conversion, keeping only essential details
    cm.set(floatArrayOf(
        3f, 3f, 3f, 0f, -450f,  // Red
        3f, 3f, 3f, 0f, -450f,  // Green
        3f, 3f, 3f, 0f, -450f,  // Blue
        0f, 0f, 0f, 1f, 0f      // Alpha
    ))

    paint.colorFilter = ColorMatrixColorFilter(cm)
    canvas.drawBitmap(this, 0f, 0f, paint)

    return bmp
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit) {
    val icon = when (text.lowercase()) {
        "left" -> Icons.Filled.RotateLeft
        "right" -> Icons.Filled.RotateRight
        "next" -> Icons.Filled.ArrowCircleRight
        "previous" -> Icons.Filled.ArrowCircleLeft
        "no crop" -> Icons.Filled.Crop
        "auto crop" -> Icons.Filled.AutoAwesome
        else -> Icons.Filled.Air // Default icon
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(60.dp),
        ) {
            Icon(imageVector = icon, contentDescription = text, Modifier.size(40.dp))
        }
        Text(text, fontSize = 12.sp, color = Color.White)
    }
}

