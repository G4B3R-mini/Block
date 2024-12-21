package com.shmibblez.inferno.toolbar

import android.content.Context
import androidx.annotation.Px
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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.getActivity
import com.shmibblez.inferno.compose.sessionUseCases
import com.shmibblez.inferno.tabs.TabsTrayFragment
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarEmptyIndicator
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarSecurityIndicator
import com.shmibblez.inferno.toolbar.ToolbarOriginScopeInstance.ToolbarTrackingProtectionIndicator

// TODO: review views implementations

@Composable
fun RowScope.ToolbarSeparator() {
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
    val useCases = sessionUseCases()
    Icon(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1F)
            .alpha(if (enabled) 1F else 0.5F)
            .clickable(enabled = enabled) { useCases.goBack() },
        painter = painterResource(id = R.drawable.mozac_ic_chevron_left_24),
        contentDescription = "back",
        tint = Color.White
    )
}

@Composable
fun ToolbarRightArrow(enabled: Boolean) {
    val useCases = sessionUseCases()
    Icon(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1F)
            .alpha(if (enabled) 1F else 0.5F)
            .clickable(enabled = enabled) { useCases.goForward() },
        painter = painterResource(id = R.drawable.mozac_ic_chevron_right_24),
        contentDescription = "forward",
        tint = Color.White,
    )
}

@Composable
fun ToolbarReload(enabled: Boolean) {
    val useCases = sessionUseCases()
    Icon(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1F)
            .alpha(if (enabled) 1F else 0.5F)
            .clickable(enabled = enabled) { useCases.reload() },
        painter = painterResource(id = R.drawable.mozac_ic_arrow_clockwise_24),
        contentDescription = "reload page",
        tint = Color.White
    )
}

@Composable
fun ToolbarShowTabsTray() {
    fun showTabs(context: Context) {
        // For now we are performing manual fragment transactions here. Once we can use the new
        // navigation support library we may want to pass navigation graphs around.
        // TODO: use navigation instead of fragment transactions
        context.getActivity()?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, TabsTrayFragment())
            commit()
        }
    }

    val context = LocalContext.current
    Icon(
        modifier = Modifier
            .fillMaxHeight()
            .aspectRatio(1F)
            .alpha(1F)
            .clickable { showTabs(context) },
        painter = painterResource(id = R.drawable.mozac_ic_tab_tray_24),
        contentDescription = "show tabs tray",
        tint = Color.White
    )
}

data class ToolbarOriginData(
    val siteSecure: SiteSecurity,
    val siteTrackingProtection: SiteTrackingProtection,
    val url: String?,
    val searchTerms: String,
    val setEditMode: (Boolean) -> Unit
)

data class OriginBounds(
    val left: Dp,
    val right: Dp,
)

@Composable
fun RowScope.ToolbarOrigin(
    modifier: Modifier,
    toolbarOriginData: ToolbarOriginData,
    setOriginBounds: (OriginBounds) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .weight(1F)
            .padding(all = 4.dp)
            .border(BorderStroke(0.dp, Color.Transparent), shape = RoundedCornerShape(2.dp))
            .background(Color.DarkGray)
            .onGloballyPositioned { layoutCoordinates ->
                val left = layoutCoordinates.boundsInWindow().left.dp
                val right = layoutCoordinates.boundsInWindow().right.dp
                setOriginBounds(OriginBounds(left, right))
            },
    ) {
        // toolbar indicators
        with(toolbarOriginData) {
            ToolbarTrackingProtectionIndicator(trackingProtection = siteTrackingProtection)
            if (siteTrackingProtection != SiteTrackingProtection.OFF_GLOBALLY) ToolbarSeparator()
            ToolbarSecurityIndicator(siteSecure)
            ToolbarSeparator()
            ToolbarEmptyIndicator(enabled = url == null)
            if (url == null) ToolbarSeparator()
            // url
            Text(text = url ?: "",
                modifier = Modifier.clickable {
                    setEditMode(true)
                })
        }
    }
}

interface ToolbarOriginScope {
    @Composable
    fun ToolbarEmptyIndicator(enabled: Boolean)

    @Composable
    fun ToolbarTrackingProtectionIndicator(trackingProtection: SiteTrackingProtection?)

    @Composable
    fun ToolbarSecurityIndicator(siteSecurity: SiteSecurity)
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

enum class SiteSecurity {
    INSECURE, SECURE,
}

object ToolbarOriginScopeInstance : ToolbarOriginScope {
    @Composable
    override fun ToolbarEmptyIndicator(enabled: Boolean) {
        Icon(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
                .clickable(enabled = enabled) {
                    // TODO: what to do on click empty
                    //  show explanation message?
                },
            painter = painterResource(id = R.drawable.mozac_ic_tracking_protection_on_trackers_blocked),
            contentDescription = "empty indicator",
            tint = Color.White
        )
    }

    @Composable
    override fun ToolbarTrackingProtectionIndicator(trackingProtection: SiteTrackingProtection?) {
        // TODO: add to toolbar
        when (trackingProtection) {
            SiteTrackingProtection.ON_TRACKERS_BLOCKED, SiteTrackingProtection.ON_NO_TRACKERS_BLOCKED -> {
                Icon(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1F)
                        .clickable() {
                            // TODO: what to do on click
                        },
                    painter = painterResource(id = R.drawable.mozac_ic_tracking_protection_on_trackers_blocked),
                    contentDescription = "tracking protection indicator",
                    tint = Color.White
                )
            }

            SiteTrackingProtection.OFF_FOR_A_SITE -> {
                Icon(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1F)
                        .clickable() {
                            // TODO: what to do on click
                        },
                    painter = painterResource(id = R.drawable.mozac_ic_tracking_protection_on_trackers_blocked),
                    contentDescription = "tracking protection indicator",
                    tint = Color.White
                )
            }

            SiteTrackingProtection.OFF_GLOBALLY -> {}

            else -> {}
        }
    }

    @Composable
    override fun ToolbarSecurityIndicator(siteSecurity: SiteSecurity) {
        if (siteSecurity == SiteSecurity.SECURE) {
            Icon(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1F)
                    .clickable() {
                        // TODO: what to do on click if secure
                    },
                painter = painterResource(id = R.drawable.mozac_ic_lock_20),
                contentDescription = "security indicator",
                tint = Color.White
            )
        } else if (siteSecurity == SiteSecurity.INSECURE) {
            Icon(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1F)
                    .clickable() {
                        // TODO: what to do on click if insecure
                    },
                painter = painterResource(id = R.drawable.mozac_ic_broken_lock),
                contentDescription = "security indicator",
                tint = Color.White
            )
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
        contentDescription = "menu",
        tint = Color.White
    )
}