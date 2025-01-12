/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.trackingprotection

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.shmibblez.inferno.R
import com.shmibblez.inferno.databinding.TrackingProtectionCategoryBinding

class TrackingProtectionCategoryItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val binding = TrackingProtectionCategoryBinding.inflate(
        LayoutInflater.from(context),
        this,
    )

    init {
        context.withStyledAttributes(
            attrs,
            R.styleable.TrackingProtectionCategory,
            defStyleAttr,
            0,
        ) {
            binding.trackingProtectionCategoryTitle.text = resources.getString(
                getResourceId(
                    R.styleable.TrackingProtectionCategory_categoryItemTitle,
                    R.string.etp_cookies_title,
                ),
            )
            binding.trackingProtectionCategoryItemDescription.text = resources.getString(
                getResourceId(
                    R.styleable.TrackingProtectionCategory_categoryItemDescription,
                    R.string.etp_cookies_description,
                ),
            )
        }
    }

    /**
     * The displayed title of this item.
     */
    val trackingProtectionCategoryTitle = binding.trackingProtectionCategoryTitle

    /**
     * The displayed description of this item.
     */
    val trackingProtectionCategoryItemDescription = binding.trackingProtectionCategoryItemDescription
}
