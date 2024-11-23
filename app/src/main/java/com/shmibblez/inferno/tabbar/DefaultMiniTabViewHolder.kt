package com.shmibblez.inferno.tabbar

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Dimension
import androidx.annotation.Dimension.Companion.DP
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatImageButton
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.tabstray.TabViewHolder
import mozilla.components.browser.tabstray.TabsTray
import mozilla.components.browser.tabstray.TabsTrayStyling
import mozilla.components.browser.tabstray.thumbnail.TabThumbnailView
import mozilla.components.concept.base.images.ImageLoadRequest
import mozilla.components.concept.base.images.ImageLoader
import mozilla.components.support.ktx.android.util.dpToPx
import mozilla.components.support.ktx.kotlin.tryGetHostFromUrl

class DefaultMiniTabViewHolder(
    itemView: View,
    private val thumbnailLoader: ImageLoader? = null,
) : TabViewHolder(itemView) {
    @VisibleForTesting
    internal val iconView: ImageView? = itemView.findViewById(R.id.mozac_browser_tabstray_icon)

    @VisibleForTesting
    internal val titleView: TextView = itemView.findViewById(R.id.mozac_browser_tabstray_title)

    @VisibleForTesting
    internal val closeView: AppCompatImageButton = itemView.findViewById(R.id.mozac_browser_tabstray_close)
    private val thumbnailView: TabThumbnailView = itemView.findViewById(R.id.mozac_browser_tabstray_thumbnail)
    private val urlView: TextView? = itemView.findViewById(R.id.mozac_browser_tabstray_url)

    override var tab: TabSessionState? = null

    @VisibleForTesting
    internal var styling: TabsTrayStyling? = null

    /**
     * Displays the data of the given session and notifies the given observable about events.
     */
    override fun bind(
        tab: TabSessionState,
        isSelected: Boolean,
        styling: TabsTrayStyling,
        delegate: TabsTray.Delegate,
    ) {
        this.tab = tab
        this.styling = styling

        val title = if (tab.content.title.isNotEmpty()) {
            tab.content.title
        } else {
            tab.content.url
        }

        titleView.text = title
        urlView?.text = tab.content.url.tryGetHostFromUrl()

        itemView.setOnClickListener {
            delegate.onTabSelected(tab)
        }

        closeView.setOnClickListener {
            delegate.onTabClosed(tab)
        }

        updateSelectedTabIndicator(isSelected)

        // In the final else case, we have no cache or fresh screenshot; do nothing instead of clearing the image.
        if (thumbnailLoader != null) {
            val thumbnailSize = THUMBNAIL_SIZE.dpToPx(thumbnailView.context.resources.displayMetrics)
            thumbnailLoader.loadIntoView(
                thumbnailView,
                ImageLoadRequest(id = tab.id, size = thumbnailSize, isPrivate = tab.content.private),
            )
        }

        iconView?.setImageBitmap(tab.content.icon)
    }

    override fun updateSelectedTabIndicator(showAsSelected: Boolean) {
        if (showAsSelected) {
            showItemAsSelected()
        } else {
            showItemAsNotSelected()
        }
    }

    @VisibleForTesting
    internal fun showItemAsSelected() {
        styling?.let { styling ->
            titleView.setTextColor(styling.selectedItemTextColor)
            itemView.setBackgroundColor(styling.selectedItemBackgroundColor)
            closeView.imageTintList = ColorStateList.valueOf(styling.selectedItemTextColor)
        }
    }

    @VisibleForTesting
    internal fun showItemAsNotSelected() {
        styling?.let { styling ->
            titleView.setTextColor(styling.itemTextColor)
            itemView.setBackgroundColor(styling.itemBackgroundColor)
            closeView.imageTintList = ColorStateList.valueOf(styling.itemTextColor)
        }
    }

    companion object {
        @Dimension(unit = DP)
        private const val THUMBNAIL_SIZE = 100
    }
}