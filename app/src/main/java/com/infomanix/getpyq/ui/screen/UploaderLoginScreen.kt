package com.infomanix.getpyq.ui.screen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.infomanix.getpyq.data.UserState
import com.infomanix.getpyq.ui.viewmodels.UserViewModel
import com.infomanix.getpyq.utils.AuthManagerUtils

@Composable
fun UploaderLoginScreen(navController: NavHostController, userViewModel: UserViewModel) {
    Log.d("UploadTracking", "UploaderLoginScreen LaunchedEffect")
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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

        errorMessage?.let {
            Text(it, color = Color.Red, modifier = Modifier.padding(8.dp))
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        } else {
            Button(onClick = {
                isLoading = true
                AuthManagerUtils.login(
                    context = context,
                    email = email,
                    password = password
                ) { success, messafe ->
                    isLoading = false
                    if (success) {
                        val scholarId = AuthManagerUtils.loadScholarId(context)
                        val userName = AuthManagerUtils.loadUserName(context)
                        userViewModel.setUserState(UserState.Uploader(
                            userName,
                            email, scholarId
                        ))
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        errorMessage = messafe
                    }
                }
            }) {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Don’t have an account? Sign up!",
            color = Color.Blue,
            modifier = Modifier.clickable {
                navController.navigate("signup")
            }
        )
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
            Button(onClick = {
                isLoading = true
                AuthManagerUtils.signup(
                    context = context,
                    email = email,
                    password = password,
                    scholarId = scholarId
                ) { success,message ->
                    isLoading = false
                    if (success) {
                        val userNamer = AuthManagerUtils.loadUserName(context)
                        userViewModel.setUserState(UserState.Uploader(userName,email, scholarId))
                        navController.navigate("home") {
                            popUpTo("signup") { inclusive = true }
                        }
                    } else {
                        errorMessage = message
                    }
                }
            }) {
                Text("Sign Up")
            }
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
