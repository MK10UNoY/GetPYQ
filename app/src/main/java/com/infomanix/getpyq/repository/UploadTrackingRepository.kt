package com.infomanix.getpyq.repository

import android.util.Log
import com.infomanix.getpyq.data.PyqMetaData
import com.infomanix.getpyq.data.UploadMetadata
import javax.inject.Inject
import javax.inject.Singleton
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import java.util.Locale


@Singleton
class UploadTrackingRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    suspend fun insertUploadRecord(uploadData: UploadMetadata) {
        supabase.postgrest["uploadtrackregister"]
            .insert(uploadData)
    }

    suspend fun fetchUploadsByEmail(email: String): List<Upload> {
        Log.d("UploadTracking", "Fetching uploads for email: $email")
        return try {
            supabase.postgrest["uploadtrackregister"]
                .select(columns = Columns.list("filepath")) { // Use the correct column name
                    filter { eq("uploaderemail", email) }
                }
                .decodeList<Upload>() // Decode directly to the Upload data class
                .also { result ->
                    if (result.isEmpty()) Log.w("UploadTracking", "No uploads found for $email")
                }
        } catch (e: Exception) {
            Log.e("Supabase", "Fetch failed: ${e.message}", e)
            emptyList()
        }
    }


    suspend fun fetchSubjectPdfUrls(metaData: PyqMetaData): List<PyqMetaData> {
        return try {
            Log.d(
                "Supabase",
                "Fetching PDFs for ${metaData.uploadsubject}"
            )
            val searchTerm = metaData.uploadterm

            supabase.postgrest["uploadtrackregister"]
                .select {
                    filter { eq("uploadsubject", metaData.uploadsubject) }
                    filter { ilike("uploadterm", "$searchTerm%") }
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
