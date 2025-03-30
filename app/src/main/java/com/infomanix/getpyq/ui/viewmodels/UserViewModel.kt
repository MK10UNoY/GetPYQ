package com.infomanix.getpyq.ui.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomanix.getpyq.data.UserState
import com.infomanix.getpyq.utils.AuthManagerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val _userState = MutableStateFlow<UserState>(UserState.Guest)
    val userState: StateFlow<UserState> = _userState

    private val _userType = MutableLiveData<String>()
    val userType: LiveData<String> get() = _userType
    // ✅ Set user state manually
    fun setUserState(state: UserState) {
        _userState.value = state
    }

    // ✅ Check and load saved login state
    fun loadUserState(context: Context) {
        viewModelScope.launch {
            val userType = AuthManagerUtils.loadUserType(context)
            val scholarId = AuthManagerUtils.loadScholarId(context)
            val username = AuthManagerUtils.loadUserName(context)
            if (AuthManagerUtils.isLoggedIn() && userType == "uploader") {
                _userState.value = UserState.Uploader(
                    username = username,
                    AuthManagerUtils.getCurrentUserEmail() ?: "",
                    scholarId = scholarId
                )
            } else {
                _userState.value = UserState.Guest
            }
        }
    }
    fun logout(context: Context) {
        viewModelScope.launch {
            AuthManagerUtils.logout(context)  // Clears stored user data
            _userState.value = UserState.Guest // Reset state to Guest
        }
    }
}
