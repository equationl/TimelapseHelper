// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.MinWindowSize
import view.MainPager
import java.awt.Dimension
import kotlin.math.roundToInt


/*@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!") }

    MaterialTheme {
        Button(onClick = {
            text = "Hello, Desktop!"
        }) {
            Text(text)
        }
    }
}*/

fun main() = application {
    Window(
        title = "时间水印助手",
        onCloseRequest = ::exitApplication
    ) {
        // 设置窗口的最小尺寸
        window.minimumSize = Dimension(MinWindowSize.width.value.roundToInt(), MinWindowSize.height.value.roundToInt())

        MainPager(window)
    }
}

