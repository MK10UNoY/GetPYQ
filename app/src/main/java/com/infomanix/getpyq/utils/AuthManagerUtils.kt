package com.infomanix.getpyq.utils

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

object AuthManagerUtils {
    val FAuth: FirebaseAuth = FirebaseAuth.getInstance()
    /** 🔑 Handles sign-in */
    fun signIn(email: String, password: String, onResult: (Boolean) -> Unit) {
        FAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true)
                } else {
                    Log.e("AuthManagerUtils", "Sign-in failed: ${task.exception?.message}")
                    onResult(false)
                }
            }
    }
    /** 🚪 Logs out the user */
    fun logout(context: Context) {
        FAuth.signOut()
        clearSessionData(context)
    }

    /** ✅ Checks if user is logged in */
    fun isLoggedIn(): Boolean {
        return FAuth.currentUser != null
    }

    /** 🆔 Gets the current user's email */
    fun getCurrentUserEmail(): String? {
        return FAuth.currentUser?.email
    }

    fun loadUserType(context: Context): String {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("userType", "") ?: ""
    }
    /** 🏷️ Loads username from SharedPreferences */
    fun loadUserName(context: Context): String {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("username", "Unknown") ?: "Unknown"
    }

    /** 📚 Loads scholar ID from SharedPreferences */
    fun loadScholarId(context: Context): String {
        return context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .getString("scholarId", "") ?: ""
    }

    /** 🗑️ Clears session data from SharedPreferences */
    private fun clearSessionData(context: Context) {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
