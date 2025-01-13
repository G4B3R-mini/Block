/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.onboarding

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import mozilla.components.concept.engine.webextension.InstallationMethod
import mozilla.components.service.nimbus.evalJexlSafe
import mozilla.components.service.nimbus.messaging.use
import mozilla.components.support.base.ext.areNotificationsEnabledSafe
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.utils.BrowsersCache
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.accounts.FenixFxAEntryPoint
//import com.shmibblez.inferno.components.initializeGlean
import com.shmibblez.inferno.components.lazyStore
import com.shmibblez.inferno.components.startMetricsIfEnabled
import com.shmibblez.inferno.compose.LinkTextState
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.hideToolbar
import com.shmibblez.inferno.ext.isDefaultBrowserPromptSupported
import com.shmibblez.inferno.ext.isLargeWindow
import com.shmibblez.inferno.ext.nav
import com.shmibblez.inferno.ext.openSetDefaultBrowserOption
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.nimbus.FxNimbus
import com.shmibblez.inferno.onboarding.store.OnboardingAction.OnboardingAddOnsAction
import com.shmibblez.inferno.onboarding.store.OnboardingAddonStatus
import com.shmibblez.inferno.onboarding.store.OnboardingStore
import com.shmibblez.inferno.onboarding.view.Caption
import com.shmibblez.inferno.onboarding.view.ManagePrivacyPreferencesDialogFragment
import com.shmibblez.inferno.onboarding.view.OnboardingAddOn
import com.shmibblez.inferno.onboarding.view.OnboardingPageUiData
import com.shmibblez.inferno.onboarding.view.OnboardingScreen
import com.shmibblez.inferno.onboarding.view.ThemeOptionType
import com.shmibblez.inferno.onboarding.view.ToolbarOptionType
import com.shmibblez.inferno.onboarding.view.sequencePosition
import com.shmibblez.inferno.onboarding.view.telemetrySequenceId
import com.shmibblez.inferno.onboarding.view.toPageUiData
import com.shmibblez.inferno.settings.SupportUtils
import com.shmibblez.inferno.theme.FirefoxTheme
import com.shmibblez.inferno.utils.canShowAddSearchWidgetPrompt
import com.shmibblez.inferno.utils.showAddSearchWidgetPrompt

/**
 * Fragment displaying the onboarding flow.
 */
class OnboardingFragment : Fragment() {
    private val logger = Logger("OnboardingFragment")

    private val termsOfServiceEventHandler by lazy {
        DefaultOnboardingTermsOfServiceEventHandler(
            telemetryRecorder = telemetryRecorder,
            this::launchSandboxCustomTab,
            this::showPrivacyPreferencesDialog,
        )
    }

    private val pagesToDisplay by lazy {
        pagesToDisplay(
            isNotDefaultBrowser(requireContext()) &&
                activity?.isDefaultBrowserPromptSupported() == false,
            canShowNotificationPage(requireContext()),
            canShowAddSearchWidgetPrompt(),
        )
    }
    private val telemetryRecorder by lazy { OnboardingTelemetryRecorder() }
    private val onboardingStore by lazyStore { OnboardingStore() }
    private val pinAppWidgetReceiver = WidgetPinnedReceiver()

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = requireContext()
        if (pagesToDisplay.isEmpty()) {
            /* do not continue if there's no onboarding pages to display */
            onFinish(null)
        }

        if (!isLargeWindow()) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        val filter = IntentFilter(WidgetPinnedReceiver.ACTION)
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(pinAppWidgetReceiver, filter)

        if (isNotDefaultBrowser(context) &&
            activity?.isDefaultBrowserPromptSupported() == true
        ) {
            requireComponents.strictMode.resetAfter(StrictMode.allowThreadDiskReads()) {
                promptToSetAsDefaultBrowser()
            }
        }

