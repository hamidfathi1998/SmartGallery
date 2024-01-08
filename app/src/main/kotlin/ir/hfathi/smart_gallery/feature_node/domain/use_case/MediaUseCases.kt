package ir.hfathi.smart_gallery.feature_node.domain.use_case

import android.content.Context
import ir.hfathi.smart_gallery.feature_node.domain.repository.MediaRepository

data class MediaUseCases(
    private val context: Context,
    private val repository: MediaRepository
) {
    val getAlbumsUseCase = GetAlbumsUseCase(repository)
    val getMediaUseCase = GetMediaUseCase(repository)
    val getMediaByAlbumUseCase = GetMediaByAlbumUseCase(repository)
    val getMediaFavoriteUseCase = GetMediaFavoriteUseCase(repository)
    val getMediaTrashedUseCase = GetMediaTrashedUseCase(repository)
    val mediaHandleUseCase = MediaHandleUseCase(repository, context)
    val insertPinnedAlbumUseCase = InsertPinnedAlbumUseCase(repository)
    val deletePinnedAlbumUseCase = DeletePinnedAlbumUseCase(repository)
}