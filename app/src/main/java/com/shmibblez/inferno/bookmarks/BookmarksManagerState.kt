package com.shmibblez.inferno.bookmarks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.shmibblez.inferno.biometric.BiometricPromptCallbackManager
import mozilla.components.support.base.feature.LifecycleAwareFeature

@Composable
fun rememberBookmarksManagerState(): MutableState<BookmarksManagerState> {
    val state = remember { mutableStateOf(BookmarksManagerState()) }

    DisposableEffect(null) {
        state.value.start()
        onDispose { state.value.stop() }
    }

    return state
}

/**
 * todo: also create ActivityResultManager, similar to [BiometricPromptCallbackManager],
 *  - allow adding listeners
 *  - store in activity, update listeners with manager.onActivityResult() with result code etc.
 *  - check way to create launchers, might be error if created after activity onCreate(), if not,
 *    then add fun manager.createLauncher() with necessary params
 *  - check compatibility with moz FileManager and AndroidPhotoPicker, those are the main reason why
 *    this is needed
 */

class BookmarksManagerState(

): LifecycleAwareFeature {
    // todo: add states for selection and normal

    override fun start() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }
}