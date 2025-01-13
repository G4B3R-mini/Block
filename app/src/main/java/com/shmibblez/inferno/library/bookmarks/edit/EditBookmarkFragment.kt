/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.shmibblez.inferno.library.bookmarks.edit

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.appservices.places.uniffi.PlacesApiException
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.prompt.ShareData
import mozilla.components.concept.storage.BookmarkInfo
import mozilla.components.concept.storage.BookmarkNode
import mozilla.components.concept.storage.BookmarkNodeType
import mozilla.components.support.ktx.android.content.getColorFromAttr
import mozilla.components.support.ktx.android.view.hideKeyboard
import mozilla.components.support.ktx.android.view.showKeyboard
import mozilla.components.support.ktx.kotlin.toShortUrl
import mozilla.components.ui.widgets.withCenterAlignedButtons
//import mozilla.telemetry.glean.private.NoExtras
import com.shmibblez.inferno.BrowserDirection
//import com.shmibblez.inferno.GleanMetrics.BookmarksManagement
import com.shmibblez.inferno.HomeActivity
import com.shmibblez.inferno.NavHostActivity
import com.shmibblez.inferno.R
import com.shmibblez.inferno.components.StoreProvider
import com.shmibblez.inferno.components.accounts.FenixFxAEntryPoint
import com.shmibblez.inferno.components.appstate.AppAction.BookmarkAction
import com.shmibblez.inferno.components.metrics.MetricsUtils
import com.shmibblez.inferno.databinding.FragmentEditBookmarkBinding
import com.shmibblez.inferno.ext.bookmarkStorage
import com.shmibblez.inferno.ext.components
import com.shmibblez.inferno.ext.nav
import com.shmibblez.inferno.ext.placeCursorAtEnd
import com.shmibblez.inferno.ext.requireComponents
import com.shmibblez.inferno.ext.setToolbarColors
import com.shmibblez.inferno.ext.settings
import com.shmibblez.inferno.library.bookmarks.BookmarkFragmentDirections
import com.shmibblez.inferno.library.bookmarks.BookmarksSharedViewModel
import com.shmibblez.inferno.library.bookmarks.composeRootTitles
import com.shmibblez.inferno.library.bookmarks.friendlyRootTitle
import com.shmibblez.inferno.library.bookmarks.ui.BookmarksDestinations
import com.shmibblez.inferno.library.bookmarks.ui.BookmarksMiddleware
import com.shmibblez.inferno.library.bookmarks.ui.BookmarksScreen
import com.shmibblez.inferno.library.bookmarks.ui.BookmarksState
import com.shmibblez.inferno.library.bookmarks.ui.BookmarksStore
import com.shmibblez.inferno.library.bookmarks.ui.LifecycleHolder
import com.shmibblez.inferno.theme.FirefoxTheme

/**
 * Menu to edit the name, URL, and location of a bookmark item.
 */
class EditBookmarkFragment : Fragment(R.layout.fragment_edit_bookmark), MenuProvider {
    private var _binding: FragmentEditBookmarkBinding? = null
    private val binding get() = _binding!!

    private val args by navArgs<EditBookmarkFragmentArgs>()
    private val sharedViewModel: BookmarksSharedViewModel by activityViewModels()
    private var bookmarkNode: BookmarkNode? = null
    private var bookmarkParent: BookmarkNode? = null
    private var initialParentGuid: String? = null

