package com.infomanix.getpyq.ui.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EditImageScreen(navController: NavController, fileViewModel: FileViewModel) {
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    val imageList = fileViewModel.imageFiles
    var currentIndex by remember { mutableIntStateOf(0) }

    if (imageList.isEmpty()) return

    val imageFile = imageList[currentIndex]
    var bitmap by remember { mutableStateOf(loadBitmap(imageFile.path)) }

    // Update bitmap when currentIndex changes
    LaunchedEffect(currentIndex) {
        rotationAngle = 0f // Reset rotation when changing images
        bitmap = loadBitmap(imageList[currentIndex].path)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(Color(0xFF222222)),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Crop",
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp)
        )

        // Swipe Animation using AnimatedContent
        AnimatedContent(
            targetState = currentIndex to rotationAngle,
            transitionSpec = {
                (slideInHorizontally { it } togetherWith slideOutHorizontally { -it })
            },
            label = "Image Transition"
        ) { (index, angle) ->
            bitmap?.asImageBitmap()?.let { imageBitmap ->
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Editing Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .graphicsLayer(rotationZ = angle) // Apply rotation visually
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                ActionButton("No Crop") { /* Implement No Crop */ }
                ActionButton("Auto Crop") { /* Implement Auto Crop */ }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().background(Color.DarkGray).padding(12.dp)
            ) {
                ActionButton("Previous") {
                    if (currentIndex > 0) {
                        currentIndex--
                    } else {
                        navController.popBackStack()
                    }
                }
                ActionButton("Left") {
                    rotationAngle -= 90f
                    ImageUtils.rotateAndSaveImage2(imageFile, -90f) // Rotate and save image
                    bitmap = loadBitmap(imageFile.path) // Reload the updated image
                }
                ActionButton("Right") {
                    rotationAngle += 90f
                    ImageUtils.rotateAndSaveImage2(imageFile, 90f)
                    bitmap = loadBitmap(imageFile.path)
                }
                ActionButton("Next") {
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

// Helper function to load a bitmap from file
private fun loadBitmap(path: String): Bitmap? {
    return BitmapFactory.decodeFile(path)
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
            colors = IconButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.White
            )
        ) {
            Icon(imageVector = icon, contentDescription = text, Modifier.size(40.dp))
        }
        Text(
            text,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }

}
