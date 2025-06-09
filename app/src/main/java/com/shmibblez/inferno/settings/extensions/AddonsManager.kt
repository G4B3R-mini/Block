package com.shmibblez.inferno.settings.extensions

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.compose.base.InfernoIcon
import com.shmibblez.inferno.ext.components
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.AddonManager
import mozilla.components.support.base.feature.LifecycleAwareFeature
import com.shmibblez.inferno.R

@Composable
internal fun rememberAddonsManagerState(
    addonManager: AddonManager = LocalContext.current.components.addonManager,
): MutableState<AddonsManagerState> {
    val state = remember {
        mutableStateOf(
            AddonsManagerState(
                addonManager = addonManager,
            )
        )
    }

    DisposableEffect(null) {
        state.value.start()
        onDispose { state.value.stop() }
    }

    return state
}

internal class AddonsManagerState(
    val addonManager: AddonManager,
) : LifecycleAwareFeature {

    var addons by mutableStateOf<List<Addon>>(emptyList())

    override fun start() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

}

private const val VIEW_HOLDER_TYPE_SECTION = 0
private const val VIEW_HOLDER_TYPE_NOT_YET_SUPPORTED_SECTION = 1
private const val VIEW_HOLDER_TYPE_ADDON = 2
private const val VIEW_HOLDER_TYPE_FOOTER = 3
private const val VIEW_HOLDER_TYPE_HEADER = 4

private val ADDON_ICON_SIZE = 34.dp

@Composable
internal fun AddonsManager(
    state: AddonsManagerState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
    ) {

    }
}

@Composable
private fun AddonItem(addon: Addon) {
    Row() {
        when (addon.icon) {
            null -> {
                InfernoIcon(
                    painter = painterResource(R.drawable.ic_globe_24),
                    contentDescription = "",
                    modifier = Modifier.size(ADDON_ICON_SIZE),
                )
            }

            else -> {
                Image(
                    bitmap = addon.icon!!.asImageBitmap(),
                    contentDescription = "",
                    modifier = Modifier.size(ADDON_ICON_SIZE),
                )
            }
        }
    }
}