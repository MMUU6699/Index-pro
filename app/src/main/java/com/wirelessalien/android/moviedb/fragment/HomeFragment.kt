/*
 *     This file is part of "ShowCase" formerly Movie DB. <https://github.com/WirelessAlien/MovieDB>
 *     forked from <https://notabug.org/nvb/MovieDB>
 *
 *     Copyright (C) 2024  WirelessAlien <https://github.com/WirelessAlien>
 *
 *     ShowCase is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ShowCase is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with "ShowCase".  If not, see <https://www.gnu.org/licenses/>.
 */
package com.wirelessalien.android.moviedb.fragment

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.CarouselSnapHelper
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.wirelessalien.android.moviedb.R
import com.wirelessalien.android.moviedb.activity.BaseActivity
import com.wirelessalien.android.moviedb.activity.MainActivity
import com.wirelessalien.android.moviedb.activity.PersonActivity
import com.wirelessalien.android.moviedb.adapter.NowPlayingMovieAdapter
import com.wirelessalien.android.moviedb.adapter.TrendingPagerAdapter
import com.wirelessalien.android.moviedb.databinding.ActivityMainBinding
import com.wirelessalien.android.moviedb.databinding.FragmentHomeBinding
import com.wirelessalien.android.moviedb.helper.ConfigHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.Date
import java.util.Locale

