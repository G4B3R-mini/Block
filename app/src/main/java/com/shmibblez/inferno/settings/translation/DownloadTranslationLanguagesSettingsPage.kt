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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.getActivity
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.translations.preferences.downloadlanguages.DownloadLanguageItemPreference
import com.shmibblez.inferno.translations.preferences.downloadlanguages.DownloadLanguageItemTypePreference
import com.shmibblez.inferno.utils.AccessibilityUtils.moveFocusToBackNavButton
import mozilla.components.browser.state.action.TranslationsAction
import mozilla.components.concept.engine.translate.ModelManagementOptions
import mozilla.components.concept.engine.translate.ModelOperation
import mozilla.components.concept.engine.translate.ModelState
import mozilla.components.concept.engine.translate.OperationLevel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DownloadTranslationLanguagesSettingsPage(goBack: () -> Unit) {
    val context = LocalContext.current
    val browserStore = context.components.core.store

    val managerState by rememberDownloadTranslationMangerState()

    var showDownloadTranslationLanguageDialogFor by remember {
        mutableStateOf<DownloadLanguageItemPreference?>(
            null
        )
    }

    InfernoSettingsPage(
        title = stringResource(R.string.translation_settings_download_language),
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgeInsets),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            // translation item manager
            downloadTranslationManager(
                state = managerState,
                onItemClick = { downloadLanguageItemPreference ->
                    if (downloadLanguageItemPreference.languageModel.status == ModelState.DOWNLOADED || managerState.shouldShowPrefDownloadLanguageFileDialog(
                            downloadLanguageItemPreference,
                        )
                    ) {
//                        var size = 0L
//                        downloadLanguageItemPreference.languageModel.size?.let { size = it }

                        // show dialog
                        showDownloadTranslationLanguageDialogFor = downloadLanguageItemPreference

                        // old code:
//                        findNavController().navigate(
//                            DownloadLanguagesPreferenceFragmentDirections
//                                .actionDownloadLanguagesPreferenceToDownloadLanguagesDialogPreference(
//                                    modelState = downloadLanguageItemPreference.languageModel.status,
//                                    itemType = downloadLanguageItemPreference.type,
//                                    languageCode = downloadLanguageItemPreference.languageModel.language?.code,
//                                    languageDisplayName =
//                                    downloadLanguageItemPreference.languageModel.language?.localizedDisplayName,
//                                    modelSize = size,
//                                ),
//                        )
                    } else {
                        if (downloadLanguageItemPreference.type == DownloadLanguageItemTypePreference.AllLanguages) {
                            val options = ModelManagementOptions(
                                operation = if (downloadLanguageItemPreference.languageModel.status == ModelState.NOT_DOWNLOADED) {
                                    ModelOperation.DOWNLOAD
                                } else {
                                    ModelOperation.DELETE
                                },
                                operationLevel = OperationLevel.ALL,
                            )
                            browserStore.dispatch(
                                TranslationsAction.ManageLanguageModelsAction(
                                    options = options,
                                ),
                            )
                        } else {
                            managerState.deleteOrDownloadModel(downloadLanguageItemPreference)
                        }

                        context.getActivity()?.let { activity ->
                            // todo: currently breaks / does nothing
//                            moveFocusToBackNavButton(activity)
                        }
                    }
                },
            )
        }

        if (showDownloadTranslationLanguageDialogFor != null) {
            DownloadTranslationLanguageDialog(
                onDismiss = { showDownloadTranslationLanguageDialogFor = null },
                preference = showDownloadTranslationLanguageDialogFor!!,
            )
        }
    }
}