package state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import utils.FilterColorHex
import utils.FilterGMT
import utils.FilterNumber
import utils.TextPos
import java.io.File
import java.util.*

class ControlState {
    var outputPath by mutableStateOf("原路径")
    var ffmpegPath by  mutableStateOf("")
    var isUsingSourcePath by mutableStateOf(true)
    var isReciprocalPictureKeepTime by mutableStateOf(false)
    var ffmpegFrom by mutableStateOf(FFmpegFrom.Bundle)
    var isGenerateVideo by mutableStateOf(false)
    var isAddTimeText by  mutableStateOf(true)
    var textPos by mutableStateOf(TextPos.LEFT_BOTTOM)
    val textColorFilter = FilterColorHex(defaultValue = TextFieldValue("#FFCCCCCC"))
    val textSizeFilter = FilterNumber(minValue = 1.0, decimalNumber = 0, defaultValue = TextFieldValue("80"))
    var dateFormat by mutableStateOf("yyyy.MM.dd HH:mm:ss")
    val timeZoneFilter = FilterGMT(TextFieldValue("GMT+8:00"))
    val outputQualityTextFilter = FilterNumber(minValue = 0.0, maxValue = 1.0, defaultValue = TextFieldValue("0.7"))
    val pictureKeepTime = FilterNumber(minValue = 0.0, maxValue = Double.MAX_VALUE, decimalNumber = Int.MAX_VALUE, defaultValue = TextFieldValue("1.0"))
    val videoRate = FilterNumber(minValue = 1.0, maxValue = Double.MAX_VALUE, decimalNumber = 0, defaultValue = TextFieldValue("25"))

    enum class FFmpegFrom {
        System,
        Bundle,
        Customize
    }

    fun getFfmpegBinaryPath(): String {
       return when (ffmpegFrom) {
           FFmpegFrom.System -> {
                "ffmpeg"
            }
            FFmpegFrom.Bundle -> {
                val fileName = if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win")) "ffmpeg.exe" else "ffmpeg"
                val executableFile = File(System.getProperty("compose.application.resources.dir")).resolve(fileName)
                if (!executableFile.canExecute()) {
                    println("没有执行权限，正在尝试授予")
                    executableFile.setExecutable(true)
                }
                executableFile.absolutePath
            }
            FFmpegFrom.Customize -> {
                ffmpegPath
            }
        }
    }
}