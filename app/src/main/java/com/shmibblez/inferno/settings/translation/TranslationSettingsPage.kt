package com.shmibblez.inferno.settings.translation

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.shmibblez.inferno.nimbus.FxNimbus
import com.shmibblez.inferno.proto.InfernoSettings
import com.shmibblez.inferno.proto.infernoSettingsDataStore
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
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

    val showAutomaticTranslations = remember {
        FxNimbus.features.translations.value().globalLangSettingsEnabled
    }
    val showNeverTranslate = remember {
        FxNimbus.features.translations.value().globalSiteSettingsEnabled
    }
    val showDownloads = remember {
        FxNimbus.features.translations.value().downloadsEnabled
    }
    val showHeader = showAutomaticTranslations || showNeverTranslate || showDownloads


    InfernoSettingsPage(
        title = stringResource(R.string.preferences_translations),
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgeInsets),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
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
            if (showHeader) {
                item {
                    PreferenceTitle(text = stringResource(R.string.translation_settings_translation_preference))
                }
            }

            // automatic translation settings
            if (showAutomaticTranslations) {
                item {
                    PreferenceAction(
                        title = stringResource(R.string.translation_settings_automatic_translation),
                        action = onNavigateToAutomaticTranslationSettings,
                    )
                }
            }

            // downloaded languages settings
            if (showAutomaticTranslations) {
                item {
                    PreferenceAction(
                        title = stringResource(R.string.translation_settings_download_language),
                        action = onNavigateToDownloadTranslationLanguagesSettings,
                    )
                }
            }

            // exceptions / never translate these sites settings
            if (showNeverTranslate) {
                item {
                    PreferenceAction(
                        title = stringResource(R.string.preference_exceptions),
                        action = onNavigateToTranslationExceptionsSettings,
                    )
                }
            }
        }
    }
}