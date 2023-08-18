import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.*
import constant.Constant
import state.ApplicationState
import state.rememberApplicationState
import ui.MinWindowSize
import view.MainPager
import view.ShowImgView
import java.awt.Dimension
import kotlin.math.roundToInt

fun main() = application {
    val applicationState = rememberApplicationState(rememberCoroutineScope(), rememberScrollState())

    Window(
        title = if (applicationState.imageShowModel == ApplicationState.ImgShowModel.List) Constant.WindowTitleMain else Constant.WindowTitleGridImage,
        onCloseRequest = {
            if (applicationState.imageShowModel == ApplicationState.ImgShowModel.List) {
                exitApplication()
            }
            else {
                applicationState.imageShowModel = ApplicationState.ImgShowModel.List
            }
        },
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

