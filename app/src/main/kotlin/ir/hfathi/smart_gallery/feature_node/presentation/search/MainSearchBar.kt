package ir.hfathi.smart_gallery.feature_node.presentation.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.hfathi.smart_gallery.R
import ir.hfathi.smart_gallery.core.Constants.Animation.enterAnimation
import ir.hfathi.smart_gallery.core.Constants.Animation.exitAnimation
import ir.hfathi.smart_gallery.core.Settings.Search.rememberSearchHistory
import ir.hfathi.smart_gallery.core.presentation.components.LoadingMedia
import ir.hfathi.smart_gallery.feature_node.presentation.common.components.MediaGridView
import ir.hfathi.smart_gallery.feature_node.presentation.search.components.SearchBarElevation.Collapsed
import ir.hfathi.smart_gallery.feature_node.presentation.search.components.SearchBarElevation.Expanded
import ir.hfathi.smart_gallery.feature_node.presentation.search.components.SearchHistory
import ir.hfathi.smart_gallery.feature_node.presentation.util.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSearchBar(
    bottomPadding: Dp,
    selectionState: MutableState<Boolean>? = null,
    navigate: (String) -> Unit,
    toggleNavbar: (Boolean) -> Unit,
    isScrolling: MutableState<Boolean>,
    activeState: MutableState<Boolean>,
) {
    var historySet by rememberSearchHistory()
    val vm = hiltViewModel<SearchViewModel>()
    var query by rememberSaveable { mutableStateOf("") }
    val state by vm.mediaState.collectAsStateWithLifecycle()
    val alpha by animateFloatAsState(
        targetValue = if (selectionState != null && selectionState.value) 0.6f else 1f,
        label = "alpha"
    )
    val elevation by animateDpAsState(
        targetValue = if (activeState.value) Expanded() else Collapsed(),
        label = "elevation"
    )
    LaunchedEffect(LocalConfiguration.current, activeState) {
        if (selectionState == null || !selectionState.value)
            toggleNavbar(!activeState.value)
    }

    Box(
        modifier = Modifier
            .semantics { isTraversalGroup = true }
            .zIndex(1f)
            .alpha(alpha)
            .fillMaxWidth()
    ) {
        /**
         * TODO: fillMaxWidth with fixed lateral padding on the search container only
         * It is not yet possible because of the material3 compose limitations
         */
        val searchBarAlpha by animateFloatAsState(
            targetValue = if (isScrolling.value) 0f else 1f,
            label = "searchBarAlpha"
        )
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .alpha(searchBarAlpha),
            enabled = (selectionState == null || !selectionState.value) && !isScrolling.value,
            query = query,
            onQueryChange = {
                query = it
                if (it != vm.lastQuery.value && vm.lastQuery.value.isNotEmpty())
                    vm.clearQuery()
            },
            onSearch = {
                if (it.isNotEmpty())
                    historySet = historySet.toMutableSet().apply { add("${System.currentTimeMillis()}/$it") }
                vm.queryMedia(it)
            },
            active = activeState.value,
            onActiveChange = { activeState.value = it },
            placeholder = {
                Text(text = stringResource(id = R.string.searchbar_title))
            },
            tonalElevation = elevation,
            leadingIcon = {
                IconButton(onClick = {
                    activeState.value = !activeState.value
                    if (query.isNotEmpty()) query = ""
                    vm.queryMedia(query)
                }) {
                    val leadingIcon = if (activeState.value)
                        Icons.AutoMirrored.Outlined.ArrowBack else Icons.Outlined.Search
                    Icon(
                        imageVector = leadingIcon,
                        modifier = Modifier.fillMaxHeight(),
                        contentDescription = null
                    )
                }
            },
            colors = SearchBarDefaults.colors(
                dividerColor = Color.Transparent
            )
        ) {
            /**
             * Recent searches
             */
            AnimatedVisibility(
                visible = vm.lastQuery.value.isEmpty(),
                enter = enterAnimation,
                exit = exitAnimation
            ) {
                SearchHistory {
                    query = it
                    vm.queryMedia(it)
                }
            }

            /**
             * Searched content
             */
            AnimatedVisibility(
                visible = vm.lastQuery.value.isNotEmpty(),
                enter = enterAnimation,
                exit = exitAnimation
            ) {
                if (state.media.isEmpty() && !state.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 72.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ImageSearch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(64.dp)
                        )
                        Text(
                            text = stringResource(R.string.no_media_found),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    val pd = PaddingValues(
                        bottom = bottomPadding + 16.dp
                    )
                    Box {
                        MediaGridView(
                            mediaState = state,
                            paddingValues = pd,
                            isScrolling = remember { mutableStateOf(false) }
                        ) {
                            navigate(Screen.MediaViewScreen.route + "?mediaId=${it.id}")
                        }
                        androidx.compose.animation.AnimatedVisibility(
                            visible = state.isLoading,
                            enter = enterAnimation,
                            exit = exitAnimation
                        ) {
                            LoadingMedia(paddingValues = pd)
                        }
                    }

                }
            }

        }
    }

    BackHandler(activeState.value) {
        if (vm.lastQuery.value.isEmpty()) {
            activeState.value = false
            query = ""
            vm.queryMedia(query)
        } else {
            query = ""
            vm.clearQuery()
        }
    }
}
