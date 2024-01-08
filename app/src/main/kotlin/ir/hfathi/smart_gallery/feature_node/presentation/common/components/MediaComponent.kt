package ir.hfathi.smart_gallery.feature_node.presentation.common.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.RequestBuilder
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.ui.theme.Shapes

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGridItemScope.MediaComponent(
    media: Media,
    exoPlayer: ExoPlayer,
    selectionState: MutableState<Boolean>,
    selectedMedia: SnapshotStateList<Media>,
    preloadRequestBuilder: RequestBuilder<Drawable>,
    thisMediaIsPlayNow: Boolean = false,
    onTapToDisplayPreVideos: (Long) -> Unit,
    onItemClick: (Media) -> Unit,
    onItemLongClick: (Media) -> Unit,
) {
    val isSelected = remember { mutableStateOf(false) }
    MediaImage(
        media = media,
        onTapToDisplayPreVideos = onTapToDisplayPreVideos,
        preloadRequestBuilder = preloadRequestBuilder,
        selectionState = selectionState,
        selectedMedia = selectedMedia,
        isSelected = isSelected,
        exoPlayer = exoPlayer,
        thisMediaIsPlayNow = thisMediaIsPlayNow,
        onItemClick = {
            onItemClick(media)
            if (selectionState.value) {
                isSelected.value = !isSelected.value
            }
        },
        modifier = Modifier
            .clip(Shapes.small)
            .animateItemPlacement()
            .combinedClickable(
                onClick = {
                    onItemClick(media)
                    if (selectionState.value) {
                        isSelected.value = !isSelected.value
                    }
                },
                onLongClick = {
                    onItemLongClick(media)
                    if (selectionState.value) {
                        isSelected.value = !isSelected.value
                    }
                },
            )
    )
}