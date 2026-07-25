package com.thelightphone.homeassistant

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightFullscreenModal
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.LightIcons

@InitialScreen
class HomeAssistantHomeScreen(sealedActivity: SealedLightActivity) :
    LightScreen<Unit, HomeAssistantViewModel>(sealedActivity) {

    override val viewModelClass: Class<HomeAssistantViewModel>
        get() = HomeAssistantViewModel::class.java

    override fun createViewModel(): HomeAssistantViewModel = HomeAssistantViewModel(lightContext.dataStore)

    @Composable
    override fun Content() {
        val message by viewModel.message.collectAsState()

        LightTheme {
            Column(modifier = Modifier.fillMaxSize()) {
                LightTopBar(
                    center = LightTopBarCenter.Text("Home Assistant"),
                    rightButton = LightBarButton.LightIcon(
                        icon = LightIcons.SETTINGS,
                        onClick = { navigateTo({ a -> HomeAssistantSettingsScreen(a) }) },
                    ),
                )
                LightText(text = "Example Home Assistant integration", variant = LightTextVariant.Copy)

                LightBottomBar(
                    items = listOf(
                        LightBarButton.Text(
                            text = "Fetch Entities",
                            onClick = viewModel::fetchStates,
                        ),
                    ),
                )

                message?.let { msg ->
                    LightFullscreenModal(message = msg, onClose = viewModel::dismissMessage)
                }
            }
        }
    }
}
