package com.shmibblez.inferno.settings.extensions

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.R
import com.shmibblez.inferno.ext.components

@Composable
fun ExtensionsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val addonManagerState by rememberAddonsManagerState(
        addonManager = context.components.addonManager,
    )

    InfernoSettingsPage(
        title = stringResource(R.string.preferences_extensions),
        goBack = goBack,
    ) { edgeInsets ->
        AddonsManager(
            state = addonManagerState,
            modifier = Modifier.padding(edgeInsets),
        )
    }
}