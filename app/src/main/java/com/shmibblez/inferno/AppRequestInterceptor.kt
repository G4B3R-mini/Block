/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.shmibblez.inferno.ext.components
import mozilla.components.browser.errorpages.ErrorPages
import mozilla.components.browser.errorpages.ErrorType
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.request.RequestInterceptor
import com.shmibblez.inferno.ext.isOnline
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.proto.InfernoSettings
import java.lang.ref.WeakReference


class AppRequestInterceptor(
    private val context: Context,
) : RequestInterceptor {

    interface AppRequestCallback {
        fun onRequestReceived(openInAppAvailable: Boolean)
    }

    private var navController: WeakReference<NavController>? = null
    private val requestListeners = mutableListOf<AppRequestCallback>()

    fun setNavigationController(navController: NavController) {
        this.navController = WeakReference(navController)
    }

    override fun interceptsAppInitiatedRequests() = true

    private fun getHostName(url: String): String? {
        val uri = url.toUri()
        val domain: String = uri.host ?: return null
        return domain.split('.').let {
            Log.d("AppRequestsInterceptor", "getHostName, domain parts: $it")
            when (it.size) {
                0 -> null
                1 -> it[0]
                2 -> it[0]
                else -> it[1]
            }
        }
    }

    override fun onLoadRequest(
        engineSession: EngineSession,
        uri: String,
        lastUri: String?,
        hasUserGesture: Boolean,
        isSameDomain: Boolean,
        isRedirect: Boolean,
        isDirectNavigation: Boolean,
        isSubframeRequest: Boolean,
    ): RequestInterceptor.InterceptionResponse? {

//        Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER

        // check app links setting, todo: app requests, change behaviour depending on user setting
        val appLinksSetting = context.settings().latestSettings?.appLinksSetting
        when (appLinksSetting) {
            InfernoSettings.AppLinks.APP_LINKS_BLOCKED -> {
                return null
            }

            InfernoSettings.AppLinks.APP_LINKS_ALLOWED -> {
                // todo: if allowed just open dont ask
            }

            InfernoSettings.AppLinks.APP_LINKS_ASK_TO_OPEN -> {}
            null -> {}
        }

        val openInAppAvailable = when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            true -> openInAppAvailable(uri)
            false -> openInAppAvailableBelowApi30(uri)
        }

        requestListeners.forEach { it.onRequestReceived(openInAppAvailable) }

        return when (openInAppAvailable) {
            true -> {
                val services = context.components.services
                services.appLinksInterceptor.onLoadRequest(
                    engineSession,
                    uri,
                    lastUri,
                    hasUserGesture,
                    isSameDomain,
                    isRedirect,
                    isDirectNavigation,
                    isSubframeRequest,
                )
            }

            false -> null
        }
    }

    private fun openInAppAvailable(uri: String): Boolean {
        return false
        // todo: use Intent().addFlags(Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER) to check
        //  reference here: https://medium.com/androiddevelopers/package-visibility-in-android-11-cc857f221cd9
        //  in order to support this, add a setting for prompting open in app dialog (if disabled, do nothing, if enabled, show app prompt)
        //  either way, include open in app toolbar and toolbar menu option, when clicked send url to all apps, show chooser
    }

    private fun openInAppAvailableBelowApi30(uri: String): Boolean {
        // intent for testing matches
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setData(uri.toUri())

        val results =
            context.packageManager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER)
        var resultSize = results.size
        val host = getHostName(uri)

        Log.d(
            "AppRequestsInterceptor",
            "-----------------------------\n\nNew Request, size: $resultSize\nfull url: $uri\nurl host: $host\n\n-----------------------------"
        )

        results@ for (r in results) {
            Log.d(
                "AppRequestsInterceptor",
                "result schemes: ${
                    r?.filter?.schemesIterator()?.asSequence()?.toList()
                }\nactivity package strs: ${r?.activityInfo?.packageName?.split(".")}",
            )
            val packageName = r?.activityInfo?.packageName
            if (packageName == null) {
                resultSize -= 1
                continue@results
            }

            // if package name and host matches, break, there are valid results
            // if doesn't, keep checking
            when (packageName.split('.').contains(host)) {
                true -> break@results
                false -> resultSize -= 1
            }

