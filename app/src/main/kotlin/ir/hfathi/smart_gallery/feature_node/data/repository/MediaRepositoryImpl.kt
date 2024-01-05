package ir.hfathi.smart_gallery.feature_node.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityOptionsCompat
import ir.hfathi.smart_gallery.core.Resource
import ir.hfathi.smart_gallery.core.contentFlowObserver
import ir.hfathi.smart_gallery.feature_node.data.data_source.InternalDatabase
import ir.hfathi.smart_gallery.feature_node.data.data_source.Query
import ir.hfathi.smart_gallery.feature_node.data.data_types.copyMedia
import ir.hfathi.smart_gallery.feature_node.data.data_types.findMedia
import ir.hfathi.smart_gallery.feature_node.data.data_types.getAlbums
import ir.hfathi.smart_gallery.feature_node.data.data_types.getMedia
import ir.hfathi.smart_gallery.feature_node.data.data_types.getMediaByUri
import ir.hfathi.smart_gallery.feature_node.data.data_types.getMediaFavorite
import ir.hfathi.smart_gallery.feature_node.data.data_types.getMediaListByUris
import ir.hfathi.smart_gallery.feature_node.data.data_types.getMediaTrashed
import ir.hfathi.smart_gallery.feature_node.data.data_types.updateMedia
import ir.hfathi.smart_gallery.feature_node.data.data_types.updateMediaExif
import ir.hfathi.smart_gallery.feature_node.domain.model.Album
import ir.hfathi.smart_gallery.feature_node.domain.model.ExifAttributes
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.model.PinnedAlbum
import ir.hfathi.smart_gallery.feature_node.domain.repository.MediaRepository
import ir.hfathi.smart_gallery.feature_node.domain.util.MediaOrder
import ir.hfathi.smart_gallery.feature_node.domain.util.OrderType
import ir.hfathi.smart_gallery.feature_node.presentation.picker.AllowedMedia
import ir.hfathi.smart_gallery.feature_node.presentation.picker.AllowedMedia.BOTH
import ir.hfathi.smart_gallery.feature_node.presentation.picker.AllowedMedia.PHOTOS
import ir.hfathi.smart_gallery.feature_node.presentation.picker.AllowedMedia.VIDEOS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map

