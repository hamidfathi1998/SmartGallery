package ir.hfathi.smart_gallery.feature_node.data.data_source

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import ir.hfathi.smart_gallery.feature_node.domain.model.PinnedAlbum

@Database(
    entities = [PinnedAlbum::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class InternalDatabase: RoomDatabase() {

    abstract fun getPinnedDao(): PinnedDao

    companion object {
        const val NAME = "internal_db"
    }
}