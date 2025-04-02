package com.infomanix.getpyq

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.infomanix.getpyq.ui.navigation.AppNavigation
import com.infomanix.getpyq.utils.AuthManagerUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // ✅ Add this annotation
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //AuthManagerUtils.initialize(this)
        enableEdgeToEdge()
        WindowManager.LayoutParams.FLAG_SECURE
        setContent {
            val systemUiController = rememberSystemUiController()
            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color(0xFF2D3139),
                    darkIcons = false
                )
            }
            Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
                Log.e("AppCrash", "🔥 Uncaught exception", throwable)
            }

            val navController = rememberNavController()

            AppNavigation(navController) // Pass currentUser for navigation logic
        }
    }
}
