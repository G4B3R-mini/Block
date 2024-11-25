package com.shmibblez.inferno.tabbar

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import mozilla.components.concept.toolbar.ScrollableToolbar
import mozilla.components.ui.widgets.behavior.EngineViewScrollingBehavior

class ToolbarWrapper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayoutCompat(context, attrs, defStyleAttr), ScrollableToolbar {

    override fun enableScrolling() {
        // Behavior can be changed without us knowing. Not safe to use a memoized value.
        (layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
            (behavior as? EngineViewScrollingBehavior)?.enableScrolling()
        }
    }

    override fun disableScrolling() {
        // Behavior can be changed without us knowing. Not safe to use a memoized value.
        (layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
            (behavior as? EngineViewScrollingBehavior)?.disableScrolling()
        }
    }

    override fun expand() {
        (layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
            (behavior as? EngineViewScrollingBehavior)?.forceExpand(this@ToolbarWrapper)
        }
    }

    override fun collapse() {
        (layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
            (behavior as? EngineViewScrollingBehavior)?.forceCollapse(this@ToolbarWrapper)
        }
    }
}
