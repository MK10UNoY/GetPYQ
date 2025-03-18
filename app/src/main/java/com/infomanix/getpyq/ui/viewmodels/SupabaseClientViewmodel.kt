package com.infomanix.getpyq.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject

@HiltViewModel
class SupabaseViewModel @Inject constructor(
    val supabaseClient: SupabaseClient // ✅ Injected automatically
) : ViewModel()
