package ir.hfathi.smart_gallery.feature_node.domain.use_case

import ir.hfathi.smart_gallery.core.Resource
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class GetMediaTrashedUseCase(
    private val repository: MediaRepository
) {

    operator fun invoke(): Flow<Resource<List<Media>>> = repository.getTrashed()

}