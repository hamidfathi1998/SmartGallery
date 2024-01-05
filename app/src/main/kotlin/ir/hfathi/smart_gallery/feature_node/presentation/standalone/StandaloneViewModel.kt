package ir.hfathi.smart_gallery.feature_node.presentation.standalone

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.hfathi.smart_gallery.core.MediaState
import ir.hfathi.smart_gallery.feature_node.domain.model.Media
import ir.hfathi.smart_gallery.feature_node.domain.use_case.MediaUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StandaloneViewModel @Inject constructor(
    private val mediaUseCases: MediaUseCases
) : ViewModel() {

    private val _mediaState = MutableStateFlow(MediaState())
    val mediaState = _mediaState.asStateFlow()
    val handler = mediaUseCases.mediaHandleUseCase
    var reviewMode: Boolean = false

    var dataList: List<Uri> = emptyList()
        set(value) {
            if (value.isNotEmpty() && value != dataList) {
                getMedia(value)
            }
            field = value
        }

    var mediaId: Long = -1

    private fun getMedia(clipDataUriList: List<Uri> = emptyList()) {
        viewModelScope.launch(Dispatchers.IO) {
            if (clipDataUriList.isNotEmpty()) {
                mediaUseCases.getMediaListByUrisUseCase(clipDataUriList, reviewMode)
                    .flowOn(Dispatchers.IO)
                    .collectLatest { result ->
                        val data = result.data
                        if (data != null) {
                            mediaId = data.first().id
                            _mediaState.value = MediaState(media = data)
                        } else {
                            _mediaState.value = mediaFromUris()
                        }
                    }
            }
        }
    }

    private fun mediaFromUris(): MediaState {
        val list = mutableListOf<Media>()
        dataList.forEach {
            Media.createFromUri(it)?.let { it1 -> list.add(it1) }
        }
        return MediaState(media = list)
    }

}