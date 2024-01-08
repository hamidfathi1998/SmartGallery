package ir.hfathi.smart_gallery.feature_node.domain.use_case

import android.content.Context
import ir.hfathi.smart_gallery.feature_node.domain.repository.MediaRepository

data class MediaUseCases(
    private val context: Context,
    private val repository: MediaRepository
) {
    val getAlbumsUseCase = GetAlbumsUseCase(repository)
    val getAlbumsWithTypeUseCase = GetAlbumsWithTypeUseCase(repository)
    val getMediaUseCase = GetMediaUseCase(repository)
    val getMediaByAlbumUseCase = GetMediaByAlbumUseCase(repository)
    val getMediaByAlbumWithTypeUseCase = GetMediaByAlbumWithTypeUseCase(repository)
    val getMediaFavoriteUseCase = GetMediaFavoriteUseCase(repository)
    val getMediaTrashedUseCase = GetMediaTrashedUseCase(repository)
    val getMediaByTypeUseCase = GetMediaByTypeUseCase(repository)
    val getMediaListByUrisUseCase = GetMediaListByUrisUseCase(repository)
    val mediaHandleUseCase = MediaHandleUseCase(repository, context)
    val insertPinnedAlbumUseCase = InsertPinnedAlbumUseCase(repository)
    val deletePinnedAlbumUseCase = DeletePinnedAlbumUseCase(repository)
}