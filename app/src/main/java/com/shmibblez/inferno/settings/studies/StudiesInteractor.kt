/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.settings.studies

import mozilla.components.service.nimbus.NimbusApi
import org.mozilla.experiments.nimbus.internal.EnrolledExperiment
import com.shmibblez.inferno.BrowserDirection
import com.shmibblez.inferno.HomeActivity

interface StudiesInteractor {
    /**
     * Open the given [url] in the browser.
     */
    fun openWebsite(url: String)

    /**
     * Remove a study by the given [experiment].
     */
    fun removeStudy(experiment: EnrolledExperiment)
}

class DefaultStudiesInteractor(
    private val homeActivity: HomeActivity,
    private val experiments: NimbusApi,
) : StudiesInteractor {
    override fun openWebsite(url: String) {
        homeActivity.openToBrowserAndLoad(
            searchTermOrURL = url,
            newTab = true,
            from = BrowserDirection.FromStudiesFragment,
        )
    }

    override fun removeStudy(experiment: EnrolledExperiment) {
        experiments.optOut(experiment.slug)
        experiments.applyPendingExperiments()
    }
}
