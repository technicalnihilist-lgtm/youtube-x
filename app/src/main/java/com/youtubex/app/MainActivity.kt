package com.youtubex.app

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.youtubex.app.data.YtClient
import com.youtubex.app.databinding.ActivityMainBinding
import com.youtubex.app.model.VideoItem
import com.youtubex.app.ui.VideoAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val feedAdapter = VideoAdapter { openPlayer(it) }
    private val searchAdapter = VideoAdapter { openPlayer(it) }

    private val chips = listOf("All", "Anime", "Cartoon", "Movies", "Music", "Gaming")
    private var activeChip = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        YtClient.init(applicationContext)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = feedAdapter
        binding.searchResults.layoutManager = LinearLayoutManager(this)
        binding.searchResults.adapter = searchAdapter

        buildChips()

        binding.iconSearch.setOnClickListener { openSearch() }
        binding.searchButton.setOnClickListener { openSearch() }
        binding.iconRefresh.setOnClickListener { loadFeed(activeChip) }
        binding.cancelSearch.setOnClickListener { closeSearch() }

        binding.searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                runSearch(binding.searchInput.text?.toString()?.trim().orEmpty())
                true
            } else {
                false
            }
        }

        binding.tabHome.setOnClickListener { selectTab(0); closeSearch(); loadFeed(activeChip) }
        binding.tabExplore.setOnClickListener { selectTab(1); openSearch() }
        binding.tabSubs.setOnClickListener { selectTab(2); closeSearch(); showSubsMessage() }

        selectTab(0)
        loadFeed(activeChip)
    }

    // ---------------- chips ----------------

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun buildChips() {
        binding.chipRow.removeAllViews()
        chips.forEach { name ->
            val chip = TextView(this).apply {
                text = name
                textSize = 13.5f
                typeface = Typeface.DEFAULT_BOLD
                setPadding(dp(16), dp(8), dp(16), dp(8))
                background = ContextCompat.getDrawable(this@MainActivity, R.drawable.chip_bg)
                isSelected = name == activeChip
                setTextColor(
                    ContextCompat.getColor(
                        this@MainActivity,
                        if (name == activeChip) R.color.bg else R.color.ink
                    )
                )
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = dp(8) }
                setOnClickListener { selectChip(name) }
            }
            binding.chipRow.addView(chip)
        }
    }

    private fun selectChip(name: String) {
        activeChip = name
        for (i in 0 until binding.chipRow.childCount) {
            val chip = binding.chipRow.getChildAt(i) as TextView
            val selected = chip.text.toString() == name
            chip.isSelected = selected
            chip.setTextColor(
                ContextCompat.getColor(this, if (selected) R.color.bg else R.color.ink)
            )
        }
        closeSearch()
        loadFeed(name)
    }

    // ---------------- tabs ----------------

    private fun selectTab(index: Int) {
        val icons = listOf(binding.tabHomeIcon, binding.tabExploreIcon, binding.tabSubsIcon)
        val labels = listOf(binding.tabHomeLabel, binding.tabExploreLabel, binding.tabSubsLabel)
        icons.forEachIndexed { i, icon ->
            tint(icon, if (i == index) R.color.red else R.color.mute)
            labels[i].setTextColor(
                ContextCompat.getColor(this, if (i == index) R.color.red else R.color.mute)
            )
        }
    }

    private fun tint(view: ImageView, colorRes: Int) {
        view.setColorFilter(ContextCompat.getColor(this, colorRes))
    }

    // ---------------- data ----------------

    private fun loadFeed(category: String) {
        setBusy(true, null)
        lifecycleScope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    if (category == "All") YtClient.trending() else YtClient.search("$category videos")
                }
            }
            result.onSuccess { list ->
                feedAdapter.submit(list)
                setBusy(false, if (list.isEmpty()) "Kuch nahi mila. Doosra chip try karo." else null)
            }.onFailure { error ->
                feedAdapter.submit(emptyList())
                setBusy(false, "Load nahi hua: ${describe(error)}")
            }
        }
    }

    private fun runSearch(query: String) {
        if (query.isBlank()) return
        hideKeyboard()
        binding.searchResults.isVisible = true
        setBusy(true, null)
        lifecycleScope.launch {
            val result = runCatching { withContext(Dispatchers.IO) { YtClient.search(query) } }
            result.onSuccess { list ->
                searchAdapter.submit(list)
                setBusy(false, if (list.isEmpty()) "Kuch nahi mila: \"$query\"" else null)
            }.onFailure { error ->
                searchAdapter.submit(emptyList())
                setBusy(false, "Search fail hua: ${describe(error)}")
            }
        }
    }

    private fun showSubsMessage() {
        feedAdapter.submit(emptyList())
        setBusy(false, "Subscriptions ke liye login chahiye — abhi app bina login chalta hai.")
    }

    private fun setBusy(busy: Boolean, message: String?) {
        binding.progressBar.isVisible = busy
        if (message != null) binding.emptyText.text = message
        binding.emptyText.isVisible = !busy && !message.isNullOrBlank()
    }

    // ---------------- search overlay ----------------

    private fun openSearch() {
        binding.searchOverlay.isVisible = true
        binding.searchInput.requestFocus()
        binding.searchInput.post {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT)
        }
        selectTab(1)
    }

    private fun closeSearch() {
        binding.searchOverlay.isVisible = false
        hideKeyboard()
        selectTab(0)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
    }

    private fun openPlayer(item: VideoItem) {
        startActivity(
            Intent(this, PlayerActivity::class.java)
                .putExtra(PlayerActivity.EXTRA_URL, item.url)
                .putExtra(PlayerActivity.EXTRA_TITLE, item.title)
        )
    }

    private fun describe(error: Throwable): String =
        error.message?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName
}
