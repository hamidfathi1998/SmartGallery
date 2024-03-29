package ir.hfathi.smart_gallery.feature_node.domain.use_case

import ir.hfathi.smart_gallery.core.Resource
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.repository.MediaRepository
import ir.hfathi.smart_gallery.feature_node.domain.util.MediaOrder
import ir.hfathi.smart_gallery.feature_node.domain.util.OrderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetMediaByAlbumUseCase(
    private val repository: MediaRepository
) {
    operator fun invoke(
        albumId: Long,
        mediaOrder: MediaOrder = MediaOrder.Date(OrderType.Descending)
    ): Flow<Resource<List<Media>>> {
        return repository.getMediaByAlbumId(albumId).map {
            it.apply {
                data = data?.let { it1 -> mediaOrder.sortMedia(it1) }
            }
        }
    }

}

