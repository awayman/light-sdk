package com.thelightphone.frigate

import androidx.datastore.preferences.core.stringPreferencesKey

internal object FrigatePreferences {
    val STREAM_URL = stringPreferencesKey("frigate_stream_url")
}
