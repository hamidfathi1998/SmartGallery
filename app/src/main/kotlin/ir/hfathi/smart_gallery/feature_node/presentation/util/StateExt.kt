package ir.hfathi.smart_gallery.feature_node.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ir.hfathi.smart_gallery.core.Constants
import ir.hfathi.smart_gallery.core.MediaState
import ir.hfathi.smart_gallery.core.Resource
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.model.MediaItem
import ir.hfathi.smart_gallery.feature_node.domain.use_case.MediaUseCases
import ir.hfathi.smart_gallery.feature_node.presentation.picker.AllowedMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RepeatOnResume(action: () -> Unit) {
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                action()
            }
        }
    }
}

fun <T> MutableState<T>.update(newState: T) {
    if (value != newState) {
        value = newState
    }
}

fun MediaUseCases.mediaFlowWithType(
    albumId: Long,
    allowedMedia: AllowedMedia
): Flow<Resource<List<Media>>> =
    (if (albumId != -1L) {
        getMediaByAlbumWithTypeUseCase(albumId, allowedMedia)
    } else {
        getMediaByTypeUseCase(allowedMedia)
    }).flowOn(Dispatchers.IO).conflate()

fun MediaUseCases.mediaFlow(albumId: Long, target: String?): Flow<Resource<List<Media>>> =
    (if (albumId != -1L) {
        getMediaByAlbumUseCase(albumId)
    } else if (!target.isNullOrEmpty()) {
        when (target) {
            Constants.Target.TARGET_FAVORITES -> getMediaFavoriteUseCase()
            Constants.Target.TARGET_TRASH -> getMediaTrashedUseCase()
            else -> getMediaUseCase()
        }
    } else {
        getMediaUseCase()
    }).flowOn(Dispatchers.IO).conflate()

suspend fun MutableStateFlow<MediaState>.collectMedia(
    data: List<Media>,
    error: String,
    albumId: Long,
    groupByMonth: Boolean = false,
    withMonthHeader: Boolean = true
) {
    var mappedData: ArrayList<MediaItem>? = ArrayList()
    var mappedDataWithMonthly: ArrayList<MediaItem>? = ArrayList()
    var monthHeaderList: MutableSet<String>? = mutableSetOf()
    withContext(Dispatchers.IO) {
        data.groupBy {
            if (groupByMonth) {
                it.timestamp.getMonth()
            } else {
                it.timestamp.getDate(
                    stringToday = "Today"
                    /** Localized in composition */
                    ,
                    stringYesterday = "Yesterday"
                    /** Localized in composition */
                )
            }
        }.forEach { (date, data) ->
            val dateHeader = MediaItem.Header("header_$date", date, data)
            val groupedMedia = data.map {
                MediaItem.MediaViewItem("media_${it.id}_${it.label}", it)
            }
            if (groupByMonth) {
                mappedData!!.add(dateHeader)
                mappedData!!.addAll(groupedMedia)
                mappedDataWithMonthly!!.add(dateHeader)
                mappedDataWithMonthly!!.addAll(groupedMedia)
            } else {
                val month = getMonth(date)
                if (month.isNotEmpty() && !monthHeaderList!!.contains(month)) {
                    monthHeaderList!!.add(month)
                    if (withMonthHeader && mappedDataWithMonthly!!.isNotEmpty()) {
                        mappedDataWithMonthly!!.add(
                            MediaItem.Header(
                                "header_big_${month}_${data.size}",
                                month,
                                emptyList()
                            )
                        )
                    }
                }
                mappedData!!.add(dateHeader)
                if (withMonthHeader) {
                    mappedDataWithMonthly!!.add(dateHeader)
                }
                mappedData!!.addAll(groupedMedia)
                if (withMonthHeader) {
                    mappedDataWithMonthly!!.addAll(groupedMedia)
                }
            }
        }
    }
    withContext(Dispatchers.Main) {
        tryEmit(
            MediaState(
                isLoading = false,
                error = error,
                media = data,
                mappedMedia = mappedData!!,
                mappedMediaWithMonthly = if (withMonthHeader) mappedDataWithMonthly!! else emptyList(),
                dateHeader = data.dateHeader(albumId)
            )
        )
    }
    mappedData = null
    mappedDataWithMonthly = null
    monthHeaderList = null
}

private fun List<Media>.dateHeader(albumId: Long): String =
    if (albumId != -1L) {
        val startDate: DateExt = last().timestamp.getDateExt()
        val endDate: DateExt = first().timestamp.getDateExt()
        getDateHeader(startDate, endDate)
    } else ""