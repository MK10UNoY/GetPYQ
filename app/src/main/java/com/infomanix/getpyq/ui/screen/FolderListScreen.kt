package com.infomanix.getpyq.ui.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.infomanix.getpyq.ui.viewmodels.FileViewModel
import java.io.File

@Composable
fun FolderListScreen(navController: NavController, fileViewModel: FileViewModel, rootPath: String) {
    val pdfFiles = remember { mutableStateListOf<File>().apply { addAll(getFolders(rootPath)) } }

    Scaffold(
        topBar = { AppTopBar() },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (pdfFiles.isEmpty()) {
                item {
                    Text(
                        text = "No PDFs found",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                items(pdfFiles) { folder ->
                    FolderItem(folder,
                        onClick = {
                            val folderPath = folder.absolutePath // ✅ Get full folder path
                            val images = folder.listFiles()
                                ?.filter { it.extension in listOf("jpg", "jpeg", "png") }
                                ?: emptyList()

                            fileViewModel.isViewSessionActive = true // ✅ Set viewing session active
                            fileViewModel.setSessionFolderName(folderPath) // ✅ Store full folder path
                            fileViewModel.imageFiles = images // ✅ Save images

                            navController.navigate("gridPreview")
                        },
                        onDelete = {
                            folder.deleteRecursively() // Delete folder and contents
                            pdfFiles.remove(folder) // Refresh list
                        }
                    )
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar() {
    TopAppBar(
        title = { Text("Scans Folder", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        actions = {
            IconButton(onClick = { /* Search */ }) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF6EB8FA),
            titleContentColor = Color.White
        )
    )
}

@SuppressLint("SimpleDateFormat")
@Composable
fun FolderItem(folder: File, onClick: () -> Unit, onDelete: (File) -> Unit) {
    val firstImage =
        folder.listFiles()?.firstOrNull { it.extension in listOf("jpg", "jpeg", "png") }
    val creationDate = java.text.SimpleDateFormat("dd MMM yyyy").format(folder.lastModified())
    var showMenu by remember { mutableStateOf(false) }
    Log.v("FolderItem", "Rendering folder: $folder")

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = rememberAsyncImagePainter(firstImage?.absolutePath ?: ""),
                contentDescription = "Folder Thumbnail",
                modifier = Modifier
                    .size(80.dp)
                    .background(Transparent, shape = RoundedCornerShape(0.5f))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 10.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    folder.name,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(creationDate, color = Color.Gray, fontSize = 12.sp)
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Delete") }, onClick = {
                        onDelete(folder)
                        showMenu = false
                    })
                }
            }
        }
    }
}

fun getPdfs(rootPath: String): List<File> {
    return File(rootPath).listFiles()?.filter { it.extension == "pdf" } ?: emptyList()
}

fun getFolders(rootPath: String): List<File> {
    return File(rootPath).listFiles()?.filter { it.isDirectory }?.reversed() ?: emptyList()
}
