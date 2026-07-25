package com.thelightphone.frigate

import androidx.compose.runtime.Composable
import androidx.compose.foundation.text.input.rememberTextFieldState
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.SimpleLightScreen
import com.thelightphone.sdk.rememberKeyboardOptions
import com.thelightphone.sdk.ui.LightTextInputEditor

class FrigateEditorScreen(
    sealedActivity: SealedLightActivity,
    private val title: String,
    private val initialValue: String,
) : SimpleLightScreen<String>(sealedActivity) {

    @Composable
    override fun Content() {
        val textState = rememberTextFieldState(initialValue)
        val keyboardOptionsFlow = rememberKeyboardOptions()
        LightTextInputEditor(
            title = title,
            state = textState,
            keyboardOptionsFlow = keyboardOptionsFlow,
            onSubmit = { r -> goBack(r.toString()) },
            onBack = { goBack(null) },
        )
    }
}
