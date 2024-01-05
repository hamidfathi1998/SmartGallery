package ir.hfathi.smart_gallery.feature_node.presentation.mediaview.components.media

import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import ir.hfathi.smart_gallery.core.Constants.DEFAULT_TOP_BAR_ANIMATION_DURATION
import ir.hfathi.smart_gallery.core.Settings
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomablePagerImage(
    modifier: Modifier = Modifier,
    media: Media,
    scrollEnabled: MutableState<Boolean>,
    uiEnabled: Boolean,
    maxScale: Float = 25f,
    maxImageSize: Int,
    onItemClick: () -> Unit
) {
    val zoomState = rememberZoomState(
        maxScale = maxScale
    )
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(media.uri)
            .memoryCacheKey("media_${media.label}_${media.id}")
            .diskCacheKey("media_${media.label}_${media.id}")
            .size(maxImageSize)
            .build(),
        contentScale = ContentScale.Fit,
        filterQuality = FilterQuality.None,
        onSuccess = {
            zoomState.setContentSize(it.painter.intrinsicSize)
        }
    )

    LaunchedEffect(zoomState.scale) {
        scrollEnabled.value = zoomState.scale == 1f
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val allowBlur by Settings.Misc.rememberAllowBlur()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && allowBlur) {
            val blurAlpha by animateFloatAsState(
                animationSpec = tween(DEFAULT_TOP_BAR_ANIMATION_DURATION),
                targetValue = if (uiEnabled) 0.7f else 0f,
                label = "blurAlpha"
            )
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(blurAlpha)
                    .blur(100.dp),
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
        Image(
            modifier = modifier
                .fillMaxSize()
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onDoubleClick = {},
                    onClick = onItemClick
                )
                .zoomable(
                    zoomState = zoomState,
                ),
            painter = painter,
            contentScale = ContentScale.Fit,
            contentDescription = media.label
        )
    }


}
