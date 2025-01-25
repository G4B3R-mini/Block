package com.shmibblez.inferno.ext

import androidx.annotation.VisibleForTesting
import com.shmibblez.inferno.R
import com.shmibblez.inferno.mozillaAndroidComponents.feature.customtabs.CustomTabsToolbarListeners
import com.shmibblez.inferno.mozillaAndroidComponents.feature.customtabs.MENU_WEIGHT
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.feature.customtabs.CustomTabsToolbarFeature

/**
 * Updates the visibility of the menu in the toolbar.
 */
fun CustomTabsToolbarFeature.updateMenuVisibility(isVisible: Boolean) {
    if (isVisible && isMenuAvailable()) {
        addMenuButton()
    } else if (!isVisible) {
        menuButton?.let {
            toolbar.removeBrowserAction(it)
        }
        menuButton = null
    }
}

/**
 * Display a menu button on the toolbar. When clicked, it activates
 * [CustomTabsToolbarListeners.menuListener].
 */
@VisibleForTesting
internal fun CustomTabsToolbarFeature.addMenuButton() {
    this.menuButton = Toolbar.ActionButton(
        imageDrawable = menuDrawableIcon,
        contentDescription = context.getString(R.string.mozac_feature_customtabs_menu_button),
        weight = { MENU_WEIGHT },
    ) {
        customTabsToolbarListeners.menuListener?.invoke()
    }.also {
        toolbar.addBrowserAction(it)
    }
}