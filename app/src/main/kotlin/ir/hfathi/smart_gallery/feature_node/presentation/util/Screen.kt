package ir.hfathi.smart_gallery.feature_node.presentation.util

sealed class Screen(val route: String) {
    data object TimelineScreen : Screen("timeline_screen")
    data object AlbumsScreen : Screen("albums_screen")

    data object AlbumViewScreen : Screen("album_view_screen")
    data object MediaViewScreen : Screen("media_screen")

    data object TrashedScreen : Screen("trashed_screen")
    data object FavoriteScreen : Screen("favorite_screen")

    data object SetupScreen: Screen("setup_screen")

    operator fun invoke() = route
}
