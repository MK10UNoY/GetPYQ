package com.infomanix.getpyq.repository

import kotlinx.serialization.Serializable

@Serializable
data class Upload(
    val filepath: String
)
