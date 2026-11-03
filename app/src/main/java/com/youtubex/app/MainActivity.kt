package com.youtubex.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
    private val adapter = VideoAdapter { openPlayer(it) }
    private var lastQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        YtClient.init(applicationContext)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val q = query?.trim().orEmpty()
                if (q.isNotEmpty()) {
                    binding.searchView.clearFocus()
                    search(q)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        binding.swipeRefresh.setOnRefreshListener {
            val q = lastQuery
            if (q != null) search(q) else loadTrending()
        }

        loadTrending()
    }

    private fun loadTrending() {
        setBusy(true, "Trending load ho raha hai...")
        lifecycleScope.launch {
            val items = runCatching { withContext(Dispatchers.IO) { YtClient.trending() } }
            items.onSuccess { list ->
                showItems(
                    list,
                    if (list.isEmpty()) "Trending khali hai. Upar se kuch search karo." else null
                )
            }.onFailure { error ->
                showItems(emptyList(), "Trending load nahi hua: ${describe(error)}\nUpar se search karke dekho.")
            }
        }
    }

    private fun search(query: String) {
        lastQuery = query
        setBusy(true, "\"$query\" search ho raha hai...")
        lifecycleScope.launch {
            val result = runCatching { withContext(Dispatchers.IO) { YtClient.search(query) } }
            result.onSuccess { list ->
                showItems(list, if (list.isEmpty()) "Kuch nahi mila: \"$query\"" else null)
            }.onFailure { error ->
                showItems(emptyList(), "Search fail hua: ${describe(error)}")
            }
        }
    }

    private fun showItems(items: List<VideoItem>, message: String?) {
        adapter.submit(items)
        setBusy(false, message)
        binding.emptyText.isVisible = items.isEmpty()
    }

    private fun setBusy(busy: Boolean, message: String?) {
        binding.progressBar.isVisible = busy
        binding.swipeRefresh.isRefreshing = false
        if (message != null) binding.emptyText.text = message
        binding.emptyText.isVisible = !busy && binding.emptyText.text.isNotEmpty()
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