class MediaRepositoryImpl(
    private val context: Context,
    private val database: InternalDatabase
) : MediaRepository {

    override fun getMedia(): Flow<Resource<List<Media>>> =
        context.retrieveMedia {
            it.getMedia(mediaOrder = DEFAULT_ORDER)
        }

    override fun getMediaByType(allowedMedia: AllowedMedia): Flow<Resource<List<Media>>> =
        context.retrieveMedia {
            val query = when (allowedMedia) {
                PHOTOS -> Query.PhotoQuery()
                VIDEOS -> Query.VideoQuery()
                BOTH -> Query.MediaQuery()
            }
            it.getMedia(mediaQuery = query, mediaOrder = DEFAULT_ORDER)
        }

    override fun getFavorites(mediaOrder: MediaOrder): Flow<Resource<List<Media>>> =
        context.retrieveMedia {
            it.getMediaFavorite(mediaOrder = mediaOrder)
        }

    override fun getTrashed(): Flow<Resource<List<Media>>> =
        context.retrieveMedia {
            it.getMediaTrashed()
        }

    override fun getAlbums(mediaOrder: MediaOrder, ignoreBlacklisted: Boolean): Flow<Resource<List<Album>>> =
        context.retrieveAlbums {
            it.getAlbums(mediaOrder = mediaOrder).toMutableList().apply {
                replaceAll { album ->
                    album.copy(isPinned = database.getPinnedDao().albumIsPinned(album.id))
                }
            }
        }

    override suspend fun insertPinnedAlbum(pinnedAlbum: PinnedAlbum) =
        database.getPinnedDao().insertPinnedAlbum(pinnedAlbum)

    override suspend fun removePinnedAlbum(pinnedAlbum: PinnedAlbum) =
        database.getPinnedDao().removePinnedAlbum(pinnedAlbum)

    override suspend fun getMediaById(mediaId: Long): Media? {
        val query = Query.MediaQuery().copy(
            bundle = Bundle().apply {
                putString(
                    ContentResolver.QUERY_ARG_SQL_SELECTION,
                    MediaStore.MediaColumns._ID + "= ?"
                )
                putStringArray(
                    ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                    arrayOf(mediaId.toString())
                )
            }
        )
        return context.contentResolver.findMedia(query)
    }

    override fun getMediaByAlbumId(albumId: Long): Flow<Resource<List<Media>>> =
        context.retrieveMedia {
            val query = Query.MediaQuery().copy(
                bundle = Bundle().apply {
                    putString(
                        ContentResolver.QUERY_ARG_SQL_SELECTION,
                        MediaStore.MediaColumns.BUCKET_ID + "= ?"
                    )
                    putStringArray(
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                        arrayOf(albumId.toString())
                    )
                }
            )
            /** return@retrieveMedia */
            it.getMedia(query)
        }

    override fun getMediaByAlbumIdWithType(
        albumId: Long,
        allowedMedia: AllowedMedia
    ): Flow<Resource<List<Media>>> =
        context.retrieveMedia {
            val query = Query.MediaQuery().copy(
                bundle = Bundle().apply {
                    val mimeType = when (allowedMedia) {
                        PHOTOS -> "image%"
                        VIDEOS -> "video%"
                        BOTH -> "%/%"
                    }
                    putString(
                        ContentResolver.QUERY_ARG_SQL_SELECTION,
                        MediaStore.MediaColumns.BUCKET_ID + "= ? and " + MediaStore.MediaColumns.MIME_TYPE + " like ?"
                    )
                    putStringArray(
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                        arrayOf(albumId.toString(), mimeType)
                    )
                }
            )
            /** return@retrieveMedia */
            it.getMedia(query)
        }

    override fun getAlbumsWithType(allowedMedia: AllowedMedia): Flow<Resource<List<Album>>> =
        context.retrieveAlbums {
            val query = Query.AlbumQuery().copy(
                bundle = Bundle().apply {
                    val mimeType = when (allowedMedia) {
                        PHOTOS -> "image%"
                        VIDEOS -> "video%"
                        BOTH -> "%/%"
                    }
                    putString(
                        ContentResolver.QUERY_ARG_SQL_SELECTION,
                        MediaStore.MediaColumns.MIME_TYPE + " like ?"
                    )
                    putStringArray(
                        ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                        arrayOf(mimeType)
                    )
                }
            )
            it.getAlbums(query, mediaOrder = MediaOrder.Label(OrderType.Ascending))
        }

    override fun getMediaByUri(
        uriAsString: String,
        isSecure: Boolean
    ): Flow<Resource<List<Media>>> =
        context.retrieveMediaAsResource {
            val media = it.getMediaByUri(Uri.parse(uriAsString))
            /** return@retrieveMediaAsResource */
            if (media == null) {
                Resource.Error(message = "Media could not be opened")
            } else {
                val query = Query.MediaQuery().copy(
                    bundle = Bundle().apply {
                        putString(
                            ContentResolver.QUERY_ARG_SQL_SELECTION,
                            MediaStore.MediaColumns.BUCKET_ID + "= ?"
                        )
                        putStringArray(
                            ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                            arrayOf(media.albumID.toString())
                        )
                    }
                )
                Resource.Success(
                    data = if (isSecure) listOf(media) else it.getMedia(query)
                        .ifEmpty { listOf(media) })
            }
        }

    override fun getMediaListByUris(
        listOfUris: List<Uri>,
        reviewMode: Boolean
    ): Flow<Resource<List<Media>>> =
        context.retrieveMediaAsResource {
            var mediaList = it.getMediaListByUris(listOfUris)
            if (reviewMode) {
                val query = Query.MediaQuery().copy(
                    bundle = Bundle().apply {
                        putString(
                            ContentResolver.QUERY_ARG_SQL_SELECTION,
                            MediaStore.MediaColumns.BUCKET_ID + "= ?"
                        )
                        putStringArray(
                            ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                            arrayOf(mediaList.first().albumID.toString())
                        )
                    }
                )
                mediaList = it.getMedia(query)
            }
            if (mediaList.isEmpty()) {
                Resource.Error(message = "Media could not be opened")
            } else {
                Resource.Success(data = mediaList)
            }
        }

    override suspend fun toggleFavorite(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>,
        favorite: Boolean
    ) {
        val intentSender = MediaStore.createFavoriteRequest(
            context.contentResolver,
            mediaList.map { it.uri },
            favorite
        ).intentSender
        val senderRequest: IntentSenderRequest = IntentSenderRequest.Builder(intentSender)
            .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
            .build()
        result.launch(senderRequest)
    }

    override suspend fun trashMedia(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>,
        trash: Boolean
    ) {
        val intentSender = MediaStore.createTrashRequest(
            context.contentResolver,
            mediaList.map { it.uri },
            trash
        ).intentSender
        val senderRequest: IntentSenderRequest = IntentSenderRequest.Builder(intentSender)
            .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
            .build()
        result.launch(senderRequest, ActivityOptionsCompat.makeTaskLaunchBehind())
    }

    override suspend fun deleteMedia(
        result: ActivityResultLauncher<IntentSenderRequest>,
        mediaList: List<Media>
    ) {
        val intentSender =
            MediaStore.createDeleteRequest(
                context.contentResolver,
                mediaList.map { it.uri }).intentSender
        val senderRequest: IntentSenderRequest = IntentSenderRequest.Builder(intentSender)
            .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
            .build()
        result.launch(senderRequest)
    }

    override suspend fun copyMedia(
        from: Media,
        path: String
    ): Boolean = context.contentResolver.copyMedia(
        from = from,
        path = path
    )

    override suspend fun renameMedia(
        media: Media,
        newName: String
    ): Boolean = context.contentResolver.updateMedia(
        media = media,
        contentValues = displayName(newName)
    )

    override suspend fun moveMedia(
        media: Media,
        newPath: String
    ): Boolean = context.contentResolver.updateMedia(
        media = media,
        contentValues = relativePath(newPath)
    )

    override suspend fun updateMediaExif(
        media: Media,
        exifAttributes: ExifAttributes
    ): Boolean = context.contentResolver.updateMediaExif(
        media = media,
        exifAttributes = exifAttributes
    )


    companion object {
        private val DEFAULT_ORDER = MediaOrder.Date(OrderType.Descending)
        private val URIs = arrayOf(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )

        private fun displayName(newName: String) = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, newName)
        }

        private fun relativePath(newPath: String) = ContentValues().apply {
            put(MediaStore.MediaColumns.RELATIVE_PATH, newPath)
        }

        private fun Context.retrieveMediaAsResource(dataBody: suspend (ContentResolver) -> Resource<List<Media>>) =
            contentFlowObserver(URIs).map {
                try {
                    dataBody.invoke(contentResolver)
                } catch (e: Exception) {
                    Resource.Error(message = e.localizedMessage ?: "An error occurred")
                }
            }.conflate()

        private fun Context.retrieveMedia(dataBody: suspend (ContentResolver) -> List<Media>) =
            contentFlowObserver(URIs).map {
                try {
                    Resource.Success(data = dataBody.invoke(contentResolver))
                } catch (e: Exception) {
                    Resource.Error(message = e.localizedMessage ?: "An error occurred")
                }
            }.conflate()

        private fun Context.retrieveAlbums(dataBody: suspend (ContentResolver) -> List<Album>) =
            contentFlowObserver(URIs).map {
                try {
                    Resource.Success(data = dataBody.invoke(contentResolver))
                } catch (e: Exception) {
                    Resource.Error(message = e.localizedMessage ?: "An error occurred")
                }
            }.conflate()
    }
}