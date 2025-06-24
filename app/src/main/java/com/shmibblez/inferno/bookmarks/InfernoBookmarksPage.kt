package com.shmibblez.inferno.bookmarks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage

@Composable
fun InfernoBookmarksPage(goBack: () -> Unit) {

    val managerState by rememberBookmarksManagerState()

    InfernoSettingsPage(
        title = TODO(),
        goBack = goBack,
    ) {

    }
}