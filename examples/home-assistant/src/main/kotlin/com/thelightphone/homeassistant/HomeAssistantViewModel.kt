package com.thelightphone.homeassistant

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

class HomeAssistantViewModel(
    private val dataStore: DataStore<Preferences>,
) : LightViewModel<Unit>() {
    private val api = HomeAssistantApi()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun fetchStates() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = dataStore.data.first()
            val baseUrl = prefs[HomeAssistantPreferences.BASE_URL] ?: "http://homeassistant.local:8123"
            val token = prefs[HomeAssistantPreferences.TOKEN] ?: ""

            val result = api.listStates(baseUrl, token)
            withContext(Dispatchers.Main) {
                result.fold(
                    onSuccess = { arr -> _message.value = "Fetched ${arr.size} entities" },
                    onFailure = { e -> _message.value = "Error: ${e.message}" },
                )
            }
        }
    }

    fun dismissMessage() {
        _message.value = null
    }

    override fun onCleared() {
        super.onCleared()
        api.close()
    }
}
