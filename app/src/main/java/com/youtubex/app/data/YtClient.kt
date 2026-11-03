package com.youtubex.app.data

import android.content.Context
import com.youtubex.app.model.Playable
import com.youtubex.app.model.SourceKind
import com.youtubex.app.model.VideoItem
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

/**
 * NewPipeExtractor ke around ek patla wrapper.
 * Saari calls background thread par chalao (network hota hai).
 */
object YtClient {

    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            NewPipe.init(DownloaderImpl.getInstance(), Localization("en", "IN"))
            initialized = true
        }
    }

    /** YouTube search -> video list */
    fun search(query: String): List<VideoItem> {
        val service = ServiceList.YouTube
        val queryHandler = service.searchQHFactory.fromQuery(query)
        val extractor = service.getSearchExtractor(queryHandler)
        extractor.fetchPage()
        return extractor.initialPage.items
            .filterIsInstance<StreamInfoItem>()
            .map { toVideoItem(it) }
    }

    /**
     * Home screen ke liye feed.
     *
     * YouTube ka "default" kiosk (live / Trending) ab kaam nahi karta aur NPE deta hai,
     * isliye hum ek-ek kiosk try karte hain aur jo pehla chale wahi dikhate hain.
     * Sab fail ho jayein to search-based feed par gir jaate hain.
     */
    fun trending(): List<VideoItem> {
        val service = ServiceList.YouTube
        val kioskList = service.kioskList
        val preferred = listOf("trending_music", "trending_movies_and_shows")
        val order = preferred + kioskList.availableKiosks.filterNot { it in preferred }

        var lastError: Throwable? = null
        for (kioskId in order) {
            try {
                val extractor = kioskList.getExtractorById(kioskId, null)
                extractor.fetchPage()
                val items = extractor.initialPage.items
                    .filterIsInstance<StreamInfoItem>()
                    .map { toVideoItem(it) }
                if (items.isNotEmpty()) return items
            } catch (t: Throwable) {
                lastError = t
            }
        }

        // Fallback: koi bhi kiosk na chale to search se feed bana lo
        val fallback = search("trending music videos")
        if (fallback.isNotEmpty()) return fallback

        throw lastError ?: IllegalStateException("Feed load nahi hua")
    }

    /**
     * Ek video page URL se chalane layak stream nikalta hai.
     * Pehle normal (video+audio) progressive stream, phir HLS, phir DASH.
     */
    fun playable(videoUrl: String): Playable {
        val service = ServiceList.YouTube
        val info = StreamInfo.getInfo(service, videoUrl)
        val title = info.name.orEmpty()
        val uploader = info.uploaderName.orEmpty()

        // 1) Progressive (muxed) stream - ek hi URL me video + audio
        val progressive = info.videoStreams
            .filter { it.isUrl && !it.content.isNullOrBlank() }
            .filter { it.format?.mimeType?.startsWith("video/") == true }
            .maxByOrNull { it.height }

        if (progressive != null) {
            return Playable(
                url = progressive.content,
                kind = SourceKind.PROGRESSIVE,
                title = title,
                uploader = uploader
            )
        }

        // 2) HLS
        val hls = info.hlsUrl
        if (!hls.isNullOrBlank()) {
            return Playable(hls, SourceKind.HLS, title, uploader)
        }

        // 3) DASH
        val dash = info.dashMpdUrl
        if (!dash.isNullOrBlank()) {
            return Playable(dash, SourceKind.DASH, title, uploader)
        }

        throw IllegalStateException("Is video ke liye koi playable stream nahi mila")
    }

    private fun toVideoItem(item: StreamInfoItem) = VideoItem(
        url = item.url,
        title = item.name.orEmpty(),
        uploader = item.uploaderName.orEmpty(),
        durationSeconds = item.duration,
        views = item.viewCount,
        thumbnailUrl = bestThumbnail(item.thumbnails),
        avatarUrl = bestThumbnail(item.uploaderAvatars)
    )

    private fun bestThumbnail(images: List<Image>?): String {
        if (images.isNullOrEmpty()) return ""
        val preferred = images
            .filter { it.height in 200..800 }
            .maxByOrNull { it.height }
        val chosen = preferred ?: images.maxByOrNull { it.height } ?: images.first()
        return chosen.url.orEmpty()
    }
}
