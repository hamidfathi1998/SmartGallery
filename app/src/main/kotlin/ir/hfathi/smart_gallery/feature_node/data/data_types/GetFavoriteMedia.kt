package ir.hfathi.smart_gallery.feature_node.data.data_types

import android.content.ContentResolver
import android.provider.MediaStore
import ir.hfathi.smart_gallery.feature_node.data.data_source.Query.Companion.defaultBundle
import ir.hfathi.smart_gallery.feature_node.data.data_source.Query.MediaQuery
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.util.MediaOrder
import ir.hfathi.smart_gallery.feature_node.domain.util.OrderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun ContentResolver.getMediaFavorite(
    mediaOrder: MediaOrder = MediaOrder.Date(OrderType.Descending)
): List<Media> {
    return withContext(Dispatchers.IO) {
        val mediaQuery = MediaQuery().copy(
            bundle = defaultBundle.apply {
                putInt(MediaStore.QUERY_ARG_MATCH_FAVORITE, MediaStore.MATCH_ONLY)
            }
        )
        return@withContext mediaOrder.sortMedia(getMedia(mediaQuery))
    }
}
