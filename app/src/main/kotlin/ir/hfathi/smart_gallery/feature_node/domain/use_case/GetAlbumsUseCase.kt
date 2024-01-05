package ir.hfathi.smart_gallery.feature_node.domain.use_case

import ir.hfathi.smart_gallery.core.Resource
import ir.hfathi.smart_gallery.feature_node.domain.model.Album
import ir.hfathi.smart_gallery.feature_node.domain.repository.MediaRepository
import ir.hfathi.smart_gallery.feature_node.domain.util.MediaOrder
import ir.hfathi.smart_gallery.feature_node.domain.util.OrderType
import kotlinx.coroutines.flow.Flow

class GetAlbumsUseCase(
    private val repository: MediaRepository
) {

    operator fun invoke(
        mediaOrder: MediaOrder = MediaOrder.Date(OrderType.Descending),
        ignoreBlacklisted: Boolean = false
    ): Flow<Resource<List<Album>>> = repository.getAlbums(mediaOrder, ignoreBlacklisted)
}