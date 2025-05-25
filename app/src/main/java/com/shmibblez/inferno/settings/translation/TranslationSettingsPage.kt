package com.shmibblez.inferno.settings.translation

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.PreferenceAction
import com.shmibblez.inferno.settings.compose.components.PreferenceSwitch
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import kotlinx.coroutines.launch
import mozilla.components.browser.state.action.TranslationsAction
import mozilla.components.lib.state.ext.observeAsComposableState

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TranslationSettingsPage(
    goBack: () -> Unit,
    onNavigateToAutomaticTranslationSettings: () -> Unit,
    onNavigateToDownloadTranslationLanguagesSettings: () -> Unit,
    onNavigateToTranslationExceptionsSettings: () -> Unit,
    ) {
    val context = LocalContext.current
    val browserStore = context.components.core.store
    val coroutineScope = rememberCoroutineScope()
    val settings by context.infernoSettingsDataStore.data.collectAsState(InfernoSettings.getDefaultInstance())

    val offerToTranslate by browserStore.observeAsComposableState { state ->
        state.translationEngine.offerTranslation
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    InfernoIcon(
                        painter = painterResource(R.drawable.ic_back_button),
                        contentDescription = stringResource(R.string.browser_menu_back),
                        modifier = Modifier.clickable(onClick = goBack),
                    )
                },
                title = { InfernoText(stringResource(R.string.preferences_translations)) }, // todo: string res
            )
        },
    ) {
        // todo: first though, make tab optimizations
        //  - default width based on available space
        //  - calculate width only if necessary
        //  - only use onGloballyPositioned in top level to determine space available
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            // todo: check moz implementation
            //  - events / options managed through browser store with TranslationsAction,
            //    may need to sub to state to keep page updated
            //  - organize settings
            //  - main screens
            //    - sub screens

            // offer to translate when possible
            if (offerToTranslate != null) {
                item {
                    PreferenceSwitch(
                        text = stringResource(R.string.translation_settings_offer_to_translate),
                        summary = null,
                        selected = offerToTranslate!!,
                        onSelectedChange = { selected ->
                            browserStore.dispatch(
                                TranslationsAction.UpdateGlobalOfferTranslateSettingAction(
                                    offerTranslation = selected,
                                ),
                            )
                        },
                        enabled = true,
                    )
                }
            }


            item {
                PreferenceSwitch(
                    text = stringResource(R.string.translation_settings_always_download),
                    summary = null,
                    selected = settings.downloadLanguagesInDataSavingMode,
                    onSelectedChange = { selected ->
                        coroutineScope.launch {
                            context.infernoSettingsDataStore.updateData {
                                it.toBuilder().setDownloadLanguagesInDataSavingMode(selected)
                                    .build()
                            }
                        }
                    },
                    enabled = true,
                )
            }

            // translation preferences title
            item {
                PreferenceTitle(text = stringResource(R.string.translation_settings_translation_preference))
            }

            // automatic translation settings
            item {
                PreferenceAction(
                    title = stringResource(R.string.translation_settings_automatic_translation),
                    action = onNavigateToAutomaticTranslationSettings,
                )
            }

            // downloaded languages settings
            item {
                PreferenceAction(
                    title = stringResource(R.string.translation_settings_download_language),
                    action = onNavigateToDownloadTranslationLanguagesSettings,
                )
            }

            // exceptions / never translate these sites settings
            item {
                PreferenceAction(
                    title = stringResource(R.string.preference_exceptions),
                    action = onNavigateToTranslationExceptionsSettings,
                )
            }
        }
    }
}