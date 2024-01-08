package ir.hfathi.smart_gallery.core

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ir.hfathi.smart_gallery.core.Settings.PREFERENCE_NAME
import ir.hfathi.smart_gallery.core.util.rememberPreference
import ir.hfathi.smart_gallery.feature_node.presentation.util.Screen
import ir.hfathi.smart_gallery.ui.theme.Dimens
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCE_NAME)

object Settings {

    const val PREFERENCE_NAME = "settings"

    object Album {
        private val LAST_SORT = intPreferencesKey("album_last_sort")

        @Composable
        fun rememberLastSort() =
            rememberPreference(key = LAST_SORT, defaultValue = 0)

        private val ALBUM_SIZE = floatPreferencesKey("album_size")

        @Composable
        fun rememberAlbumSize() =
            rememberPreference(key = ALBUM_SIZE, defaultValue = Dimens.Album.size.value)

        private val HIDE_TIMELINE_ON_ALBUM = booleanPreferencesKey("hide_timeline_on_album")

        @Composable
        fun rememberHideTimelineOnAlbum() =
            rememberPreference(key = HIDE_TIMELINE_ON_ALBUM, defaultValue = false)
    }

    object Glide {
        private val MAX_IMAGE_SIZE = intPreferencesKey("max_image_size")

        @Composable
        fun rememberMaxImageSize() =
            rememberPreference(key = MAX_IMAGE_SIZE, defaultValue = 4096)
    }

    object Search {
        private val HISTORY = stringSetPreferencesKey("search_history")

        @Composable
        fun rememberSearchHistory() =
            rememberPreference(key = HISTORY, defaultValue = emptySet())
    }

    object Misc {
        private val USER_CHOICE_MEDIA_MANAGER = booleanPreferencesKey("use_media_manager")

        @Composable
        fun rememberIsMediaManager() =
            rememberPreference(key = USER_CHOICE_MEDIA_MANAGER, defaultValue = false)

        private val ENABLE_TRASH = booleanPreferencesKey("enable_trashcan")

        fun getTrashEnabled(context: Context) =
            context.dataStore.data.map { it[ENABLE_TRASH] ?: true }

        private val LAST_SCREEN = stringPreferencesKey("last_screen")

        @Composable
        fun rememberLastScreen() =
            rememberPreference(key = LAST_SCREEN, defaultValue = Screen.TimelineScreen.route)

        private val FORCE_THEME = booleanPreferencesKey("force_theme")

        @Composable
        fun rememberForceTheme() =
            rememberPreference(key = FORCE_THEME, defaultValue = false)

        private val DARK_MODE = booleanPreferencesKey("dark_mode")

        @Composable
        fun rememberIsDarkMode() =
            rememberPreference(key = DARK_MODE, defaultValue = false)

        private val AMOLED_MODE = booleanPreferencesKey("amoled_mode")

        @Composable
        fun rememberIsAmoledMode() =
            rememberPreference(key = AMOLED_MODE, defaultValue = false)

        private val SECURE_MODE = booleanPreferencesKey("secure_mode")

        fun getSecureMode(context: Context) =
            context.dataStore.data.map { it[SECURE_MODE] ?: false }

        private val TIMELINE_GROUP_BY_MONTH = booleanPreferencesKey("timeline_group_by_month")

        @Composable
        fun rememberTimelineGroupByMonth() =
            rememberPreference(key = TIMELINE_GROUP_BY_MONTH, defaultValue = false)

        private val ALLOW_BLUR = booleanPreferencesKey("allow_blur")

        @Composable
        fun rememberAllowBlur() = rememberPreference(key = ALLOW_BLUR, defaultValue = true)

        private val OLD_NAVBAR = booleanPreferencesKey("old_navbar")

        @Composable
        fun rememberOldNavbar() = rememberPreference(key = OLD_NAVBAR, defaultValue = false)

        private val MEDIA_VERSION = stringPreferencesKey("media_version")

        suspend fun getStoredMediaVersion(context: Context): String {
            var version = "null"
            context.dataStore.data.map { it[MEDIA_VERSION] ?: "null" }.collectLatest {
                version = it
            }
            return version
        }

        suspend fun updateStoredMediaVersion(context: Context, newVersion: String) {
            context.dataStore.edit {
                it[MEDIA_VERSION] = newVersion
            }
        }

        private val ALLOW_VIBRATIONS = booleanPreferencesKey("allow_vibrations")

        fun allowVibrations(context: Context) = context.dataStore.data.map { it[ALLOW_VIBRATIONS] ?: true }

    }
}
