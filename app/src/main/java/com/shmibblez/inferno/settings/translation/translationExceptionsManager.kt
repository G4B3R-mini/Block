package com.shmibblez.inferno.settings.translation

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.shmibblez.inferno.R
import com.shmibblez.inferno.compose.InfoCard
import com.shmibblez.inferno.compose.InfoType
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.settings.compose.components.PrefUiConst
import com.shmibblez.inferno.translations.preferences.downloadlanguages.DownloadLanguagesPreferenceFragment
import kotlinx.coroutines.CoroutineScope
import mozilla.components.browser.state.action.BrowserAction
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.translate.TranslationError
import mozilla.components.lib.state.Store
import mozilla.components.lib.state.ext.observe
import mozilla.components.support.base.feature.LifecycleAwareFeature

private val ICON_SIZE = 18.dp

internal class TranslationExceptionsManagerState(
    val context: Context,
    val browserStore: BrowserStore,
    val lifecycleOwner: LifecycleOwner,
    val coroutineScope: CoroutineScope,
) : LifecycleAwareFeature {
    var observer: Store.Subscription<BrowserState, BrowserAction>? = null
    var couldNotLoadNeverTranslateSites by mutableStateOf<TranslationError.CouldNotLoadNeverTranslateSites?>(
        null
    )

    var neverTranslateSitesListPreferences by mutableStateOf<List<String>?>(null)

    override fun start() {
        // todo: switch to flowScoped
        // pref/error observer
        observer = browserStore.observe(
            owner = lifecycleOwner,
            observer = { state ->
                // refresh list
                neverTranslateSitesListPreferences = state.translationEngine.neverTranslateSites
                // refresh error
                couldNotLoadNeverTranslateSites =
                    state.translationEngine.engineError as? TranslationError.CouldNotLoadNeverTranslateSites
            },
        )
    }

    override fun stop() {
        // language models observer
        observer?.unsubscribe()
    }
}

@Composable
internal fun rememberTranslationExceptionsManagerState(): MutableState<TranslationExceptionsManagerState> {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val state = remember {
        mutableStateOf(
            TranslationExceptionsManagerState(
                context = context,
                browserStore = context.components.core.store,
                coroutineScope = coroutineScope,
                lifecycleOwner = lifecycleOwner,
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

/**
 *  based off [DownloadLanguagesPreferenceFragment]
 */
internal fun LazyListScope.translationExceptionsManager(
    state: TranslationExceptionsManagerState,
    onItemClick: (String) -> Unit,
) {
    // title
    item {
        InfernoText(
            text = stringResource(R.string.never_translate_site_header_preference),
            modifier = Modifier.padding(
                horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING,
                vertical = PrefUiConst.PREFERENCE_VERTICAL_PADDING,
            ),
        )
    }

    // error
    if (state.couldNotLoadNeverTranslateSites != null) {
        item {
            NeverTranslateSitesErrorWarning()
        }
    }

    state.neverTranslateSitesListPreferences?.let {
        items(state.neverTranslateSitesListPreferences!!) { item: String ->
            ExceptionItem(
                label = stringResource(
                    id = R.string.never_translate_site_item_list_content_description_preference,
                    item,
                ),
                onDeleteItemClicked = { onItemClick.invoke(item) },
            )
        }
    }

    // bottom spacer
    item {
        Spacer(
            modifier = Modifier.padding(bottom = PrefUiConst.PREFERENCE_VERTICAL_PADDING),
        )
    }
}

@Composable
private fun NeverTranslateSitesErrorWarning() {
    val modifier = Modifier
        .fillMaxWidth()
        .padding(start = 72.dp, end = 16.dp, bottom = 16.dp, top = 16.dp)
        .defaultMinSize(minHeight = 56.dp)
        .wrapContentHeight()

    InfoCard(
        description = stringResource(id = R.string.never_translate_site_error_warning_text),
        type = InfoType.Warning,
        verticalRowAlignment = Alignment.CenterVertically,
        modifier = modifier,
    )
}

@Composable
private fun ExceptionItem(
    label: String,
    onDeleteItemClicked: () -> Unit,
) {
    // delete icon
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PrefUiConst.PREFERENCE_HORIZONTAL_PADDING)
            .padding(top = PrefUiConst.PREFERENCE_INTERNAL_PADDING)
            .clickable(onClick = onDeleteItemClicked),
        horizontalArrangement = Arrangement.spacedBy(PrefUiConst.PREFERENCE_INTERNAL_PADDING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // label
        InfernoText(
            text = label,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1F),
        )

        // delete icon
        InfernoIcon(
            painter = painterResource(R.drawable.ic_delete_24),
            contentDescription = null,
            modifier = Modifier.size(ICON_SIZE),
        )
    }
}