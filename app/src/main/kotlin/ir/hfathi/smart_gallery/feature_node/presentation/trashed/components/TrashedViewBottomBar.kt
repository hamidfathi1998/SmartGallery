package ir.hfathi.smart_gallery.feature_node.presentation.trashed.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ir.hfathi.smart_gallery.R
import ir.hfathi.smart_gallery.core.Constants
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.use_case.MediaHandleUseCase
import ir.hfathi.smart_gallery.feature_node.presentation.mediaview.components.BottomBarColumn
import ir.hfathi.smart_gallery.feature_node.presentation.util.rememberActivityResult
import ir.hfathi.smart_gallery.ui.theme.BlackScrim
import kotlinx.coroutines.launch

@Composable
fun BoxScope.TrashedViewBottomBar(
    handler: MediaHandleUseCase,
    showUI: Boolean,
    paddingValues: PaddingValues,
    currentMedia: Media?,
    currentIndex: Int,
    onDeleteMedia: (Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val result = rememberActivityResult()
    AnimatedVisibility(
        visible = showUI,
        enter = Constants.Animation.enterAnimation(Constants.DEFAULT_TOP_BAR_ANIMATION_DURATION),
        exit = Constants.Animation.exitAnimation(Constants.DEFAULT_TOP_BAR_ANIMATION_DURATION),
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, BlackScrim)
                    )
                )
                .padding(
                    top = 24.dp,
                    bottom = paddingValues.calculateBottomPadding()
                )
                .align(Alignment.BottomCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Favourite Component
            BottomBarColumn(
                currentMedia = currentMedia,
                imageVector = Icons.Outlined.RestoreFromTrash,
                title = stringResource(id = R.string.trash_restore)
            ) {
                scope.launch {
                    onDeleteMedia.invoke(currentIndex)
                    handler.trashMedia(result = result, arrayListOf(it), trash = false)
                }
            }
            // Delete Component
            BottomBarColumn(
                currentMedia = currentMedia,
                imageVector = Icons.Outlined.DeleteOutline,
                title = stringResource(id = R.string.trash_delete)
            ) {
                scope.launch {
                    onDeleteMedia.invoke(currentIndex)
                    handler.deleteMedia(result = result, arrayListOf(it))
                }
            }
        }
    }
}