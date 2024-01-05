package ir.hfathi.smart_gallery.feature_node.presentation.common

import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.hfathi.smart_gallery.core.Constants.Animation.enterAnimation
import ir.hfathi.smart_gallery.core.Constants.Animation.exitAnimation
import ir.hfathi.smart_gallery.core.Constants.Target.TARGET_TRASH
import ir.hfathi.smart_gallery.core.MediaState
import ir.hfathi.smart_gallery.core.presentation.components.Error
import ir.hfathi.smart_gallery.core.presentation.components.LoadingMedia
import ir.hfathi.smart_gallery.core.presentation.components.NavigationActions
import ir.hfathi.smart_gallery.core.presentation.components.NavigationButton
import ir.hfathi.smart_gallery.core.presentation.components.SelectionSheet
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.use_case.MediaHandleUseCase
import ir.hfathi.smart_gallery.feature_node.presentation.common.components.MediaGridView
import ir.hfathi.smart_gallery.feature_node.presentation.common.components.TwoLinedDateToolbarTitle
import ir.hfathi.smart_gallery.feature_node.presentation.search.MainSearchBar
import ir.hfathi.smart_gallery.feature_node.presentation.util.Screen
import kotlinx.coroutines.flow.StateFlow

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun MediaScreen(
    paddingValues: PaddingValues,
    albumId: Long = -1L,
    target: String? = null,
    albumName: String,
    handler: MediaHandleUseCase,
    mediaState: StateFlow<MediaState>,
    selectionState: MutableState<Boolean>,
    selectedMedia: SnapshotStateList<Media>,
    toggleSelection: (Int) -> Unit,
    allowHeaders: Boolean = true,
    showMonthlyHeader: Boolean = false,
    enableStickyHeaders: Boolean = true,
    allowNavBar: Boolean = false,
    navActionsContent: @Composable() (RowScope.(expandedDropDown: MutableState<Boolean>, result: ActivityResultLauncher<IntentSenderRequest>) -> Unit),
    emptyContent: @Composable () -> Unit,
    aboveGridContent: @Composable() (() -> Unit)? = null,
    navigate: (route: String) -> Unit,
    navigateUp: () -> Unit,
    toggleNavbar: (Boolean) -> Unit,
    refresh: () -> Unit = {},
    isScrolling: MutableState<Boolean> = remember { mutableStateOf(false) },
    searchBarActive: MutableState<Boolean> = mutableStateOf(false),
    onActivityResult: (result: ActivityResult) -> Unit,
) {
    val showSearchBar = remember { albumId == -1L && target == null }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    /** STATES BLOCK **/
    val state by mediaState.collectAsStateWithLifecycle()
    /** ************ **/

    /** Selection state handling **/
    LaunchedEffect(LocalConfiguration.current, selectionState.value) {
        if (allowNavBar) {
            toggleNavbar(!selectionState.value)
        }
    }
    /** ************  **/

    Box {
        Scaffold(
            modifier = Modifier
                .then(
                    if (!showSearchBar)
                        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                    else Modifier
                ),
            topBar = {
                if (!showSearchBar) {
                    LargeTopAppBar(
                        title = {
                            TwoLinedDateToolbarTitle(
                                albumName = albumName,
                                dateHeader = state.dateHeader
                            )
                        },
                        navigationIcon = {
                            NavigationButton(
                                albumId = albumId,
                                target = target,
                                navigateUp = navigateUp,
                                clearSelection = {
                                    selectionState.value = false
                                    selectedMedia.clear()
                                },
                                selectionState = selectionState,
                                alwaysGoBack = true,
                            )
                        },
                        actions = {
                            NavigationActions(
                                actions = navActionsContent,
                                onActivityResult = onActivityResult
                            )
                        },
                        scrollBehavior = scrollBehavior
                    )
                } else {
                    MainSearchBar(
                        bottomPadding = paddingValues.calculateBottomPadding(),
                        navigate = navigate,
                        toggleNavbar = toggleNavbar,
                        selectionState = selectionState,
                        isScrolling = isScrolling,
                        activeState = searchBarActive
                    )
                }
            }
        ) { it ->
            AnimatedVisibility(
                visible = state.isLoading,
                enter = enterAnimation,
                exit = exitAnimation
            ) {
                LoadingMedia(
                    paddingValues = PaddingValues(
                        top = it.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + 16.dp + 64.dp
                    )
                )
            }
            MediaGridView(
                mediaState = state,
                allowSelection = true,
                showSearchBar = showSearchBar,
                searchBarPaddingTop = paddingValues.calculateTopPadding(),
                enableStickyHeaders = enableStickyHeaders,
                paddingValues = PaddingValues(
                    top = it.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding() + 16.dp + 64.dp
                ),
                selectionState = selectionState,
                selectedMedia = selectedMedia,
                allowHeaders = allowHeaders,
                showMonthlyHeader = showMonthlyHeader,
                toggleSelection = toggleSelection,
                aboveGridContent = aboveGridContent,
                isScrolling = isScrolling
            ) {
                val albumRoute = "albumId=$albumId"
                val targetRoute = "target=$target"
                val param =
                    if (target != null) targetRoute else albumRoute
                navigate(Screen.MediaViewScreen.route + "?mediaId=${it.id}&$param")
            }
            /** Error State Handling Block **/
            if (state.error.isNotEmpty())
                Error(errorMessage = state.error)
            else if (!state.isLoading && state.media.isEmpty())
                emptyContent.invoke()
            /** ************ **/
        }
        if (target != TARGET_TRASH) {
            SelectionSheet(
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                selectedMedia = selectedMedia,
                target = target,
                selectionState = selectionState,
                refresh = refresh,
                handler = handler
            )
        }
    }
}