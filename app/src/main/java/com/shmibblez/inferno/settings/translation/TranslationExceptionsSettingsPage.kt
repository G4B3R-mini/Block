package com.shmibblez.inferno.settings.translation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import mozilla.components.browser.state.action.TranslationsAction

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TranslationExceptionsSettingsPage(goBack: () -> Unit) {
    val browserStore = LocalContext.current.components.core.store
    val translationExceptionsManagerState by rememberTranslationExceptionsManagerState()
    var showNeverTranslateSiteDialogFor by remember { mutableStateOf<String?>(null) }

    InfernoSettingsPage(
        title = stringResource(R.string.never_translate_site_toolbar_title_preference),
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgeInsets),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            // exceptions manager
            translationExceptionsManager(state = translationExceptionsManagerState,
                onItemClick = { showNeverTranslateSiteDialogFor = it })
        }

        // dialog for deleting site
        if (showNeverTranslateSiteDialogFor != null) {
            NeverTranslateSiteDialog(
                websiteUrl = showNeverTranslateSiteDialogFor!!,
                onDismiss = { showNeverTranslateSiteDialogFor = null },
                onConfirmDelete = {
                    browserStore.dispatch(
                        TranslationsAction.RemoveNeverTranslateSiteAction(
                            origin = it,
                        ),
                    )
                },
            )
        }
    }
}