package com.shmibblez.inferno.toolbar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import mozilla.components.browser.state.state.TabSessionState

// TODO: review views implementations

@Composable
fun ToolbarSeparator() {
    Divider(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 2.dp),
        color = Color.White,
        thickness = 1.dp,
    )
}

@Composable
fun ToolbarLeftArrow(enabled: Boolean) {
    Icon(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1F)
            .alpha(if (enabled) 1F else 0.5F)
            .clickable(enabled = enabled) {
                // TODO: what to do on click back
            },
        painter = painterResource(id = R.drawable.mozac_ic_chevron_left_24),
        contentDescription = "back",
        tint = Color.White
    )
}

@Composable
fun ToolbarRightArrow(enabled: Boolean) {
    Icon(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1F)
            .alpha(if (enabled) 1F else 0.5F)
            .clickable(enabled = enabled) {
                // TODO: what to do on click forward
            },
        painter = painterResource(id = R.drawable.mozac_ic_chevron_right_24),
        contentDescription = "back",
        tint = Color.White,
    )
}

@Composable
fun ToolbarReload(enabled: Boolean) {
    Icon(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1F)
            .alpha(if (enabled) 1F else 0.5F)
            .clickable(enabled = enabled) {
                // TODO: what to do on click back
            },
        painter = painterResource(id = R.drawable.mozac_ic_arrow_clockwise_24),
        contentDescription = "back",
        tint = Color.White
    )
}

@Composable
public fun RowScope.ToolbarOrigin(tabSessionState: TabSessionState) {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .weight(1F)
            .padding(all = 4.dp)
            .border(BorderStroke(0.dp, Color.Transparent), shape = RoundedCornerShape(2.dp))
            .background(Color.DarkGray)
    ) {
        // TODO:

    }
}


enum class SiteSecurity {
    INSECURE, SECURE,
}

/**
 * Indicates which tracking protection status a site has.
 */
enum class SiteTrackingProtection {
    /**
     * The site has tracking protection enabled, but none trackers have been blocked or detected.
     */
    ON_NO_TRACKERS_BLOCKED,

    /**
     * The site has tracking protection enabled, and trackers have been blocked or detected.
     */
    ON_TRACKERS_BLOCKED,

    /**
     * Tracking protection has been disabled for a specific site.
     */
    OFF_FOR_A_SITE,

    /**
     * Tracking protection has been disabled for all sites.
     */
    OFF_GLOBALLY,
}

interface ToolbarOriginIcon {
    @Composable
    fun ToolbarEmptyIndicator() {
        // TODO: what is this
    }

    @Composable
    fun ToolbarTrackingProtectionIndicator(trackingProtection: SiteTrackingProtection?) {
        // TODO: add to toolbar
        when (trackingProtection) {
            SiteTrackingProtection.ON_TRACKERS_BLOCKED, SiteTrackingProtection.ON_NO_TRACKERS_BLOCKED -> {
                Icon(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1F)
                        .clickable() {
                            // TODO: what to do on click back
                        },
                    painter = painterResource(id = R.drawable.mozac_ic_tracking_protection_on_trackers_blocked),
                    contentDescription = "back",
                    tint = Color.White
                )
            }

            SiteTrackingProtection.OFF_FOR_A_SITE -> {
                Icon(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1F)
                        .clickable() {
                            // TODO: what to do on click back
                        },
                    painter = painterResource(id = R.drawable.mozac_ic_tracking_protection_on_trackers_blocked),
                    contentDescription = "back",
                    tint = Color.White
                )
            }

            SiteTrackingProtection.OFF_GLOBALLY -> {}

            else -> {}
        }
    }
}


@Composable
fun ToolbarMenu() {
    // TODO: menu popup
    Icon(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1F)
            .clickable() {
                // TODO: what to do on click back
            },
        painter = painterResource(id = R.drawable.mozac_ic_app_menu_24),
        contentDescription = "back",
        tint = Color.White
    )
}

@Composable
fun ToolbarSecurityIndicator(secure: Boolean) {
    if (secure) {
        Icon(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
                .clickable() {
                    // TODO: what to do on click if secure
                },
            painter = painterResource(id = R.drawable.mozac_ic_lock_20),
            contentDescription = "back",
            tint = Color.White
        )
    } else {
        Icon(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
                .clickable() {
                    // TODO: what to do on click if insecure
                },
            painter = painterResource(id = R.drawable.mozac_ic_broken_lock),
            contentDescription = "back",
            tint = Color.White
        )
    }
}