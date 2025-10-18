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

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.wirelessalien.android.moviedb.R
import com.wirelessalien.android.moviedb.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var credentialManager: CredentialManager
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        private const val TAG = "LoginActivity"
        private const val PREF_USER_LOGGED_IN = "user_logged_in"
        private const val PREF_USER_NAME = "user_name"
        private const val PREF_USER_EMAIL = "user_email"
        private const val PREF_USER_PHOTO = "user_photo"
        // Google OAuth client ID - Replace with your actual client ID from Google Cloud Console
        private const val WEB_CLIENT_ID = "your-web-client-id.googleusercontent.com"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        credentialManager = CredentialManager.create(this)
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        
        // Check if user is already logged in
        if (isUserLoggedIn()) {
            navigateToMain()
            return
        }
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
        
        binding.skipButton.setOnClickListener {
            navigateToMain()
        }
        
        binding.appTitle.text = getString(R.string.app_name)
        binding.welcomeText.text = getString(R.string.welcome_to_app, getString(R.string.app_name))
    }
    
    private fun signInWithGoogle() {
        binding.googleSignInButton.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .build()
        
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        
        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity,
                )
                
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                
                // Save user data
                saveUserData(
                    name = googleIdTokenCredential.displayName ?: "",
                    email = googleIdTokenCredential.id,
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString() ?: ""
                )
                
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.login_successful),
                    Toast.LENGTH_SHORT
                ).show()
                
                navigateToMain()
                
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Sign in failed", e)
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.login_failed),
                    Toast.LENGTH_LONG
                ).show()
                
                binding.googleSignInButton.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
                
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(TAG, "Invalid Google ID token", e)
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.login_failed),
                    Toast.LENGTH_LONG
                ).show()
                
                binding.googleSignInButton.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun saveUserData(name: String, email: String, photoUrl: String) {
        sharedPreferences.edit().apply {
            putBoolean(PREF_USER_LOGGED_IN, true)
            putString(PREF_USER_NAME, name)
            putString(PREF_USER_EMAIL, email)
            putString(PREF_USER_PHOTO, photoUrl)
            apply()
        }
    }
    
    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(PREF_USER_LOGGED_IN, false)
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}