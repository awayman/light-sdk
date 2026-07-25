package com.thelightphone.homeassistant

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.thelightphone.sdk.LightViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeAssistantSettingsViewModel(
    private val dataStore: DataStore<Preferences>,
) : LightViewModel<Unit>() {
    private val _baseUrl = MutableStateFlow("")
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    private val _token = MutableStateFlow("")
    val token: StateFlow<String> = _token.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = dataStore.data.first()
            _baseUrl.value = prefs[HomeAssistantPreferences.BASE_URL] ?: ""
            _token.value = prefs[HomeAssistantPreferences.TOKEN] ?: ""
        }
    }

    fun saveBaseUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.edit { prefs -> prefs[HomeAssistantPreferences.BASE_URL] = url }
            withContext(Dispatchers.Main) { _baseUrl.value = url }
        }
    }

    fun saveToken(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.edit { prefs -> prefs[HomeAssistantPreferences.TOKEN] = token }
            withContext(Dispatchers.Main) { _token.value = token }
        }
    }
}
