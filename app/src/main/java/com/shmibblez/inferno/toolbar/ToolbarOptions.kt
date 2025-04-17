package com.shmibblez.inferno.toolbar

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.ComponentDimens
import com.shmibblez.inferno.compose.base.InfernoText
import com.shmibblez.inferno.compose.sessionUseCases
import mozilla.components.support.ktx.android.content.createChooserExcludingCurrentApp

// TODO: test implementations
// todo: add options icons for
//  - settings (settings icon)
//  - find in page (search icon)
//  - view desktop site toggle (desktop icon)
//  - share (share icon)
//  - private mode toggle (incog symbol) (incog with browser icon bottom right, reversed for switch back)
//  - print page
//  - scrolling screenshot
//  - extensions (go to extensions page, installed)
//  - reader view (if enabled for page)
// todo:
//   - add settings screen for options shown on toolbar, max 7, rearrangeable and selectable from list
//   - add double up and down chevron icon for options tray
//     - options tray is a sheet that pops up or down and shows all options available as icons,
//     scrollable horizontal
//   - make options in menu a grid


internal val TOOLBAR_ICON_SIZE = 18.dp
internal val TOOLBAR_ICON_PADDING = 12.dp
internal val TOOLBAR_INDICATOR_ICON_SIZE = 16.dp
internal val TOOLBAR_INDICATOR_ICON_PADDING = 4.dp
internal val TOOLBAR_MENU_OPTION_HEIGHT = 40.dp

internal class ToolbarOptions {
    companion object {
        /**
         * @param progress 0.0 is 0%, 1.0 is 100%
         */
        @Composable
        internal fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = modifier
                    .height(ComponentDimens.PROGRESS_BAR_HEIGHT)
                    .fillMaxWidth(),
                color = Color.Red,
                trackColor = Color.Black
            )
        }
        // TODO: add options icons for
        //  - find in page (search icon)
        //  - switch to desktop site (desktop icon)
        //  - share (share icon)
        //  - settings (settings icon)
        //  - private mode (incog symbol)
        //  - print page
        //  - scrolling screenshot

        private const val DISABLED_ALPHA = 0.5F

        @Composable
        fun ToolbarSeparator() {
            VerticalDivider(
                modifier = Modifier.height(TOOLBAR_ICON_SIZE),
                color = Color.White,
                thickness = 1.dp,
            )
        }

        @Composable
        fun ToolbarBack(enabled: Boolean) {
            val useCases = sessionUseCases()
            Icon(
                modifier = Modifier
                    .size(TOOLBAR_ICON_SIZE)
                    .alpha(if (enabled) 1F else DISABLED_ALPHA)
                    .clickable(enabled = enabled) { useCases.goBack.invoke() },
                painter = painterResource(id = R.drawable.ic_chevron_left_24),
                contentDescription = "back",
                tint = Color.White
            )
        }

        @Composable
        fun ToolbarForward(enabled: Boolean) {
            val useCases = sessionUseCases()
            Icon(
                modifier = Modifier
                    .size(TOOLBAR_ICON_SIZE)
                    .alpha(if (enabled) 1F else DISABLED_ALPHA)
                    .clickable(enabled = enabled) { useCases.goForward.invoke() },
                painter = painterResource(id = R.drawable.ic_chevron_right_24),
                contentDescription = "forward",
                tint = Color.White,
            )
        }

        @Composable
        fun ToolbarReload(enabled: Boolean, loading: Boolean) {
            val useCases = sessionUseCases()
            Icon(
                modifier = Modifier
                    .size(TOOLBAR_ICON_SIZE)
                    .alpha(if (enabled) 1F else DISABLED_ALPHA)
                    .clickable(enabled = enabled) {
                        if (loading) useCases.stopLoading.invoke() else useCases.reload.invoke()
                    },
                painter = if (loading) painterResource(id = R.drawable.ic_cross_24) else painterResource(
                    id = R.drawable.ic_arrow_clockwise_24
                ),
                contentDescription = "reload page",
                tint = Color.White
            )
        }

        @Composable
        fun ToolbarShare(url: String?) {
            val context = LocalContext.current
            fun share() {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(Intent.EXTRA_TEXT, url)
                }

                try {
                    context.startActivity(
                        intent.createChooserExcludingCurrentApp(
                            context,
                            context.getString(R.string.mozac_feature_contextmenu_share_link),
                        ),
                    )
                } catch (e: ActivityNotFoundException) {
                    mozilla.components.support.base.log.Log.log(
                        mozilla.components.support.base.log.Log.Priority.WARN,
                        message = "No activity to share to found",
                        throwable = e,
                        tag = "createShareLinkCandidate",
                    )
                }
            }

            val enabled = url != null
            Icon(
                modifier = Modifier
                    .size(TOOLBAR_ICON_SIZE)
                    .alpha(if (enabled) 1F else DISABLED_ALPHA)
                    .clickable(enabled = enabled, onClick = ::share),
                painter = painterResource(id = R.drawable.ic_share),
                contentDescription = "share",
                tint = Color.White
            )
        }

        @Composable
        fun ToolbarShowTabsTray(tabCount: Int, onNavToTabsTray: () -> Unit) {
            Box(
                modifier = Modifier
                    .size(TOOLBAR_ICON_SIZE)
//                .alpha(0.5F)
                    .clickable { onNavToTabsTray.invoke() }
                    .wrapContentHeight(unbounded = true),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = R.drawable.ic_tabcounter_box_24),
                    contentDescription = "show tabs tray",
                    tint = Color.White,
                )
                InfernoText(
                    modifier = Modifier.fillMaxSize(),
                    text = tabCount.toString(),
                    fontColor = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 9.sp
                )
            }
        }

        @Composable
        fun ToolbarMenuIcon(onShowMenuBottomSheet: () -> Unit) {
            Icon(
                modifier = Modifier
                    .size(TOOLBAR_ICON_SIZE)
                    .clickable(onClick = onShowMenuBottomSheet),
                painter = painterResource(id = R.drawable.ic_app_menu_24),
                contentDescription = "menu",
                tint = Color.White
            )
        }
    }
}
