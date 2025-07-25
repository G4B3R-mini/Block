package com.shmibblez.inferno.browser

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityManager
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.shmibblez.inferno.R
import com.shmibblez.inferno.nimbus.FxNimbus
import mozilla.components.support.base.feature.ActivityResultHandler
import mozilla.components.support.base.feature.UserInteractionHandler

class OnActivityResultModel(
    val requestCode: Int, val data: Intent?, val resultCode: Int,
) {

}

class BrowserComponentWrapperFragment : Fragment(), UserInteractionHandler, ActivityResultHandler,
    AccessibilityManager.AccessibilityStateChangeListener {

    // helper for compose migration, might be a lil sloppy
    var onActivityResultHandler: ((OnActivityResultModel) -> Boolean)? = null
        private set
    val setOnActivityResultHandler: ((OnActivityResultModel) -> Boolean) -> Unit =
        { f -> onActivityResultHandler = f }

    private val baseComposeView: ComposeView
        get() = requireView().findViewById(R.id.baseComposeView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        bundleArgs = args.toBundle()
        if (savedInstanceState != null) {
            arguments?.putBoolean(FOCUS_ON_ADDRESS_BAR, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_browser_component_wrapper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // todo: implement functionality, reference [HomeFragment]
        val focusOnAddressBar =
            arguments?.getBoolean(FOCUS_ON_ADDRESS_BAR) ?: false || FxNimbus.features.oneClickSearch.value().enabled
        val scrollToCollection = arguments?.getBoolean(SCROLL_TO_COLLECTION) ?: false

        baseComposeView.setContent {
//            BrowserNavHost()
        }

//        requireContext().components.crashReporter.install(requireContext())
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        // todo: implement functionality, reference [HomeFragment]
        // Used to set homeViewModel.sessionToDelete when all tabs of a browsing mode are closed
        const val ALL_NORMAL_TABS = "all_normal"
        const val ALL_PRIVATE_TABS = "all_private"

        // Navigation arguments passed to HomeFragment
        const val FOCUS_ON_ADDRESS_BAR = "focusOnAddressBar"
        private const val SCROLL_TO_COLLECTION = "scrollToCollection"

        // Delay for scrolling to the collection header
        private const val ANIM_SCROLL_DELAY = 100L

        // Sponsored top sites titles and search engine names used for filtering
        const val AMAZON_SPONSORED_TITLE = "Amazon"
        const val AMAZON_SEARCH_ENGINE_NAME = "Amazon.com"
        const val EBAY_SPONSORED_TITLE = "eBay"

        // Elevation for undo toasts
        internal const val TOAST_ELEVATION = 80f
        private const val SESSION_ID = "session_id"

//        @JvmStatic
//        private fun Bundle.putSessionId(sessionId: String?) {
//            putString(SESSION_ID, sessionId)
//        }

        fun create(sessionId: String? = null) = BrowserComponentWrapperFragment().apply {
            arguments = Bundle().apply {
//                putSessionId(sessionId)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, data: Intent?, resultCode: Int): Boolean {
        Log.d("BrowserWrapperFrag", "BrowserComponentWrapperFragment.onActivityResult called")
        Log.d(
            "BrowserWrapperFrag",
            "BrowserComponentWrapperFragment.onActivityResult, onActivityResultHandler: $onActivityResultHandler"
        )

        if (onActivityResultHandler != null) {
            Log.d(
                "BrowserWrapperFrag",
                "BrowserComponentWrapperFragment.onActivityResult, handled correctly"
            )
            return onActivityResultHandler!!.invoke(
                OnActivityResultModel(requestCode, data, resultCode)
            )
        } else return false
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onAccessibilityStateChanged(enabled: Boolean) {
//        todo: anything to do here?
    }
}
