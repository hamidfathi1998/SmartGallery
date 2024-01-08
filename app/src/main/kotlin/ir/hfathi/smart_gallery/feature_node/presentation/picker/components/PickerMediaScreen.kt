
package ir.hfathi.smart_gallery.feature_node.presentation.picker.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.compose.rememberGlidePreloadingData
import com.bumptech.glide.signature.MediaStoreSignature
import ir.hfathi.smart_gallery.R
import ir.hfathi.smart_gallery.core.MediaState
import ir.hfathi.smart_gallery.core.presentation.components.StickyHeader
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.model.MediaItem
import ir.hfathi.smart_gallery.feature_node.domain.model.isHeaderKey
import ir.hfathi.smart_gallery.feature_node.presentation.common.components.MediaComponent
import ir.hfathi.smart_gallery.feature_node.presentation.util.FeedbackManager
import ir.hfathi.smart_gallery.ui.theme.Dimens
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun PickerMediaScreen(
    mediaState: StateFlow<MediaState>,
    selectedMedia: SnapshotStateList<Media>,
    allowSelection: Boolean,
) {
    val scope = rememberCoroutineScope()
    val stringToday = stringResource(id = R.string.header_today)
    val stringYesterday = stringResource(id = R.string.header_yesterday)
    val state by mediaState.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    val isCheckVisible = rememberSaveable { mutableStateOf(allowSelection) }
    val feedbackManager = FeedbackManager.rememberFeedbackManager()
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    exoPlayer.prepare()
    exoPlayer.volume = 0f
    val exoPlayerMediaId = remember { mutableStateOf(-1L) }
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }
    LaunchedEffect(key1 = exoPlayerMediaId.value) { exoPlayer.stop() }

    /** Glide Preloading **/
    val preloadingData = rememberGlidePreloadingData(
        data = state.media,
        preloadImageSize = Size(50f, 50f)
    ) { media: Media, requestBuilder: RequestBuilder<Drawable> ->
        requestBuilder
            .signature(MediaStoreSignature(media.mimeType, media.timestamp, media.orientation))
            .load(media.uri)
    }
    /** ************ **/
    LazyVerticalGrid(
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Adaptive(Dimens.Photo()),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        items(
            items = state.mappedMedia,
            key = { if (it is MediaItem.MediaViewItem) it.media.toString() else it.key },
            contentType = { it.key.startsWith("media_") },
            span = { item ->
                GridItemSpan(if (item.key.isHeaderKey) maxLineSpan else 1)
            }
        ) { item ->
            when (item) {
                is MediaItem.Header -> {
                    val isChecked = rememberSaveable { mutableStateOf(false) }
                    if (allowSelection) {
                        LaunchedEffect(selectedMedia.size) {
                            // Partial check of media items should not check the header
                            isChecked.value = selectedMedia.containsAll(item.data)
                        }
                    }
                    val title = item.text
                        .replace("Today", stringToday)
                        .replace("Yesterday", stringYesterday)
                    StickyHeader(
                        date = title,
                        showAsBig = item.key.contains("big"),
                        isCheckVisible = isCheckVisible,
                        isChecked = isChecked
                    ) {
                        if (allowSelection) {
                            feedbackManager.vibrate()
                            scope.launch {
                                isChecked.value = !isChecked.value
                                if (isChecked.value) {
                                    val toAdd = item.data.toMutableList().apply {
                                        // Avoid media from being added twice to selection
                                        removeIf { selectedMedia.contains(it) }
                                    }
                                    selectedMedia.addAll(toAdd)
                                } else selectedMedia.removeAll(item.data)
                            }
                        }
                    }
                }

                is MediaItem.MediaViewItem -> {
                    val mediaIndex = state.media.indexOf(item.media).coerceAtLeast(0)
                    val (media, preloadRequestBuilder) = preloadingData[mediaIndex]
                    val selectionState = remember { mutableStateOf(true) }
                    MediaComponent(
                        media = media,
                        selectionState = selectionState,
                        selectedMedia = selectedMedia,
                        exoPlayer = exoPlayer,
                        thisMediaIsPlayNow = exoPlayerMediaId.value == media.id,
                        preloadRequestBuilder = preloadRequestBuilder,
                        onItemLongClick = {
                            feedbackManager.vibrate()
                            if (allowSelection) {
                                if (selectedMedia.contains(it)) selectedMedia.remove(it)
                                else selectedMedia.add(it)
                            } else if (!selectedMedia.contains(it) && selectedMedia.size == 1) {
                                selectedMedia[0] = it
                            } else if (selectedMedia.isEmpty()) {
                                selectedMedia.add(it)
                            } else {
                                selectedMedia.remove(it)
                            }
                        },
                        onTapToDisplayPreVideos = {
                            exoPlayerMediaId.value = it
                        },
                        onItemClick = {
                            feedbackManager.vibrate()
                            if (allowSelection) {
                                if (selectedMedia.contains(it)) selectedMedia.remove(it)
                                else selectedMedia.add(it)
                            } else if (!selectedMedia.contains(it) && selectedMedia.size == 1) {
                                selectedMedia[0] = it
                            } else if (selectedMedia.isEmpty()) {
                                selectedMedia.add(it)
                            } else {
                                selectedMedia.remove(it)
                            }
                        }
                    )
                }
            }
        }
    }
}