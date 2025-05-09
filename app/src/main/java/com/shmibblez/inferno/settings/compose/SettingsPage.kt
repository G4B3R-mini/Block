package com.shmibblez.inferno.settings.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.shmibblez.inferno.settings.compose.components.PreferenceTitle
//import com.shmibblez.R

private const val SCROLL_INDICATOR_DELAY = 10L
private const val FXA_SYNC_OVERRIDE_EXIT_DELAY = 2000L
private const val AMO_COLLECTION_OVERRIDE_EXIT_DELAY = 3000L

// todo: settings page hierarchy:
//  - toolbar item
//    - address bar settings item
//      - address bar settings page
//    - toolbar icons list
//  - tabs item
//    - general title
//      - close tabs automatically after item
//        - never
//        - one day
//        - one week
//        - one month
//        - custom [whole number input] [day/week/month selector] max is 12 months
//    - tab tray title
//      - grid or list selector
//    - tab bar title
//      - show x to close
//        - all tabs
//        - active tab
@Composable
fun SettingsPage() {
    val context = LocalContext.current

//    PreferenceTitle(R.string.def)

}