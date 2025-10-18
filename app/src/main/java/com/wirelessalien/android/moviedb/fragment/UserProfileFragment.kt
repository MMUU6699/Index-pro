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
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wirelessalien.android.moviedb.R
import com.wirelessalien.android.moviedb.activity.LoginActivity
import com.wirelessalien.android.moviedb.databinding.FragmentUserProfileBinding
import com.wirelessalien.android.moviedb.helper.MovieDatabaseHelper

class UserProfileFragment : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseHelper: MovieDatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        databaseHelper = MovieDatabaseHelper(requireContext())
        sharedPreferences = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        
        setupLoginButton()
        loadUserStats()
    }
    
    private fun setupLoginButton() {
        val isLoggedIn = sharedPreferences.getBoolean("user_logged_in", false)
        
        if (isLoggedIn) {
            binding.loginButton.visibility = View.GONE
            binding.logoutButton.visibility = View.VISIBLE
            
            binding.logoutButton.setOnClickListener {
                logout()
            }
        } else {
            binding.loginButton.visibility = View.VISIBLE
            binding.logoutButton.visibility = View.GONE
            
            binding.loginButton.setOnClickListener {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }
    
    private fun logout() {
        sharedPreferences.edit().apply {
            clear()
            apply()
        }
        
        // Refresh the UI
        setupLoginButton()
        loadUserStats()
    }

    private fun loadUserStats() {
        val movieCount = getMovieCount()
        val tvShowCount = getTVShowCount()
        val favoriteCount = getFavoriteCount()
        val watchlistCount = getWatchlistCount()

        binding.apply {
            movieCountText.text = movieCount.toString()
            tvShowCountText.text = tvShowCount.toString()
            favoriteCountText.text = favoriteCount.toString()
            watchlistCountText.text = watchlistCount.toString()
            
            // Set user name based on login status
            val isLoggedIn = sharedPreferences.getBoolean("user_logged_in", false)
            if (isLoggedIn) {
                val userName = sharedPreferences.getString("user_name", "")
                val userEmail = sharedPreferences.getString("user_email", "")
                userNameText.text = userName?.ifEmpty { getString(R.string.app_name) } ?: getString(R.string.app_name)
                userEmailText.text = userEmail?.ifEmpty { "Billie Eilish Fan" } ?: "Billie Eilish Fan"
            } else {
                userNameText.text = getString(R.string.guest_user)
                userEmailText.text = getString(R.string.not_logged_in)
            }
        }
    }

    private fun getMovieCount(): Int {
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${MovieDatabaseHelper.TABLE_MOVIES} WHERE ${MovieDatabaseHelper.COLUMN_MOVIE} = 1", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }
    
    private fun getTVShowCount(): Int {
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(DISTINCT ${MovieDatabaseHelper.COLUMN_MOVIES_ID}) FROM ${MovieDatabaseHelper.TABLE_MOVIES} WHERE ${MovieDatabaseHelper.COLUMN_MOVIE} = 0", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }
    
    private fun getFavoriteCount(): Int {
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${MovieDatabaseHelper.TABLE_MOVIES} WHERE ${MovieDatabaseHelper.COLUMN_CATEGORIES} = 2", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }
    
    private fun getWatchlistCount(): Int {
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${MovieDatabaseHelper.TABLE_MOVIES} WHERE ${MovieDatabaseHelper.COLUMN_CATEGORIES} = 3", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}