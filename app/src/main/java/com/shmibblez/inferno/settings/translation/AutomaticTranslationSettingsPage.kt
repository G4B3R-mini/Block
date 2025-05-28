package com.shmibblez.inferno.settings.translation

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.InfoCard
import com.shmibblez.inferno.compose.InfoType
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import com.shmibblez.inferno.settings.compose.components.PreferenceSelect
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
import com.shmibblez.inferno.translations.preferences.automatic.AutomaticTranslationItemPreference
import com.shmibblez.inferno.translations.preferences.automatic.AutomaticTranslationOptionsPreferenceFragment
import com.shmibblez.inferno.translations.preferences.automatic.AutomaticTranslationPreferenceFragment
import com.shmibblez.inferno.translations.preferences.automatic.getAutomaticTranslationOptionPreference
import mozilla.components.browser.state.action.TranslationsAction
import mozilla.components.concept.engine.translate.LanguageSetting
import mozilla.components.concept.engine.translate.TranslationError
import mozilla.components.concept.engine.translate.TranslationSupport
import mozilla.components.concept.engine.translate.findLanguage
import mozilla.components.lib.state.ext.observeAsComposableState

/**
 * based off of [AutomaticTranslationPreferenceFragment] for getting language list
 * and [AutomaticTranslationOptionsPreferenceFragment] for individual language pref
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AutomaticTranslationSettingsPage(goBack: () -> Unit) {

    val context = LocalContext.current
    val browserStore = context.components.core.store

    val languageSettings = browserStore.observeAsComposableState { state ->
        state.translationEngine.languageSettings
    }.value
    val translationSupport = browserStore.observeAsComposableState { state ->
        state.translationEngine.supportedLanguages
    }.value
    val engineError = browserStore.observeAsComposableState { state ->
        state.translationEngine.engineError
    }.value
    val couldNotLoadLanguagesError = engineError as? TranslationError.CouldNotLoadLanguagesError
    val couldNotLoadLanguageSettingsError =
        engineError as? TranslationError.CouldNotLoadLanguageSettingsError

    val hasLanguageError =
        couldNotLoadLanguagesError != null || couldNotLoadLanguageSettingsError != null || languageSettings == null

    val automaticTranslationListPreferences = getAutomaticTranslationListPreferences(
        languageSettings = languageSettings,
        translationSupport = translationSupport,
    )

    InfernoSettingsPage(
        title = stringResource(R.string.translation_settings_automatic_translation),
        goBack = goBack,
    ) { edgeInsets ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(edgeInsets),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            // header
            item {
                PreferenceTitle(text = stringResource(R.string.automatic_translation_header_preference))
            }

            // error item (in case of error)
            if (hasLanguageError) {
                item {
                    CouldNotLoadLanguagesErrorWarning()
                }
            }

            // automatic translation preference for each language
            items(automaticTranslationListPreferences) { item ->
                // selected LanguageSetting
                val languageCode = item.language.code
                val selected: LanguageSetting = languageSettings!![languageCode]!!
                PreferenceSelect(
                    text = item.language.localizedDisplayName ?: "",
                    description = selected.toDescriptionString(context),
                    enabled = true,
                    selectedMenuItem = selected,
                    menuItems = listOf(
                        LanguageSetting.ALWAYS,
                        LanguageSetting.OFFER,
                        LanguageSetting.NEVER,
                    ),
                    mapToTitle = { it.toPrefString(context) },
                    onSelectMenuItem = {
                        browserStore.dispatch(
                            TranslationsAction.UpdateLanguageSettingsAction(
                                languageCode = languageCode,
                                setting = it,
                            ),
                        )
                    },
                )
            }
        }
    }
}

private fun LanguageSetting.toPrefString(context: Context): String {
    return when (this) {
        LanguageSetting.ALWAYS -> context.getString(R.string.automatic_translation_option_always_translate_title_preference)
        LanguageSetting.OFFER -> context.getString(R.string.automatic_translation_option_offer_to_translate_title_preference)
        LanguageSetting.NEVER -> context.getString(R.string.automatic_translation_option_never_translate_title_preference)
    }
}

private fun LanguageSetting.toDescriptionString(context: Context): String {
    return when (this) {
        LanguageSetting.ALWAYS -> context.getString(
            R.string.automatic_translation_option_always_translate_summary_preference,
            "Mozilla" // mozilla the legend
        )

        LanguageSetting.OFFER -> context.getString(
            R.string.automatic_translation_option_offer_to_translate_summary_preference, "Mozilla"
        )

        LanguageSetting.NEVER -> context.getString(
            R.string.automatic_translation_option_never_translate_summary_preference, "Mozilla"
        )
    }
}

private fun getAutomaticTranslationListPreferences(
    languageSettings: Map<String, LanguageSetting>? = null,
    translationSupport: TranslationSupport? = null,
): List<AutomaticTranslationItemPreference> {
    val automaticTranslationListPreferences = mutableListOf<AutomaticTranslationItemPreference>()

    if (translationSupport != null && languageSettings != null) {
        languageSettings.forEach { entry ->
            translationSupport.findLanguage(entry.key)?.let {
                automaticTranslationListPreferences.add(
                    AutomaticTranslationItemPreference(
                        language = it,
                        automaticTranslationOptionPreference = getAutomaticTranslationOptionPreference(
                            entry.value,
                        ),
                    ),
                )
            }
        }
    }
    return automaticTranslationListPreferences
}

@Composable
private fun CouldNotLoadLanguagesErrorWarning() {
    val modifier = Modifier
        .fillMaxWidth()
        .padding(start = 72.dp, end = 16.dp, bottom = 16.dp, top = 16.dp)
        .defaultMinSize(minHeight = 56.dp)
        .wrapContentHeight()

    InfoCard(
        description = stringResource(id = R.string.automatic_translation_error_warning_text),
        type = InfoType.Warning,
        verticalRowAlignment = Alignment.CenterVertically,
        modifier = modifier,
    )
}