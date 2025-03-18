package com.infomanix.getpyq.repository

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest


@Singleton
class UploadTrackingRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun insertUploadRecord(uploadData: Map<String, Any>) {
        supabase.postgrest["uploadtrackregister"]
            .insert(uploadData)
    }
    suspend fun fetchUploadsByEmail(email: String): List<Map<String, Any>> {
        Log.d("UploadTracking", "Fetching uploads for email: $email")
        return try {
            supabase.postgrest["uploadtrackregister"]
                .select {
                    filter { eq("uploaderemail", email) }
                }
                .decodeList<Map<String, Any>>()
                .also { result ->
                    if (result.isEmpty()) Log.w("UploadTracking", "No uploads found for $email")
                }
        } catch (e: Exception) {
            Log.e("Supabase", "Fetch failed: ${e.message}", e)
            emptyList() // ✅ Return empty list to prevent crashes
        }
    }
}
