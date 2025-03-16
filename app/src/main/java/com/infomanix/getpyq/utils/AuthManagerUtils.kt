package com.infomanix.getpyq.utils

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.infomanix.getpyq.data.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object AuthManagerUtils {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    @SuppressLint("StaticFieldLeak")
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val currentUser get() = firebaseAuth.currentUser

    fun isLoggedIn(): Boolean = currentUser != null

    fun getCurrentUserEmail(): String? = currentUser?.email

    // 🔥 Updated Signup function in AuthManagerUtils with UX enhancements
    fun signup(
        context: Context,
        email: String,
        password: String,
        scholarId: String,
        onResult: (Boolean, String) -> Unit, // Returning both success status and message
    ) {
        // Basic validation for password length
        if (password.length < 6) {
            onResult(false, "Password must be at least 6 characters long.")
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userType = if (email.contains("uploader")) "uploader" else "guest"

                    // ✅ Save the user data directly to UserPreferences (no Firestore involved)
                    saveUserType(
                        context,
                        scholarId = scholarId,
                        userEmail = email,
                        userType = userType
                    )
                    onResult(true, "Signup successful!")
                } else {
                    task.exception?.let { exception ->
                        when (exception) {
                            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                                // Email already registered
                                onResult(false, "This email is already registered.")
                            }

                            is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> {
                                // Password too weak
                                onResult(false, "Password must be at least 6 characters.")
                            }

                            else -> {
                                // General network error or unknown error
                                onResult(false, "Network error — check your connection.")
                            }
                        }
                    } ?: run {
                        // Fallback in case exception is null
                        onResult(false, "An unknown error occurred.")
                    }
                }
            }
    }

    // 🔒 Updated Login function with UX enhancements
    fun login(
        context: Context,
        email: String,
        password: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        // Basic validation for empty fields
        if (email.isEmpty() || password.isEmpty()) {
            onResult(false, "Please enter both email and password.")
            return
        }

        // Simple email format check (e.g., using regex)
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onResult(false, "Please enter a valid email address.")
            return
        }

        // Show loading indicator (if needed, depending on UI implementation)
        // Example: showLoadingIndicator(true)

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // Hide loading indicator (if applicable)
                // Example: showLoadingIndicator(false)

                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        // Load scholarId from UserPreferences
                        val savedScholarId = runBlocking {
                            UserPreferences.getInstance(context).scholarId.first() ?: "unknown"
                        }
                        // Save user info with the saved scholarId
                        saveUserType(
                            context,
                            scholarId = savedScholarId,
                            userEmail = email,
                            userType = "uploader"
                        )
                        onResult(true, "Login successful!")
                    } ?: run {
                        onResult(false, "User not found. Please check your credentials.")
                    }
                } else {
                    task.exception?.let { exception ->
                        when (exception) {
                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                                // Incorrect password or email
                                onResult(false, "Incorrect email or password. Please try again.")
                            }

                            is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                                // Email registered but still failed login (rare case)
                                onResult(
                                    false,
                                    "This email is registered, but an error occurred during login."
                                )
                            }

                            else -> {
                                // General network error or unknown error
                                onResult(false, "Network error — check your connection.")
                            }
                        }
                    } ?: run {
                        // Fallback in case exception is null
                        onResult(false, "An unknown error occurred. Please try again later.")
                    }
                }
            }
    }

    // 🚪 Logout and clear user state
    fun logout(context: Context) {
        firebaseAuth.signOut()
        /*runBlocking {
            UserPreferences.getInstance(context).clearUserInfo()
        }*/ //Above one waits for suspend function but this is called on a ui thread action so the composition is effected so we use below one
        // Use viewModelScope or a CoroutineScope to launch in the background thread
        CoroutineScope(Dispatchers.IO).launch {
            UserPreferences.getInstance(context).clearUserInfo() // Clear stored user data
        }
    }

    // 🌟 Save user type and scholarId using UserPreferences
    private fun saveUserType(
        context: Context,
        scholarId: String,
        userEmail: String,
        userType: String,
    ) {
        runBlocking {
            UserPreferences.getInstance(context).saveUserInfo(
                email = userEmail,
                scholarId = scholarId,
                userType = userType
            )
        }
    }

    // 🔍 Restore user type from UserPreferences
    fun loadUserType(context: Context): String {
        return runBlocking {
            UserPreferences.getInstance(context).scholarId.first() ?: "guest"
        }
    }

    // 🔍 Restore Scholar ID from UserPreferences
    fun loadScholarId(context: Context): String {
        return runBlocking {
            UserPreferences.getInstance(context).scholarId.first() ?: ""
        }
    }
}
