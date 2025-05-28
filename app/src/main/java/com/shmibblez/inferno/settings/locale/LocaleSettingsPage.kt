package com.shmibblez.inferno.settings.locale

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LocaleSettingsPage(goBack: () -> Unit) {
    val localeManagerState by rememberLocaleManagerState()

    InfernoSettingsPage(
        title = stringResource(R.string.preferences_language),
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgeInsets),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            localeManager(
                state = localeManagerState,
                onLocaleSelected = { localeManagerState.setLocale(it) },
                onDefaultLocaleSelected = { localeManagerState.useDefaultLocale() },
            )
        }
    }
}