package com.infomanix.getpyq.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun insertUpload(uploadData: Map<String, Any>) {
        viewModelScope.launch {
            repository.insertUploadRecord(uploadData)
        }
    }
}
