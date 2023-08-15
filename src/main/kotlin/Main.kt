import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.*
import state.rememberApplicationState
import ui.MinWindowSize
import view.MainPager
import view.ShowImgView
import java.awt.Dimension
import kotlin.math.roundToInt

fun main() = application {
    val applicationState = rememberApplicationState(rememberCoroutineScope(), rememberScrollState())

    Window(
        title = "时间水印助手",
        onCloseRequest = ::exitApplication,
        onKeyEvent = {
            applicationState.onKeyEvent(it)
        },
        state = rememberWindowState().apply {
            position = WindowPosition(Alignment.Center)
        }
    ) {
        // 设置窗口的最小尺寸
        window.minimumSize = Dimension(MinWindowSize.width.value.roundToInt(), MinWindowSize.height.value.roundToInt())

        applicationState.window = window

        MainPager(applicationState)
    }

    if (applicationState.windowShowPicture != null) {
        Window(
            title = "${applicationState.windowShowPicture?.name}",
            onCloseRequest = {
                applicationState.showPicture(null)
            },
            state = rememberWindowState().apply {
                position = WindowPosition(Alignment.Center)
                placement = WindowPlacement.Fullscreen
            }
        ) {
            ShowImgView(applicationState.windowShowPicture!!)
        }
    }
}

