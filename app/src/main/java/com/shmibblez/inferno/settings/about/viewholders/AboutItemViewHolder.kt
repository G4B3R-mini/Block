/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.settings.about.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.shmibblez.inferno.R
import com.shmibblez.inferno.databinding.AboutListItemBinding
import com.shmibblez.inferno.settings.about.AboutPageItem
import com.shmibblez.inferno.settings.about.AboutPageListener

class AboutItemViewHolder(
    view: View,
    listener: AboutPageListener,
) : RecyclerView.ViewHolder(view) {

    private lateinit var item: AboutPageItem
    val binding = AboutListItemBinding.bind(view)

    init {
        itemView.setOnClickListener {
            listener.onAboutItemClicked(item.type)
        }
    }

    fun bind(item: AboutPageItem) {
        this.item = item
        binding.aboutItemTitle.text = item.title
    }

    companion object {
        val LAYOUT_ID
 = R.layout.about_list_item
    }
}
