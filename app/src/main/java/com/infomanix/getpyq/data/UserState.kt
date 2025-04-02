package com.infomanix.getpyq.data

sealed class UserState {
    data object Guest : UserState()
    data class Uploader(
        val username: String,
        val useremail: String,
        val scholarId: String,
    ) : UserState()

    override fun toString(): String = when (this) {
        is Guest -> "UserState: Guest"
        is Uploader -> "UserState: Uploader(username=$username, useremail=$useremail, scholarId=$scholarId)"
    }
}
