package com.infomanix.getpyq.storage

import android.content.Context
import java.io.File

object FileStorage {
    fun saveFile(originalFile: File, fileName: String, context: Context) {
        val newFile = File(context.cacheDir, fileName)
        originalFile.copyTo(newFile, overwrite = true)
    }
}