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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.infomanix.getpyq.data.UserState
import com.infomanix.getpyq.repository.Upload
import com.infomanix.getpyq.ui.viewmodels.FileViewModel
import com.infomanix.getpyq.ui.viewmodels.UploadTrackingViewModel
import com.infomanix.getpyq.ui.viewmodels.UserViewModel
import com.infomanix.getpyq.utils.UploadUtils
import com.infomanix.getpyq.utils.UploadUtils.mapMonthToNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderListScreen(
    navController: NavController,
    fileViewModel: FileViewModel,
    userViewModel: UserViewModel,
    rootPath: String,
    uploadTrackingViewModel: UploadTrackingViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = remember{ SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val pdfFiles = remember { mutableStateListOf<File>().apply { addAll(getFolders(rootPath)) } }
    val uploadedPdfs by uploadTrackingViewModel.uploadList.observeAsState(emptyList())

    val uploaderEmail =
        when (val userState = userViewModel.userState.collectAsStateWithLifecycle().value) {
            is UserState.Uploader -> userState.useremail
            else -> null
        }

    LaunchedEffect(key1 = uploaderEmail) {
        Log.d("Upload", "Entered screen")
        if (!uploaderEmail.isNullOrEmpty()) {
            uploadTrackingViewModel.fetchUploads(uploaderEmail)
        }
        Log.d("Upload", "Entered screen")
    }
    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar { scope.launch { drawerState.open() } }
        // ✅ Modal Drawer that slides over content
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(uploadedPdfs)
            }
        ) {

            // ✅ Top bar remains visible
            Scaffold(
                snackbarHost = { SnackbarHost(scaffoldState) }, // ✅ Attach SnackbarHost
                modifier = Modifier.weight(1f),
                content = { padding ->
                    LazyColumn(
                        contentPadding = padding,
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
                                FolderItem(
                                    folder,
                                    onClick = {
                                        val folderPath = folder.absolutePath
                                        val images = folder.listFiles()
                                            ?.filter {
                                                it.extension in listOf(
                                                    "jpg",
                                                    "jpeg",
                                                    "png"
                                                )
                                            }
                                            ?: emptyList()
                                        fileViewModel.isViewSessionActive = true
                                        fileViewModel.setSessionFolderName(folderPath)
                                        fileViewModel.imageFiles = images
                                        navController.navigate("gridPreview")
                                    },
                                    onDelete = {
                                        folder.deleteRecursively()
                                        pdfFiles.remove(folder)
                                    },
                                    scaffoldState = scaffoldState, // ✅ Pass snackbar state
                                    scope = scope // ✅ Pass coroutine scope
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

// 🔹 Top App Bar with Drawer Button
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        title = { Text("Scans Folder", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.WorkHistory, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = { /* Optional action */ }) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF6EB8FA),
            titleContentColor = Color.White
        )
    )
}

@Composable
fun DrawerContent(uploadedPdfs: List<Upload>) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val topBarHeight = 0.dp
    val sidePadding = 0.dp
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = topBarHeight, start = sidePadding, end = sidePadding)
            .width(screenWidth * 0.7f)
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Text(
            "Uploaded PDFs",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(8.dp)
        )

        if (uploadedPdfs.isEmpty()) {
            Text(
                "No uploads yet.",
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            LazyColumn {
                items(uploadedPdfs.reversed()) { pdf ->
                    val fileName = pdf.filepath as? String ?: "Unknown.pdf"
                    UploadedPdfItem(fileName)
                }
            }
        }
    }
}

