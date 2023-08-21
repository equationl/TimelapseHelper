package view

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import state.ApplicationState
import ui.BackgroundColor
import view.widget.dropFileTarget
import view.widget.filterFileList


@Composable
fun MainPager(applicationState: ApplicationState) {

    applicationState.window.contentPane.dropTarget = dropFileTarget {
        applicationState.fileList.addAll(filterFileList(it, applicationState.controlState.timeZoneFilter.getInputValue().text))
    }

    /*DisposableEffect(Unit) {
        // 拖拽文件
        *//*window.contentPane.dropTarget = dropFileTarget {
            fileList.addAll(filterFileList(it))
        }*//*

        onDispose {  }
    }*/

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundColor
    ) {
        Row (
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (applicationState.imageShowModel == ApplicationState.ImgShowModel.List) {
                ImageContent(
                    applicationState = applicationState,
                    modifier = Modifier.fillMaxSize().weight(1f)
                )

                ControlContent(
                    applicationState = applicationState,
                    modifier = Modifier.fillMaxSize().weight(1f)
                )
            }
            else if (applicationState.imageShowModel == ApplicationState.ImgShowModel.Grid) {
                ImageGridContent(applicationState)
            }
        }
    }

    if (applicationState.dialogText.isNotBlank()) {
        Dialog(
            onCloseRequest = { if (!applicationState.isRunning) applicationState.dialogText = "" },
            title = if (applicationState.isRunning) "处理中" else "处理完成",
            resizable = false
        ) {
            Box {
                Column(
                    modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(applicationState.dialogScrollState),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SelectionContainer {
                        Text(applicationState.dialogText)
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(applicationState.dialogScrollState)
                )
            }
        }
    }
}