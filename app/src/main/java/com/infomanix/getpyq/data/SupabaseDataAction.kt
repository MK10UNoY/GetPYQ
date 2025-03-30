package com.infomanix.getpyq.data
import kotlinx.serialization.Serializable

@Serializable
data class UploadMetadata(
    val filepath: String,
    val uploaderemail: String,
    val cloudurl: String,
    val uploadsem: Int,
    val uploadsubject: String,
    val uploadmonth: String,
    val uploadyear: String,
    val uploadtime: String,
    val uploadterm: String,
)
@Serializable
data class PyqMetaData(
    val filepath: String,
    val uploadsubject: String,
    val cloudurl: String,
    val uploadmonth: String,
    val uploadyear: String,
    val uploadtime: String,
    val uploadterm: String,
    )