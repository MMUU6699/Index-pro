/*
 *     This file is part of "Billie Eilish" formerly Movie DB. <https://github.com/WirelessAlien/MovieDB>
 *     forked from <https://notabug.org/nvb/MovieDB>
 *
 *     Copyright (C) 2024  WirelessAlien <https://github.com/WirelessAlien>
 *
 *     Billie Eilish is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Billie Eilish is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with "Billie Eilish".  If not, see <https://www.gnu.org/licenses/>.
 */

package com.wirelessalien.android.moviedb.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wirelessalien.android.moviedb.R
import com.wirelessalien.android.moviedb.databinding.ActivityVideoPlayerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class VideoPlayerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityVideoPlayerBinding
    private var movieId: String? = null
    private var movieTitle: String? = null
    private var isMovie: Boolean = true
    private var seasonNumber: Int = 0
    private var episodeNumber: Int = 0
    
    companion object {
        const val EXTRA_MOVIE_ID = "movie_id"
        const val EXTRA_MOVIE_TITLE = "movie_title"
        const val EXTRA_IS_MOVIE = "is_movie"
        const val EXTRA_SEASON_NUMBER = "season_number"
        const val EXTRA_EPISODE_NUMBER = "episode_number"
        private const val TAG = "VideoPlayerActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get data from intent
        movieId = intent.getStringExtra(EXTRA_MOVIE_ID)
        movieTitle = intent.getStringExtra(EXTRA_MOVIE_TITLE)
        isMovie = intent.getBooleanExtra(EXTRA_IS_MOVIE, true)
        seasonNumber = intent.getIntExtra(EXTRA_SEASON_NUMBER, 0)
        episodeNumber = intent.getIntExtra(EXTRA_EPISODE_NUMBER, 0)
        
        setupToolbar()
        setupWebView()
        loadVideo()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = movieTitle ?: getString(R.string.video_player)
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.builtInZoomControls = false
        webSettings.displayZoomControls = false
        webSettings.setSupportZoom(false)
        webSettings.defaultTextEncodingName = "utf-8"
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.mediaPlaybackRequiresUserGesture = false
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        webSettings.databaseEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.userAgentString = "Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36"
        
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
                binding.loadingText.visibility = View.GONE
                binding.webView.visibility = View.VISIBLE
                Log.d(TAG, "Page finished loading: $url")
            }
            
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
                binding.loadingText.visibility = View.VISIBLE
                binding.errorText.visibility = View.GONE
                Log.d(TAG, "Page started loading: $url")
            }
            
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e(TAG, "WebView error: $errorCode - $description for URL: $failingUrl")
                binding.progressBar.visibility = View.GONE
                binding.loadingText.visibility = View.GONE
                binding.errorText.visibility = View.VISIBLE
                binding.errorText.text = getString(R.string.video_not_available)
            }
        }
        
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress < 100) {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.progressBar.progress = newProgress
                } else {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }
    
    private fun loadVideo() {
        if (movieId.isNullOrEmpty()) {
            showError(getString(R.string.video_not_available))
            return
        }
        
        binding.progressBar.visibility = View.VISIBLE
        binding.loadingText.text = getString(R.string.loading_video)
        
        // First try to get streaming links from SuperEmbed API
        getStreamingLinks()
    }
    
    private fun getStreamingLinks() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use SuperEmbed API directly
                loadSuperEmbedPlayer()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading video player", e)
                withContext(Dispatchers.Main) {
                    loadDirectEmbed()
                }
            }
        }
    }
    
    private fun loadSuperEmbedPlayer() {
        val superEmbedUrl = if (isMovie) {
            "https://www.superembed.stream/embed/movie?id=$movieId"
        } else {
            "https://www.superembed.stream/embed/tv?id=$movieId&s=$seasonNumber&e=$episodeNumber"
        }
        
        Log.d(TAG, "Loading SuperEmbed URL: $superEmbedUrl")
        binding.webView.loadUrl(superEmbedUrl)
    }
    
    private fun loadDirectEmbed() {
        // Use SuperEmbed as fallback too
        val embedUrl = if (isMovie) {
            "https://www.superembed.stream/embed/movie?id=$movieId"
        } else {
            "https://www.superembed.stream/embed/tv?id=$movieId&s=$seasonNumber&e=$episodeNumber"
        }
        
        Log.d(TAG, "Loading fallback SuperEmbed URL: $embedUrl")
        binding.webView.loadUrl(embedUrl)
    }
    
    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.loadingText.visibility = View.GONE
        binding.webView.visibility = View.GONE
        binding.errorText.visibility = View.VISIBLE
        binding.errorText.text = message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}