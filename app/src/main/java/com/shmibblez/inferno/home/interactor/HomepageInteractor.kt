/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.home.interactor

import com.shmibblez.inferno.home.bookmarks.interactor.BookmarksInteractor
//import com.shmibblez.inferno.home.pocket.interactor.PocketStoriesInteractor
import com.shmibblez.inferno.home.privatebrowsing.interactor.PrivateBrowsingInteractor
import com.shmibblez.inferno.home.recentsyncedtabs.interactor.RecentSyncedTabInteractor
import com.shmibblez.inferno.home.recenttabs.interactor.RecentTabInteractor
import com.shmibblez.inferno.home.recentvisits.interactor.RecentVisitsInteractor
import com.shmibblez.inferno.home.sessioncontrol.CollectionInteractor
import com.shmibblez.inferno.home.sessioncontrol.CustomizeHomeIteractor
import com.shmibblez.inferno.home.sessioncontrol.MessageCardInteractor
import com.shmibblez.inferno.home.sessioncontrol.TabSessionInteractor
import com.shmibblez.inferno.home.sessioncontrol.TopSiteInteractor
import com.shmibblez.inferno.home.sessioncontrol.WallpaperInteractor
import com.shmibblez.inferno.home.toolbar.ToolbarInteractor
import com.shmibblez.inferno.search.toolbar.SearchSelectorInteractor

/**
 * Homepage interactor for interactions with the homepage UI.
 */
interface HomepageInteractor :
    CollectionInteractor,
    TopSiteInteractor,
    TabSessionInteractor,
    ToolbarInteractor,
    MessageCardInteractor,
    RecentTabInteractor,
    RecentSyncedTabInteractor,
    BookmarksInteractor,
    RecentVisitsInteractor,
    CustomizeHomeIteractor,
//    PocketStoriesInteractor,
    PrivateBrowsingInteractor,
    SearchSelectorInteractor,
    WallpaperInteractor
