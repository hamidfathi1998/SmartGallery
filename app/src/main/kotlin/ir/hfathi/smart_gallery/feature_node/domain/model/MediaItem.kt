
package ir.hfathi.smart_gallery.feature_node.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class MediaItem : Parcelable {
    abstract val key: String

    data class Header(
        override val key: String,
        val text: String,
        val data: List<Media>
    ) : MediaItem()

    data class MediaViewItem(
        override val key: String,
        val media: Media
    ) : MediaItem()

}

val Any.isHeaderKey: Boolean
    get() = this is String && this.startsWith("header")

val Any.isBigHeaderKey: Boolean
    get() = this is String && isHeaderKey && this.contains("big")

val Any.isIgnoredKey: Boolean
    get() = this is String && this == "aboveGrid"