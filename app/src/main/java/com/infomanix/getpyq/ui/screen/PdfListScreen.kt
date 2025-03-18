package com.infomanix.getpyq.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.navigation.NavHostController
import com.infomanix.getpyq.storage.FileStorage
import com.infomanix.getpyq.ui.navigation.popOut
import com.infomanix.getpyq.ui.viewmodels.SupabaseViewModel
import io.github.jan.supabase.SupabaseClient
import io.ktor.websocket.Frame

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("RememberReturnType")
@Composable
fun PdfListScreen(
    navController: NavController,
    semester: String,
    subject: String,
    examType: String,
    viewModel: SupabaseViewModel = hiltViewModel() // ✅ Get ViewModel automatically
) {
    val pdfList = remember { mutableStateListOf<Pair<String, String>>() } // (PDF Name, URL)
    val context = LocalContext.current
    val isLoading = remember { mutableStateOf(true) }
    val errorMessage = remember { mutableStateOf<String?>(null) }

    // ✅ Fetch PDFs when screen loads
    LaunchedEffect(semester, subject, examType) {
        FileStorage.fetchUploadedFiles(
            viewModel.supabaseClient,// Here we get instance of the supabase Client via dependency injection
            uploadSem = semester,
            uploadSubject = subject,
            onResult = { urls ->
                pdfList.clear()
                urls.forEach { url ->
                    pdfList.add(url.substringAfterLast("/") to url) // Extract file name
                }
                isLoading.value = false
            },
            onFailure = { error ->
                errorMessage.value = error.message
                isLoading.value = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Frame.Text("$subject - $examType") },
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
                .padding(16.dp)
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (errorMessage.value != null) {
                Text(
                    text = "Error: ${errorMessage.value}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (pdfList.isEmpty()) {
                Text(
                    text = "No PDFs found for $subject - $examType",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pdfList) { (pdfName, pdfUrl) ->
                        PdfItem(pdfName, pdfUrl, context)
                    }
                }
            }
        }
    }
}
@Composable
fun PdfItem(pdfName: String, pdfUrl: String, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openPdf(context, pdfUrl) },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = Color.Red)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = pdfName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = pdfUrl, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// 🔹 Open PDF in External Viewer
fun openPdf(context: Context, pdfUrl: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(Uri.parse(pdfUrl), "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(intent)
}
