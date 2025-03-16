package com.infomanix.getpyq.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import android.content.Context
import com.infomanix.getpyq.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(navController: NavHostController, context: Context) {
    val userPreferences = remember { UserPreferences.getInstance(context) }
    var name by remember { mutableStateOf("") }
    var scholarId by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("guest") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome! Let’s get to know you", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(value = name, onValueChange = { name = it }, label = { Text("Your Name") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = scholarId,
            onValueChange = { scholarId = it },
            label = { Text("Scholar ID") })

        errorMessage?.let {
            Text(it, color = Color.Red, modifier = Modifier.padding(8.dp))
        }

        Button(onClick = {
            if (name.isNotBlank() && scholarId.isNotBlank()) {
                // Start a coroutine to save data and mark splash as seen
                CoroutineScope(Dispatchers.IO).launch {
                    userPreferences.saveUserInfo(name, scholarId,userType)
                    userPreferences.setSplashSeen()

                    // Navigate back to main UI thread to switch screens
                    withContext(Dispatchers.Main) {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            } else {
                errorMessage = "Please fill in all fields."
            }
        }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Continue")
        }
    }
}
