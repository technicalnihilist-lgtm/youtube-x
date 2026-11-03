package com.youtubex.app.model

data class VideoItem(
    val url: String,
    val title: String,
    val uploader: String,
    val durationSeconds: Long,
    val views: Long,
    val thumbnailUrl: String,
    val avatarUrl: String = ""
)

enum class SourceKind {
    PROGRESSIVE,
    HLS,
    DASH
}

data class Playable(
    val url: String,
    val kind: SourceKind,
    val title: String,
    val uploader: String
)
