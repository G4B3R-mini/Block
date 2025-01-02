/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.library

import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import mozilla.components.support.ktx.android.content.getColorFromAttr
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.browsingmode.BrowsingMode
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.setToolbarColors

abstract class LibraryPageFragment<T> : Fragment() {

    abstract val selectedItems: Set<T>

    protected fun close() {
        if (!findNavController().popBackStack(R.id.browserFragment, false)) {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    protected fun openItemsInNewTab(private: Boolean = false, toUrl: (T) -> String?) {
        context?.components?.useCases?.tabsUseCases?.let { tabsUseCases ->
            selectedItems.asSequence()
                .mapNotNull(toUrl)
                .forEach { url ->
                    tabsUseCases.addTab.invoke(url, private = private)
                }
        }

        (activity as HomeActivity).browsingModeManager.mode = BrowsingMode.fromBoolean(private)
    }

    override fun onDetach() {
        super.onDetach()
        context?.let {
            activity?.title = getString(R.string.app_name)
            activity?.findViewById<Toolbar>(R.id.navigationToolbar)?.setToolbarColors(
                it.getColorFromAttr(R.attr.textPrimary),
                it.getColorFromAttr(R.attr.layer1),
            )
        }
    }
}