    @Suppress("LongMethod")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return if (requireContext().settings().useNewBookmarks) {
            ComposeView(requireContext()).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                val buildStore = { navController: NavHostController ->
                    val isSignedIntoSync = requireComponents
                        .backgroundServices.accountManager.authenticatedAccount() != null

                    val store = StoreProvider.get(this@EditBookmarkFragment) {
                        val lifecycleHolder = LifecycleHolder(
                            context = requireContext(),
                            navController = this@EditBookmarkFragment.findNavController(),
                            composeNavController = navController,
                            homeActivity = (requireActivity() as HomeActivity),
                        )

                        BookmarksStore(
                            initialState = BookmarksState.default.copy(
                                isSignedIntoSync = isSignedIntoSync,
                            ),
                            middleware = listOf(
                                BookmarksMiddleware(
                                    bookmarksStorage = requireContext().bookmarkStorage,
                                    clipboardManager = requireContext().getSystemService(),
                                    addNewTabUseCase = requireComponents.useCases.tabsUseCases.addTab,
                                    navigateToSignIntoSync = {
                                        lifecycleHolder.navController
                                            .navigate(
                                                BookmarkFragmentDirections.actionGlobalTurnOnSync(
                                                    entrypoint = FenixFxAEntryPoint.BookmarkView,
                                                ),
                                            )
                                    },
                                    getNavController = { lifecycleHolder.composeNavController },
                                    exitBookmarks = { lifecycleHolder.navController.popBackStack() },
                                    wasPreviousAppDestinationHome = { false },
                                    navigateToSearch = { },
                                    shareBookmark = { url, title ->
                                        lifecycleHolder.navController.nav(
                                            R.id.bookmarkFragment,
                                            BookmarkFragmentDirections.actionGlobalShareFragment(
                                                data = arrayOf(
                                                    ShareData(url = url, title = title),
                                                ),
                                            ),
                                        )
                                    },
                                    showTabsTray = { },
                                    resolveFolderTitle = {
                                        friendlyRootTitle(
                                            context = lifecycleHolder.context,
                                            node = it,
                                            rootTitles = composeRootTitles(lifecycleHolder.context),
                                        ) ?: ""
                                    },
                                    showUrlCopiedSnackbar = { },
                                    getBrowsingMode = {
                                        lifecycleHolder.homeActivity.browsingModeManager.mode
                                    },
                                    openTab = { url, openInNewTab ->
                                        lifecycleHolder.homeActivity.openToBrowserAndLoad(
                                            searchTermOrURL = url,
                                            newTab = openInNewTab,
                                            from = BrowserDirection.FromBookmarks,
                                            flags = EngineSession.LoadUrlFlags.select(
                                                EngineSession.LoadUrlFlags.ALLOW_JAVASCRIPT_URL,
                                            ),
                                        )
                                    },
                                ),
                            ),
                            lifecycleHolder = lifecycleHolder,
                            bookmarkToLoad = args.guidToEdit,
                        )
                    }
                    store.lifecycleHolder?.apply {
                        this.navController = this@EditBookmarkFragment.findNavController()
                        this.composeNavController = navController
                        this.homeActivity = (requireActivity() as HomeActivity)
                        this.context = requireContext()
                    }

                    store
                }
                setContent {
                    FirefoxTheme {
                        BookmarksScreen(
                            buildStore = buildStore,
                            startDestination = BookmarksDestinations.EDIT_BOOKMARK,
                        )
                    }
                }
            }
        } else {
            super.onCreateView(inflater, container, savedInstanceState)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (requireContext().settings().useNewBookmarks) {
            return
        }

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        _binding = FragmentEditBookmarkBinding.bind(view)

        initToolbar()

        viewLifecycleOwner.lifecycleScope.launch(Main) {
            val context = requireContext()
            val bookmarkNodeBeforeReload = bookmarkNode
            val bookmarksStorage = context.components.core.bookmarksStorage

            bookmarkNode = withContext(IO) {
                bookmarksStorage.getBookmark(args.guidToEdit)
            }

            if (initialParentGuid == null) {
                initialParentGuid = bookmarkNode?.parentGuid
            }

            bookmarkParent = withContext(IO) {
                // Use user-selected parent folder if it's set, or node's current parent otherwise.
                if (sharedViewModel.selectedFolder != null) {
                    sharedViewModel.selectedFolder
                } else {
                    bookmarkNode?.parentGuid?.let { bookmarksStorage.getBookmark(it) }
                }
            }

            when (bookmarkNode?.type) {
                BookmarkNodeType.FOLDER -> {
                    activity?.title = getString(R.string.edit_bookmark_folder_fragment_title)
                    binding.inputLayoutBookmarkUrl.visibility = View.GONE
                    binding.bookmarkUrlEdit.visibility = View.GONE
                    binding.bookmarkUrlLabel.visibility = View.GONE
                }
                BookmarkNodeType.ITEM -> {
                    activity?.title = getString(R.string.edit_bookmark_fragment_title)
                }
                else -> throw IllegalArgumentException()
            }

            val currentBookmarkNode = bookmarkNode
            if (currentBookmarkNode != null && currentBookmarkNode != bookmarkNodeBeforeReload) {
                binding.bookmarkNameEdit.setText(currentBookmarkNode.title)
                binding.bookmarkUrlEdit.setText(currentBookmarkNode.url)
            }

            bookmarkParent?.let { node ->
                binding.bookmarkParentFolderSelector.text = friendlyRootTitle(context, node)
            }

            binding.bookmarkParentFolderSelector.setOnClickListener {
                sharedViewModel.selectedFolder = null
                nav(
                    R.id.bookmarkEditFragment,
                    EditBookmarkFragmentDirections
                        .actionBookmarkEditFragmentToBookmarkSelectFolderFragment(
                            allowCreatingNewFolder = false,
                            // Don't allow moving folders into themselves.
                            hideFolderGuid = when (bookmarkNode!!.type) {
                                BookmarkNodeType.FOLDER -> bookmarkNode!!.guid
                                else -> null
                            },
                        ),
                )
            }

            binding.bookmarkNameEdit.apply {
                requestFocus()
                placeCursorAtEnd()
                showKeyboard()
            }

            binding.bookmarkUrlEdit.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int,
                    ) {
                        // NOOP
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int,
                    ) {
                        binding.bookmarkUrlEdit.onTextChanged(s)

                        binding.inputLayoutBookmarkUrl.error = null
                        binding.inputLayoutBookmarkUrl.errorIconDrawable = null
                    }

                    override fun afterTextChanged(s: Editable?) {
                        // NOOP
                    }
                },
            )
        }
    }

    private fun initToolbar() {
        val activity = activity as AppCompatActivity
        val actionBar = (activity as NavHostActivity).getSupportActionBarAndInflateIfNecessary()
        val toolbar = activity.findViewById<Toolbar>(R.id.navigationToolbar)
        toolbar?.setToolbarColors(
            foreground = activity.getColorFromAttr(R.attr.textPrimary),
            background = activity.getColorFromAttr(R.attr.layer1),
        )
        actionBar.show()
    }

    override fun onPause() {
        super.onPause()
        if (requireContext().settings().useNewBookmarks) {
            return
        }
        binding.bookmarkNameEdit.hideKeyboard()
        binding.bookmarkUrlEdit.hideKeyboard()
        binding.progressBarBookmark.visibility = View.GONE
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        if (requireContext().settings().useNewBookmarks) {
            return
        }
        inflater.inflate(R.menu.bookmarks_edit, menu)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_bookmark_button -> {
                displayDeleteBookmarkDialog()
                true
            }
            R.id.save_bookmark_button -> {
                updateBookmarkFromTextChanges()
                true
            }

            // other options are not handled by this menu provider
            else -> false
        }
    }

    private fun displayDeleteBookmarkDialog() {
        activity?.let { activity ->
            AlertDialog.Builder(activity).apply {
                setMessage(R.string.bookmark_deletion_confirmation)
                setNegativeButton(R.string.bookmark_delete_negative) { dialog: DialogInterface, _ ->
                    dialog.cancel()
                }
                setPositiveButton(R.string.tab_collection_dialog_positive) { dialog: DialogInterface, _ ->
                    // Use fragment's lifecycle; the view may be gone by the time dialog is interacted with.
                    lifecycleScope.launch(IO) {
                        requireComponents.core.bookmarksStorage.deleteNode(args.guidToEdit)
//                        BookmarksManagement.removed.record(NoExtras())
                        MetricsUtils.recordBookmarkMetrics(MetricsUtils.BookmarkAction.DELETE, METRIC_SOURCE)

                        launch(Main) {
                            Navigation.findNavController(requireActivity(), R.id.container)
                                .popBackStack()

                            bookmarkNode?.let { bookmark ->
                                requireComponents.appStore.dispatch(
                                    BookmarkAction.BookmarkDeleted(
                                        bookmark.url?.toShortUrl(context.components.publicSuffixList)
                                            ?: bookmark.title,
                                    ),
                                )
                            }
                        }
                    }
                    dialog.dismiss()
                }
                create().withCenterAlignedButtons()
            }.show()
        }
    }

    private fun updateBookmarkFromTextChanges() {
        binding.progressBarBookmark.visibility = View.VISIBLE
        val nameText = binding.bookmarkNameEdit.text.toString()
        val urlText = binding.bookmarkUrlEdit.text.toString()
        updateBookmarkNode(nameText, urlText)
    }

    private fun updateBookmarkNode(title: String?, url: String?) {
        viewLifecycleOwner.lifecycleScope.launch(IO) {
            try {
                requireComponents.let { components ->
                    if (title != bookmarkNode?.title || url != bookmarkNode?.url) {
//                        BookmarksManagement.edited.record(NoExtras())
                    }
                    val parentGuid =
                        sharedViewModel.selectedFolder?.guid ?: bookmarkNode!!.parentGuid
                    val parentChanged = initialParentGuid != parentGuid
                    // Only track the 'moved' event if new parent was selected.
                    if (parentChanged) {
//                        BookmarksManagement.moved.record(NoExtras())
                    }
                    components.core.bookmarksStorage.updateNode(
                        args.guidToEdit,
                        BookmarkInfo(
                            parentGuid,
                            // Setting position to 'null' is treated as a 'move to the end' by the storage API.
                            if (parentChanged) null else bookmarkNode?.position,
                            title,
                            if (bookmarkNode?.type == BookmarkNodeType.ITEM) url else null,
                        ),
                    )
                }
                withContext(Main) {
                    binding.inputLayoutBookmarkUrl.error = null
                    binding.inputLayoutBookmarkUrl.errorIconDrawable = null

                    findNavController().popBackStack()
                }
            } catch (e: PlacesApiException.UrlParseFailed) {
                withContext(Main) {
                    binding.inputLayoutBookmarkUrl.error =
                        getString(R.string.bookmark_invalid_url_error)
                    binding.inputLayoutBookmarkUrl.setErrorIconDrawable(R.drawable.mozac_ic_warning_with_bottom_padding)
                    binding.inputLayoutBookmarkUrl.setErrorIconTintList(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.fx_mobile_text_color_critical,
                            ),
                        ),
                    )
                }
            }
        }
        binding.progressBarBookmark.visibility = View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.title = getString(R.string.app_name)

        _binding = null
    }

    companion object {
        private const val METRIC_SOURCE = "bookmark_edit_page"
    }
}
