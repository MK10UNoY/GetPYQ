package com.infomanix.getpyq.ui.viewmodels

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
import java.io.File

class FileViewModel : ViewModel() {
    var imageFiles by mutableStateOf<List<File>>(emptyList())
    var pdfFile by mutableStateOf<File?>(null) // Stores the Pdf after the scan session
    var folderName by mutableStateOf<String?>(null) //Stores the file path Absolute

    // Tracks scan session status
    var isScanSessionActive by mutableStateOf(false)

    fun startScanSession() {
        isScanSessionActive = true
    }

    fun endScanSession() {
        isScanSessionActive = false
        clearSessionData() // Automatically clears when session ends
    }

    var isViewSessionActive: Boolean = false  // Tracks view session status

    fun setSessionFolderName(name: String) {
        Log.d("SetFolder", "Setting folder name: $name")
        folderName = name
    }

    fun getSessionFolderName(): String? {
        Log.d("SetFolder", "Getting folder: $folderName")
        return folderName
    }

    fun updateImagePaths(newFolderPath: String) {
        imageFiles = File(newFolderPath).listFiles()?.filter { it.isFile }?.toMutableStateList()
            ?: mutableStateListOf()
    }

    fun renameFolder(newName: String, customFolderPath: String? = null): Boolean {
        val TAG = "RenameDebug"  // Unified logging tag

        val folderToRename = customFolderPath ?: folderName ?: return false
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
                folderName = newFile.absolutePath
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

    fun clearSessionData() {
        imageFiles = emptyList()
        pdfFile = null
        folderName = null
    }
}
