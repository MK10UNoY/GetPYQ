package com.infomanix.getpyq.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.infomanix.getpyq.data.UploadMetadata
import com.infomanix.getpyq.data.UserState
import com.infomanix.getpyq.storage.FileStorage
import com.infomanix.getpyq.ui.viewmodels.FileViewModel
import com.infomanix.getpyq.ui.viewmodels.UploadTrackingViewModel
import com.infomanix.getpyq.ui.viewmodels.UserViewModel
import com.infomanix.getpyq.utils.AuthManagerUtils
import com.infomanix.getpyq.utils.PdfUtils
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val pdfFiles = remember { mutableStateListOf<File>().apply { addAll(getFolders(rootPath)) } }
    val uploadedPdfs by uploadTrackingViewModel.uploadList.observeAsState(emptyList())

    val uploaderEmail = when (val userState = userViewModel.userState.collectAsState().value) {
        is UserState.Uploader -> userState.useremail
        else -> null
    }

    LaunchedEffect(key1 = uploaderEmail) {
        if (!uploaderEmail.isNullOrEmpty()) {
            uploadTrackingViewModel.fetchUploads(uploaderEmail)
        }
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
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DrawerContent(uploadedPdfs: List<Map<String, Any>>) {
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
                items(uploadedPdfs) { pdf ->
                    val fileName = pdf["filepath"] as? String ?: "Unknown.pdf"
                    UploadedPdfItem(fileName)
                }
            }
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

// 🔹 Modal Drawer Content - List of Uploaded PDFs
@Composable
fun UploadedPdfsList(uploadedPdfs: List<Map<String, Any>>) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(0.dp)
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Text(
            "Uploads",
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
                items(uploadedPdfs) { pdf ->
                    val fileName = pdf["filepath"] as? String ?: "Unknown.pdf"
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
) {
    val context = LocalContext.current
    val firstImage =
        folder.listFiles()?.firstOrNull { it.extension in listOf("jpg", "jpeg", "png") }
    val creationDate = SimpleDateFormat("dd MMM yyyy").format(folder.lastModified())

    var showMenu by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadDone by remember { mutableStateOf(false) }
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
                                if (uploadDone) {
                                    showReuploadDialog = true
                                } else {
                                    startUpload(
                                        folder,
                                        context,
                                        uploadTrackingViewModel = uploadTrackingViewModel,
                                        onProgress = { progress ->
                                            uploadProgress = progress
                                        },
                                        onComplete = {
                                            isUploading = false
                                            uploadDone = true
                                        })
                                    isUploading = true
                                }
                            },
                            enabled = !isUploading // 🔹 Disable button when upload is in progress
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
                    uploadDone = false
                    isUploading = true
                    startUpload(folder, context, onProgress = { progress ->
                        uploadProgress = progress
                    }, onComplete = {
                        isUploading = false
                        uploadDone = true
                    }, uploadTrackingViewModel)
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

fun startUpload(
    folder: File,
    context: Context,
    onProgress: (Int) -> Unit,
    onComplete: () -> Unit,
    uploadTrackingViewModel: UploadTrackingViewModel,
) {
    val folderNameParts = folder.name.split("_")

    // Ensure correct folder name format
    if (folderNameParts.size < 5) {
        Log.e("Upload", "Invalid folder name format: ${folder.name}")
        return
    }

    val courseCode = folderNameParts[0] // ✅ Extract course code (e.g., "EE210")
    val subject = folderNameParts[1] // ✅ Extract subject name (e.g., "Microprocessors")
    val semester =
        extractSemesterNumber(folderNameParts[2]) // ✅ Extract semester number (e.g., "Sem4" -> 4)
    val examType = folderNameParts[3] // ✅ Extract exam type (e.g., "Mid-Semester")

    val monthYearParts = folderNameParts[4].split("-") // ✅ Extract month and year
    val uploadMonth = mapMonthToNumber(monthYearParts[0]) // ✅ Convert "March" -> 3
    val uploadYear = monthYearParts[1] // ✅ Extract year (e.g., "2025")

    val uploadTimestamp =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()) // ✅ Current timestamp

    uploadPdfToCloudinary(
        fileName= folder.name,
        folder = folder, courseCode = courseCode, subject = subject,
        semester = semester, examType = examType, monthYear = uploadMonth, year = uploadYear,
        onProgress = onProgress,
        context = context,
        onComplete = onComplete,
        uploadTrackingViewModel = uploadTrackingViewModel
    )
}

fun mapMonthToNumber(month: String): Int {
    return when (month.lowercase()) {
        "january" -> 1
        "february" -> 2
        "march" -> 3
        "april" -> 4
        "may" -> 5
        "june" -> 6
        "july" -> 7
        "august" -> 8
        "september" -> 9
        "october" -> 10
        "november" -> 11
        "december" -> 12
        else -> 0 // Default case
    }
}

fun uploadPdfToCloudinary(
    fileName: String,
    folder: File,
    courseCode: String,
    subject: String,
    semester: Int,
    examType: String,
    monthYear: Int,
    year: String,
    uploadTrackingViewModel: UploadTrackingViewModel,
    onProgress: (Int) -> Unit,
    context: Context,
    onComplete: () -> Unit,
) {
    val images =
        folder.listFiles()?.filter { it.extension in listOf("jpg", "jpeg", "png") } ?: emptyList()

    if (images.isEmpty()) {
        Log.e("Upload", "❌ No images found to compile into a PDF")
        onComplete() // Ensure onComplete is always called
        return
    }

    val pdfFile = File(folder, "$fileName.pdf")

    // ✅ Convert images to PDF
    PdfUtils.compileImagesToPdf4Upload(images, pdfFile.toString())

    if (!pdfFile.exists()) {
        Log.e("Upload", "❌ PDF creation failed at path: ${pdfFile.absolutePath}")
        onComplete() // Ensure onComplete is called even if PDF creation fails
        return
    }

    val fileUri = Uri.fromFile(pdfFile)
    Log.d("Upload", "✅ File ready for upload: ${pdfFile.absolutePath}")

    // ✅ Upload to Cloudinary
    FileStorage.uploadToCloudinary2(
        fileUri = fileUri,
        onProgress = { progress -> onProgress(progress) }, // 🔹 Update UI with progress
        onSuccess = { fileUrl ->
            // ✅ Save metadata to Supabase
            val uploaderEmail = AuthManagerUtils.getCurrentUserEmail().toString()
            saveFileMetadata(
                semester,
                courseCode,
                pdfFile.name,
                monthYear.toString(),
                year,
                fileUrl,
                uploaderEmail,
                uploadTrackingViewModel = uploadTrackingViewModel, // ✅ Pass uploader email
                onSuccess = {
                    Log.d("Upload", "✅ PDF metadata saved successfully in Supabase")
                    pdfFile.delete() // ✅ Delete local PDF after upload
                    onComplete() // ✅ Notify completion
                },
                onFailure = { e ->
                    Log.e("Upload", "❌ Error saving metadata to Supabase", e)
                    onComplete() // ✅ Ensure onComplete runs even if metadata saving fails
                }
            )
        },
        onFailure = { e ->
            Log.e("Upload", "❌ Cloudinary Upload failed", e)
            onComplete() // ✅ Ensure onComplete runs even if upload fails
        },
        context = context
    )
}

@SuppressLint("SimpleDateFormat")
fun saveFileMetadata(
    semester: Int,
    subjectcode: String,
    name: String,
    month: String,
    year: String,
    fileUrl: String,
    uploaderEmail: String, // ✅ Add uploader email
    uploadTrackingViewModel: UploadTrackingViewModel, // ✅ Inject ViewModel
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit,
) {
    try {
        val uploadTimestamp =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()) // ✅ Timestamp
        val metadata = UploadMetadata(
            filepath = name, // ✅ Store the PDF name
            uploaderemail = uploaderEmail, // ✅ Store uploader's email
            cloudurl = fileUrl, // ✅ Cloudinary file URL
            uploadsem = semester, // ✅ Convert semester to Int
            uploadsubject = subjectcode, // ✅ Subject name
            uploadmonth = month,// ✅ Extract current month
            uploadyear = year,
            uploadtime = uploadTimestamp
        )
        Log.d("Upload", "✅ File metadata: $metadata")
        // ✅ Insert metadata into Supabase
        uploadTrackingViewModel.insertUpload(metadata)

        Log.d("Upload", "✅ File metadata saved successfully in Supabase: $metadata")
        onSuccess()
    } catch (e: Exception) {
        Log.e("Upload", "❌ Error saving metadata to Supabase", e)
        onFailure(e)
    }
}

fun extractSemesterNumber(semester: String): Int {
    return semester.filter { it.isDigit() }.toIntOrNull() ?: 0
}

fun getPdfs(rootPath: String): List<File> {
    return File(rootPath).listFiles()?.filter { it.extension == "pdf" } ?: emptyList()
}

fun getFolders(rootPath: String): List<File> {
    return File(rootPath).listFiles()?.filter { it.isDirectory }?.reversed() ?: emptyList()
}