//            var containsNotHttpOrHttps = false
//            r.filter?.schemesIterator()
//            r.filter?.schemesIterator()?.forEach { scheme ->
//                // check if scheme is anything other than http or https
//                if (scheme?.matches(IGNORED_SCHEMES) == false) {
//                    containsNotHttpOrHttps = true
//                }
//                // will open any intent, ignore
//            }
//            if (!containsNotHttpOrHttps) {
//                resultSize -= 1
//            }
//            r.activityInfo.labelRes
        }
        Log.d(
            "AppRequestsInterceptor",
            "-----------------------------\n\nRequest End, size after filter: $resultSize\n\n-----------------------------"
        )

        val openInAppAvailable: Boolean = resultSize > 0
        return openInAppAvailable
    }

    override fun onErrorRequest(
        session: EngineSession,
        errorType: ErrorType,
        uri: String?,
    ): RequestInterceptor.ErrorResponse {
        val improvedErrorType = improveErrorType(errorType)
        val riskLevel = getRiskLevel(improvedErrorType)

        val errorPageUri = ErrorPages.createUrlEncodedErrorPage(
            context = context,
            errorType = improvedErrorType,
            uri = uri,
            htmlResource = riskLevel.htmlRes,
            titleOverride = { type -> getErrorPageTitle(context, type) },
            descriptionOverride = { type -> getErrorPageDescription(context, type) },
        )

        return RequestInterceptor.ErrorResponse(errorPageUri)
    }

    /**
     * Where possible, this will make the error type more accurate by including information not
     * available to AC.
     */
    private fun improveErrorType(errorType: ErrorType): ErrorType {
        // This is not an ideal solution. For context, see:
        // https://github.com/mozilla-mobile/android-components/pull/5068#issuecomment-558415367

        val isConnected: Boolean = context.getSystemService<ConnectivityManager>()!!.isOnline()

        return when {
            errorType == ErrorType.ERROR_UNKNOWN_HOST && !isConnected -> ErrorType.ERROR_NO_INTERNET
            errorType == ErrorType.ERROR_HTTPS_ONLY -> ErrorType.ERROR_HTTPS_ONLY
            else -> errorType
        }
    }

    private fun getRiskLevel(errorType: ErrorType): RiskLevel = when (errorType) {
        ErrorType.UNKNOWN,
        ErrorType.ERROR_NET_INTERRUPT,
        ErrorType.ERROR_NET_TIMEOUT,
        ErrorType.ERROR_CONNECTION_REFUSED,
        ErrorType.ERROR_UNKNOWN_SOCKET_TYPE,
        ErrorType.ERROR_REDIRECT_LOOP,
        ErrorType.ERROR_OFFLINE,
        ErrorType.ERROR_NET_RESET,
        ErrorType.ERROR_UNSAFE_CONTENT_TYPE,
        ErrorType.ERROR_CORRUPTED_CONTENT,
        ErrorType.ERROR_CONTENT_CRASHED,
        ErrorType.ERROR_INVALID_CONTENT_ENCODING,
        ErrorType.ERROR_UNKNOWN_HOST,
        ErrorType.ERROR_MALFORMED_URI,
        ErrorType.ERROR_FILE_NOT_FOUND,
        ErrorType.ERROR_FILE_ACCESS_DENIED,
        ErrorType.ERROR_PROXY_CONNECTION_REFUSED,
        ErrorType.ERROR_UNKNOWN_PROXY_HOST,
        ErrorType.ERROR_NO_INTERNET,
        ErrorType.ERROR_HTTPS_ONLY,
        ErrorType.ERROR_BAD_HSTS_CERT,
        ErrorType.ERROR_UNKNOWN_PROTOCOL,
            -> RiskLevel.Low

        ErrorType.ERROR_SECURITY_BAD_CERT,
        ErrorType.ERROR_SECURITY_SSL,
        ErrorType.ERROR_PORT_BLOCKED,
            -> RiskLevel.Medium

        ErrorType.ERROR_SAFEBROWSING_HARMFUL_URI,
        ErrorType.ERROR_SAFEBROWSING_MALWARE_URI,
        ErrorType.ERROR_SAFEBROWSING_PHISHING_URI,
        ErrorType.ERROR_SAFEBROWSING_UNWANTED_URI,
            -> RiskLevel.High
    }

    private fun getErrorPageTitle(context: Context, type: ErrorType): String? {
        return when (type) {
            ErrorType.ERROR_HTTPS_ONLY -> context.getString(R.string.errorpage_httpsonly_title)
            // Returning `null` will let the component use its default title for this error type
            else -> null
        }
    }

    private fun getErrorPageDescription(context: Context, type: ErrorType): String? {
        return when (type) {
            ErrorType.ERROR_HTTPS_ONLY -> context.getString(R.string.errorpage_httpsonly_message_title) + "<br><br>" + context.getString(
                R.string.errorpage_httpsonly_message_summary
            )
            // Returning `null` will let the component use its default description for this error type
            else -> null
        }
    }

    internal enum class RiskLevel(val htmlRes: String) {
        Low(LOW_AND_MEDIUM_RISK_ERROR_PAGES), Medium(LOW_AND_MEDIUM_RISK_ERROR_PAGES), High(
            HIGH_RISK_ERROR_PAGES
        ),
    }

    companion object {
        internal const val LOW_AND_MEDIUM_RISK_ERROR_PAGES = "low_and_medium_risk_error_pages.html"
        internal const val HIGH_RISK_ERROR_PAGES = "high_risk_error_pages.html"
    }
}
