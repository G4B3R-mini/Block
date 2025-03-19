package com.shmibblez.inferno.test

import android.util.Log
import androidx.core.util.toRange
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
import java.util.Calendar
import java.util.Date
import kotlin.random.Random

class PromptComponentTestObjs {
    companion object {
        val alert = Alert(
            title = "title",
            message = "message",
            hasShownManyDialogs = false,
            onConfirm = { _ -> },
            onDismiss = {},
        )

        // test all cases
        fun authentication(
            onlyShowPassword: Boolean,
            // todo: what to change if previous failed or cross origin
            previousFailed: Boolean,
            isCrossOrigin: Boolean,
        ): PromptRequest.Authentication {
            return PromptRequest.Authentication(
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

        fun confirm(hasShownManyDialogs: Boolean): PromptRequest.Confirm {
            return PromptRequest.Confirm(title = "title",
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
        // todo: massiv bugs yo
        fun file(isMultipleFilesSelection: Boolean): PromptRequest.File {
            return PromptRequest.File(
                mimeTypes = emptyArray(),
                isMultipleFilesSelection = isMultipleFilesSelection,
                onSingleFileSelected = { _, _ -> },
                onMultipleFilesSelected = { _, _ -> },
                onDismiss = {},
            )
        }

        val privacyPolicy = PromptRequest.IdentityCredential.PrivacyPolicy(
            privacyPolicyUrl = "https://www.google.com",
            termsOfServiceUrl = "https://www.google.com",
            providerDomain = "provider domain",
            host = "host",
            icon = "null",
            onConfirm = {},
            onDismiss = {},
        )

        // todo: test with real logins for icons
        val selectAccount = PromptRequest.IdentityCredential.SelectAccount(
            accounts = List(20) {
                Account(
                    id = it,
                    email = "email$it",
                    name = "name$it",
                    icon = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAtfSURBVHgBnVdbbFv1Gf/Z52If28c+dnyLndgnl15D2iRNadrSNoUxAkgjTCBg08T6hrYHyrQXNE2UbdK0p5WnsT3Rt0p74DJQkabSZlxWCr3RdM2N1Ekcx3cf34/tc4732WiVmDpAi2Qlsezzff/v+93+JnzHn9nZWYnTq7O6YRxrqo0xk4mRg8F+yTDaaDYbisMhxSx2x3WfLzDXQuOtU6dOKd/luaZv+8DM9JTMW/gXXc6en8LMSI1mHWq9DrQN+Hx9YFm20wDMZgYw84hGBmG022ibzW9YbeZXX3755dj/1cDs9LRUN+qvaK3WSYblEQiGIPb4UFNrKBZyYM1m+AMRWK02qNUyqoUkBI6FzR2EwQjwB4NgWAuaevu0JoivnnrpxD0nYr538SlZNzWv8Qx7stNjm95rtFpgrQ44Pb2wii7oZhNcHh9EpwtGQ4GoZ6CXNwFlDVbeDLWpo6RWYZiNk1yrfu3U7/8k36sW899v/OxHT495fcFzkscrO0QnTVqnT7GwO50wTG3YnG7wdGrCALzeIDjOAqa8ilx6i1ahQ6moiAYlpDQWhpWBYLXAzFkk3dBmD91/eO7DDz9I/s8GZqbGZKvDeY7nGNnh8cLEcmgaBn2KAW+hBzEcbA4PgoEQ7DYRvGCnJnxobFzCRjwLtWGg0WzC0Ktw9IRRbrSgai3ksxlwbUj5XHbG5w2+vbKycHcd7H/+mJ4ek9SqcWFifK/cpkMvxTOwOkSIHgb52CLsNPZQWAZnFZGi025trEIe3oUjE8NYOVeCUmyCZXR0+q1Wq9gXFmGRp2CihxlNDRwdIpNKyt6A78IzzzwxfuLEV5i42wCqxisWwSZfuXoF49u9eGhPCDfWCHAtwOHyYWjbKNzuHsTia9hMrMMheVEvl6DGL0OnUzPmNvRWZ1gManU6gaFBDofgcDhQq9VgF2wYHh5CrVKSTTBeoYov3V3B1NhO2WQ2neU5Di3DBJvkRyaTwfcO7UR/wNM9XZBOrxTLuLO2BDeNXXK5EXTbYMlcRTpfpoJm1MoNWEkX7E4bJg5M4p0338TNG1dxZ2URiXiMfi/gy6UF5ArKlFsSz8zPzyvdBsLR6B9FURrroJ2hPavgqYAL6VQSOyIeHD2wC5OyFVG/gEy+BJNWR1Qy40CfgWuXPkYLApIZ0gdFJSoKNB0HgqEeCK4gPpi7iEq9SnQVsHTrChbmr2Bp8RbSWxumza30+8w08V3Xcdbl9qFKAlMoKSgoReJ8LwJ0Qq1agFsU0COaEe4N4Mj+/ZgeC2Hfdg8qW/OY+/gaYUXCnfUCWrTrjjKabAxabRajI8NYIHB2GNRPApXL5dDUDNRpZevJzM4yTH9mdV2f5fgOVfguvZLpTdRVAtQtKyZGZuCylBHqj0Bga2hXU2jVV9FRw1w6geRWGuFIH67djqParMDrFlBp6JjaF0CiUMJQr9jVBCcBuEp4qdVIsOhVVMoolUsSB32WtYnSMaI3iQ3tzuUhcfGj1+bs0q5S03Hg0afgMNN4N28CvB1NmlA2X8BmqoxclYdTcqPNV8HyTdhEBv1hK8qpLzE2KGMg2ofJPbtRUFkUiIolRUGrpRNd65g8PIFAKHiMfeDYzNjy8jzamgYrIdUpEvWcEiqVCoxWg0YGlLKrKKXpy80E0aaOIjVWbwCC04eA6McPiB0bGxu48sUKgd9As1LG9sB9xH0Vh/dsx7ufLGJ1dQEKTUGnOirpg6OjIR7PGOshxQOBoF6t0O7z1GELxVIZ5WIaIwNTqObTSN34HAyBq5hcIHES0DIJqGom0gQelVIBZpMJQ+Ee0MSJ6ylEfHQIdwCVxE2EXINIx26iWiyQqhpdBdV0DXMffATh0mWZXVmal5Jb67QDeqDF2pWEdCZB/DRgI5VYu/we4mtxOB08NHK8ldvLyClV+L1eEikvzBZijULqqhFWiLIj2/sw2OclCzHDYu9BKrZKwpWEw07PJnxUyjVomk56UCMdqUisXaCh1io0BB0i0wOfP0hfJi1gqNtcEklzGW6ngFI+gfNXV/HpigKWHM+9uowBXxxyrws8b4KVtHYjHoeFZDjsD8A7OAzOvxvZW/+g6dnIJ7SOpXSx1iYr76yqTWJlvm/bbqUvHCWV89ODBKptQl84QibD4pObC9g+so8ewON2LIv5LRqP1YWwy4L+HpF2rSC2vo5cJglVraNJrxYdJJ+Oo6VsUkEGh48ch42jE9dJrKi4QWsQSeL7w30YHxtXWLVciR2ePDR24fJH3WChtZoEQg9UwYWnH5mCvHs/gbFJjV3HzOQwMUNFu1FEs14CRzqRLRZpYla4nA5IPcNgmgVkie+h4jptgSgYlHF4YhTnb6ySXrjh9bUQ9Pq7IPRJnhjzwNEHD+4e2jam6QZWCKktKuYk/f7xE4/gwPgY1MImSpuLqFSb0CnpmIwqMSRLKcEgqvLw+0VEdoyibh9C77ZJcLT7jeWr8NpbCO08AsY9gB2DUSgN8griEEcOayO9iYT8GJZD77Ora3cuSoL9+am9E1iiBjL5DJ6aOYJtfh5K4l+o0P+KboFKWs/RnvcceZhKU9hQcsSALLjGJoK7D8IeHkfAG0DquooW7bdAvtDZu7VRgoOoHSB1zDts2LuTGg30wE/pSlObc8zIyM7YnbWtFyLhPuuAvA11SwBOYmZZyYCRImiLvVhYJaWrlLBj+ocwu0NdtjjDQ3CRXPOuQHevZvL9RrlAehFHYWuRdKUBv4uHKxilqGhFYW0BZ989T4ewkXNyuL2yCs7hPtHNhA89+OjpaLj/RZ3Q+bdz76Bf3olf/vq3IMcihyuS3daQWJknbDioWIPyIIki0S5J1iwQi+yip5sDGAqoxcQK2tkvoNfzCLhE3H//fgw9/iuiXAEv/PwX+Oh2gr5jQTK5+YZSUk903XBHdHjBYM0n+/oHEd+IYTASAjEPhloEQwU5E/k7acDHc3O49vln5Acl2GwcWHpfsNm7GeArerW7oZUnBWyS4Xg8bvT4I3CFd8PmCeHw3p04MOCGxDRwaXHtSVXVvrLjldiKsue+UbemN6dG9+7D+p1lpMm/K9kEaiQy5cwGFuevIyjxOHRwEgMDMiyUHSw8S/RqIpVMUgxnUFV1AirgpDim1XLknkECNWUxT5BknidtaZAy6tg3Mvjab/7y3tmvZUKf33uJxOhZXdek0fFJNIopCIzW3WUniDhFQq4c7Y46m02hohTI2VTyemogW0AimUeR1A2MBSwF1VAoAq3DfVaCpV0j2jEwtRSoZSVmZqrP/eHMRbVT92v3gunpGRnt5oVIdFiW5QHafQWRvih6JB/OvfdXVGis8a0kzESjo0cPIbX4z65rGkTh9fVNckSB3FFCb9AHod3C/bsG0CtZ4bAYBM5VygXumDscPd57/KXYPVNxjFbhc3vnElvxmeWFm1JHWjWDpVMSpRpqd8ciaUQmm8Xg2DHy+DxRqQKny0HaUAf1QVmiTvgQEB6eQKR/gDBA4+fbKNWNmJKvPTn63O8W8E33gkQykdw1IL9NCWVWtNukdDqFpeUFDG0fIRY48dj3H6fg4YGJlNJJ2WGLQGul7G+zW5BO5ij1ZAmUpBlWJzkixXeOTM3KxBgrc3z/T75e/J4NdCcRjytev/dMW2cEXrBMuaig1U45r7cPbl8veoJ9ECUP3Y5YLN++TVige4DNSoCkSFcg2zWx2P/Aw90swfHsaz67+Ny2h08k71XrWy+nszPPyg6v8xTHm563251d2nUuombBiYA8jE///ibUGtGS10gfdHz2yWViB6ccnH70jGESTr/++unYNz3/Wxu42whdz3NpZZbuiNNk/nt9kV3y1EOPSXfmP0OjXlUa5XTM7XZcv3Tx/Fy5xbwVi8W+0/X83zizJdeTv85mAAAAAElFTkSuQmCC",
                )
            },
            provider = Provider(
                id = 1,
                icon = "data:image/png;base64,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",
                name = "name",
                domain = "domain"
            ),
            onConfirm = {},
            onDismiss = {},
        )
        val selectProvider = PromptRequest.IdentityCredential.SelectProvider(
            providers = List(20) {
                Provider(
                    id = it,
                    icon = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAARCAYAAADUryzEAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAH+SURBVHgBhVO/b9NQEL5z7Bi1lWJUCYRYHBaWDh6SKhspAYnRoVO3SqxIbiUGEEIRQ1kbxB9AM7VISJiNoSUeI6DCYmSpt1ZUak2U0GLXuZ7dOnUsp73l+e593/343hkhZYePKqp/EhhAoLOrRkEEmwgd/mrd3PpmJvGYdPbvl1cJYQkuMQI085KwfP1Lxwl9Mb74Uyu/J4BFuMIEoKp/HCixHyXYf1BqEF2QuS13gPQ2P1OyQt//ta0CYgOBFApg7ob13R5ij9rX1P67uzuDv/k45ASBMHfLOmsxabvVipqOo78pNZls/Nu8Df7vAgRBrphFHmfhCPeEggdT8zvg2dNrk8/2Rsi1N12dx1OyyIjghgnUOCBrB5/TIAJhNYkZuSNwBT4vsiNlVrrEFJHf3UE6q7DtTfO5N4Jg5W3u1Um0pPBza+eeE4nIH8aHozvQ7M+4J3JQtOumO65kbaU33BfWwOK9IPN5dzYkRy1JntAYR3640tOSy8YatKJVLm3Mt/moJrAWEL7+sfDRCp3qJ13peYIxsft0SezPxjo5X19OFaMEGgOk/7mflK12OM5QXPlAB/mwDjjA+tarSXP4M1XWdXVCLrS7Xi8ryUhCsV9a7jx5sRbpkL4trz9eJBQMnlBLExncypHU7CxsOHEQx5WJ5j4WoyQiiE6SlLRTu5e/Z+DrlXsAAAAASUVORK5CYII=",
                    name = "name$it",
                    domain = "domain$it",
                )
            },
            onConfirm = {},
            onDismiss = {},
        )
        val menuChoice = PromptRequest.MenuChoice(
            choices = Array(40) {
                if (it % 2 == 0) Choice(
                    id = "id$it",
                    enable = true,
                    label = "labelSingleChoice$it",
                    selected = false,
                    isASeparator = false,
                    children = null,
                )
                else Choice(
                    id = "id$it",
                    enable = true,
                    label = "labelSingleChoice$it",
                    selected = false,
                    isASeparator = true,
                    children = null,
                )
            },
            onConfirm = {},
            onDismiss = {},
        )
        val multipleChoice = PromptRequest.MultipleChoice(
            choices = Array(40) {
                if (it % 2 == 0) Choice(
                    id = "id$it",
                    enable = true,
                    label = "labelSingleChoice$it",
                    selected = false,
                    isASeparator = false,
                    children = null,
                )
                else Choice(
                    id = "id$it",
                    enable = false,
                    label = "labelSingleChoice$it",
                    selected = false,
                    isASeparator = false,
                    children = null,
                )
            },
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
                number = List(16) {
                    Random.nextInt(
                        0, 10
                    )
                }.joinToString(separator = "") { n -> "$n" },
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

                )[Random.nextInt(0, 9)] // random card type
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

        // todo: bugs out for some reason
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
                    timeCreated = Calendar.getInstance().run {
                        this.set(2020, 1, 1)
                        this.time.time
                    },
                    timeLastUsed = Calendar.getInstance().run {
                        this.set(2022, 1, 1)
                        this.time.time
                    },
                    timeLastModified = Calendar.getInstance().run {
                        this.set(2024, 1, 1)
                        this.time.time
                    },
                    timesUsed = 0,
                )
            },
            onConfirm = {},
            onDismiss = {},
        )

        // todo: bugs out for some reason
        val selectCreditCard = PromptRequest.SelectCreditCard(
            creditCards = List(20) {
                CreditCardEntry(guid = "$it",
                    name = "creditCardEntry$it",
                    number = List(16) {
                        Random.nextInt(
                            0, 10
                        )
                    }.joinToString(separator = "") { n -> "$n" },
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

                    )[Random.nextInt(0, 9)] // random card type
                )
            },
            onConfirm = {},
            onDismiss = {},
        )

        // todo: bugs out for some reason
        val selectLoginPrompt = PromptRequest.SelectLoginPrompt(
            logins = List(20) {
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
            },
            generatedPassword = listOf(
                "a",
                "b",
                "c",
                "d",
                "e",
                "f",
                "g",
                "h",
                "i",
                "j",
                "k",
                "l",
                "m",
                "n",
                "o",
                "p",
                "q",
                "r",
                "s",
                "t",
                "u",
                "v",
                "w",
                "x",
                "y",
                "z",
                "A",
                "B",
                "C",
                "D",
                "E",
                "F",
                "G",
                "H",
                "I",
                "J",
                "K",
                "L",
                "M",
                "N",
                "O",
                "P",
                "Q",
                "R",
                "S",
                "T",
                "U",
                "V",
                "W",
                "X",
                "Y",
                "Z",
                "0",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
            ).asSequence()
                .shuffled().take(10).joinToString(separator = "") { it },
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

        fun textPrompt(hasShownManyDialogs: Boolean): PromptRequest.TextPrompt {
            return PromptRequest.TextPrompt(
                title = "title",
                inputLabel = "inputLabel",
                inputValue = "inputValue",
                hasShownManyDialogs = hasShownManyDialogs,
                onConfirm = { _, _ -> },
                onDismiss = {},
            )
        }

        // test each type
        fun timeSelection(type: PromptRequest.TimeSelection.Type): TimeSelection {
            return TimeSelection(
                title = "title",
                initialDate = Calendar.getInstance().run {
                    this.set(2025, 1, 1)
                    this.time
                },
                minimumDate = Calendar.getInstance().run {
                    this.set(1950, 1, 1)
                    this.time
                },
                maximumDate = Date(),
                stepValue = null,
                type = type,
                onConfirm = {},
                onClear = {},
                onDismiss = {},
            )
        }
    }
}