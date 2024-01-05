
package ir.hfathi.smart_gallery.core

import android.os.Parcelable
import com.bumptech.glide.load.Key
import kotlinx.parcelize.Parcelize
import java.nio.ByteBuffer
import java.security.MessageDigest

@Parcelize
data class MediaKey(val id: Long, val timestamp: Long, val mimeType: String, val orientation: Int): Key, Parcelable {

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        val data = ByteBuffer.allocate(20).putLong(id).putLong(timestamp).putInt(orientation)
        messageDigest.update(data)
        messageDigest.update(mimeType.toByteArray(Key.CHARSET))
    }
}