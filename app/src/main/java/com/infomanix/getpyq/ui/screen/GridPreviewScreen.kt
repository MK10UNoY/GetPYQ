package com.infomanix.getpyq.ui.screen

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Airplay
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.infomanix.getpyq.ui.fragments.RenameFolderBottomSheet
import com.infomanix.getpyq.ui.viewmodels.FileViewModel
import com.infomanix.getpyq.utils.PdfUtils
import java.io.File
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.CheckCircle

@OptIn(ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class)
@Composable
fun GridPreviewScreen(navController: NavController, fileViewModel: FileViewModel) {
    var pdfFile = fileViewModel.pdfFile
    fileViewModel.setViewSession()
    val folderAddress by remember { mutableStateOf(fileViewModel.getSessionFolderName()) } // Dynamic if needed
    val imageList by remember { mutableStateOf(fileViewModel.imageFiles) }

    //val folderName by remember { derivedStateOf { fileViewModel.getSessionFolderName()?.substringAfterLast("/") ?: "Unnamed" } }
    val folderName by fileViewModel.folderName.collectAsStateWithLifecycle()  // ✅ Auto-updates UI

    var currentFolderName by remember { mutableStateOf("") } // Track UI state
    var showRenameBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var multiSelectMode by remember { mutableStateOf(false) } // Track multi-select mode
    var selectedImages by remember { mutableStateOf(setOf<String>()) } // Track selected images


    // 🔥 Ensure UI updates when folderName changes in ViewModel
    LaunchedEffect(folderName) {
        fileViewModel.updateImagePaths(folderName)
        currentFolderName = folderName.toString().trim().substringAfterLast("/")
    }
    BackHandler(enabled = true) {
        fileViewModel.endViewSession()
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showRenameBottomSheet = true }
                        ) {
                            Text(
                                text = currentFolderName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1, // Limit to one line
                                overflow = TextOverflow.Ellipsis // Add "..." if text is too long
                            )
                            Text(
                                text = "click to rename",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            modifier = Modifier.size(30.dp),
                            onClick = {
                                Toast.makeText(context, "Auto rename Started", Toast.LENGTH_SHORT).show()
                                fileViewModel.autoRenameFolder(context) }
                        ) {
                            Icon(Icons.Filled.Autorenew, contentDescription = "Auto Rename")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (multiSelectMode) { // Show actions only in multi-select mode
                        IconButton(onClick = {
                            if (selectedImages.isNotEmpty()) {
                                selectedImages.forEach { path -> File(path).delete() }
                                fileViewModel.updateImagePaths(folderName)
                                Toast.makeText(context, "Selected images deleted", Toast.LENGTH_SHORT).show()
                                multiSelectMode = false
                                selectedImages = emptySet()
                            } else {
                                Toast.makeText(context, "No images selected", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Delete Selected")
                        }
                    } else { // Show default actions when not in multi-select mode
                        IconButton(onClick = {
                            pdfFile = PdfUtils.compileImagesToPdf(context, imageList, currentFolderName)
                                ?: return@IconButton
                            pdfFile?.let {
                                fileViewModel.pdfFile = it
                                PdfUtils.openPdfWithExternalApp2(context, it)
                            }
                        }) {
                            Icon(imageVector = Icons.Filled.Download, contentDescription = "Download")
                        }
                        IconButton(onClick = { /* More Options */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                    }
                } //
            )
        },
        bottomBar = {
            if (!multiSelectMode) {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        BottomBarButton("Add Page", Icons.Default.Add) {
                            Log.d("AddPage", fileViewModel.isViewSessionActive.toString())
                            navController.navigate("camera")
                        }
                        BottomBarButton("Share", Icons.Default.Share) { /* Share Logic */ }
                        //BottomBarButton("Edit", Icons.Default.Edit) { /* Edit Logic */ }
                        //BottomBarButton("Search", Icons.Default.Search) { /* Search Logic */ }
                        BottomBarButton("View PDF", Icons.Filled.Airplay) {
                            pdfFile?.let { PdfUtils.openPdfWithExternalApp2(context, it) }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text(
                text = "${imageList.size} Pages | 470.9 KB",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(bottom = 56.dp)
            ) {
//                itemsIndexed(imageList) { index, imageFile ->
//                    Column(
//                        modifier = Modifier
//                            .padding(4.dp)
//                            .background(
//                                if (multiSelectMode && selectedImages.contains(imageFile.path)) {
//                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
//                                }else{
//                           Color.LightGray}, shape = RoundedCornerShape(8.dp))
//                            .combinedClickable( // Use combinedClickable for long press
//                                onClick = {
//                                    if (multiSelectMode) {
//                                        selectedImages = if (selectedImages.contains(imageFile.path)) {
//                                            selectedImages - imageFile.path // Deselect on click in multi-select
//                                        } else {
//                                            selectedImages + imageFile.path // Select on click in multi-select
//                                        }
//                                    }
//                                },
//                                onLongClick = {
//                                    multiSelectMode = true // Activate multi-select on long press
//                                    selectedImages = setOf(imageFile.path) // Select current item
//                                }
//                            )
//                    ) {
//                        Image(
//                            bitmap = BitmapFactory.decodeFile(imageFile.path).asImageBitmap(),
//                            contentDescription = "Preview Image",
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(150.dp)
//                        )
//                        Text(
//                            text = (index + 1).toString(),
//                            fontSize = 14.sp,
//                            color = Color.White,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .background(Color.Gray)
//                                .padding(4.dp),
//                            textAlign = TextAlign.Center
//                        )
//                    }
//                }//
                itemsIndexed(imageList) { index, imageFile ->
                    Box( // Use Box to overlay items
                        modifier = Modifier
                            .padding(4.dp)
                            .background(
                                if (multiSelectMode && selectedImages.contains(imageFile.path)) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                } else {
                                    Color.LightGray
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .combinedClickable(
                                onClick = {
                                    if (multiSelectMode) {
                                        selectedImages = if (selectedImages.contains(imageFile.path)) {
                                            selectedImages - imageFile.path
                                        } else {
                                            selectedImages + imageFile.path
                                        }
                                    }
                                },
                                onLongClick = {
                                    multiSelectMode = true
                                    selectedImages = setOf(imageFile.path)
                                }
                            )
                    ) {
                        Column { // Put image and text in a Column inside the Box
                            Image(
                                bitmap = BitmapFactory.decodeFile(imageFile.path).asImageBitmap(),
                                contentDescription = "Preview Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            )
                            Text(
                                text = (index + 1).toString(),
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Gray)
                                    .padding(4.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        if (multiSelectMode && selectedImages.contains(imageFile.path)) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary, // Adjust color as needed
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.TopEnd) // Position the icon
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    // Show rename bottom sheet
    if (showRenameBottomSheet) {
        folderAddress?.let { currentFolderPath ->
            RenameFolderBottomSheet(
                fileViewModel = fileViewModel,
                initialFolderName = currentFolderName,
                currentFolderPath = currentFolderPath, // ✅ Pass full folder path
                onDismiss = { showRenameBottomSheet = false },
                onRenameSuccess = { newName ->
                    if (fileViewModel.renameFolder(newName, currentFolderPath)) {
                        fileViewModel.setSessionFolderName(newName) // ✅ Update UI
                        Log.d("RenameDebug", "After Rename = $currentFolderName")
                        showRenameBottomSheet = false // ✅ Close rename modal
                    }
                }
            )
        }
    }
}

@Composable
fun BottomBarButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp))
        Text(text = label, fontSize = 12.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun PdfPreviewScreen() {
    val navController = rememberNavController()
    GridPreviewScreen(navController, FileViewModel())
}