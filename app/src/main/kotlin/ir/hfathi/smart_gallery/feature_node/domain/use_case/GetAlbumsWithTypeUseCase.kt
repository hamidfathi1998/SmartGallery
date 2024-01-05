package ir.hfathi.smart_gallery.feature_node.domain.use_case

import ir.hfathi.smart_gallery.core.Resource
import ir.hfathi.smart_gallery.feature_node.domain.model.Album
import ir.hfathi.smart_gallery.feature_node.domain.repository.MediaRepository
import ir.hfathi.smart_gallery.feature_node.presentation.picker.AllowedMedia
import kotlinx.coroutines.flow.Flow

class GetAlbumsWithTypeUseCase(
    private val repository: MediaRepository
) {

    operator fun invoke(
        allowedMedia: AllowedMedia
    ): Flow<Resource<List<Album>>> = repository.getAlbumsWithType(allowedMedia)
}