// 🔹 Uploaded PDF Item
@Composable
fun UploadedPdfItem(fileName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F3F3))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CloudDone, contentDescription = "Uploaded PDF", tint = Color.Blue)
            Text(
                text = fileName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@SuppressLint("SimpleDateFormat")
@Composable
fun FolderItem(
    folder: File,
    onClick: () -> Unit,
    onDelete: (File) -> Unit,
    uploadTrackingViewModel: UploadTrackingViewModel = hiltViewModel(),
    scaffoldState: androidx.compose.material3.SnackbarHostState, // ✅ Pass snack bar state
    scope:CoroutineScope // ✅ Pass coroutine scope
) {
    val context = LocalContext.current
    val firstImage =
        folder.listFiles()?.firstOrNull { it.extension in listOf("jpg", "jpeg", "png") }
    val creationDate = SimpleDateFormat("dd MMM yyyy").format(folder.lastModified())

    var showMenu by remember { mutableStateOf(false) }
    val uploadProgress by UploadUtils.uploadProgress.collectAsState()
    val isUploading by UploadUtils.isUploading.collectAsState()
    val uploadDone by UploadUtils.uploadDone.collectAsState()
    var showReuploadDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Column {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Delete") }, onClick = {
                                onDelete(folder)
                                showMenu = false
                            })
                        }
                        IconButton(
                            onClick = {
                                if (!isValidFolderName(folder.name) { errorMsg ->
                                        scope.launch {
                                            scaffoldState.showSnackbar(errorMsg, duration = SnackbarDuration.Short) // ✅ Show specific error
                                        }
                                    }) return@IconButton
                                if (uploadDone) {
                                    showReuploadDialog = true
                                } else {
                                    // ✅ Launch in CoroutineScope
                                    CoroutineScope(Dispatchers.IO).launch {
                                        UploadUtils.startUpload(
                                            folder,
                                            context,
                                            uploadTrackingViewModel = uploadTrackingViewModel,
                                            onProgress = { progress ->
                                            },
                                            onComplete = {
                                            }
                                        )
                                    }
                                }
                            },
                            enabled = !isUploading
                        ) {
                            Icon(
                                imageVector = when {
                                    isUploading -> Icons.Default.CloudSync
                                    uploadDone -> Icons.Default.CloudDone
                                    else -> Icons.Default.CloudUpload
                                },
                                contentDescription = "Upload to Cloud",
                                tint = when {
                                    isUploading -> Color.Red
                                    uploadDone -> Color.Green
                                    else -> Color.Black
                                }
                            )
                        }
                    }
                }
            }
            if (isUploading) {
                LinearProgressIndicator(
                    progress = uploadProgress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }

    if (showReuploadDialog) {
        AlertDialog(
            onDismissRequest = { showReuploadDialog = false },
            title = { Text("Re-upload Confirmation") },
            text = { Text("Do you want to re-upload this folder?") },
            confirmButton = {
                TextButton(onClick = {
                    showReuploadDialog = false
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            UploadUtils.startUpload(
                                folder,
                                context,
                                uploadTrackingViewModel = uploadTrackingViewModel,
                                onProgress = { progress -> /* Direct observation via uploadProgress */ },
                                onComplete = { /* Direct observation via uploadDone */ }
                            )
                        } catch (e: Exception) {
                            scaffoldState.showSnackbar("Upload failed: ${e.message}", duration = SnackbarDuration.Short)
                        }
                    }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReuploadDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}
fun isValidFolderName(folderName: String, onError: (String) -> Unit): Boolean {
    val parts = folderName.split("_")

    if (parts.size != 5) {
        onError("❌ Folder name should have 5 parts: Course_Subject_Sem_ExamType_Month-Year")
        return false
    }

    val courseCode = parts[0]
    val subject = parts[1]
    val semester = parts[2]
    val examType = parts[3]
    val monthYear = parts[4]

    // Check if semester contains numbers
    if (semester.filter { it.isDigit() }.isEmpty()) {
        onError("❌ Semester should contain a number, e.g., Sem4")
        return false
    }

    // Check if Month-Year is valid
    val monthYearParts = monthYear.split("-")
    if (monthYearParts.size != 2) {
        onError("❌ Month-Year should be formatted as 'March-2025'")
        return false
    }

    val month = monthYearParts[0]
    val year = monthYearParts[1]

    if (!mapMonthToNumber(month).isValidMonth()) {
        onError("❌ Invalid month '$month'. Use full month names (e.g., March).")
        return false
    }

    if (year.length != 4 || !year.all { it.isDigit() }) {
        onError("❌ Year should be a 4-digit number, e.g., 2025")
        return false
    }

    return true
}

// ✅ Helper function to check valid month
fun Int.isValidMonth() = this in 1..12

fun getFolders(rootPath: String): List<File> {
    return File(rootPath).listFiles()?.filter { it.isDirectory }?.reversed() ?: emptyList()
}