package ir.hfathi.smart_gallery.feature_node.presentation.trashed

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ir.hfathi.smart_gallery.R
import ir.hfathi.smart_gallery.core.Constants.Target.TARGET_TRASH
import ir.hfathi.smart_gallery.core.MediaState
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.use_case.MediaHandleUseCase
import ir.hfathi.smart_gallery.feature_node.presentation.common.MediaScreen
import ir.hfathi.smart_gallery.feature_node.presentation.trashed.components.AutoDeleteFooter
import ir.hfathi.smart_gallery.feature_node.presentation.trashed.components.EmptyTrash
import ir.hfathi.smart_gallery.feature_node.presentation.trashed.components.TrashedNavActions
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TrashedGridScreen(
    paddingValues: PaddingValues,
    albumName: String = stringResource(id = R.string.trash),
    handler: MediaHandleUseCase,
    mediaState: StateFlow<MediaState>,
    selectionState: MutableState<Boolean>,
    selectedMedia: SnapshotStateList<Media>,
    toggleSelection: (Int) -> Unit,
    navigate: (route: String) -> Unit,
    navigateUp: () -> Unit,
    toggleNavbar: (Boolean) -> Unit
) = MediaScreen(
    paddingValues = paddingValues,
    target = TARGET_TRASH,
    albumName = albumName,
    handler = handler,
    mediaState = mediaState,
    selectionState = selectionState,
    selectedMedia = selectedMedia,
    toggleSelection = toggleSelection,
    allowHeaders = false,
    enableStickyHeaders = false,
    navActionsContent = { _: MutableState<Boolean>,
                          _: ActivityResultLauncher<IntentSenderRequest> ->
        TrashedNavActions(handler, mediaState, selectedMedia, selectionState)
    },
    emptyContent = { EmptyTrash(Modifier.fillMaxSize()) },
    aboveGridContent = { AutoDeleteFooter() },
    navigate = navigate,
    navigateUp = navigateUp,
    toggleNavbar = toggleNavbar
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        selectedMedia.clear()
        selectionState.value = false
    }
}