package state

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector


class ImgPreviewState {
    var showImageIndex by mutableStateOf(0)
    lateinit var lazyListState: LazyListState

    var imgRowNum by mutableStateOf(3)

    fun changeRowNum() {
        imgRowNum++
        if (imgRowNum > 6) {
            imgRowNum = 1
        }
    }

    fun getRowIcon(rowNum: Int): ImageVector {
        return when (rowNum) {
            1 -> Icons.Outlined.LooksOne
            2 -> Icons.Outlined.LooksTwo
            3 -> Icons.Outlined.Looks3
            4 -> Icons.Outlined.Looks4
            5 -> Icons.Outlined.Looks5
            6 -> Icons.Outlined.Looks6
            else -> Icons.Outlined.Looks
        }
    }
}