package dev.chris.emvtlvparser.model

data class TlvItem(
    val tag: String,
    val rawLength: String,
    val length: Int,
    val value: String,
    val interpretation: String,
    val level: Int = 0,
    val isError: Boolean = false
)
