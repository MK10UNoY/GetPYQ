package com.infomanix.getpyq

import android.app.Application
import android.util.Log
import com.infomanix.getpyq.utils.AuthManagerUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // ✅ This makes Hilt work across the app
class GPQApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AuthManagerUtils.initialize(this) // Initialize with application context
        // Catch any uncaught exceptions globally
        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            Log.e("AppCrash", "Uncaught exception", throwable)
        }
    }
}

