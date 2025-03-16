package com.infomanix.getpyq.data

sealed class UserState {
    data object Guest : UserState()
    data class Uploader(val username: String, val scholarId: String) : UserState()
}

