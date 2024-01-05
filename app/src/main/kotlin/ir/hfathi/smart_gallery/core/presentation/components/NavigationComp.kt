package ir.hfathi.smart_gallery.core.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import ir.hfathi.smart_gallery.R
import ir.hfathi.smart_gallery.core.Constants
import ir.hfathi.smart_gallery.core.Constants.Animation.navigateInAnimation
import ir.hfathi.smart_gallery.core.Constants.Animation.navigateUpAnimation
import ir.hfathi.smart_gallery.core.Constants.Target.TARGET_FAVORITES
import ir.hfathi.smart_gallery.core.Constants.Target.TARGET_TRASH
import ir.hfathi.smart_gallery.core.Settings.Album.rememberHideTimelineOnAlbum
import ir.hfathi.smart_gallery.core.Settings.Misc.rememberLastScreen
import ir.hfathi.smart_gallery.core.Settings.Misc.rememberTimelineGroupByMonth
import ir.hfathi.smart_gallery.core.presentation.components.util.OnLifecycleEvent
import ir.hfathi.smart_gallery.core.presentation.components.util.permissionGranted
import ir.hfathi.smart_gallery.feature_node.presentation.albums.AlbumsScreen
import ir.hfathi.smart_gallery.feature_node.presentation.albums.AlbumsViewModel
import ir.hfathi.smart_gallery.feature_node.presentation.common.ChanneledViewModel
import ir.hfathi.smart_gallery.feature_node.presentation.common.MediaViewModel
import ir.hfathi.smart_gallery.feature_node.presentation.favorites.FavoriteScreen
import ir.hfathi.smart_gallery.feature_node.presentation.mediaview.MediaViewScreen
import ir.hfathi.smart_gallery.feature_node.presentation.setup.SetupScreen
import ir.hfathi.smart_gallery.feature_node.presentation.timeline.TimelineScreen
import ir.hfathi.smart_gallery.feature_node.presentation.trashed.TrashedGridScreen
import ir.hfathi.smart_gallery.feature_node.presentation.util.Screen

