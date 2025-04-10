package com.shmibblez.inferno.browser.readermode


import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.ext.components
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.ktx.kotlinx.coroutines.flow.filterChanged
import mozilla.components.support.webextensions.WebExtensionController

@Composable
fun rememberInfernoReaderViewFeatureState(): InfernoReaderViewFeatureState {
    var scope by remember { mutableStateOf<CoroutineScope?>(null) }
    val context = LocalContext.current
    val store = context.components.core.store
    val extensionController = remember {
        WebExtensionController(
            READER_VIEW_EXTENSION_ID,
            READER_VIEW_EXTENSION_URL,
            READER_VIEW_CONTENT_PORT,
        )
    }
    val config = remember {
        InfernoReaderViewConfig(context) { message ->
            val engineSession = store.state.selectedTab?.engineState?.engineSession
            extensionController.sendContentMessage(
                message, engineSession,
                READER_VIEW_ACTIVE_CONTENT_PORT,
            )
        }
    }
    var readerBaseUrl by remember { mutableStateOf<String?>(null) }

    DisposableEffect(null) {

        ensureExtensionInstalled(
            extensionController = extensionController,
            engine = engine,
            updateReaderBaseUrl = { readerBaseUrl = it },
        )

        scope = store.flowScoped { flow ->
            flow.mapNotNull { state -> state.tabs }.filterChanged {
                it.readerState
            }.collect { tab ->
                if (tab.readerState.connectRequired) {
                    connectReaderViewContentScript(tab)
                }
                if (tab.readerState.checkRequired) {
                    checkReaderState(tab)
                }
                if (tab.id == store.state.selectedTabId) {
                    maybeNotifyReaderStatusChange(
                        tab.readerState.readerable, tab.readerState.active
                    )
                }
            }
        }

        onDispose {
            scope?.cancel()
        }
    }

    return rememberSaveable(saver = InfernoReaderViewFeatureState.Saver) {
        InfernoReaderViewFeatureState()
    }
}

class InfernoReaderViewFeatureState constructor(
//    private val prefetchStrategy: InfernoReaderViewFeaturePrefetchStrategy = InfernoReaderViewFeaturePrefetchStrategy(),
    ) {

    // todo: put logic here, create interface for feature lifecycle events (start, stop, onBackPressed,
    //  etc, copy moz LifecycleAwareFeature)

    companion object {
        val Saver: Saver<InfernoReaderViewFeatureState, *> = infernoReaderViewSaver(
            save = {listOf(0)},
            restore ={ InfernoReaderViewFeatureState()},
        )
        internal fun saver(): Saver<InfernoReaderViewFeatureState, *> = infernoReaderViewSaver(
            save = {listOf(0)},
            restore ={ InfernoReaderViewFeatureState()},
        )
    }
}


