package com.infomanix.getpyq.ui.viewmodels

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.infomanix.getpyq.utils.OCRUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class FileViewModel : ViewModel() {
    var imageFiles by mutableStateOf<List<File>>(emptyList())
    var pdfFile by mutableStateOf<File?>(null) // Stores the Pdf after the scan session
    private val _folderName = MutableStateFlow("GPQ_untitled")
    val folderName = _folderName.asStateFlow()

    var isViewSessionActive by mutableStateOf(false)  // Tracks view session status
    var isScanSessionActive by mutableStateOf(false)  // Tracks scan session status

    fun startScanSession() {
        isScanSessionActive = true
    }
    fun setViewSession() {
        isViewSessionActive = true
    }
    fun endViewSession() {
        isViewSessionActive = false
        clearSessionData() // Automatically clears when session ends
    }
    fun endScanSession() {
        isScanSessionActive = false
        clearSessionData() // Automatically clears when session ends
    }
    fun setSessionFolderName(name: String) {
        Log.d("SetFolder", "Setting folder name: $name")
        _folderName.value = name
    }

    fun getSessionFolderName(): String? {
        Log.d("SetFolder", "Getting folder: $_folderName")
        return _folderName.value
    }

    fun updateImagePaths(newFolderPath: String) {
        imageFiles = File(newFolderPath).listFiles()?.filter { it.isFile }?.toMutableStateList()
            ?: mutableStateListOf()
    }

    fun renameFolder(newName: String, customFolderPath: String? = null): Boolean {
        val TAG = "RenameDebug"  // Unified logging tag

        val folderToRename = customFolderPath ?: _folderName.value ?: return false
        val oldFile = File(folderToRename)

        if (!oldFile.exists() || !oldFile.isDirectory) {
            Log.d(TAG, "Folder does not exist or is not a directory: $folderToRename")
            return false
        }

        val newFile = File(oldFile.parent, newName)

        Log.d(TAG, "Attempting to rename: $folderToRename → ${newFile.absolutePath}")

        return if (oldFile.renameTo(newFile)) {
            Log.d(TAG, "Rename successful: ${newFile.absolutePath}")

            if (customFolderPath == null) {
                _folderName.value = newFile.absolutePath
                imageFiles = emptyList() // ✅ Ensures UI update
            }

            Handler(Looper.getMainLooper()).postDelayed({
                val updatedFiles =
                    File(newFile.absolutePath).listFiles()?.filter { it.isFile } ?: emptyList()
                imageFiles = updatedFiles.toMutableStateList()
                Log.d(
                    TAG,
                    "Updated Image List: ${imageFiles.size} images found in ${newFile.absolutePath}"
                )
            }, 600)

            true
        } else {
            Log.e(TAG, "Rename failed: $folderToRename → ${newFile.absolutePath}")
            false
        }
    }

    fun setFiles(images: List<File>, pdf: File) {
        imageFiles = images
        pdfFile = pdf
    }

    private fun clearSessionData() {
        imageFiles = emptyList()
        pdfFile = null
        _folderName.value = null.toString()
    }
    private fun extractTextForRenaming(context: Context, onTextExtracted: (String) -> Unit) {
        val firstImage = imageFiles.firstOrNull()
        if (firstImage == null) {
            Log.e("FileViewModel", "No images found in folder.")
            onTextExtracted("Renamed_Folder") // Fallback name
            return
        }

        OCRUtils.extractTextFromImage(firstImage, context) { extractedName ->
            // 🔹 Sanitize extracted name (remove invalid characters)
            var cleanName = extractedName.replace(Regex("[^a-zA-Z0-9_ -]"), "_").trim()
            if (cleanName.isBlank()) cleanName = "Renamed_Folder"

            onTextExtracted(cleanName) // ✅ Callback with extracted name
        }
    }

    fun autoRenameFolder(context: Context) {
        extractTextForRenaming(context) { extractedName ->
            val currentPath = _folderName.value
            Log.d("AutoRename", "Current Folder Path: $currentPath")
            Log.d("AutoRename", "Extracted Name: $extractedName")

            if (currentPath.isEmpty()) {
                Log.e("FileViewModel", "Current folder path is null or empty.")
                return@extractTextForRenaming
            }

            renameFolder(extractedName, currentPath)
        }
    }
}
