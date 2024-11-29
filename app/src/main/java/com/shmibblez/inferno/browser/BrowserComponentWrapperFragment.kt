package com.shmibblez.inferno.browser

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.shmibblez.inferno.R
import mozilla.components.support.base.feature.ActivityResultHandler
import mozilla.components.support.base.feature.UserInteractionHandler

class OnActivityResultModel(
    val requestCode: Int,
    val data: Intent?,
    val resultCode: Int
)

class BrowserComponentWrapperFragment : Fragment(), UserInteractionHandler, ActivityResultHandler {
    private val baseComposeView: ComposeView
        get() = requireView().findViewById(R.id.baseComposeView)

    private val sessionId: String?
        get() = arguments?.getString(SESSION_ID)

    // helper for compose migration, might be a lil sloppy
    private var onActivityResultHandler: ((OnActivityResultModel) -> Boolean)? = null
    private val setOnActivityResultHandler: ((OnActivityResultModel) -> Boolean) -> Unit =
        { f -> onActivityResultHandler = f }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_browser_component_wrapper, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        baseComposeView.setContent {
            BrowserComponent(
                sessionId = sessionId,
                setOnActivityResultHandler = setOnActivityResultHandler
            )
        }
        super.onViewCreated(view, savedInstanceState)
    }


    companion object {
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
        } else
            return false
    }

    override fun onBackPressed(): Boolean {
//        TODO("Not yet implemented")
        return false
    }
}