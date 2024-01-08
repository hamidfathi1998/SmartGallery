package ir.hfathi.smart_gallery.feature_node.presentation.common.components

import android.graphics.drawable.Drawable
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.GestureDetectorCompat
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DecodeFormat
import ir.hfathi.smart_gallery.core.Constants.Animation
import ir.hfathi.smart_gallery.core.MediaKey
import ir.hfathi.smart_gallery.core.presentation.components.CheckBox
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.presentation.mediaview.components.video.VideoDurationHeader
import ir.hfathi.smart_gallery.ui.theme.Dimens
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import ir.hfathi.smart_gallery.R

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(
    ExperimentalGlideComposeApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun MediaImage(
    media: Media,
    preloadRequestBuilder: RequestBuilder<Drawable>,
    selectionState: MutableState<Boolean>,
    selectedMedia: SnapshotStateList<Media>,
    isSelected: MutableState<Boolean>,
    onItemClick: (Media) -> Unit,
    onTapToDisplayPreVideos: (Long) -> Unit,
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier,
    thisMediaIsPlayNow: Boolean = false
) {
    if (!selectionState.value) {
        isSelected.value = false
    } else {
        isSelected.value = selectedMedia.find { it.id == media.id } != null
    }
    val selectedSize by animateDpAsState(
        if (isSelected.value) 12.dp else 0.dp, label = "selectedSize"
    )
    val scale by animateFloatAsState(
        if (isSelected.value) 0.5f else 1f, label = "scale"
    )
    val selectedShapeSize by animateDpAsState(
        if (isSelected.value) 16.dp else 0.dp, label = "selectedShapeSize"
    )
    val strokeSize by animateDpAsState(
        targetValue = if (isSelected.value) 2.dp else 0.dp, label = "strokeSize"
    )
    val strokeColor by animateColorAsState(
        targetValue = if (isSelected.value) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "strokeColor"
    )

    val context = LocalContext.current
    val gestureDetector = remember {
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onItemClick.invoke(media)
                return true
            }
        })
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .size(Dimens.Photo())
            .pointerInteropFilter { motionEvent ->
                gestureDetector.onTouchEvent(motionEvent)
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        onTapToDisplayPreVideos.invoke(media.id)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        true
                    }
                    else -> false
                }
            }

    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .aspectRatio(1f)
                .padding(selectedSize)
                .clip(RoundedCornerShape(selectedShapeSize))
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(selectedShapeSize)
                )
                .border(
                    width = strokeSize,
                    shape = RoundedCornerShape(selectedShapeSize),
                    color = strokeColor
                )
        ) {
            if (thisMediaIsPlayNow.not()) {
                GlideImage(
                    modifier = Modifier
                        .fillMaxSize(),
                    model = media.uri,
                    contentDescription = media.label,
                    contentScale = ContentScale.Crop,
                ) {
                    it.thumbnail(preloadRequestBuilder)
                        .signature(
                            MediaKey(
                                media.id,
                                media.timestamp,
                                media.mimeType,
                                media.orientation
                            )
                        )
                        .format(DecodeFormat.PREFER_RGB_565)
                        .override(250)
                }
            } else {
                LaunchedEffect(key1 = thisMediaIsPlayNow) {
                    if (thisMediaIsPlayNow) {
                        val mediaItem = MediaItem.fromUri(media.uri)
                        exoPlayer.setMediaItem(mediaItem)
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                    } else {
                        exoPlayer.stop() // Stop playback when the media is not in view
                    }
                }
                GlideImage(
                    modifier = Modifier
                        .fillMaxSize(),
                    model = media.uri,
                    contentDescription = media.label,
                    contentScale = ContentScale.Crop,
                ) {
                    it.thumbnail(preloadRequestBuilder)
                        .signature(
                            MediaKey(
                                media.id,
                                media.timestamp,
                                media.mimeType,
                                media.orientation
                            )
                        )
                        .format(DecodeFormat.PREFER_RGB_565)
                        .override(250)
                }
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()

                )
            }
        }

        AnimatedVisibility(
            visible = media.duration != null,
            enter = Animation.enterAnimation,
            exit = Animation.exitAnimation,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            VideoDurationHeader(
                modifier = Modifier
                    .padding(selectedSize / 2)
                    .scale(scale),
                media = media
            )
        }

        AnimatedVisibility(
            visible = media.isFavorite,
            enter = Animation.enterAnimation,
            exit = Animation.exitAnimation,
            modifier = Modifier
                .align(Alignment.BottomEnd)
        ) {
            Image(
                modifier = Modifier
                    .padding(selectedSize / 2)
                    .scale(scale)
                    .padding(8.dp)
                    .size(16.dp),
                imageVector = Icons.Filled.Favorite,
                colorFilter = ColorFilter.tint(Color.Red),
                contentDescription = null
            )
        }

        AnimatedVisibility(
            visible = selectionState.value,
            enter = Animation.enterAnimation,
            exit = Animation.exitAnimation
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                CheckBox(isChecked = isSelected.value)
            }
        }
    }
}
