/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.exceptions.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.shmibblez.inferno.R
import com.shmibblez.inferno.exceptions.ExceptionsInteractor

class ExceptionsDeleteButtonViewHolder(
    view: View,
    private val interactor: ExceptionsInteractor<*>,
) : RecyclerView.ViewHolder(view) {

    init {
        val deleteButton: MaterialButton = view.findViewById(R.id.removeAllExceptions)
        deleteButton.setOnClickListener {
            interactor.onDeleteAll()
        }
    }
}
