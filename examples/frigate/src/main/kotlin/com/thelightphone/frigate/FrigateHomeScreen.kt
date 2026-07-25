package com.thelightphone.frigate

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter

@InitialScreen
class FrigateHomeScreen(sealedActivity: SealedLightActivity) :
    LightScreen<Unit, FrigateViewModel>(sealedActivity) {

    override val viewModelClass: Class<FrigateViewModel>
        get() = FrigateViewModel::class.java

    override fun createViewModel(): FrigateViewModel = FrigateViewModel(lightContext.dataStore)

    @Composable
    override fun Content() {
        val url by viewModel.streamUrl.collectAsState()

        LightTopBar(
            center = LightTopBarCenter.Text("Frigate"),
            rightButton = LightBarButton.LightIcon(
                icon = LightIcons.SETTINGS,
                onClick = { navigateTo({ a -> FrigateSettingsScreen(a) }) },
            ),
        )

        AndroidView(factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(Uri.parse(url))
                setOnPreparedListener { mp -> mp.isLooping = true }
                start()
            }
        }, modifier = Modifier.fillMaxSize())
    }
}
