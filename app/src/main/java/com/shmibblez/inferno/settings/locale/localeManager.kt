package com.shmibblez.inferno.settings.locale

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import com.shmibblez.inferno.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.base.InfernoTextStyle
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.settings.advanced.getSupportedLocales
import com.shmibblez.inferno.settings.advanced.isDefaultLocaleSelected
import com.shmibblez.inferno.settings.compose.components.PreferenceConstants
import kotlinx.coroutines.CoroutineScope
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.locale.LocaleManager
import mozilla.components.support.locale.LocaleUseCases
import java.util.Locale

private val ICON_SIZE = 18.dp

internal class LocaleManagerState(
    val context: Context,
    val localeUseCases: LocaleUseCases,
    val coroutineScope: CoroutineScope,
) : LifecycleAwareFeature {

    internal var locales by mutableStateOf(emptyList<Locale>())

    var selectedLocale by run {
        val state = mutableStateOf(LocaleManager.getCurrentLocale(context))
        object : MutableState<Locale?> by state {
            override var value: Locale?
                get() = state.value
                set(value) {
                    state.value = value
                    // update isDefaultSelected value
                    isDefaultSelected = LocaleManager.isDefaultLocaleSelected(context)
                }
        }
    }
        private set

    var defaultLocale by run {
        val state = mutableStateOf(LocaleManager.getSystemDefault())
        object : MutableState<Locale> by state {
            override var value: Locale
                get() = state.value
                set(value) {
                    state.value = value
                }
        }
    }
        private set

    var isDefaultSelected by run {
        val state = mutableStateOf(LocaleManager.isDefaultLocaleSelected(context))
        object : MutableState<Boolean> by state {
            override var value: Boolean
                get() = state.value
                set(value) {
                    state.value = value
                }
        }
    }
        private set

    private fun refreshSelectedLocale() {
        selectedLocale = LocaleManager.getCurrentLocale(context)
    }

    fun setLocale(locale: Locale) {
        LocaleManager.setNewLocale(
            context = context,
            localeUseCase = localeUseCases,
            locale = locale,
        )
        refreshSelectedLocale()
    }

    fun useDefaultLocale() {
        LocaleManager.resetToSystemDefault(context = context, localeUseCase = localeUseCases)
        refreshSelectedLocale()
    }

    override fun start() {
        locales = LocaleManager.getSupportedLocales()
        refreshSelectedLocale()
    }

    override fun stop() {}

}

@Composable
internal fun rememberLocaleManagerState(): MutableState<LocaleManagerState> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val state = remember {
        mutableStateOf(
            LocaleManagerState(
                context = context,
                localeUseCases = context.components.useCases.localeUseCases,
                coroutineScope = coroutineScope,
            )
        )
    }

    DisposableEffect(null) {
        state.value.start()

        onDispose {
            state.value.stop()
        }
    }

    return state
}

internal fun LazyListScope.localeManager(
    state: LocaleManagerState,
    onLocaleSelected: (Locale) -> Unit,
    onDefaultLocaleSelected: () -> Unit,
) {
    item {
        DefaultLocaleItem(
            locale = state.defaultLocale,
            selected = state.isDefaultSelected,
            onDefaultLocaleSelected = onDefaultLocaleSelected,
        )
    }
    items(state.locales) {
        LocaleItem(
            locale = it,
            selected = it == state.selectedLocale,
            onLocaleSelected = onLocaleSelected,
        )
    }
    item {
        Spacer(
            modifier = Modifier.padding(bottom = PreferenceConstants.PREFERENCE_VERTICAL_PADDING),
        )
    }
}

@Composable
private fun DefaultLocaleItem(
    locale: Locale,
    selected: Boolean,
    onDefaultLocaleSelected: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable { onDefaultLocaleSelected.invoke() }
            .fillMaxWidth()
            .padding(horizontal = PreferenceConstants.PREFERENCE_HORIZONTAL_PADDING)
            .padding(top = PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // locale info
        Column(modifier = Modifier.weight(1F)) {
            // display name
            InfernoText(
                text = locale.displayName,
                fontWeight = FontWeight.Bold,
            )
            // language info
            InfernoText(
                text = locale.toLanguageTag(),
                infernoStyle = InfernoTextStyle.Subtitle,
                maxLines = 2,
            )
            // default locale (recommended)
            InfernoText(
                text = stringResource(R.string.default_locale_text) + " (${stringResource(R.string.phone_feature_recommended)})"
            )
        }

        // selected icon
        if (selected) {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_checkmark_24),
                contentDescription = null,
                modifier = Modifier.size(ICON_SIZE),
            )
        }
    }
}

@Composable
private fun LocaleItem(
    locale: Locale,
    selected: Boolean,
    onLocaleSelected: (Locale) -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable { onLocaleSelected.invoke(locale) }
            .fillMaxWidth()
            .padding(horizontal = PreferenceConstants.PREFERENCE_HORIZONTAL_PADDING)
            .padding(top = PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(PreferenceConstants.PREFERENCE_INTERNAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // locale info
        Column(modifier = Modifier.weight(1F)) {
            // display name
            InfernoText(
                text = locale.displayName,
                fontWeight = FontWeight.Bold,
            )
            // language info
            InfernoText(
                text = locale.toLanguageTag(),
                infernoStyle = InfernoTextStyle.Subtitle,
                maxLines = 2,
            )
        }

        // selected icon
        if (selected) {
            InfernoIcon(
                painter = painterResource(R.drawable.ic_checkmark_24),
                contentDescription = null,
                modifier = Modifier.size(ICON_SIZE),
            )
        }
    }
}