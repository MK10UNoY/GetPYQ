package com.infomanix.getpyq.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.infomanix.getpyq.data.UserState
import com.infomanix.getpyq.ui.viewmodels.UserViewModel
import com.infomanix.getpyq.utils.AuthManagerUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploaderLoginScreen(navController: NavHostController, userViewModel: UserViewModel) {
    Log.d("UploadTracking", "UploaderLoginScreen LaunchedEffect")

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEEF2F5)), // Light background color for a modern look
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineMedium.copy(color = Color(0xFF1B1F23)),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Email Input
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = Color(0xFF616161)) },
                placeholder = { Text("Enter your email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                /*colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White,
                    focusedIndicatorColor = Color(0xFF4A90E2),
                    unfocusedIndicatorColor = Color(0xFFBDBDBD)
                )*/
            )

            // Password Input
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color(0xFF616161)) },
                placeholder = { Text("Enter your password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                /*colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White,
                    focusedIndicatorColor = Color(0xFF4A90E2),
                    unfocusedIndicatorColor = Color(0xFFBDBDBD)
                )*/
            )

            // Error Message
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Loading Indicator or Login Button
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF4A90E2) // Use theme color for consistency
                )
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        AuthManagerUtils.signIn(
                            email = email,
                            password = password
                        ) { success ->
                            isLoading = false
                            if (success) {
                                val scholarId = AuthManagerUtils.loadScholarId(context)
                                val userName = AuthManagerUtils.loadUserName(context)
                                userViewModel.setUserState(
                                    UserState.Uploader(
                                        userName,
                                        email, scholarId
                                    )
                                )
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90E2),
                        contentColor = Color.White
                    )
                ) {
                    Text("Login", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign-Up Link
            Text(
                text = "Don’t have an account? Sign up!",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF4A90E2)),
                modifier = Modifier.clickable {
                    navController.navigate("signup")
                }
            )
        }
    }
}


@Composable
fun UploaderSignupScreen(navController: NavHostController, userViewModel: UserViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var scholarId by remember { mutableStateOf("") }
    val userName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        TextField(
            value = scholarId,
            onValueChange = { scholarId = it },
            label = { Text("Scholar ID") }
        )

        errorMessage?.let {
            Text(it, color = Color.Red, modifier = Modifier.padding(8.dp))
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Already have an account? Login!",
            color = Color.Blue,
            modifier = Modifier.clickable {
                navController.navigate("login")
            }
        )
    }
}
