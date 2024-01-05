package ir.hfathi.smart_gallery.feature_node.presentation.standalone

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import ir.hfathi.smart_gallery.feature_node.presentation.mediaview.MediaViewScreen
import ir.hfathi.smart_gallery.feature_node.presentation.util.toggleOrientation
import ir.hfathi.smart_gallery.ui.theme.GalleryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StandaloneActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val action = intent.action.toString()
        val isSecure = action.toLowerCase(Locale.current).contains("secure")
        val clipData = intent.clipData
        val uriList = mutableSetOf<Uri>()
        intent.data?.let(uriList::add)
        if (clipData != null) {
            for (i in 0 until clipData.itemCount) {
                uriList.add(clipData.getItemAt(i).uri)
            }
        }
        setShowWhenLocked(isSecure)
        setContent {
            GalleryTheme(darkTheme = true) {
                Scaffold { paddingValues ->
                    val viewModel = hiltViewModel<StandaloneViewModel>().apply {
                        reviewMode = action.contains("REVIEW")
                        dataList = uriList.toList()
                    }

                    MediaViewScreen(
                        navigateUp = { finish() },
                        toggleRotate = ::toggleOrientation,
                        paddingValues = paddingValues,
                        isStandalone = true,
                        mediaId = viewModel.mediaId,
                        mediaState = viewModel.mediaState,
                        handler = viewModel.handler,
                        refresh = {}
                    )
                }
                BackHandler {
                    finish()
                }
            }
        }
    }
}