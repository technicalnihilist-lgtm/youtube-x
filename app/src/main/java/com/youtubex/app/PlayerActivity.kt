package com.youtubex.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.youtubex.app.data.YtClient
import com.youtubex.app.databinding.ActivityPlayerBinding
import com.youtubex.app.model.Playable
import com.youtubex.app.model.SourceKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private var videoPageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        YtClient.init(applicationContext)

        videoPageUrl = intent.getStringExtra(EXTRA_URL)
        binding.title.text = intent.getStringExtra(EXTRA_TITLE).orEmpty()

        binding.btnCloseTop.setOnClickListener { finish() }
        binding.btnClose.setOnClickListener { finish() }
        binding.btnExternal.setOnClickListener { openOnYouTube() }

        load()
    }

    private fun load() {
        val url = videoPageUrl
        if (url.isNullOrBlank()) {
            showError("Video URL nahi mila")
            return
        }
        binding.progress.isVisible = true
        binding.error.isVisible = false

        lifecycleScope.launch {
            val result = runCatching { withContext(Dispatchers.IO) { YtClient.playable(url) } }
            binding.progress.isVisible = false
            result.onSuccess { playable ->
                binding.title.text = playable.title.ifBlank { binding.title.text }
                binding.uploader.text = playable.uploader
                binding.uploader.isVisible = playable.uploader.isNotBlank()
                startPlayback(playable)
            }.onFailure { error ->
                showError("Stream nahi mila: ${error.message ?: error.javaClass.simpleName}")
            }
        }
    }

    private fun startPlayback(playable: Playable) {
        val dataSourceFactory = DefaultDataSource.Factory(
            this,
            DefaultHttpDataSource.Factory().setUserAgent(USER_AGENT)
        )
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(playable.url))
            .apply {
                when (playable.kind) {
                    SourceKind.HLS -> setMimeType(MimeTypes.APPLICATION_M3U8)
                    SourceKind.DASH -> setMimeType(MimeTypes.APPLICATION_MPD)
                    SourceKind.PROGRESSIVE -> Unit
                }
            }
            .build()

        val exoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                showError("Player error: ${error.errorCodeName}")
            }
        })

        player = exoPlayer
        binding.playerView.player = exoPlayer
        exoPlayer.setMediaSource(mediaSourceFactory.createMediaSource(mediaItem))
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    private fun showError(message: String) {
        binding.error.text = message
        binding.error.isVisible = true
    }

    private fun openOnYouTube() {
        val url = videoPageUrl ?: return
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            showError("Koi app nahi mila jo is link ko khol sake")
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }
}
