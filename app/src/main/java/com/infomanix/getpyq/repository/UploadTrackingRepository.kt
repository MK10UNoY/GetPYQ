package com.infomanix.getpyq.repository

import android.util.Log
import com.infomanix.getpyq.data.PyqMetaData
import com.infomanix.getpyq.data.UploadMetadata
import javax.inject.Inject
import javax.inject.Singleton
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest


@Singleton
class UploadTrackingRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun insertUploadRecord(uploadData: UploadMetadata) {
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

    suspend fun fetchSubjectPdfUrls(metaData: PyqMetaData): List<PyqMetaData> {
        return try {
            Log.d(
                "Supabase",
                "Fetching PDFs for ${metaData.uploadsubject}"
            )

            supabase.postgrest["uploadtrackregister"]
                .select() {
                    filter { eq("uploadsubject", metaData.uploadsubject) }
                    //filter { eq("uploadmonth", metaData.uploadmonth) }
                    //filter { eq("uploadyear", metaData.uploadyear) }
                }
                .decodeList<PyqMetaData>() // ✅ Convert response to PyqMetaData list
                .also { result ->
                    if (result.isEmpty()) Log.w(
                        "Supabase",
                        "⚠ No PDFs found for ${metaData.uploadsubject}"
                    )
                }
        } catch (e: Exception) {
            Log.e("Supabase", "❌ Fetch failed: ${e.message}", e)
            emptyList() // ✅ Ensure UI doesn't crash
        }
    }
}