@Composable
fun NavigationComp(
    navController: NavHostController,
    paddingValues: PaddingValues,
    bottomBarState: MutableState<Boolean>,
    systemBarFollowThemeState: MutableState<Boolean>,
    toggleRotate: () -> Unit,
    isScrolling: MutableState<Boolean>
) {
    val searchBarActive = rememberSaveable {
        mutableStateOf(false)
    }
    val bottomNavEntries = rememberNavigationItems()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    navBackStackEntry?.destination?.route?.let {
        val shouldDisplayBottomBar =
            bottomNavEntries.find { item -> item.route == it && !searchBarActive.value } != null
        bottomBarState.value = shouldDisplayBottomBar
        systemBarFollowThemeState.value = !it.contains(Screen.MediaViewScreen.route)
    }
    val navPipe = hiltViewModel<ChanneledViewModel>()
    navPipe
        .initWithNav(navController, bottomBarState)
        .collectAsStateWithLifecycle(LocalLifecycleOwner.current)

    val groupTimelineByMonth by rememberTimelineGroupByMonth()

    val context = LocalContext.current
    var permissionState by remember { mutableStateOf(context.permissionGranted(Constants.PERMISSIONS)) }
    var lastStartScreen by rememberLastScreen()
    val startDest = remember(permissionState, lastStartScreen) {
        if (permissionState) {
            lastStartScreen
        } else Screen.SetupScreen()
    }
    OnLifecycleEvent { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
            val currentDest = navController.currentDestination?.route ?: Screen.TimelineScreen()
             if (currentDest == Screen.TimelineScreen() || currentDest == Screen.AlbumsScreen()) {
                 lastStartScreen = currentDest
             }
        }
    }

    // Preloaded viewModels
    val albumsViewModel = hiltViewModel<AlbumsViewModel>().apply {
        attachToLifecycle()
    }

    val timelineViewModel = hiltViewModel<MediaViewModel>().apply {
        groupByMonth = groupTimelineByMonth
        attachToLifecycle()
    }

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(
            route = Screen.SetupScreen(),
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation }
        ) {
            navPipe.toggleNavbar(false)
            SetupScreen {
                permissionState = true
                navPipe.navigate(Screen.TimelineScreen())
            }
        }
        composable(
            route = Screen.TimelineScreen.route,
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation }
        ) {
            TimelineScreen(
                paddingValues = paddingValues,
                handler = timelineViewModel.handler,
                mediaState = timelineViewModel.mediaState,
                selectionState = timelineViewModel.multiSelectState,
                selectedMedia = timelineViewModel.selectedPhotoState,
                toggleSelection = timelineViewModel::toggleSelection,
                navigate = navPipe::navigate,
                navigateUp = navPipe::navigateUp,
                toggleNavbar = navPipe::toggleNavbar,
                refresh = timelineViewModel::refresh,
                isScrolling = isScrolling,
                searchBarActive = searchBarActive,
            )
        }
        composable(
            route = Screen.TrashedScreen.route,
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation }
        ) {
            val viewModel = hiltViewModel<MediaViewModel>()
                .apply { target = TARGET_TRASH }
                .apply { groupByMonth = groupTimelineByMonth }
            viewModel.attachToLifecycle()
            TrashedGridScreen(
                paddingValues = paddingValues,
                mediaState = viewModel.mediaState,
                selectionState = viewModel.multiSelectState,
                selectedMedia = viewModel.selectedPhotoState,
                handler = viewModel.handler,
                toggleSelection = viewModel::toggleSelection,
                navigate = navPipe::navigate,
                navigateUp = navPipe::navigateUp,
                toggleNavbar = navPipe::toggleNavbar
            )
        }
        composable(
            route = Screen.FavoriteScreen.route,
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation }
        ) {
            val viewModel = hiltViewModel<MediaViewModel>()
                .apply { target = TARGET_FAVORITES }
                .apply { groupByMonth = groupTimelineByMonth }
            viewModel.attachToLifecycle()
            FavoriteScreen(
                paddingValues = paddingValues,
                mediaState = viewModel.mediaState,
                handler = viewModel.handler,
                selectionState = viewModel.multiSelectState,
                selectedMedia = viewModel.selectedPhotoState,
                toggleFavorite = viewModel::toggleFavorite,
                toggleSelection = viewModel::toggleSelection,
                navigate = navPipe::navigate,
                navigateUp = navPipe::navigateUp,
                toggleNavbar = navPipe::toggleNavbar
            )
        }
        composable(
            route = Screen.AlbumsScreen.route,
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation }
        ) {
            AlbumsScreen(
                navigate = navPipe::navigate,
                toggleNavbar = navPipe::toggleNavbar,
                paddingValues = paddingValues,
                viewModel = albumsViewModel,
                isScrolling = isScrolling,
                searchBarActive = searchBarActive
            )
        }
        composable(
            route = Screen.AlbumViewScreen.route +
                    "?albumId={albumId}&albumName={albumName}",
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation },
            arguments = listOf(
                navArgument(name = "albumId") {
                    type = NavType.LongType
                    defaultValue = -1
                },
                navArgument(name = "albumName") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val argumentAlbumName = backStackEntry.arguments?.getString("albumName")
                ?: stringResource(id = R.string.app_name)
            val argumentAlbumId = backStackEntry.arguments?.getLong("albumId") ?: -1
            val viewModel: MediaViewModel = hiltViewModel<MediaViewModel>()
                .apply { albumId = argumentAlbumId }
                .apply { groupByMonth = groupTimelineByMonth }
            viewModel.attachToLifecycle()
            val hideTimeline by rememberHideTimelineOnAlbum()
            TimelineScreen(
                paddingValues = paddingValues,
                albumId = argumentAlbumId,
                albumName = argumentAlbumName,
                handler = viewModel.handler,
                mediaState = viewModel.mediaState,
                selectionState = viewModel.multiSelectState,
                selectedMedia = viewModel.selectedPhotoState,
                allowNavBar = false,
                allowHeaders = !hideTimeline,
                enableStickyHeaders = !hideTimeline,
                toggleSelection = viewModel::toggleSelection,
                navigate = navPipe::navigate,
                navigateUp = navPipe::navigateUp,
                toggleNavbar = navPipe::toggleNavbar,
                refresh = viewModel::refresh,
                isScrolling = isScrolling
            )
        }
        composable(
            route = Screen.MediaViewScreen.route +
                    "?mediaId={mediaId}&albumId={albumId}",
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation },
            arguments = listOf(
                navArgument(name = "mediaId") {
                    type = NavType.LongType
                    defaultValue = -1
                },
                navArgument(name = "albumId") {
                    type = NavType.LongType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val mediaId: Long = backStackEntry.arguments?.getLong("mediaId") ?: -1
            val albumId: Long = backStackEntry.arguments?.getLong("albumId") ?: -1
            val entryName =
                if (albumId == -1L) Screen.TimelineScreen.route else Screen.AlbumViewScreen.route
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(entryName)
            }
            val viewModel =
                if (albumId != -1L) hiltViewModel<MediaViewModel>(parentEntry)
                    .apply { attachToLifecycle() }
                else timelineViewModel
            MediaViewScreen(
                navigateUp = navPipe::navigateUp,
                toggleRotate = toggleRotate,
                paddingValues = paddingValues,
                mediaId = mediaId,
                mediaState = viewModel.mediaState,
                handler = viewModel.handler,
                refresh = viewModel::refresh
            )
        }
        composable(
            route = Screen.MediaViewScreen.route +
                    "?mediaId={mediaId}&target={target}",
            enterTransition = { navigateInAnimation },
            exitTransition = { navigateUpAnimation },
            popEnterTransition = { navigateInAnimation },
            popExitTransition = { navigateUpAnimation },
            arguments = listOf(
                navArgument(name = "mediaId") {
                    type = NavType.LongType
                    defaultValue = -1
                },
                navArgument(name = "target") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val mediaId: Long = backStackEntry.arguments?.getLong("mediaId") ?: -1
            val target: String? = backStackEntry.arguments?.getString("target")
            val entryName = when (target) {
                TARGET_FAVORITES -> Screen.FavoriteScreen.route
                TARGET_TRASH -> Screen.TrashedScreen.route
                else -> Screen.TimelineScreen.route
            }
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(entryName)
            }
            val viewModel = hiltViewModel<MediaViewModel>(parentEntry)
            viewModel.attachToLifecycle()
            MediaViewScreen(
                navigateUp = navPipe::navigateUp,
                toggleRotate = toggleRotate,
                paddingValues = paddingValues,
                mediaId = mediaId,
                target = target,
                mediaState = viewModel.mediaState,
                handler = viewModel.handler,
                refresh = viewModel::refresh
            )
        }
    }
}