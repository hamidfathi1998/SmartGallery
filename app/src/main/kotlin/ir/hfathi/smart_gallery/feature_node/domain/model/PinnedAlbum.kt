
package ir.hfathi.smart_gallery.feature_node.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinned_table")
data class PinnedAlbum(
    @PrimaryKey(autoGenerate = false)
    val id: Long
)
