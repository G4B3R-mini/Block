/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.messaging

import mozilla.components.service.nimbus.messaging.Message
import mozilla.components.service.nimbus.messaging.NimbusMessagingControllerInterface
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.components.AppStore
import com.shmibblez.inferno.components.appstate.AppAction.MessagingAction.MessageClicked
import com.shmibblez.inferno.components.appstate.AppAction.MessagingAction.MessageDismissed

/**
 * Handles default interactions with the ui of Nimbus Messaging messages.
 */
class DefaultMessageController(
    private val appStore: AppStore,
    private val messagingController: NimbusMessagingControllerInterface,
    private val homeActivity: HomeActivity,
) : MessageController {

    override fun onMessagePressed(message: Message) {
        val intent = messagingController.getIntentForMessage(message)
        homeActivity.processIntent(intent)

        appStore.dispatch(MessageClicked(message))
    }

    override fun onMessageDismissed(message: Message) {
        appStore.dispatch(MessageDismissed(message))
    }
}