        telemetryRecorder.onOnboardingStarted()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            FirefoxTheme {
                ScreenContent()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideToolbar()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isLargeWindow()) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(pinAppWidgetReceiver)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Composable
    @Suppress("LongMethod", "ThrowsCount")
    private fun ScreenContent() {
        OnboardingScreen(
            pagesToDisplay = pagesToDisplay,
            onMakeFirefoxDefaultClick = {
                promptToSetAsDefaultBrowser()
            },
            onSkipDefaultClick = {
                telemetryRecorder.onSkipSetToDefaultClick(
                    pagesToDisplay.telemetrySequenceId(),
                    pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.DEFAULT_BROWSER),
                )
            },
            onSignInButtonClick = {
                findNavController().nav(
                    id = R.id.onboardingFragment,
                    directions = OnboardingFragmentDirections.actionGlobalTurnOnSync(
                        entrypoint = FenixFxAEntryPoint.NewUserOnboarding,
                    ),
                )
                telemetryRecorder.onSyncSignInClick(
                    sequenceId = pagesToDisplay.telemetrySequenceId(),
                    sequencePosition = pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.SYNC_SIGN_IN),
                )
            },
            onSkipSignInClick = {
                telemetryRecorder.onSkipSignInClick(
                    pagesToDisplay.telemetrySequenceId(),
                    pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.SYNC_SIGN_IN),
                )
            },
            onNotificationPermissionButtonClick = {
                requireComponents.notificationsDelegate.requestNotificationPermission()
                telemetryRecorder.onNotificationPermissionClick(
                    sequenceId = pagesToDisplay.telemetrySequenceId(),
                    sequencePosition =
                    pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.NOTIFICATION_PERMISSION),
                )
            },
            onSkipNotificationClick = {
                telemetryRecorder.onSkipTurnOnNotificationsClick(
                    sequenceId = pagesToDisplay.telemetrySequenceId(),
                    sequencePosition =
                    pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.NOTIFICATION_PERMISSION),
                )
            },
            onAddFirefoxWidgetClick = {
                telemetryRecorder.onAddSearchWidgetClick(
                    pagesToDisplay.telemetrySequenceId(),
                    pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.ADD_SEARCH_WIDGET),
                )
                showAddSearchWidgetPrompt(requireActivity())
            },
            onSkipFirefoxWidgetClick = {
                telemetryRecorder.onSkipAddWidgetClick(
                    pagesToDisplay.telemetrySequenceId(),
                    pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.ADD_SEARCH_WIDGET),
                )
            },
            onAddOnsButtonClick = {
                telemetryRecorder.onAddOnsButtonClick(
                    pagesToDisplay.telemetrySequenceId(),
                    pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.ADD_ONS),
                )
            },
            onFinish = {
                onFinish(it)
                disableNavBarCFRForNewUser()
            },
            onImpression = {
                telemetryRecorder.onImpression(
                    sequenceId = pagesToDisplay.telemetrySequenceId(),
                    pageType = it.type,
                    sequencePosition = pagesToDisplay.sequencePosition(it.type),
                )
            },
            onboardingStore = onboardingStore,
            onInstallAddOnButtonClick = { installUrl -> installAddon(installUrl) },
            termsOfServiceEventHandler = termsOfServiceEventHandler,
            onCustomizeToolbarClick = {
                requireContext().settings().shouldUseBottomToolbar =
                    onboardingStore.state.toolbarOptionSelected == ToolbarOptionType.TOOLBAR_BOTTOM

                telemetryRecorder.onSelectToolbarPlacementClick(
                    pagesToDisplay.telemetrySequenceId(),
                    pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.TOOLBAR_PLACEMENT),
                    onboardingStore.state.toolbarOptionSelected.id,
                )
            },
            onSkipCustomizeToolbarClick = {
                telemetryRecorder.onSkipToolbarPlacementClick(
                    pagesToDisplay.telemetrySequenceId(),
                    pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.TOOLBAR_PLACEMENT),
                )
            },

            onCustomizeThemeClick = {
                val selectedTheme = when {
                    requireContext().settings().shouldFollowDeviceTheme -> ThemeOptionType.THEME_SYSTEM
                    requireContext().settings().shouldUseDarkTheme -> ThemeOptionType.THEME_DARK
                    else -> ThemeOptionType.THEME_LIGHT
                }

                telemetryRecorder.onSelectThemeClick(
                    selectedTheme.id,
                    pagesToDisplay.telemetrySequenceId(),
                    pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.THEME_SELECTION),
                )
            },

            onCustomizeThemeSkip = {
                telemetryRecorder.onSkipThemeClick(
                    pagesToDisplay.telemetrySequenceId(),
                    pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.THEME_SELECTION),
                )
            },
        )
    }

    private fun installAddon(addOn: OnboardingAddOn) {
        onboardingStore.dispatch(
            OnboardingAddOnsAction.UpdateStatus(
                addOnId = addOn.id,
                status = OnboardingAddonStatus.INSTALLING,
            ),
        )
        requireComponents.addonManager.installAddon(
            url = addOn.installUrl,
            installationMethod = InstallationMethod.ONBOARDING,
            onSuccess = { addon ->
                logger.info("Extension installed successfully")
                telemetryRecorder.onAddOnInstalled(addon.id)
                onboardingStore.dispatch(
                    OnboardingAddOnsAction.UpdateStatus(
                        addOnId = addOn.id,
                        status = OnboardingAddonStatus.INSTALLED,
                    ),
                )
            },
            onError = { e ->
                logger.error("Unable to install extension", e)
                onboardingStore.dispatch(
                    OnboardingAddOnsAction.UpdateStatus(
                        addOn.id,
                        status = OnboardingAddonStatus.NOT_INSTALLED,
                    ),
                )
            },
        )
    }

    private fun onFinish(onboardingPageUiData: OnboardingPageUiData?) {
        /* onboarding page UI data can be null if there was no pages to display */
        if (onboardingPageUiData != null) {
            val sequenceId = pagesToDisplay.telemetrySequenceId()
            val sequencePosition = pagesToDisplay.sequencePosition(onboardingPageUiData.type)

            telemetryRecorder.onOnboardingComplete(
                sequenceId = sequenceId,
                sequencePosition = sequencePosition,
            )
        }

        requireComponents.fenixOnboarding.finish()

        val settings = requireContext().settings()
        initializeGlean(
            requireContext().applicationContext,
            logger,
            settings.isTelemetryEnabled,
            requireComponents.core.client,
        )

        startMetricsIfEnabled(
            logger = logger,
            analytics = requireComponents.analytics,
            isTelemetryEnabled = settings.isTelemetryEnabled,
            isMarketingTelemetryEnabled = settings.isMarketingTelemetryEnabled,
        )

        findNavController().nav(
            id = R.id.onboardingFragment,
            directions = OnboardingFragmentDirections.actionHome(),
        )
    }

    private fun disableNavBarCFRForNewUser() {
        requireContext().settings().shouldShowNavigationBarCFR = false
    }

    // Marked as internal since it is used in unit tests
    internal fun isNotDefaultBrowser(context: Context) =
        !BrowsersCache.all(context.applicationContext).isDefaultBrowser

    private fun canShowNotificationPage(context: Context) =
        !NotificationManagerCompat.from(context.applicationContext)
            .areNotificationsEnabledSafe() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private fun pagesToDisplay(
        showDefaultBrowserPage: Boolean,
        showNotificationPage: Boolean,
        showAddWidgetPage: Boolean,
    ): List<OnboardingPageUiData> {
        val jexlConditions = FxNimbus.features.junoOnboarding.value().conditions
        val jexlHelper = requireContext().components.nimbus.createJexlHelper()

        val privacyCaption = Caption(
            text = getString(R.string.juno_onboarding_privacy_notice_text),
            linkTextState = LinkTextState(
                text = getString(R.string.juno_onboarding_privacy_notice_text),
                url = SupportUtils.getMozillaPageUrl(SupportUtils.MozillaPage.PRIVATE_NOTICE),
                onClick = {
                    startActivity(
                        SupportUtils.createSandboxCustomTabIntent(
                            context = requireContext(),
                            url = it,
                        ),
                    )
                    telemetryRecorder.onPrivacyPolicyClick(
                        pagesToDisplay.telemetrySequenceId(),
                        pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.DEFAULT_BROWSER),
                    )
                },
            ),
        )
        return jexlHelper.use {
            FxNimbus.features.junoOnboarding.value().cards.values.toPageUiData(
                privacyCaption,
                showDefaultBrowserPage,
                showNotificationPage,
                showAddWidgetPage,
                jexlConditions,
            ) { condition -> jexlHelper.evalJexlSafe(condition) }
        }
    }

    private fun promptToSetAsDefaultBrowser() {
        activity?.openSetDefaultBrowserOption(useCustomTab = true)
        requireContext().settings().coldStartsBetweenSetAsDefaultPrompts = 0
        requireContext().settings().lastSetAsDefaultPromptShownTimeInMillis = System.currentTimeMillis()
        telemetryRecorder.onSetToDefaultClick(
            sequenceId = pagesToDisplay.telemetrySequenceId(),
            sequencePosition = pagesToDisplay.sequencePosition(OnboardingPageUiData.Type.DEFAULT_BROWSER),
        )
    }

    private fun launchSandboxCustomTab(url: String) {
        val intent = SupportUtils.createSandboxCustomTabIntent(
            context = requireContext(),
            url = url,
        )
        requireContext().startActivity(intent)
    }

    private fun showPrivacyPreferencesDialog() {
        ManagePrivacyPreferencesDialogFragment(
            onCrashReportingLinkClick = {},
            onUsageDataLinkClick = {},
        ).show(parentFragmentManager, ManagePrivacyPreferencesDialogFragment.TAG)
    }
}
