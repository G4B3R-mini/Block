package com.shmibblez.inferno.toolbar

import androidx.compose.runtime.Composable

internal enum class ToolbarItemKey {
    toolbar_item_settings,
    toolbar_item_origin,
    toolbar_item_origin_mini,
    toolbar_item_back,
    toolbar_item_forward,
    toolbar_item_reload,
    toolbar_item_request_desktop,
    toolbar_item_find_in_page,
    toolbar_item_request_reader_view,
    toolbar_item_private_mode,
    toolbar_item_show_tabs_tray,
    toolbar_item_share,
    toolbar_item_menu,
}

//internal object ToolbarItemKey {
//    const val TOOLBAR_ITEM_SETTINGS = "toolbar_item_settings"
//    const val TOOLBAR_ITEM_BACK = "toolbar_item_back"
//    const val TOOLBAR_ITEM_FORWARD = "toolbar_item_forward"
//    const val TOOLBAR_ITEM_RELOAD = "toolbar_item_reload"
//    const val TOOLBAR_ITEM_REQUEST_DESKTOP = "toolbar_item_request_desktop"
//    const val TOOLBAR_ITEM_FIND_IN_PAGE = "toolbar_item_find_in_page"
//    const val TOOLBAR_ITEM_REQUEST_READER_VIEW = "toolbar_item_request_reader_view"
//    const val TOOLBAR_ITEM_PRIVATE_MODE = "toolbar_item_private_mode"
//    const val TOOLBAR_ITEM_SHOW_TABS_TRAY = "toolbar_item_show_tabs_tray"
//    const val TOOLBAR_ITEM_SHARE = "toolbar_item_share"
//    const val TOOLBAR_ITEM_MENU = "toolbar_item_menu"
//}

internal fun ToolbarItemKey.fromString(key: String): ToolbarItemKey {
    return when(key) {
        "toolbar_item_settings" -> ToolbarItemKey.toolbar_item_settings
        "toolbar_item_origin" -> ToolbarItemKey.toolbar_item_origin
        "toolbar_item_origin_mini" -> ToolbarItemKey.toolbar_item_origin_mini
        "toolbar_item_back" -> ToolbarItemKey.toolbar_item_back
        "toolbar_item_forward" -> ToolbarItemKey.toolbar_item_forward
        "toolbar_item_reload" -> ToolbarItemKey.toolbar_item_reload
        "toolbar_item_request_desktop" -> ToolbarItemKey.toolbar_item_request_desktop
        "toolbar_item_find_in_page" -> ToolbarItemKey.toolbar_item_find_in_page
        "toolbar_item_request_reader_view" -> ToolbarItemKey.toolbar_item_request_reader_view
        "toolbar_item_private_mode" -> ToolbarItemKey.toolbar_item_private_mode
        "toolbar_item_show_tabs_tray" -> ToolbarItemKey.toolbar_item_show_tabs_tray
        "toolbar_item_share" -> ToolbarItemKey.toolbar_item_share
        "toolbar_item_menu" -> ToolbarItemKey.toolbar_item_menu
        else -> {throw IllegalArgumentException("unknown key $key provided, make sure member of ${ToolbarItemKey::class.simpleName}")}
    }
}

@Composable
internal fun ToolbarItem(key: ToolbarItemKey) {
    when (key) {
        ToolbarItemKey.toolbar_item_settings -> {
            /* todo: pair with composable, add all possible params in order at the top */
        }
        ToolbarItemKey.toolbar_item_origin -> {
            /* todo: pair with composable, add all possible params in order at the top */
        }
        ToolbarItemKey.toolbar_item_origin_mini -> {
            /* todo: pair with composable, add all possible params in order at the top */
        }
        ToolbarItemKey.toolbar_item_back -> {
            /* todo: pair with composable, add all possible params in order at the top */
        }
        ToolbarItemKey.toolbar_item_forward -> {
            /* todo: pair with composable, add all possible params in order at the top */
        }
        ToolbarItemKey.toolbar_item_reload -> {
            /* todo: pair with composable, add all possible params in order at the top */
        }
        ToolbarItemKey.toolbar_item_request_desktop -> {
            /* todo: pair with composable, add all possible params in order at the top */
        }
        ToolbarItemKey.toolbar_item_find_in_page -> {
            /* todo: pair with composable, add all possible params in order at the top */
        }
        ToolbarItemKey.toolbar_item_request_reader_view -> {
            /* todo: pair with composable, add all possible params in order at the top */
        }
        ToolbarItemKey.toolbar_item_private_mode -> {
            /* todo: pair with composable, add all possible params in order at the top */
        }
        ToolbarItemKey.toolbar_item_show_tabs_tray -> {
            /* todo: pair with composable, add all possible params in order at the top */
        }
        ToolbarItemKey.toolbar_item_share -> {
            /* todo: pair with composable, add all possible params in order at the top */
        }
        ToolbarItemKey.toolbar_item_menu -> {
            /* todo: pair with composable, add all possible params in order at the top */
        }
    }
}
