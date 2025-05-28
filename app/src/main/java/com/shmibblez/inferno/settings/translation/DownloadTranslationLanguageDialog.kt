package com.shmibblez.inferno.settings.translation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.base.InfernoButton
import com.shmibblez.inferno.compose.base.InfernoDialog
import com.shmibblez.inferno.compose.base.InfernoOutlinedButton
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import com.shmibblez.inferno.theme.FirefoxTheme
import com.shmibblez.inferno.translations.preferences.downloadlanguages.DownloadLanguageFileDialogType
import com.shmibblez.inferno.translations.preferences.downloadlanguages.DownloadLanguageItemPreference
import com.shmibblez.inferno.translations.preferences.downloadlanguages.DownloadLanguageItemTypePreference
import mozilla.components.browser.state.action.TranslationsAction
import mozilla.components.concept.engine.translate.ModelManagementOptions
import mozilla.components.concept.engine.translate.ModelOperation
import mozilla.components.concept.engine.translate.ModelState
import mozilla.components.concept.engine.translate.OperationLevel
import mozilla.components.feature.downloads.toMegabyteOrKilobyteString

@Composable
fun DownloadTranslationLanguageDialog(
    onDismiss: () -> Unit,
    preference: DownloadLanguageItemPreference,
) {
    val context = LocalContext.current
    val browserStore = context.components.core.store

    val modelState = preference.languageModel.status
    val itemType = preference.type
    val languageCode = preference.languageModel.language?.code
    val languageDisplayName = preference.languageModel.language?.localizedDisplayName
    val modelSize = preference.languageModel.size ?: 0L

    fun deleteOrDownloadModel(modelOperation: ModelOperation, languageToManage: String?) {
        val options = ModelManagementOptions(
            languageToManage = languageToManage,
            operation = modelOperation,
            operationLevel = OperationLevel.LANGUAGE,
        )
        browserStore.dispatch(
            TranslationsAction.ManageLanguageModelsAction(
                options = options,
            ),
        )
    }

    InfernoDialog(onDismiss = onDismiss) {
        when (modelState) {
            ModelState.NOT_DOWNLOADED -> {

                var checkBoxEnabled by remember { mutableStateOf(false) }

                DownloadLanguageFileOptions(
                    downloadLanguageDialogType = when (itemType) {
                        DownloadLanguageItemTypePreference.AllLanguages -> DownloadLanguageFileDialogType.AllLanguages
                        else -> DownloadLanguageFileDialogType.Default
                    },
                    fileSize = modelSize,
                    isCheckBoxEnabled = checkBoxEnabled,
                    onSavingModeStateChange = { checkBoxEnabled = it },
                    onConfirmDownload = {
                        context.settings().ignoreTranslationsDataSaverWarning = checkBoxEnabled

                        if (itemType == DownloadLanguageItemTypePreference.AllLanguages) {
                            val options = ModelManagementOptions(
                                operation = ModelOperation.DOWNLOAD,
                                operationLevel = OperationLevel.ALL,
                            )
                            browserStore.dispatch(
                                TranslationsAction.ManageLanguageModelsAction(
                                    options = options,
                                ),
                            )
                        } else {
                            deleteOrDownloadModel(
                                modelOperation = ModelOperation.DOWNLOAD,
                                languageToManage = languageCode,
                            )
                        }

                        // dismiss
                        onDismiss.invoke()
                    },
                    onCancel = onDismiss,
                )
            }

            ModelState.DOWNLOADED -> {
                DeleteLanguageFileOptions(
                    language = languageDisplayName,
                    isAllLanguagesItemType = itemType == DownloadLanguageItemTypePreference.AllLanguages,
                    fileSize = modelSize,
                    onConfirmDelete = {
                        if (itemType == DownloadLanguageItemTypePreference.AllLanguages) {
                            val options = ModelManagementOptions(
                                operation = ModelOperation.DELETE,
                                operationLevel = OperationLevel.ALL,
                            )
                            browserStore.dispatch(
                                TranslationsAction.ManageLanguageModelsAction(
                                    options = options,
                                ),
                            )
                        } else {
                            deleteOrDownloadModel(
                                modelOperation = ModelOperation.DELETE,
                                languageToManage = languageCode,
                            )
                        }

                        // dismiss
                        onDismiss.invoke()
                    },
                    onCancel = onDismiss,
                )
            }

            ModelState.DOWNLOAD_IN_PROGRESS,
            ModelState.DELETION_IN_PROGRESS,
            ModelState.ERROR_DELETION,
            ModelState.ERROR_DOWNLOAD,
                -> {
            }
        }
    }
}

