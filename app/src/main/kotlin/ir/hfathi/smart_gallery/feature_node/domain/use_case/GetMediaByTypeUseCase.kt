package ir.hfathi.smart_gallery.feature_node.domain.use_case

import ir.hfathi.smart_gallery.core.Resource
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.repository.MediaRepository
import ir.hfathi.smart_gallery.feature_node.presentation.picker.AllowedMedia
import kotlinx.coroutines.flow.Flow

class GetMediaByTypeUseCase(
    private val repository: MediaRepository
) {
    operator fun invoke(type: AllowedMedia): Flow<Resource<List<Media>>> = repository.getMediaByType(type)

}