package ir.hfathi.smart_gallery.core.presentation.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.hfathi.smart_gallery.R
import ir.hfathi.smart_gallery.core.Constants.Target.TARGET_FAVORITES
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.use_case.MediaHandleUseCase
import ir.hfathi.smart_gallery.feature_node.presentation.exif.CopyMediaSheet
import ir.hfathi.smart_gallery.feature_node.presentation.exif.MoveMediaSheet
import ir.hfathi.smart_gallery.feature_node.presentation.trashed.components.TrashDialog
import ir.hfathi.smart_gallery.feature_node.presentation.trashed.components.TrashDialogAction
import ir.hfathi.smart_gallery.feature_node.presentation.util.rememberAppBottomSheetState
import ir.hfathi.smart_gallery.feature_node.presentation.util.shareMedia
import ir.hfathi.smart_gallery.ui.theme.Shapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SelectionSheet(
    modifier: Modifier = Modifier,
    target: String?,
    selectedMedia: SnapshotStateList<Media>,
    selectionState: MutableState<Boolean>,
    handler: MediaHandleUseCase,
    refresh: () -> Unit
) {
    fun clearSelection() {
        selectedMedia.clear()
        selectionState.value = false
        refresh()
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val trashSheetState = rememberAppBottomSheetState()
    val moveSheetState = rememberAppBottomSheetState()
    val copySheetState = rememberAppBottomSheetState()
    val result = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = {
            if (it.resultCode == Activity.RESULT_OK) {
                clearSelection()
                if (trashSheetState.isVisible) {
                    scope.launch {
                        trashSheetState.hide()
                    }
                }
            }
        }
    )
    val windowSizeClass = calculateWindowSizeClass(LocalContext.current as Activity)
    val tabletMode = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact
    val sizeModifier = if (!tabletMode) Modifier.fillMaxWidth()
    else Modifier.wrapContentWidth()
    AnimatedVisibility(
        modifier = modifier,
        visible = selectionState.value,
        enter = slideInVertically { it * 2 },
        exit = slideOutVertically { it * 2 }
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .then(sizeModifier)
                .wrapContentHeight()
                .clip(Shapes.extraLarge)
                .shadow(
                    elevation = 4.dp,
                    shape = Shapes.extraLarge
                )
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .then(sizeModifier)
                    .height(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = { clearSelection() },
                    modifier = Modifier.size(24.dp),
                ) {
                    Image(
                        imageVector = Icons.Outlined.Close,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        contentDescription = stringResource(R.string.selection_dialog_close_cd)
                    )
                }
                Text(
                    text = stringResource(R.string.selected_s, selectedMedia.size),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
            Row(
                modifier = Modifier
                    .then(sizeModifier)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = Shapes.large
                    )
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Share Component
                SelectionBarColumn(
                    selectedMedia = selectedMedia,
                    imageVector = Icons.Outlined.Share,
                    title = stringResource(R.string.share)
                ) {
                    context.shareMedia(it)
                }
                val favoriteTitle =
                    if (target == TARGET_FAVORITES) stringResource(id = R.string.remove_selected)
                    else stringResource(id = R.string.favorite)
                // Favorite Component
                SelectionBarColumn(
                    selectedMedia = selectedMedia,
                    imageVector = Icons.Outlined.FavoriteBorder,
                    title = favoriteTitle
                ) {
                    scope.launch {
                        handler.toggleFavorite(result = result, it)
                    }
                }
                // Copy Component
                SelectionBarColumn(
                    selectedMedia = selectedMedia,
                    imageVector = Icons.Outlined.CopyAll,
                    title = stringResource(R.string.copy)
                ) {
                    scope.launch {
                        copySheetState.show()
                    }
                }
                // Move Component
                SelectionBarColumn(
                    selectedMedia = selectedMedia,
                    imageVector = Icons.AutoMirrored.Outlined.DriveFileMove,
                    title = stringResource(R.string.move)
                ) {
                    scope.launch {
                        moveSheetState.show()
                    }
                }
                // Trash Component
                SelectionBarColumn(
                    selectedMedia = selectedMedia,
                    imageVector = Icons.Outlined.DeleteOutline,
                    title = stringResource(id = R.string.trash)
                ) {
                    scope.launch {
                        trashSheetState.show()
                    }
                }
            }
        }
    }

    MoveMediaSheet(
        sheetState = moveSheetState,
        mediaList = selectedMedia,
        onFinish = ::clearSelection
    )

    CopyMediaSheet(
        sheetState = copySheetState,
        mediaList = selectedMedia,
        onFinish = ::clearSelection
    )

    TrashDialog(
        appBottomSheetState = trashSheetState,
        data = selectedMedia,
        action = TrashDialogAction.TRASH
    ) {
        handler.trashMedia(result, it, true)
    }
}

@Composable
private fun RowScope.SelectionBarColumn(
    selectedMedia: SnapshotStateList<Media>,
    imageVector: ImageVector,
    title: String,
    onItemClick: (List<Media>) -> Unit
) {
    val tintColor = MaterialTheme.colorScheme.onSurface
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .defaultMinSize(minHeight = 80.dp)
            .weight(1f)
            .clickable {
                onItemClick.invoke(selectedMedia)
            }
            .padding(top = 12.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            imageVector = imageVector,
            colorFilter = ColorFilter.tint(tintColor),
            contentDescription = title,
            modifier = Modifier
                .height(32.dp)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = title,
            modifier = Modifier,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
            color = tintColor,
            textAlign = TextAlign.Center
        )
    }
}