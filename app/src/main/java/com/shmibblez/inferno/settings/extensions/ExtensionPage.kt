package com.shmibblez.inferno.settings.extensions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.ext.infernoTheme
import com.shmibblez.inferno.settings.compose.components.InfernoSettingsPage
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.ui.translateName

@Composable
internal fun ExtensionPage(addon: Addon, goBack: () -> Unit) {
    val context = LocalContext.current

    InfernoSettingsPage(
        title = addon.translateName(context), // todo: title?
        goBack = goBack,
    ) { edgeInsets ->
        // todo: extension page
        LazyColumn(
            modifier = Modifier.padding(edgeInsets),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {

            if (addon.isInstalled()) {
                installedAddonOptions()
            }

            // addon description
            extensionDescription(addon)
        }
    }
}

private fun LazyListScope.installedAddonOptions() {
    item {
        // todo: item options
    }

    divider()
}

private fun LazyListScope.divider() {
    item {
        val dividerColor = LocalContext.current.infernoTheme().value.secondaryBackgroundColor
        HorizontalDivider(thickness = 1.dp, color = dividerColor)
    }
}

private fun LazyListScope.extensionDescription(addon: Addon) {
    // todo: extension info items, add page separators and padding
    item {

    }

    divider()

    item {  }
}