class HomeFragment : BaseFragment() {
    private var loading = true
    private var apiKey: String? = null
    private var mShowListLoaded = false
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val date = Date()
    private lateinit var mHomeShowAdapter: NowPlayingMovieAdapter
    private lateinit var mTVShowArrayList: ArrayList<JSONObject>
    private lateinit var mHomeShowArrayList: ArrayList<JSONObject>
    private lateinit var mUpcomingTVShowArrayList: ArrayList<JSONObject>
    private lateinit var mUpcomingMovieArrayList: ArrayList<JSONObject>
    private lateinit var mTVShowAdapter: NowPlayingMovieAdapter
    private lateinit var mUpcomingMovieAdapter: NowPlayingMovieAdapter
    private lateinit var mUpcomingTVAdapter: NowPlayingMovieAdapter
    private lateinit var binding: FragmentHomeBinding
    private lateinit var activityBinding: ActivityMainBinding
    private lateinit var menuProvider: MenuProvider
    private var centeredView: View? = null
    private val snapHelper = CarouselSnapHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        apiKey = ConfigHelper.getConfigValue(
            requireContext().applicationContext,
            "api_read_access_token"
        )
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        createShowList()
    }

    override fun doNetworkWork() {
        if (!mShowListLoaded) {
            lifecycleScope.launch {
                fetchNowPlayingMovies()
                fetchNowPlayingTVShows()
                fetchTrendingList()
                fetchUpcomingMovies()
                fetchUpcomingTVShows()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val fragmentView = binding.root
        activityBinding = (activity as MainActivity).getBinding()
        showMovieList()
        showTVShowList()
        showTrendingList()
        showUpcomingMovieList()
        showUpcomingTVShowList()
        activityBinding.fab2.visibility = View.GONE
        activityBinding.fab.visibility = View.GONE
        activityBinding.searchView.setupWithSearchBar(binding.searchbar)

        binding.searchbar.inflateMenu(R.menu.menu_person)

        binding.searchbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_person -> {
                    val intent = Intent(requireContext(), PersonActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_notification, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_notifications -> {
                        val notificationFragment = NotificationBottomSheet()
                        notificationFragment.show(childFragmentManager, "notification")
                        true
                    }

                    else -> false
                }
            }
        }
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
    }

//    private fun hasNotifications(): Boolean {
//        val dbHelper = NotificationDatabaseHelper(requireContext())
//        val allNotifications = dbHelper.getAllNotifications()
//        val (pastAndPresent, _) = allNotifications.partition {
//            val notificationDate = SimpleDateFormat(
//                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
//                Locale.getDefault()
//            ).parse(it.date)
//            notificationDate?.before(Date()) ?: true
//        }
//        return pastAndPresent.isNotEmpty()
//    }

    private fun showTrendingList() {
        val layoutManager = CarouselLayoutManager()
        binding.trendingViewPager.layoutManager = layoutManager
        snapHelper.attachToRecyclerView(binding.trendingViewPager)
        val adapter = TrendingPagerAdapter(ArrayList())
        binding.trendingViewPager.adapter = adapter

        binding.trendingViewPager.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val centerView = snapHelper.findSnapView(layoutManager)
                    if (centerView != null && centerView != centeredView) {
                        centeredView?.let {
                            val movieImage = it.findViewById<ImageView>(R.id.movieImage)
                            val zoomOut = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_out)
                            movieImage.startAnimation(zoomOut)
                        }
                        val movieImage = centerView.findViewById<ImageView>(R.id.movieImage)
                        val zoomIn = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in)
                        movieImage.startAnimation(zoomIn)
                        centeredView = centerView
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        activityBinding.fab.visibility = View.GONE
        activityBinding.fab2.visibility = View.GONE
        if (preferences.getBoolean("key_show_continue_watching", true) && preferences.getString("sync_provider", "local") == "local") {
            activityBinding.upnextChip.visibility = View.VISIBLE
        } else {
            activityBinding.upnextChip.visibility = View.GONE
        }
    }

    private fun createShowList() {
        mHomeShowArrayList = ArrayList()
        mHomeShowAdapter = NowPlayingMovieAdapter(mHomeShowArrayList)
        mTVShowArrayList = ArrayList()
        mTVShowAdapter = NowPlayingMovieAdapter(mTVShowArrayList)
        mUpcomingTVShowArrayList = ArrayList()
        mUpcomingTVAdapter = NowPlayingMovieAdapter(mUpcomingTVShowArrayList)
        mUpcomingMovieArrayList = ArrayList()
        mUpcomingMovieAdapter = NowPlayingMovieAdapter(mUpcomingMovieArrayList)
        (requireActivity() as BaseActivity).checkNetwork()
    }

    private fun setupRecyclerView(
        recyclerView: RecyclerView?,
        layoutManager: LinearLayoutManager,
        adapter: RecyclerView.Adapter<*>?
    ) {
        recyclerView!!.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }

    private fun showMovieList() {
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        setupRecyclerView(binding.nowPlayingRecyclerView, layoutManager, mHomeShowAdapter)
    }

    private fun showTVShowList() {
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        setupRecyclerView(binding.nowPlayingTVRecyclerView, layoutManager, mTVShowAdapter)
    }

    private fun showUpcomingTVShowList() {
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        setupRecyclerView(binding.upcomingTVRecyclerView, layoutManager, mUpcomingTVAdapter)
    }

    private fun showUpcomingMovieList() {
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        setupRecyclerView(binding.upcomingMovieRecyclerView, layoutManager, mUpcomingMovieAdapter)
    }

    private suspend fun fetchUpcomingTVShows() {
        withContext(Dispatchers.Main) {
            binding.shimmerFrameLayout5.visibility = View.VISIBLE
            binding.shimmerFrameLayout5.startShimmer()
        }
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val dateAfterWeek = calendar.time
        val url = URL("https://api.themoviedb.org/3/discover/tv" + BaseActivity.getLanguageParameter2(requireContext()) + "&page=1&sort_by=popularity.desc&" + BaseActivity.getRegionParameter2(requireContext())  + "&with_watch_monetization_types=flatrate|free|ads|rent|buy&&air_date.lte=" + dateFormat.format(dateAfterWeek) + "&air_date.gte=" + dateFormat.format(date) + "&" + BaseActivity.getTimeZoneParameter(requireContext()))
        val response = fetchData(url)
        withContext(Dispatchers.Main) {
            if (isAdded && !response.isNullOrEmpty()) {
                handleUpcomingTVResponse(response)
            } else {
                binding.shimmerFrameLayout5.visibility = View.GONE
                binding.shimmerFrameLayout5.stopShimmer()
            }
        }
    }

    private suspend fun fetchNowPlayingTVShows() {
        withContext(Dispatchers.Main) {
            binding.shimmerFrameLayout3.visibility = View.VISIBLE
            binding.shimmerFrameLayout3.startShimmer()
        }
        val url = URL("https://api.themoviedb.org/3/discover/tv" + BaseActivity.getLanguageParameter2(requireContext()) + "&page=1&sort_by=popularity.desc&" + BaseActivity.getRegionParameter2(requireContext())  + "&with_watch_monetization_types=flatrate|free|ads|rent|buy&air_date.lte=" + dateFormat.format(date)+ "&air_date.gte=" + dateFormat.format(date) + "&" + BaseActivity.getTimeZoneParameter(requireContext()))
        val response = fetchData(url)
        withContext(Dispatchers.Main) {
            if (isAdded && !response.isNullOrEmpty()) {
                handleTVResponse(response)
            } else {
                binding.shimmerFrameLayout3.visibility = View.GONE
                binding.shimmerFrameLayout3.stopShimmer()
            }
        }
    }

    private suspend fun fetchUpcomingMovies() {
        withContext(Dispatchers.Main) {
            binding.shimmerFrameLayout4.visibility = View.VISIBLE
            binding.shimmerFrameLayout4.startShimmer()
        }
        val url = URL("https://api.themoviedb.org/3/movie/upcoming" + BaseActivity.getLanguageParameter2(requireContext()) + "&page=1" + "&" + BaseActivity.getRegionParameter(requireContext()))
        val response = fetchData(url)
        withContext(Dispatchers.Main) {
            if (isAdded && !response.isNullOrEmpty()) {
                handleUpcomingMovieResponse(response)
            } else {
                binding.shimmerFrameLayout4.visibility = View.GONE
                binding.shimmerFrameLayout4.stopShimmer()
            }
        }
    }

    private suspend fun fetchNowPlayingMovies() {
        withContext(Dispatchers.Main) {
            binding.shimmerFrameLayout2.visibility = View.VISIBLE
            binding.shimmerFrameLayout2.startShimmer()
        }
        val url = URL("https://api.themoviedb.org/3/movie/now_playing" + BaseActivity.getLanguageParameter2(requireContext()) + "&page=1" + "&" + BaseActivity.getRegionParameter(requireContext()))
        val response = fetchData(url)
        withContext(Dispatchers.Main) {
            if (isAdded && !response.isNullOrEmpty()) {
                handleMovieResponse(response)
            } else {
                binding.shimmerFrameLayout2.visibility = View.GONE
                binding.shimmerFrameLayout2.stopShimmer()
            }
        }
    }

    private suspend fun fetchTrendingList() {
        withContext(Dispatchers.Main) {
            binding.shimmerFrameLayout1.visibility = View.VISIBLE
            binding.shimmerFrameLayout1.startShimmer()
        }
        val url = URL("https://api.themoviedb.org/3/trending/all/day" + BaseActivity.getLanguageParameter2(requireContext()))
        val response = fetchData(url)
        withContext(Dispatchers.Main) {
            if (isAdded && !response.isNullOrEmpty()) {
                handleTrendingResponse(response)
            } else {
                binding.shimmerFrameLayout1.visibility = View.GONE
                binding.shimmerFrameLayout1.stopShimmer()
            }
        }
    }

    private suspend fun fetchData(url: URL): String? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Content-Type", "application/json;charset=utf-8")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .build()
                client.newCall(request).execute().use { res ->
                    res.body?.string()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun handleMovieResponse(response: String?) {
        if (isAdded && !response.isNullOrEmpty()) {
            try {
                val reader = JSONObject(response)
                val arrayData = reader.getJSONArray("results")
                mHomeShowArrayList.clear()
                for (i in 0 until arrayData.length()) {
                    val websiteData = arrayData.getJSONObject(i)
                    mHomeShowArrayList.add(websiteData)
                }
                binding.nowPlayingRecyclerView.adapter = mHomeShowAdapter
                mShowListLoaded = true
                binding.shimmerFrameLayout2.visibility = View.GONE
                binding.shimmerFrameLayout2.stopShimmer()
            } catch (je: JSONException) {
                je.printStackTrace()
                binding.shimmerFrameLayout2.visibility = View.GONE
                binding.shimmerFrameLayout2.stopShimmer()
            }
        } else {
            binding.shimmerFrameLayout2.visibility = View.GONE
            binding.shimmerFrameLayout2.stopShimmer()
        }
        loading = false
    }

    private fun handleTVResponse(response: String?) {
        if (isAdded && !response.isNullOrEmpty()) {
            try {
                val reader = JSONObject(response)
                val arrayData = reader.getJSONArray("results")
                mTVShowArrayList.clear()
                for (i in 0 until arrayData.length()) {
                    val websiteData = arrayData.getJSONObject(i)
                    mTVShowArrayList.add(websiteData)
                }
                binding.nowPlayingTVRecyclerView.adapter = mTVShowAdapter
                mShowListLoaded = true
                binding.shimmerFrameLayout3.visibility = View.GONE
                binding.shimmerFrameLayout3.stopShimmer()
            } catch (je: JSONException) {
                je.printStackTrace()
                binding.shimmerFrameLayout3.visibility = View.GONE
                binding.shimmerFrameLayout3.stopShimmer()
            }
        } else {
            binding.shimmerFrameLayout3.visibility = View.GONE
            binding.shimmerFrameLayout3.stopShimmer()
        }
        loading = false
    }

    private fun handleUpcomingTVResponse(response: String?) {
        if (isAdded && !response.isNullOrEmpty()) {
            try {
                val reader = JSONObject(response)
                val arrayData = reader.getJSONArray("results")
                mUpcomingTVShowArrayList.clear()
                for (i in 0 until arrayData.length()) {
                    val websiteData = arrayData.getJSONObject(i)
                    mUpcomingTVShowArrayList.add(websiteData)
                }
                binding.upcomingTVRecyclerView.adapter = mUpcomingTVAdapter
                mShowListLoaded = true
                binding.shimmerFrameLayout5.visibility = View.GONE
                binding.shimmerFrameLayout5.stopShimmer()
            } catch (je: JSONException) {
                je.printStackTrace()
                binding.shimmerFrameLayout5.visibility = View.GONE
                binding.shimmerFrameLayout5.stopShimmer()
            }
        } else {
            binding.shimmerFrameLayout5.visibility = View.GONE
            binding.shimmerFrameLayout5.stopShimmer()
        }
        loading = false
    }

    private fun handleUpcomingMovieResponse(response: String?) {
        if (isAdded && !response.isNullOrEmpty()) {
            try {
                val reader = JSONObject(response)
                val arrayData = reader.getJSONArray("results")
                mUpcomingMovieArrayList.clear()
                for (i in 0 until arrayData.length()) {
                    val websiteData = arrayData.getJSONObject(i)
                    mUpcomingMovieArrayList.add(websiteData)
                }
                binding.upcomingMovieRecyclerView.adapter = mUpcomingMovieAdapter
                mShowListLoaded = true
                binding.shimmerFrameLayout4.visibility = View.GONE
                binding.shimmerFrameLayout4.stopShimmer()
            } catch (je: JSONException) {
                je.printStackTrace()
                binding.shimmerFrameLayout4.visibility = View.GONE
                binding.shimmerFrameLayout4.stopShimmer()
            }
        } else {
            binding.shimmerFrameLayout4.visibility = View.GONE
            binding.shimmerFrameLayout4.stopShimmer()
        }
        loading = false
    }

    private fun handleTrendingResponse(response: String?) {
        if (isAdded && !response.isNullOrEmpty()) {
            val trendingArrayList = ArrayList<JSONObject>()
            try {
                val reader = JSONObject(response)
                val arrayData = reader.getJSONArray("results")
                trendingArrayList.clear()
                for (i in 0 until arrayData.length()) {
                    val websiteData = arrayData.getJSONObject(i)
                    val mediaType = websiteData.getString("media_type")
                    if (mediaType == "movie" || mediaType == "tv") {
                        trendingArrayList.add(websiteData)
                    }
                }
                val adapter = binding.trendingViewPager.adapter as TrendingPagerAdapter?
                if (adapter != null) {
                    centeredView?.findViewById<ImageView>(R.id.movieImage)?.clearAnimation()
                    centeredView = null
                    adapter.updateData(trendingArrayList)
                    adapter.notifyDataSetChanged()
                    binding.trendingViewPager.post {
                        val layoutManager = binding.trendingViewPager.layoutManager as CarouselLayoutManager
                        val centerView = snapHelper.findSnapView(layoutManager)
                        if (centerView != null) {
                            val movieImage = centerView.findViewById<ImageView>(R.id.movieImage)
                            val zoomIn = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_in)
                            movieImage.startAnimation(zoomIn)
                            centeredView = centerView
                        }
                    }
                }
                mShowListLoaded = true
                binding.shimmerFrameLayout1.visibility = View.GONE
                binding.shimmerFrameLayout1.stopShimmer()
            } catch (je: JSONException) {
                je.printStackTrace()
                binding.shimmerFrameLayout1.visibility = View.GONE
                binding.shimmerFrameLayout1.stopShimmer()
            }
        } else {
            binding.shimmerFrameLayout1.visibility = View.GONE
            binding.shimmerFrameLayout1.stopShimmer()
        }
    }

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }
}
