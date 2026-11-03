package com.youtubex.app.util

import java.util.Locale

object Formats {

    fun duration(seconds: Long): String {
        if (seconds <= 0L) return ""
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            String.format(Locale.US, "%d:%02d:%02d", h, m, s)
        } else {
            String.format(Locale.US, "%d:%02d", m, s)
        }
    }

    fun views(count: Long): String {
        if (count < 0L) return ""
        return when {
            count >= 1_000_000_000L ->
                String.format(Locale.US, "%.1fB views", count / 1_000_000_000.0)
            count >= 1_000_000L ->
                String.format(Locale.US, "%.1fM views", count / 1_000_000.0)
            count >= 1_000L ->
                String.format(Locale.US, "%.1fK views", count / 1_000.0)
            else -> "$count views"
        }
    }

    fun metaLine(durationSeconds: Long, views: Long): String =
        listOf(duration(durationSeconds), views(views))
            .filter { it.isNotBlank() }
            .joinToString("  \u2022  ")
}
