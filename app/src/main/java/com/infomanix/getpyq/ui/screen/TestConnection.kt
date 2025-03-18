package com.infomanix.getpyq.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.infomanix.getpyq.ui.viewmodels.TestViewModel

@Composable
fun TestScreen(viewModel: TestViewModel = hiltViewModel()) {
    var statusText by remember { mutableStateOf("Connecting...") }

    LaunchedEffect(Unit) {
        try {
            viewModel.testSupabaseConnection()
            statusText = "✅ Connection successful!"
        } catch (e: Exception) {
            statusText = "❌ Error: ${e.message}"
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = statusText)
    }
}
