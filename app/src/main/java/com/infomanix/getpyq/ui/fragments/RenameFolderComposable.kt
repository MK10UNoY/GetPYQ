package com.infomanix.getpyq.ui.fragments

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.infomanix.getpyq.ui.viewmodels.FileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameFolderBottomSheet(
    fileViewModel: FileViewModel,
    initialFolderName: String,
    currentFolderPath: String, // 🔹 Pass the full folder path
    onDismiss: () -> Unit,
    onRenameSuccess: (String) -> Unit // 🔹 Callback for successful rename
) {
    var newFolderName by remember { mutableStateOf(initialFolderName) }
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Rename Folder", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = newFolderName,
                onValueChange = { newFolderName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                TextButton(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            val success = fileViewModel.renameFolder(newFolderName, currentFolderPath)
                            if (success) {
                                onRenameSuccess(newFolderName) // ✅ Update UI
                                Toast.makeText(context, "Folder renamed", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            } else {
                                Toast.makeText(context, "Rename failed", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        }
                    }
                ) {
                    Text("Rename")
                }
            }
        }
    }
}

