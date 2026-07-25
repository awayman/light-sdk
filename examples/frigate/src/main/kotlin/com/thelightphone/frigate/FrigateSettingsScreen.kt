package com.thelightphone.frigate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.rememberKeyboardOptions
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightIcons

class FrigateSettingsScreen(sealedActivity: SealedLightActivity) :
    LightScreen<Unit, FrigateSettingsViewModel>(sealedActivity) {

    override val viewModelClass: Class<FrigateSettingsViewModel>
        get() = FrigateSettingsViewModel::class.java

    override fun createViewModel(): FrigateSettingsViewModel = FrigateSettingsViewModel(lightContext.dataStore)

    @Composable
    override fun Content() {
        val streamUrl by viewModel.streamUrl.collectAsState()
        val keyboardOptionsFlow = rememberKeyboardOptions()

        LightTheme {
            Column(modifier = Modifier.fillMaxSize()) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(
                        icon = LightIcons.BACK,
                        onClick = { goBack() },
                    ),
                    center = LightTopBarCenter.Text("Frigate Settings"),
                )

                LightTextField(
                    label = "Stream URL",
                    value = streamUrl,
                    placeholder = "http://frigate.local:5000/<camera>/index.m3u8",
                    onClick = {
                        navigateTo({ act -> FrigateEditorScreen(act, "Stream URL", streamUrl) }) { result ->
                            if (result != null) viewModel.saveStreamUrl(result)
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
