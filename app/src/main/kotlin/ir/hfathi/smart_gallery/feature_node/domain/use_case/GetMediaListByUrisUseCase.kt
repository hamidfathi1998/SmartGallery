package ir.hfathi.smart_gallery.feature_node.domain.use_case

import android.net.Uri
import ir.hfathi.smart_gallery.core.Resource
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow

class GetMediaListByUrisUseCase(
    private val repository: MediaRepository
) {
    operator fun invoke(
        listOfUris: List<Uri>,
        reviewMode: Boolean
    ): Flow<Resource<List<Media>>> {
        return repository.getMediaListByUris(listOfUris, reviewMode)
    }

}

