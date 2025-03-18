package com.infomanix.getpyq.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    fun testSupabaseConnection() {
        viewModelScope.launch {
            try {
                val response = supabaseClient
                    .from("uploadtrackregister")
                    .select().decodeList<Map<String, Any>>() // ✅ Ensure this decodes properly

                Log.d("SupabaseTest", "✅ Supabase response: $response")
            } catch (e: Exception) {
                Log.e("SupabaseTest", "❌ Exception: ${e.message}")
            }
        }
    }
}
