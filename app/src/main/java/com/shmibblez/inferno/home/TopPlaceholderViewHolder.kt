/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.home

import android.view.View
import com.shmibblez.inferno.R
import com.shmibblez.inferno.utils.view.ViewHolder

/**
 * View holder for a synchronous, unconditional and invisible placeholder.  This is to anchor home to
 * the top when home is created.
 */
class TopPlaceholderViewHolder(
    view: View,
) : ViewHolder(view) {

    fun bind() = Unit

    companion object {
        val LAYOUT_ID = R.layout.top_placeholder_item
    }
}
