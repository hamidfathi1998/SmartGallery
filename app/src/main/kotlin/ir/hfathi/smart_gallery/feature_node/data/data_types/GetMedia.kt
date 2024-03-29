package ir.hfathi.smart_gallery.feature_node.data.data_types

import android.content.ContentResolver
import ir.hfathi.smart_gallery.feature_node.data.data_source.Query
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.util.MediaOrder
import ir.hfathi.smart_gallery.feature_node.domain.util.OrderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun ContentResolver.getMedia(
    mediaQuery: Query = Query.MediaQuery(),
    mediaOrder: MediaOrder = MediaOrder.Date(OrderType.Descending)
): List<Media> {
    return withContext(Dispatchers.IO) {
        val media = ArrayList<Media>()
        with(query(mediaQuery)) {
            moveToFirst()
            while (!isAfterLast) {
                try {
                    media.add(getMediaFromCursor())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                moveToNext()
            }
            close()
        }
        return@withContext mediaOrder.sortMedia(media)
    }
}

suspend fun ContentResolver.findMedia(mediaQuery: Query): Media? {
    return withContext(Dispatchers.IO) {
        val mediaList = getMedia(mediaQuery)
        return@withContext if (mediaList.isEmpty()) null else mediaList.first()
    }
}