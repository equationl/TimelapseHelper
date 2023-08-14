package state

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class ImgPreviewState {
    var showImageIndex by mutableStateOf(0)
    lateinit var lazyListState: LazyListState
}