@Composable
private fun DeleteLanguageFileOptions(
    language: String? = null,
    isAllLanguagesItemType: Boolean,
    fileSize: Long? = null,
    onConfirmDelete: () -> Unit,
    onCancel: () -> Unit,
) {
    val title: String? = when (isAllLanguagesItemType) {
        true -> {
            stringResource(
                R.string.delete_language_all_languages_file_dialog_title,
                fileSize?.toMegabyteOrKilobyteString() ?: 0L,
            )
        }

        false -> {
            language?.let {
                stringResource(
                    id = R.string.delete_language_file_dialog_title,
                    it,
                    fileSize?.toMegabyteOrKilobyteString() ?: 0L,
                )
            }
        }
    }

    val message: String = when (isAllLanguagesItemType) {
        true -> stringResource(
            id = R.string.delete_language_all_languages_file_dialog_message,
            "Mozilla",
//            stringResource(id = R.string.firefox),
        )

        false -> stringResource(
            id = R.string.delete_language_file_dialog_message,
            "Mozilla",
//            stringResource(id = R.string.firefox),
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // title
        title?.let {
            InfernoText(
                text = it,
                infernoStyle = InfernoTextStyle.Title,
            )
        }

        // message
        InfernoText(text = message)

        // cancel/delete buttons
        Row {
            // cancel
            InfernoOutlinedButton(
                text = stringResource(id = R.string.delete_language_file_dialog_negative_button_text),
                onClick = onCancel,
                modifier = Modifier.weight(1F)
            )
            // confirm
            InfernoButton(
                text = stringResource(id = R.string.delete_language_file_dialog_positive_button_text),
                onClick = onConfirmDelete,
                modifier = Modifier.weight(1F),
            )
        }
    }
}

@Composable
private fun DownloadLanguageFileOptions(
    downloadLanguageDialogType: DownloadLanguageFileDialogType,
    fileSize: Long? = null,
    isCheckBoxEnabled: Boolean,
    onSavingModeStateChange: (Boolean) -> Unit,
    onConfirmDownload: () -> Unit,
    onCancel: () -> Unit,
) {
    val title =
        if (downloadLanguageDialogType is DownloadLanguageFileDialogType.TranslationRequest) {
            stringResource(
                R.string.translations_download_language_file_dialog_title,
                fileSize?.toMegabyteOrKilobyteString() ?: 0L,
            )
        } else {
            stringResource(
                R.string.download_language_file_dialog_title,
                fileSize?.toMegabyteOrKilobyteString() ?: 0L,
            )
        }

    Column(modifier = Modifier.fillMaxWidth()) {
        // title
        InfernoText(
            text = title,
            infernoStyle = InfernoTextStyle.Title,
        )

        // privacy message
        if (downloadLanguageDialogType is DownloadLanguageFileDialogType.AllLanguages || downloadLanguageDialogType is DownloadLanguageFileDialogType.TranslationRequest) {
            InfernoText(text = stringResource(R.string.download_language_file_dialog_message_all_languages))
        }

        // download in data mode checkbox
        Row(
            modifier = Modifier.toggleable(
                value = isCheckBoxEnabled,
                role = Role.Checkbox,
                onValueChange = onSavingModeStateChange,
            ),
            horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // checkbox
            Checkbox(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clearAndSetSemantics { },
                checked = isCheckBoxEnabled,
                onCheckedChange = onSavingModeStateChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = FirefoxTheme.colors.formSelected,
                    uncheckedColor = FirefoxTheme.colors.formDefault,
                ),
            )
            // option text
            InfernoText(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = stringResource(R.string.download_language_file_dialog_checkbox_text),
            )
        }

        // cancel/confirm buttons
        Row {
            // cancel
            InfernoOutlinedButton(
                text = stringResource(id = R.string.download_language_file_dialog_negative_button_text),
                onClick = onCancel,
                modifier = Modifier.weight(1F)
            )
            // confirm
            InfernoButton(
                text = when (downloadLanguageDialogType) {
                    DownloadLanguageFileDialogType.AllLanguages,
                    DownloadLanguageFileDialogType.TranslationRequest,
                        -> stringResource(R.string.download_language_file_dialog_positive_button_text_all_languages)

                    else -> stringResource(R.string.download_language_file_dialog_positive_button_text)
                }, onClick = onConfirmDownload, modifier = Modifier.weight(1F)
            )
        }
    }
}