/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.settings.logins.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.shmibblez.inferno.R
import com.shmibblez.inferno.settings.logins.SavedLogin
import com.shmibblez.inferno.settings.logins.interactor.SavedLoginsInteractor

class LoginsAdapter(
    private val interactor: SavedLoginsInteractor,
) : ListAdapter<SavedLogin, LoginsListViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): LoginsListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.logins_item, parent, false)
        return LoginsListViewHolder(view, interactor)
    }

    override fun onBindViewHolder(holder: LoginsListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<SavedLogin>() {
        override fun areItemsTheSame(oldItem: SavedLogin, newItem: SavedLogin) =
            oldItem.guid == newItem.guid

        override fun areContentsTheSame(oldItem: SavedLogin, newItem: SavedLogin) =
            oldItem.origin == newItem.origin && oldItem.username == newItem.username
    }
}
