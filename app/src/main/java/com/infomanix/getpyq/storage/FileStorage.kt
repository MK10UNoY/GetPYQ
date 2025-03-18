package com.infomanix.getpyq.storage

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileStorage {
    private const val CLOUDINARY_URL =
        "https://api.cloudinary.com/v1_1/dyeya9x0b/image/upload"
    private const val UPLOAD_URL =
        "https://upload-request.cloudinary.com/dyeya9x0b/bb468219bb72a589b434f9f214b96549"
    private const val UPLOAD_PRESET = "getpyq_upload_preset_ai8bc7w63f5cq265"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // ⏳ Increase connection timeout
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // ⏳ Increase read timeout
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // ⏳ Increase write timeout
        .build()

    // Save file locally (optional caching)
    fun saveFile(originalFile: File, fileName: String, context: Context) {
        val newFile = File(context.cacheDir, fileName)
        originalFile.copyTo(newFile, overwrite = true)
    }

    // List files is not directly possible in Cloudinary without additional setup.
    fun listFiles(): List<String> {
        return emptyList() // Requires Cloudinary API listing setup
    }

    // Get file URL (not needed for Cloudinary, as URLs are returned on upload)
    fun getFileUrl(filePath: String, onSuccess: (String) -> Unit) {
        onSuccess(filePath)
    }

    // Delete file from Cloudinary
    fun deleteFile(
        publicId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val deleteUrl = "https://api.cloudinary.com/v1_1/YOUR_CLOUD_NAME/image/destroy"
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("public_id", publicId)
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .build()

        val request = Request.Builder()
            .url(deleteUrl)
            .post(requestBody)
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) onSuccess() else onFailure(Exception("Deletion failed"))
            } catch (e: Exception) {
                onFailure(e)
            }
        }.start()
    }

    private fun getRealPathFromUri(context: Context, uri: Uri): String? {
        var filePath: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                if (columnIndex != -1) {
                    filePath = it.getString(columnIndex)
                } else {
                    // Handle cases where the column index is -1 (e.g., DocumentProvider URIs)
                    val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        val fileName = it.getString(displayNameIndex)
                        val file = File(context.cacheDir, fileName)
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            file.outputStream()
                                .use { outputStream -> inputStream.copyTo(outputStream) }
                        }
                        filePath = file.absolutePath
                    }
                }
            }
        }
        return filePath
    }

    fun getRealPathFromUri2(context: Context, uri: Uri): String? {
        return when (uri.scheme) {
            "file" -> uri.path // ✅ Use directly for file:// URIs
            "content" -> {
                var filePath: String? = null
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                        if (columnIndex != -1) {
                            filePath = it.getString(columnIndex)
                        }
                    }
                }
                filePath
            }

            else -> null
        }
    }

    fun uploadToCloudinary2(
        context: Context,
        fileUri: Uri,
        onProgress: (Int) -> Unit,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        Log.d("Upload", "Reached FileStorage function")

        try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: throw Exception("Unable to open input stream")

            val fileName = File(fileUri.path ?: "temp.pdf").name
            val tempFile = File(context.cacheDir, fileName)

            tempFile.outputStream().use { output -> inputStream.copyTo(output) }
            inputStream.close()

            val fileBody = tempFile.asRequestBody("application/pdf".toMediaTypeOrNull())

            // Wrapped RequestBody to track progress
            val countingRequestBody = CountingRequestBody(fileBody) { bytesWritten, contentLength ->
                val progress = ((bytesWritten.toFloat() / contentLength.toFloat()) * 100).toInt()
                Log.d("Upload", "Progress: $progress%")
                CoroutineScope(Dispatchers.Main).launch {
                    onProgress(progress) // Update UI
                }
            }

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", tempFile.name, countingRequestBody)
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .addFormDataPart("resource_type", "raw")
                .build()

            val request = Request.Builder()
                .url(CLOUDINARY_URL)
                .post(requestBody)
                .build()

            Log.d("Upload", "📤 Sending request to Cloudinary...")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = client.newCall(request).execute()
                    Log.d("Upload", "📩 Received response from Cloudinary")

                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: ""
                        Log.d("Upload", "✅ Upload success: $responseBody")

                        val jsonResponse = JSONObject(responseBody)
                        val fileUrl = jsonResponse.getString("secure_url")

                        withContext(Dispatchers.Main) { onSuccess(fileUrl) }
                    } else {
                        Log.e("Upload", "❌ Upload failed: ${response.code} ${response.message}")
                        withContext(Dispatchers.Main) { onFailure(Exception("Upload failed")) }
                    }
                } catch (e: Exception) {
                    Log.e("Upload", "❌ Exception: ${e.message}", e)
                    withContext(Dispatchers.Main) { onFailure(e) }
                }
            }
        } catch (e: Exception) {
            Log.e("Upload", "❌ Error preparing file: ${e.message}", e)
            onFailure(e)
        }
    }

    // 🔹 STORE UPLOAD METADATA IN SUPABASE
    private fun storeUploadRecord(
        supabaseClient: SupabaseClient,
        filePath: String,
        uploaderEmail: String,
        cloudUrl: String,
        cloudinaryId: String,
        uploadSem: String,
        uploadSubject: String,
    ) {
        val currentDate = Date()
        val month = SimpleDateFormat("MMMM", Locale.getDefault()).format(currentDate) // e.g., March
        val year = SimpleDateFormat("yyyy", Locale.getDefault()).format(currentDate) // e.g., 2025
        val time =
            SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(currentDate) // e.g., 14:30:55

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = supabaseClient.from("uploadTrackRegister").insert(
                    mapOf(
                        "filepath" to filePath,
                        "uploaderemail" to uploaderEmail,
                        "cloudurl" to cloudUrl,
                        "uploadsem" to uploadSem,
                        "uploadsubject" to uploadSubject,
                        "uploadmonth" to month,
                        "uploadyear" to year,
                        "uploadtime" to time
                    )
                )
                Log.d("UploadDB", "✅ File record stored in Supabase: $result")
            } catch (e: Exception) {
                Log.e("UploadDB", "❌ Error storing upload record: ${e.message}", e)
            }
        }
    }

    // 🔹 FETCH FILES FROM SUPABASE FOR A SPECIFIC FOLDER
    fun fetchUploadedFiles(
        supabaseClient: SupabaseClient,
        uploadSem: String,
        uploadSubject: String,
        page: Int = 1, // ✅ Pagination starts from page 1
        limit: Int = 20, // ✅ Fetch 20 records per request
        onResult: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val offset = (page - 1) * limit // ✅ Calculate offset for pagination
        /**
         *  Apply pagination in future
         */
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = supabaseClient.from("uploadTrackRegister")
                    .select(Columns.list("cloudurl")) {
                        filter {
                            eq("uploadsem", uploadSem)
                            eq("uploadsubject", uploadSubject)
                        }
                        range(
                            offset.toLong(), (offset + limit -
                                    1
                                    ).toLong(),
                            null
                        )//Null works here because it doesn't use an external table that's why
                    }.decodeList<Map<String, Any>>() // ✅ Ensure result is properly parsed

                val fileUrls =
                    result.mapNotNull { it["cloudurl"] as? String } // ✅ Safely extract URLs

                withContext(Dispatchers.Main) { onResult(fileUrls) } // ✅ Pass result to UI thread
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onFailure(e) } // ✅ Handle errors properly
            }
        }

    }

    // 🔹 DELETE FILE FROM CLOUDINARY & SUPABASE
    fun deleteFile(
        supabaseClient: SupabaseClient,
        publicId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
    ) {
        val deleteUrl = "https://api.cloudinary.com/v1_1/dyeya9x0b/image/destroy"
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("public_id", publicId)
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .build()

        val request = Request.Builder()
            .url(deleteUrl)
            .post(requestBody)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    // ✅ Delete entry from Supabase
                    supabaseClient.from("uploadTrackRegister")
                        .delete() { filter { eq("cloudurl", publicId) } }
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    withContext(Dispatchers.Main) { onFailure(Exception("Deletion failed")) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onFailure(e) }
            }
        }
    }
}
