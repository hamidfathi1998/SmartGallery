package ir.hfathi.smart_gallery.feature_node.domain.use_case

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import ir.hfathi.smart_gallery.core.Settings.Misc.getTrashEnabled
import ir.hfathi.smart_gallery.feature_node.domain.model.ExifAttributes
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.repository.MediaRepository
import ir.hfathi.smart_gallery.feature_node.presentation.util.mediaPair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext

class MediaHandleUseCase(
    private val repository: MediaRepository,
    private val context: Context
) {

    suspend fun toggleFavorite(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>,
        favorite: Boolean
    ) = repository.toggleFavorite(result, mediaList, favorite)

    suspend fun toggleFavorite(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>
    ) {
        val turnToFavorite = mediaList.filter { it.favorite == 0 }
        val turnToNotFavorite = mediaList.filter { it.favorite == 1 }
        if (turnToFavorite.isNotEmpty()) {
            repository.toggleFavorite(result, turnToFavorite, true)
        }
        if (turnToNotFavorite.isNotEmpty()) {
            repository.toggleFavorite(result, turnToNotFavorite, false)
        }
    }

    suspend fun trashMedia(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>,
        trash: Boolean = true
    ) {
        withContext(Dispatchers.Default) {
            getTrashEnabled(context).collectLatest { isTrashEnabled ->
                /**
                 * Trash media only if user enabled the Trash Can
                 * Or if user wants to remove existing items from the trash
                 * */
                if ((isTrashEnabled || !trash)) {
                    val pair = mediaList.mediaPair()
                    if (pair.first.isNotEmpty()) {
                        repository.trashMedia(result, mediaList, trash)
                    }
                    if (pair.second.isNotEmpty()) {
                        repository.deleteMedia(result, mediaList)
                    }
                } else {
                    repository.deleteMedia(result, mediaList)
                }
            }
        }
    }

    suspend fun copyMedia(
        from: Media,
        path: String
    ): Boolean = repository.copyMedia(from, path)

    suspend fun deleteMedia(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>
    ) = repository.deleteMedia(result, mediaList)

    suspend fun renameMedia(
        media: Media,
        newName: String
    ): Boolean = repository.renameMedia(media, newName)

    suspend fun moveMedia(
        media: Media,
        newPath: String
    ): Boolean = repository.moveMedia(media, newPath)

    suspend fun updateMediaExif(
        media: Media,
        exifAttributes: ExifAttributes
    ): Boolean = repository.updateMediaExif(media, exifAttributes)

}