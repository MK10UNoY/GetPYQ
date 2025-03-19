package com.infomanix.getpyq.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomanix.getpyq.data.PyqMetaData
import com.infomanix.getpyq.data.UploadMetadata
import com.infomanix.getpyq.repository.UploadTrackingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadTrackingViewModel @Inject constructor(
    private val repository: UploadTrackingRepository
) : ViewModel() {

    private val _uploadList = MutableLiveData<List<Map<String, Any>>>()
    val uploadList: LiveData<List<Map<String, Any>>> = _uploadList

    fun fetchUploads(email: String) {
        viewModelScope.launch {
            try {
                Log.d("UploadTracking", "Fetching uploads for email: $email")
                val uploads = repository.fetchUploadsByEmail(email)
                if (uploads.isEmpty()) {
                    Log.w("UploadTracking", "No uploads found for $email")
                }
                _uploadList.postValue(uploads) // ✅ Safe LiveData update
            } catch (e: Exception) {
                Log.e("UploadTracking", "Error fetching uploads: ${e.message}", e)
                _uploadList.postValue(emptyList()) // ✅ Ensure UI still works
            }
        }
    }
    private val _pdfList = MutableLiveData<List<PyqMetaData>>() // ✅ LiveData for UI
    val pdfList: LiveData<List<PyqMetaData>> = _pdfList
    fun fetchSubjectPdfUrls(metaData: PyqMetaData) {
        viewModelScope.launch {
            try {
                Log.d("UploadTracking", "Fetching PDFs for: $metaData")
                val uploads = repository.fetchSubjectPdfUrls(metaData) // ✅ Fetch from repository
                _pdfList.postValue(uploads) // ✅ Update LiveData for UI
            } catch (e: Exception) {
                Log.e("UploadTracking", "❌ Error fetching PDFs: ${e.message}", e)
                _pdfList.postValue(emptyList()) // ✅ Prevent crashes by returning empty list
            }
        }
    }
    /**
     * ✅ Insert upload record into Supabase.
     */
    fun insertUpload(uploadData: UploadMetadata) {
        viewModelScope.launch {
            try {
                repository.insertUploadRecord(uploadData)
            } catch (e: Exception) {
                Log.e("UploadTracking", "❌ Error inserting upload: ${e.message}", e)
            }
        }
    }
}
