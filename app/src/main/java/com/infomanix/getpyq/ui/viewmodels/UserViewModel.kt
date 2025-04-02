package com.infomanix.getpyq.ui.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.infomanix.getpyq.data.UserState
import com.infomanix.getpyq.utils.AuthManagerUtils
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    @ApplicationContext private val context: Context // Injected application context
) : ViewModel() {
    private val _isLoading = MutableStateFlow(true) // 1️⃣ Add loading state
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _userState = MutableStateFlow<UserState>(UserState.Guest)
    val userState: StateFlow<UserState> = _userState
    init {
        // Automatically check for existing logged-in user on launch
        viewModelScope.launch {
            loadUserState(context)
        }

        // Firebase Auth Listener to detect user login/logout changes
        AuthManagerUtils.FAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            updateUserState(user, context)
        }
    }
    private fun updateUserState(user: FirebaseUser?,context: Context) {
        _userState.value = if (user != null) {
            UserState.Uploader(
                username = AuthManagerUtils.loadUserName(context),
                useremail = user.email ?: "",
                scholarId = AuthManagerUtils.loadScholarId(context)
            )
        } else {
            UserState.Guest
        }
    }
    fun signIn(email: String, password: String, onResult: (Boolean) -> Unit) {
        _isLoading.value = true
        AuthManagerUtils.FAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    onResult(true)
                } else {
                    Log.e("Auth", "Sign-in failed: ${task.exception?.message}")
                    onResult(false)
                }
            }
    }
    fun signOut() {
        AuthManagerUtils.FAuth.signOut()
        _userState.value = UserState.Guest
    }
    fun loadUserState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val userType = AuthManagerUtils.loadUserType(context)
                val scholarId = AuthManagerUtils.loadScholarId(context)
                val username = AuthManagerUtils.loadUserName(context)

                if (AuthManagerUtils.isLoggedIn() && userType == "uploader") {
                    _userState.value = UserState.Uploader(username, AuthManagerUtils.getCurrentUserEmail() ?: "", scholarId)
                } else {
                    _userState.value = UserState.Guest
                }
            } catch (e: Exception) {
                _userState.value = UserState.Guest
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun setUserState(state: UserState) {
        _userState.value = state
    }
}
