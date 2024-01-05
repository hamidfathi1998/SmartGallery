package ir.hfathi.smart_gallery.feature_node.domain.use_case

import ir.hfathi.smart_gallery.feature_node.domain.model.Album
import ir.hfathi.smart_gallery.feature_node.domain.model.PinnedAlbum
import ir.hfathi.smart_gallery.feature_node.domain.repository.MediaRepository

class DeletePinnedAlbumUseCase(
    private val repository: MediaRepository
) {

    suspend operator fun invoke(
        album: Album
    ) = repository.removePinnedAlbum(PinnedAlbum(album.id))
}