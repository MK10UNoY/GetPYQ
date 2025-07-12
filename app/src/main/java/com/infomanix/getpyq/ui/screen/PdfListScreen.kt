package com.infomanix.getpyq.ui.screen

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.infomanix.getpyq.data.PyqMetaData
import com.infomanix.getpyq.ui.viewmodels.UploadTrackingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("RememberReturnType")
@Composable
fun PdfListScreen(
    navController: NavController,
    semester: String,
    subjectcode: String,
    examType: String,
    uploadTrackingViewModel: UploadTrackingViewModel = hiltViewModel(),
) {
    val pdfList by uploadTrackingViewModel.pdfList.observeAsState(emptyList()) // ✅ Observe LiveData
    val context = LocalContext.current
    val isLoading = remember { mutableStateOf(true) }

    // ✅ Fetch PDFs when screen loads
    LaunchedEffect(semester, subjectcode, examType) {
        isLoading.value = true  // ✅ Set loading before fetching
        val metaData = PyqMetaData(
            filepath = "", // ✅ Not needed for fetching
            uploadsubject = subjectcode,
            cloudurl = "",
            uploadmonth = "",
            uploadyear = "",
            uploadtime = "",
            uploadterm = examType
        )
        uploadTrackingViewModel.fetchSubjectPdfUrls(metaData) // ✅ Call ViewModel function
        isLoading.value = false  // ✅ Stop loading when fetch completes
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$subjectcode - $examType") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                isLoading.value -> {
                    CircularProgressIndicator() // ✅ Show while fetching data
                }
                pdfList.isEmpty() -> {
                    Text(
                        text = "📂 PDF uploads are in process. Please check back later.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(pdfList) { pdf ->
                            PdfItem(
                                pdf.filepath,
                                pdf.cloudurl,
                                navController,
                                context
                            ) // ✅ Show PDFs from LiveData
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PdfItem(pdfName: String, pdfUrl: String, navController: NavController, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = "PDF",
                tint = Color.Red,
                modifier = Modifier
                    .clickable { navController.navigate("pdfViewer/${Uri.encode(pdfUrl)}") } // ✅ Open PDF Viewer
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = pdfName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = { downloadPdf(context, pdfUrl, pdfName) }) {
                Icon(imageVector = Icons.Default.Download, contentDescription = "Download PDF")
            }
        }
    }
}

@SuppressLint("ServiceCast")
fun downloadPdf(context: Context, pdfUrl: String, pdfName: String) {
    val request = DownloadManager.Request(Uri.parse(pdfUrl)).apply {
        setTitle(pdfName)
        setDescription("Downloading $pdfName")
        setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$pdfName.pdf")
    }

    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    downloadManager.enqueue(request)

    Toast.makeText(context, "Downloading $pdfName...", Toast.LENGTH_SHORT).show()
}