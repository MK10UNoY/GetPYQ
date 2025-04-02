package com.infomanix.getpyq.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import com.infomanix.getpyq.data.UploadMetadata
import com.infomanix.getpyq.storage.FileStorage
import com.infomanix.getpyq.ui.viewmodels.UploadTrackingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat

object UploadUtils {
    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> get() = _uploadProgress
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> get() = _isUploading
    private val _uploadDone = MutableStateFlow(false)
    val uploadDone: StateFlow<Boolean> get() = _uploadDone

    suspend fun startUpload(
        folder: File,
        context: Context,
        onProgress: (Int) -> Unit,
        onComplete: () -> Unit,
        uploadTrackingViewModel: UploadTrackingViewModel,
    ) = withContext(Dispatchers.IO) {
        // Run in background
        _isUploading.value = true
        _uploadProgress.value = 0
        val folderNameParts = folder.name.split("_")

        if (folderNameParts.size < 5) {
            Log.e("Upload", "Invalid folder name format: ${folder.name}")
            return@withContext
        }
        Log.d("Upload", "Start upload called")
        val courseCode = folderNameParts[0]
        val subject = folderNameParts[1]
        val semester = extractSemesterNumber(folderNameParts[2])
        val examType = folderNameParts[3]
        val monthYearParts = folderNameParts[4].split("-")
        val uploadMonth = mapMonthToNumber(monthYearParts[0])
        val uploadYear = monthYearParts[1]

        uploadPdfToCloudinary(
            fileName = folder.name,
            folder = folder,
            courseCode = courseCode,
            subject = subject,
            semester = semester,
            examType = examType,
            monthYear = uploadMonth,
            year = uploadYear,
            onProgress = onProgress,
            context = context,
            onComplete = {
                _isUploading.value = false
                _uploadProgress.value = 100
                _uploadDone.value = true
                onComplete() },
            uploadTrackingViewModel = uploadTrackingViewModel
        )
    }

    private suspend fun uploadPdfToCloudinary(
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
    ) = withContext(Dispatchers.IO) {
        Log.d("Upload", "inside private upload to cloudinary called")
        // Run in background
        val images = folder.listFiles()?.filter { it.extension in listOf("jpg", "jpeg", "png") }
            ?: emptyList()

        if (images.isEmpty()) {
            Log.e("Upload", "❌ No images found to compile into a PDF")
            onComplete()
            return@withContext
        }

        val pdfFile = File(folder, "$fileName.pdf")

        // Convert images to PDF
        PdfUtils.compileImagesToPdf4Upload(images, pdfFile.toString())

        if (!pdfFile.exists()) {
            Log.e("Upload", "❌ PDF creation failed at path: ${pdfFile.absolutePath}")
            onComplete()
            return@withContext
        }
        Log.d("Upload", "pdf creation success called")

        val fileUri = Uri.fromFile(pdfFile)
        Log.d("Upload", "✅ File ready for upload: ${pdfFile.absolutePath}")

        FileStorage.uploadToCloudinary2(
            fileUri = fileUri,
            onProgress = {onProgress(_uploadProgress.value)},
            onSuccess = { fileUrl ->
                val uploaderEmail = AuthManagerUtils.getCurrentUserEmail().toString()
                saveFileMetadata(
                    semester = semester,
                    subjectcode = courseCode,
                    name = pdfFile.name,
                    month = monthYear.toString(),
                    year = year,
                    fileUrl = fileUrl,
                    uploaderEmail = uploaderEmail,
                    uploadTerm = examType,
                    uploadTrackingViewModel = uploadTrackingViewModel,
                    onSuccess = {
                        Log.d("Upload", "✅ PDF metadata saved successfully in Supabase")
                        pdfFile.delete() // Delete local PDF after upload
                        onComplete()
                    },
                    onFailure = { e ->
                        Log.e("Upload", "❌ Error saving metadata to Supabase", e)
                        onComplete()
                    }
                )
            },
            onFailure = { e ->
                Log.e("Upload", "❌ Cloudinary Upload failed", e)
                onComplete()
            },
            context = context
        )
    }

    private fun extractSemesterNumber(semester: String): Int {
        return semester.filter { it.isDigit() }.toIntOrNull() ?: 0
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
            else -> 0
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun saveFileMetadata(
        semester: Int,
        subjectcode: String,
        name: String,
        month: String,
        year: String,
        uploadTerm: String,
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
                uploaderemail = uploaderEmail, // ✅ Store uploader email
                cloudurl = fileUrl, // ✅ Cloudinary file URL
                uploadsem = semester, // ✅ Convert semester to Int
                uploadsubject = subjectcode, // ✅ Subject name
                uploadmonth = month,// ✅ Extract current month
                uploadyear = year,
                uploadtime = uploadTimestamp,
                uploadterm = uploadTerm
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
}
