/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * For more information on setting up and running this sample code, see
 * https://firebase.google.com/docs/remote-config/android
 */

package com.google.samples.quickstart.config

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val mFirebaseRemoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance().apply {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build()
            setConfigSettings(configSettings)
            setDefaults(R.xml.remote_config_defaults)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fetchButton.setOnClickListener { fetchWelcome() }
        fetchWelcome()
    }

    /**
     * Fetch a welcome message from the Remote Config service, and then activate it.
     */
    private fun fetchWelcome() {
        welcomeTextView.text = mFirebaseRemoteConfig.getString(LOADING_PHRASE_CONFIG_KEY)

        var cacheExpiration: Long = 3600 // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (mFirebaseRemoteConfig.info.configSettings.isDeveloperModeEnabled) {
            cacheExpiration = 0
        }

        // [START fetch_config_with_callback]
        // cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
        // will use fetch data from the Remote Config service, rather than cached parameter values,
        // if cached parameter values are more than cacheExpiration seconds old.
        // See Best Practices in the README for more information.
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this) { task ->

                    val showShortLengthText = fun (message: String) : Unit {
                        Toast.makeText(this@MainActivity, message,
                                Toast.LENGTH_SHORT).show()
                    }

                    if (task.isSuccessful) {
                        showShortLengthText("Fetch Succeeded")

                        // After config data is successfully fetched, it must be activated before newly fetched
                        // values are returned.
                        mFirebaseRemoteConfig.activateFetched()
                    } else {
                        showShortLengthText("Fetch Failed")
                    }
                    displayWelcomeMessage()
                }
        // [END fetch_config_with_callback]
    }

    /**
     * Display a welcome message in all caps if welcome_message_caps is set to true. Otherwise,
     * display a welcome message as fetched from welcome_message.
     */
    // [START display_welcome_message]
    private fun displayWelcomeMessage() {
        welcomeTextView.run {
            setAllCaps(mFirebaseRemoteConfig.getBoolean(WELCOME_MESSAGE_CAPS_KEY))
            text = mFirebaseRemoteConfig.getString(WELCOME_MESSAGE_KEY)
        }
    }

    companion object {

        private val TAG = "MainActivity"

        // Remote Config keys
        private val LOADING_PHRASE_CONFIG_KEY = "loading_phrase"
        private val WELCOME_MESSAGE_KEY = "welcome_message"
        private val WELCOME_MESSAGE_CAPS_KEY = "welcome_message_caps"
    }
    // [END display_welcome_message]
}
