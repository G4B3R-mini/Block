package com.shmibblez.inferno.ext

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.shmibblez.inferno.browser.getActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import mozilla.components.lib.state.Action
import mozilla.components.lib.state.State
import mozilla.components.lib.state.Store
import mozilla.components.lib.state.ext.channel
import mozilla.components.lib.state.ext.consumeFlow
import mozilla.components.lib.state.ext.flow
import mozilla.components.support.ktx.android.view.toScope

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

@SuppressLint("ComposableNaming")
@OptIn(ExperimentalCoroutinesApi::class)
fun <S : State, A : Action> consumeFrom(
    store: Store<S, A>,
    context: Context,
    lifecycleOwner: LifecycleOwner,
    block: (S) -> Unit,
) {
    val scope = lifecycleOwner.lifecycleScope // view.toScope()
    val channel = store.channel(owner = lifecycleOwner)

    scope.launch {
        channel.consumeEach { state ->
            // We are using a scope that is bound to the view being attached here. It can happen
            // that the "view detached" callback gets executed *after* the fragment was detached. If
            // a `consumeFrom` runs in exactly this moment then we run inside a detached fragment
            // without a `Context` and this can cause a variety of issues/crashes.
            // See: https://github.com/mozilla-mobile/android-components/issues/4125
            //
            // To avoid this, we check whether the fragment still has an activity and a view
            // attached. If not then we run in exactly that moment between fragment detach and view
            // detach. It would be better if we could use `viewLifecycleOwner` which is bound to
            // onCreateView() and onDestroyView() of the fragment. But:
            // - `viewLifecycleOwner` is only available in alpha versions of AndroidX currently.
            // - We found a bug where `viewLifecycleOwner.lifecycleScope` is not getting cancelled
            //   causing this coroutine to run forever.
            //   See: https://github.com/mozilla-mobile/android-components/issues/3828
            // Once those two issues get resolved we can remove the `isAdded` check and use
            // `viewLifecycleOwner.lifecycleScope` instead of the view scope.
            //
            // In a previous version we tried using `isAdded` and `isDetached` here. But in certain
            // situations they reported true/false in situations where no activity was attached to
            // the fragment. Therefore we switched to explicitly check for the activity and view here.
            if (context.getActivity() != null) { // && fragment.view != null) {
                block(state)
            }
        }
    }
}

@MainThread
fun <S : State, A : Action> consumeFlow(
    from: Store<S, A>,
    owner: LifecycleOwner,
    context: Context,
    coroutineScope: CoroutineScope,
    block: suspend (Flow<S>) -> Unit,
) {
//    val fragment = this
//    val view = checkNotNull(view) { "Fragment has no view yet. Call from onViewCreated()." }

    // It's important to create the flow here directly instead of in the coroutine below,
    // as otherwise the fragment could be removed before the subscription is created.
    // This would cause us to create an unnecessary subscription leaking the fragment,
    // as we only unsubscribe on destroy which already happened.
    val flow = from.flow(owner)

//    val scope = view.toScope()
//    scope.launch {
    coroutineScope.launch {
        val filtered = flow.filter {
            // We ignore state updates if the fragment does not have an activity or view
            // attached anymore.
            // See comment in [consumeFrom] above.
            context.getActivity() != null // && fragment.view != null
        }

        block(filtered)
    }
}

fun isLargeWindow(context: Context): Boolean {
    return context.isLargeWindow()
}