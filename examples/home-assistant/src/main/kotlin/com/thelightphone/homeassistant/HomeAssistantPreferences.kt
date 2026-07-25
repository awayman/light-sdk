package com.thelightphone.homeassistant

import androidx.datastore.preferences.core.stringPreferencesKey

internal object HomeAssistantPreferences {
    val BASE_URL = stringPreferencesKey("homeassistant_base_url")
    val TOKEN = stringPreferencesKey("homeassistant_token")
}
