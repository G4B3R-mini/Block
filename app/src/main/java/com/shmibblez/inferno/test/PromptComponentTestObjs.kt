package com.shmibblez.inferno.test

import android.os.Parcel
import androidx.compose.ui.graphics.Color
import mozilla.components.concept.engine.prompt.Choice
import mozilla.components.concept.engine.prompt.PromptRequest
import mozilla.components.concept.engine.prompt.PromptRequest.Alert
import mozilla.components.concept.engine.prompt.PromptRequest.TimeSelection
import mozilla.components.concept.engine.prompt.ShareData
import mozilla.components.concept.identitycredential.Account
import mozilla.components.concept.identitycredential.Provider
import mozilla.components.concept.storage.Address
import mozilla.components.concept.storage.CreditCardEntry
import mozilla.components.concept.storage.Login
import mozilla.components.concept.storage.LoginEntry
import java.util.Date
import kotlin.random.Random

class PromptComponentTestObjs {
    companion object {
        private val _logins = List(20) {
            Login(
                guid = "$it",
                username = "username$it",
                password = "password$it",
                origin = "www.google.com",
                formActionOrigin = null,
                httpRealm = "http realm",
                usernameField = "htmlUsernameField",
                passwordField = "htmlPasswordField",
                timesUsed = 0,
                timeCreated = Date().time,
                timeLastUsed = Date().time,
                timePasswordChanged = Date().time,
            )
        }
        val alert = Alert(
            title = "title",
            message = "message",
            hasShownManyDialogs = false,
            onConfirm = {},
            onDismiss = {},
        )

        // test all cases
        fun authentication(
            onlyShowPassword: Boolean,
            previousFailed: Boolean,
            isCrossOrigin: Boolean,
        ) {
            PromptRequest.Authentication(
                uri = null,
                title = "title",
                message = "message",
                userName = "username",
                password = "password",
                method = PromptRequest.Authentication.Method.HOST,
                level = PromptRequest.Authentication.Level.SECURED,
                onlyShowPassword = onlyShowPassword,
                previousFailed = previousFailed,
                isCrossOrigin = isCrossOrigin,
                onConfirm = { _, _ -> },
                onDismiss = {},
            )
        }

        val beforeUnload = PromptRequest.BeforeUnload(
            title = "title",
            onLeave = {},
            onStay = {},
            onDismiss = {},
        )
        val color = PromptRequest.Color(
            defaultColor = "#000000",
            onConfirm = {},
            onDismiss = {},
        )

        fun confirm(hasShownManyDialogs: Boolean) {
            PromptRequest.Confirm(title = "title",
                message = "message",
                hasShownManyDialogs = hasShownManyDialogs,
                positiveButtonTitle = "positiveTitle",
                negativeButtonTitle = "negativeTitle",
                neutralButtonTitle = "neutralTitle",
                onConfirmPositiveButton = {},
                onConfirmNegativeButton = {},
                onConfirmNeutralButton = {},
                onDismiss = {})
        }

        // test all cases
        fun file(isMultipleFilesSelection: Boolean) {
            PromptRequest.File(
                mimeTypes = emptyArray(),
                isMultipleFilesSelection = isMultipleFilesSelection,
                onSingleFileSelected = { _, _ -> },
                onMultipleFilesSelected = { _, _ -> },
                onDismiss = {},
            )
        }

        val privacyPolicy = PromptRequest.IdentityCredential.PrivacyPolicy(
            privacyPolicyUrl =,
            termsOfServiceUrl =,
            providerDomain =,
            host =,
            icon =,
            onConfirm = {},
            onDismiss = {},
        )
        val selectAccount = PromptRequest.IdentityCredential.SelectAccount(
            accounts = List(20) {
                Account(
                    id = it,
                    email = "email$it",
                    name = "name$it",
                    icon = null,
                )
            },
            provider = Provider(
                id = 1, icon = null, name = "name", domain = "domain"
            ),
            onConfirm = {},
            onDismiss = {},
        )
        val selectProvider = PromptRequest.IdentityCredential.SelectProvider(
            providers = List(20) {
                Provider(
                    id = 1, icon = null, name = "name", domain = "domain"
                )
            },
            onConfirm = {},
            onDismiss = {},
        )
        val menuChoice = PromptRequest.MenuChoice(
            choices =,
            onConfirm = {},
            onDismiss = {},
        )
        val multipleChoice = PromptRequest.MultipleChoice(
            choices =,
            onConfirm = {},
            onDismiss = {},
        )
        val popup = PromptRequest.Popup(
            targetUri = "targetUri",
            onAllow = {},
            onDeny = {},
            onDismiss = {},
        )
        val repost = PromptRequest.Repost(
            onConfirm = {},
            onDismiss = {},
        )
        val saveCreditCard = PromptRequest.SaveCreditCard(
            creditCard = CreditCardEntry(guid = "99",
                name = "creditCardEntry99",
                number = List(16) { Random.nextInt(0, 10) }.joinToString { n -> "$n" },
                expiryMonth = "10",
                expiryYear = "2024",
                cardType = listOf(
                    "amex", // 0
                    "cartebancaire", // 1
                    "diners",
                    "discover",
                    "jcb",
                    "mastercard",
                    "mir",
                    "unionpay",
                    "visa",
                    "", // 9

                )[Random.nextInt(0)] // random card type
            ),
            onConfirm = {},
            onDismiss = {},
        )
        val saveLoginPrompt = PromptRequest.SaveLoginPrompt(
            hint = 0,
            logins = List(20) {
                LoginEntry(
                    origin = "origin",
                    formActionOrigin = null,
                    httpRealm = "httpRealm",
                    usernameField = "usernameField",
                    passwordField = "passwordField",
                    username = "username",
                    password = "password",
                )
            },
            onConfirm = {},
            onDismiss = {},
        )
        val selectAddress = PromptRequest.SelectAddress(
            addresses = List(20) {
                Address(
                    guid = "$it",
                    name = "Address$it",
                    organization = "organization$it",
                    streetAddress = "streetAddress$it",
                    addressLevel3 = "addressLevel3$it",
                    addressLevel2 = "addressLevel2$it",
                    addressLevel1 = "addressLevel1$it",
                    postalCode = "postalCode$it",
                    country = "country$it",
                    tel = "+tel$it",
                    email = "email$it@email$it.test",
                    timeCreated = Date().time,
                    timeLastUsed = Date().time,
                    timeLastModified = Date().time,
                    timesUsed = 0,
                )
            },
            onConfirm = {},
            onDismiss = {},
        )
        val selectCreditCard = PromptRequest.SelectCreditCard(
            creditCards = List(20) {
                CreditCardEntry(guid = "$it",
                    name = "creditCardEntry$it",
                    number = List(16) { Random.nextInt(0, 10) }.joinToString { n -> "$n" },
                    expiryMonth = "10",
                    expiryYear = "2024",
                    cardType = listOf(
                        "amex", // 0
                        "cartebancaire", // 1
                        "diners",
                        "discover",
                        "jcb",
                        "mastercard",
                        "mir",
                        "unionpay",
                        "visa",
                        "", // 9

                    )[Random.nextInt(0)] // random card type
                )
            },
            onConfirm = {},
            onDismiss = {},
        )
        val selectLoginPrompt = PromptRequest.SelectLoginPrompt(
            logins = _logins,
            generatedPassword = listOf("a", "b", "c").asSequence().shuffled().take(10)
                .joinToString { it },
            onConfirm = {},
            onDismiss = {},
        )
        val share = PromptRequest.Share(
            data = ShareData(title = "title", text = "text", url = "https://www.google.com"),
            onSuccess = {},
            onFailure = {},
            onDismiss = {},
        )
        val singleChoice = PromptRequest.SingleChoice(
            choices = arrayOf(
                Choice(
                    id = "id1",
                    enable = true,
                    label = "labelSingleChoice",
                    selected = false,
                    isASeparator = false,
                    children = null,
                ),
            ),
            onConfirm = {},
            onDismiss = {},
        )

        fun textPrompt(hasShownManyDialogs: Boolean) {
            PromptRequest.TextPrompt(
                title = "title",
                inputLabel = "inputLabel",
                inputValue = "inputValue",
                hasShownManyDialogs = hasShownManyDialogs,
                onConfirm = { _, _ -> },
                onDismiss = {},
            )
        }

        val timeSelection = TimeSelection(
            title = "title",
            initialDate =,
            minimumDate =,
            maximumDate =,
            stepValue =,
            type =,
            onConfirm = {},
            onClear = {},
            onDismiss = {},
        )
    }
}