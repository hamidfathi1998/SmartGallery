package ir.hfathi.smart_gallery.feature_node.presentation.favorites

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
import ir.hfathi.smart_gallery.core.Constants.Target.TARGET_FAVORITES
import ir.hfathi.smart_gallery.core.MediaState
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.use_case.MediaHandleUseCase
import ir.hfathi.smart_gallery.feature_node.presentation.common.MediaScreen
import ir.hfathi.smart_gallery.feature_node.presentation.favorites.components.EmptyFavorites
import ir.hfathi.smart_gallery.feature_node.presentation.favorites.components.FavoriteNavActions
import kotlinx.coroutines.flow.StateFlow

@Composable
fun FavoriteScreen(
    paddingValues: PaddingValues,
    albumName: String = stringResource(id = R.string.favorites),
    handler: MediaHandleUseCase,
    mediaState: StateFlow<MediaState>,
    selectionState: MutableState<Boolean>,
    selectedMedia: SnapshotStateList<Media>,
    toggleFavorite: (ActivityResultLauncher<IntentSenderRequest>, List<Media>, Boolean) -> Unit,
    toggleSelection: (Int) -> Unit,
    navigate: (route: String) -> Unit,
    navigateUp: () -> Unit,
    toggleNavbar: (Boolean) -> Unit
) = MediaScreen(
    paddingValues = paddingValues,
    target = TARGET_FAVORITES,
    albumName = albumName,
    handler = handler,
    mediaState = mediaState,
    selectionState = selectionState,
    selectedMedia = selectedMedia,
    toggleSelection = toggleSelection,
    navActionsContent = { _: MutableState<Boolean>,
                          result: ActivityResultLauncher<IntentSenderRequest> ->
        FavoriteNavActions(toggleFavorite, mediaState, selectedMedia, selectionState, result)
    },
    emptyContent = { EmptyFavorites(Modifier.fillMaxSize()) },
    navigate = navigate,
    navigateUp = navigateUp,
    toggleNavbar = toggleNavbar
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        selectedMedia.clear()
        selectionState.value = false
    }
}