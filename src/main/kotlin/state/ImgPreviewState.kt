package state

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class ImgPreviewState {
    var showImageIndex by mutableStateOf(0)
    var sortType by mutableStateOf(ImgSortType.TimeAsc)

    lateinit var lazyListState: LazyListState

    enum class ImgSortType(val isAsc: Boolean) {
        TimeAsc(true),
        TimeDesc(false),
        NameAsc(true),
        NameDesc(false)
    }
}