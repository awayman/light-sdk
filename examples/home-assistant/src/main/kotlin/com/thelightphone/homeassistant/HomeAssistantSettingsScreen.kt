package com.thelightphone.homeassistant

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.input.rememberTextFieldState
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.rememberKeyboardOptions
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.LightIcons

class HomeAssistantSettingsScreen(sealedActivity: SealedLightActivity) :
    LightScreen<Unit, HomeAssistantSettingsViewModel>(sealedActivity) {

    override val viewModelClass: Class<HomeAssistantSettingsViewModel>
        get() = HomeAssistantSettingsViewModel::class.java

    override fun createViewModel(): HomeAssistantSettingsViewModel = HomeAssistantSettingsViewModel(lightContext.dataStore)

    @Composable
    override fun Content() {
        val baseUrl by viewModel.baseUrl.collectAsState()
        val token by viewModel.token.collectAsState()
        val keyboardOptionsFlow = rememberKeyboardOptions()

        LightTheme {
            Column(modifier = Modifier.fillMaxSize()) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(
                        icon = LightIcons.BACK,
                        onClick = { goBack() },
                    ),
                    center = LightTopBarCenter.Text("Home Assistant Settings"),
                )

                LightTextField(
                    label = "Base URL",
                    value = baseUrl,
                    placeholder = "http://homeassistant.local:8123",
                    onClick = {
                        navigateTo({ act -> HomeAssistantEditorScreen(act, "Base URL", baseUrl) }) { result ->
                            if (result != null) viewModel.saveBaseUrl(result)
                        }
                    },
                )

                LightTextField(
                    label = "Bearer Token",
                    value = token,
                    placeholder = "(optional)",
                    onClick = {
                        navigateTo({ act -> HomeAssistantEditorScreen(act, "Bearer Token", token) }) { result ->
                            if (result != null) viewModel.saveToken(result)
                        }
                    },
                )

                LightBottomBar(
                    items = listOf(
                        LightBarButton.Text("DONE") { goBack() },
                    ),
                )
            }
        }
    }
}
