package com.shmibblez.inferno.tabbar

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.tabstray.TabsTray
import mozilla.components.browser.tabstray.TabsTrayStyling
import mozilla.components.support.ktx.kotlin.tryGetHostFromUrl
import com.shmibblez.inferno.R

abstract class MiniTabViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract var tab: TabSessionState?

    /**
     * Binds the ViewHolder to the `Tab`.
     * @param tab the `Tab` used to bind the viewHolder.
     * @param isSelected boolean to describe whether or not the `Tab` is selected.
     * @param observable message bus to pass events to Observers of the TabsTray.
     * // TODO fix comment
     */
    abstract fun bind(
        tab: TabSessionState,
        isSelected: Boolean,
        styling: TabsTrayStyling,
        delegate: TabsTray.Delegate,
    )

    /**
     * Ask for a partial update of the current tab.
     * Allows for overriding the current behavior and add or remove the 'selected tab' UI decorator.
     *
     * When implementing this do not call super.
     */
    open fun updateSelectedTabIndicator(showAsSelected: Boolean) {
        // Not an abstract fun since not all clients of this library might be interested in this functionality.
        // But throwing an exception if this is called without an actual implementation in clients.
        throw UnsupportedOperationException("Method not yet implemented")
    }
}

class DefaultMiniTabViewHolder(
    itemView: View,
) : MiniTabViewHolder(itemView) {

    // TODO: update layout for tabs
    // TODO: review code for specific use case

    @VisibleForTesting
    internal val iconView: ImageView? = itemView.findViewById(R.id.mozac_browser_tabstray_icon)

    @VisibleForTesting
    internal val titleView: TextView = itemView.findViewById(R.id.mozac_browser_tabstray_title)

    @VisibleForTesting
    internal val closeView: AppCompatImageButton =
        itemView.findViewById(R.id.mozac_browser_tabstray_close)
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

        val title = tab.content.title.ifEmpty {
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
}