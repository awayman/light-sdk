package com.thelightphone.frigate

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.thelightphone.sdk.LightViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DEFAULT_STREAM_URL = "http://frigate.local:5000/camera-name/index.m3u8"

class FrigateViewModel(
    private val dataStore: DataStore<Preferences>,
) : LightViewModel<Unit>() {
    private val _streamUrl = MutableStateFlow(DEFAULT_STREAM_URL)
    val streamUrl: StateFlow<String> = _streamUrl.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = dataStore.data.first()
            _streamUrl.value = prefs[FrigatePreferences.STREAM_URL] ?: DEFAULT_STREAM_URL
        }
    }

    fun saveStreamUrl(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.edit { prefs -> prefs[FrigatePreferences.STREAM_URL] = url }
            withContext(Dispatchers.Main) { _streamUrl.value = url }
        }
    }
}
