package com.shmibblez.inferno.browser

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.browser.awesomebar.SuggestionFetcher
import com.shmibblez.inferno.browser.awesomebar.Suggestions
import com.shmibblez.inferno.ext.components
import mozilla.components.compose.browser.awesomebar.AwesomeBarOrientation
import mozilla.components.concept.awesomebar.AwesomeBar
import mozilla.components.concept.base.profiler.Profiler
import mozilla.components.feature.awesomebar.provider.HistoryStorageSuggestionProvider
import mozilla.components.feature.awesomebar.provider.SearchActionProvider
import mozilla.components.feature.awesomebar.provider.SearchSuggestionProvider

@Composable
fun InfernoAwesomeBar(
    text: String,
//    providers: List<AwesomeBar.SuggestionProvider>,
    orientation: AwesomeBarOrientation = AwesomeBarOrientation.TOP,
    onSuggestionClicked: (AwesomeBar.SuggestionProviderGroup, AwesomeBar.Suggestion) -> Unit,
    onAutoComplete: (AwesomeBar.SuggestionProviderGroup, AwesomeBar.Suggestion) -> Unit,
    onVisibilityStateUpdated: (AwesomeBar.VisibilityState) -> Unit = {},
    onScroll: () -> Unit = {},
    profiler: Profiler? = null,
    modifier: Modifier = Modifier,
) {
    // todo: move to launched effect
    val context = LocalContext.current
    val store = remember { context.components.core.store }
    val searchUseCase = remember { context.components.useCases.searchUseCases }
    val fetchClient = remember { context.components.core.client }
    val limit = remember { 4 }
    val mode = remember { SearchSuggestionProvider.Mode.MULTIPLE_SUGGESTIONS }
    val engine = remember { context.components.core.engine }
    val filterExactMatch = remember { false }
    val showDescription = remember { false }
    val historyStorage = remember { context.components.core.historyStorage }
    val loadUrlUseCase = remember { context.components.useCases.sessionUseCases.loadUrl }
    val icons = remember { context.components.core.icons }
    val maxNumberOfSuggestions = remember { 5 }

    val providers: List<AwesomeBar.SuggestionProvider> = remember {
        listOf(
            SearchSuggestionProvider(
                context = context,
                store = store,
                searchUseCase = searchUseCase.defaultSearch,
                fetchClient = fetchClient,
                limit = limit,
                mode = mode,
                engine = engine,
                filterExactMatch = filterExactMatch,
                showDescription = showDescription,
            ),
            SearchActionProvider(
                store,
                searchUseCase.defaultSearch,
                null, //icon,
                showDescription,
            ),
            HistoryStorageSuggestionProvider(
                historyStorage, loadUrlUseCase, icons, engine, maxNumberOfSuggestions
            ),
        )
    }
    val groups: List<AwesomeBar.SuggestionProviderGroup> = remember(providers) {
        providers.groupBy { it.groupTitle() }.map {
            AwesomeBar.SuggestionProviderGroup(
                providers = it.value,
                title = it.key,
            )
        }
    }

    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(null) {
        initialized = true
    }

    Column(
        modifier = modifier.fillMaxWidth(),
//            .testTag("inferno.awesomebar")
        verticalArrangement = Arrangement.Bottom,
    ) {
        val fetcher = remember(groups) { SuggestionFetcher(groups, profiler) }

        // This state does not need LocalContext.current.infernoTheme().valueto be remembered, because it can change if the providers list changes.
        @SuppressLint("UnrememberedMutableState") val suggestions =
            derivedStateOf { fetcher.state.value }.value.toList()
                .sortedByDescending { it.first.priority }.toMap(LinkedHashMap())

        LaunchedEffect(text, fetcher) {
            fetcher.fetch(text)
        }

        Log.d("InfernoAwesomeBar", "suggestions size: ${suggestions.size}")

        Suggestions(
            suggestions,
            orientation,
            onSuggestionClicked,
            onAutoComplete,
            onVisibilityStateUpdated,
            onScroll,
        )
    }
}