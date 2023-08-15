import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import state.rememberApplicationState
import ui.MinWindowSize
import view.MainPager
import java.awt.Dimension
import kotlin.math.roundToInt

fun main() = application {
    val applicationState = rememberApplicationState(rememberCoroutineScope(), rememberScrollState())

    Window(
        title = "时间水印助手",
        onCloseRequest = ::exitApplication,
        onKeyEvent = {
            applicationState.onKeyEvent(it)
        }
    ) {
        // 设置窗口的最小尺寸
        window.minimumSize = Dimension(MinWindowSize.width.value.roundToInt(), MinWindowSize.height.value.roundToInt())

        applicationState.window = window

        MainPager(applicationState)
    }
}

