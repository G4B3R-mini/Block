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
import androidx.navigation.fragment.findNavController
import com.shmibblez.inferno.R
import com.shmibblez.inferno.browser.prompts.webPrompts.AndroidPhotoPicker
import com.shmibblez.inferno.browser.prompts.webPrompts.FilePicker
import com.shmibblez.inferno.nimbus.FxNimbus
import mozilla.components.support.base.feature.ActivityResultHandler
import mozilla.components.support.base.feature.UserInteractionHandler

class OnActivityResultModel(
    val requestCode: Int, val data: Intent?, val resultCode: Int
)

class BrowserComponentWrapperFragment : Fragment(), UserInteractionHandler, ActivityResultHandler,
    AccessibilityManager.AccessibilityStateChangeListener {

//    private val args by navArgs<BrowserComponentWrapperFragmentArgs>()

//    @VisibleForTesting
//    internal lateinit var bundleArgs: Bundle

    private val baseComposeView: ComposeView
        get() = requireView().findViewById(R.id.baseComposeView)

    private val sessionId: String?
        get() = arguments?.getString(SESSION_ID)

    // helper for compose migration, might be a lil sloppy
    private var onActivityResultHandler: ((OnActivityResultModel) -> Boolean)? = null
    private val setOnActivityResultHandler: ((OnActivityResultModel) -> Boolean) -> Unit =
        { f -> onActivityResultHandler = f }

    private var filePicker: FilePicker? = null
    private val setFilePicker: (FilePicker) -> Unit = { f ->
        filePicker = f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        bundleArgs = args.toBundle()
        if (savedInstanceState != null) {
            arguments?.putBoolean(FOCUS_ON_ADDRESS_BAR, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_browser_component_wrapper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Registers a photo picker activity launcher in single-select mode.
        val singleMediaPicker = AndroidPhotoPicker.singleMediaPicker(
            { this },
            { filePicker },
        )

        // Registers a photo picker activity launcher in multi-select mode.
        val multipleMediaPicker = AndroidPhotoPicker.multipleMediaPicker(
            { this },
            { filePicker },
        )

        val androidPhotoPicker = AndroidPhotoPicker(
            requireContext(),
            singleMediaPicker,
            multipleMediaPicker,
        )

        // todo: implement functionality, reference [HomeFragment]
        val focusOnAddressBar =
            arguments?.getBoolean(FOCUS_ON_ADDRESS_BAR) ?: false || FxNimbus.features.oneClickSearch.value().enabled
        val scrollToCollection = arguments?.getBoolean(SCROLL_TO_COLLECTION) ?: false

        baseComposeView.setContent {
            BrowserComponent(
                navController = this.findNavController(),
                sessionId = sessionId,
                setOnActivityResultHandler = setOnActivityResultHandler,
                androidPhotoPicker = androidPhotoPicker,
                setFilePicker = setFilePicker,
            )
        }
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

        @JvmStatic
        private fun Bundle.putSessionId(sessionId: String?) {
            putString(SESSION_ID, sessionId)
        }

        fun create(sessionId: String? = null) = BrowserComponentWrapperFragment().apply {
            arguments = Bundle().apply {
                putSessionId(sessionId)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, data: Intent?, resultCode: Int): Boolean {
        Log.d("BrowserComponentWFrag", "BrowserComponentWrapperFragment.onActivityResult called")
        Log.d(
            "BrowserComponentWFrag",
            "BrowserComponentWrapperFragment.onActivityResult, onActivityResultHandler: $onActivityResultHandler"
        )

        if (onActivityResultHandler != null) {
            Log.d(
                "BrowserComponentWFrag",
                "BrowserComponentWrapperFragment.onActivityResult, handled correctly"
            )
            return onActivityResultHandler!!.invoke(
                OnActivityResultModel(requestCode, data, resultCode)
            )
        } else return false
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
////        bundleArgs.clear()
//    }

    override fun onBackPressed(): Boolean {
//        TODO("Not yet implemented")
        return false
    }

    override fun onAccessibilityStateChanged(enabled: Boolean) {
        // todo: make toolbar unscrollable if true
    }

}