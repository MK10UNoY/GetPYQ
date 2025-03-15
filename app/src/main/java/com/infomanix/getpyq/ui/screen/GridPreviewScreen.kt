package com.infomanix.getpyq.ui.screen

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.infomanix.getpyq.ui.fragments.RenameFolderBottomSheet
import com.infomanix.getpyq.ui.viewmodels.FileViewModel
import com.infomanix.getpyq.utils.PdfUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridPreviewScreen(navController: NavController, fileViewModel: FileViewModel) {
    var pdfFile = fileViewModel.pdfFile
    val folderAddress by remember { mutableStateOf(fileViewModel.getSessionFolderName()) } // Dynamic if needed
    val imageList by remember { mutableStateOf(fileViewModel.imageFiles) }


    val folderName by remember { derivedStateOf { fileViewModel.getSessionFolderName()?.substringAfterLast("/") ?: "Unnamed" } }

    var showRenameBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.clickable { showRenameBottomSheet = true }) {
                        if (folderName != null) {
                            Text(
                                text = folderName!!,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(text = "click to rename", fontSize = 14.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Sharp.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        pdfFile = PdfUtils.compileImagesToPdf(context, imageList, folderName) ?: return@IconButton
                        pdfFile?.let { fileViewModel.pdfFile = it }// Save PDF reference in ViewModel
                    }) {
                        Icon(imageVector = Icons.Filled.Download, contentDescription = "Download")
                    }
                    IconButton(onClick = { /* More Options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BottomBarButton("Add Page", Icons.Default.Add) { /* Add Page Logic */ }
                    BottomBarButton("Share", Icons.Default.Share) { /* Share Logic */ }
                    //BottomBarButton("Edit", Icons.Default.Edit) { /* Edit Logic */ }
                    //BottomBarButton("Search", Icons.Default.Search) { /* Search Logic */ }
                    BottomBarButton("View PDF", Icons.Filled.Airplay) {
                        pdfFile?.let { openPdfWithExternalApp(context, it) }
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
                itemsIndexed(imageList) { index, imageFile ->
                    Column(
                        modifier = Modifier
                            .padding(4.dp)
                            .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                    ) {
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
                }
            }
        }
    }
    // Show rename bottom sheet
    if (showRenameBottomSheet) {
        folderAddress?.let { currentFolderPath ->
            RenameFolderBottomSheet(
                fileViewModel = fileViewModel,
                initialFolderName = folderName ?: "Untitled",
                currentFolderPath = currentFolderPath, // ✅ Pass full folder path
                onDismiss = { showRenameBottomSheet = false },
                onRenameSuccess = { newName ->
                    if (fileViewModel.renameFolder(newName, currentFolderPath)) {
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

fun openPdfWithExternalApp(context: Context, pdfFile: File) {
    try {
        val uri: Uri =
            FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant read access
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Start as a new task
        }

        context.startActivity(Intent.createChooser(intent, "Open PDF with"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Preview(showBackground = true)
@Composable
fun PdfPreviewScreen() {
    val navController = rememberNavController()
    GridPreviewScreen(navController, FileViewModel())
}