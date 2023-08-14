package state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import utils.FilterColorHex
import utils.FilterGMT
import utils.FilterNumber
import utils.TextPos

class ControlState {
    var outputPath by mutableStateOf("原路径")
    var ffmpegPath by  mutableStateOf("跟随系统")
    var isUsingSourcePath by mutableStateOf(true)
    var isUsingSystemFFmpegPath by mutableStateOf(true)
    var isGenerateVideo by mutableStateOf(false)
    var isAddTimeText by  mutableStateOf(true)
    var textPos by mutableStateOf(TextPos.LEFT_BOTTOM)
    val textColorFilter = FilterColorHex(defaultValue = TextFieldValue("#FFCCCCCC"))
    val textSizeFilter = FilterNumber(minValue = 1.0, decimalNumber = 0, defaultValue = TextFieldValue("80"))
    var dateFormat by mutableStateOf("yyyy.MM.dd HH:mm:ss")
    val timeZoneFilter = FilterGMT(TextFieldValue("GMT+8:00"))
    val outputQualityTextFilter = FilterNumber(minValue = 0.0, maxValue = 1.0, defaultValue = TextFieldValue("0.7"))
}