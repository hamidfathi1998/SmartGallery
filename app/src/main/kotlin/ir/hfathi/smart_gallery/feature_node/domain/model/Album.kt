
package ir.hfathi.smart_gallery.feature_node.domain.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class Album(
    val id: Long = 0,
    val label: String,
    val pathToThumbnail: String,
    val relativePath: String,
    val timestamp: Long,
    var count: Long = 0,
    val selected: Boolean = false,
    val isPinned: Boolean = false,
) : Parcelable {

    @IgnoredOnParcel
    val volume: String = pathToThumbnail.substringBeforeLast("/").removeSuffix(relativePath.removeSuffix("/"))

    @IgnoredOnParcel
    val isOnSdcard: Boolean =
        volume.toLowerCase(Locale.current).matches(".*[0-9a-f]{4}-[0-9a-f]{4}".toRegex())

    companion object {

        val NewAlbum = Album(
            id = -200,
            label = "New Album",
            pathToThumbnail = "",
            relativePath = "",
            timestamp = 0,
            count = 0,
        )
    }
}