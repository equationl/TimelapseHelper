package state

import androidx.compose.runtime.*
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.key.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import utils.AddText
import view.widget.filterFileList
import view.widget.showFileSelector
import java.io.File

@Composable
fun rememberApplicationState(scope: CoroutineScope) = remember { ApplicationState(scope) }

class ApplicationState(val scope: CoroutineScope) {
    lateinit var window: ComposeWindow

    val imgPreviewState = ImgPreviewState()
    val controlState = ControlState()

    val fileList = mutableStateListOf<File>()

    var dialogText by mutableStateOf("")
    var isRunning by mutableStateOf(false)

    fun onClickImgChoose() {
        showFileSelector(
            onFileSelected = {
                fileList.addAll(filterFileList(it))
            }
        )
    }

    fun onDelImg(index: Int) {
        if (index < 0) {
            fileList.clear()
        }
        else {
            fileList.removeAt(index)
        }
    }

    fun onStartProgress() {
        scope.launch {
            isRunning = true
            dialogText = "正在处理中"
            val result = AddText.startAdd(
                controlState.outputPath,
                controlState.isUsingSourcePath,
                controlState.textPos,
                controlState.textColorFilter.getInputValue().text,
                controlState.textSizeFilter.getInputValue().text,
                controlState.dateFormat,
                fileList,
                controlState.timeZoneFilter.getInputValue().text,
                controlState.outputQualityTextFilter.getInputValue().text
            ) {
                dialogText = "正在处理中：$it"
            }
            result.fold(
                { stringList ->
                    isRunning = false
                    dialogText = "处理完成！"
                    var failText = ""
                    stringList.map { failText += "$it\n" }
                    if (stringList.isNotEmpty()) dialogText += "\n以下文件处理失败\n$failText"
                },
                {
                    isRunning = false
                    dialogText = "错误：${result.exceptionOrNull().toString()}"
                }
            )
        }
    }

    fun onKeyEvent(keyEvent: KeyEvent): Boolean {

        if (keyEvent.type == KeyEventType.KeyDown) {
            when (keyEvent.key.nativeKeyCode) {
                37 -> { // 向左
                    minImgIndex()
                }
                38 -> { // 向上箭头
                    minImgIndex()
                }
                39 -> { // 向右
                    plusImgIndex()
                }
                40 -> { // 向下箭头
                    plusImgIndex()
                }
            }
        }

        return false
    }

    private fun minImgIndex() {
        if (fileList.isNotEmpty()) {
            if (imgPreviewState.showImageIndex == 0) {
                imgPreviewState.showImageIndex = fileList.lastIndex
            }
            else {
                imgPreviewState.showImageIndex--
            }

            scope.launch {
                imgPreviewState.lazyListState.animateScrollToItem(imgPreviewState.showImageIndex)
            }
        }
    }

    private fun plusImgIndex() {
        if (fileList.isNotEmpty()) {
            if (imgPreviewState.showImageIndex == fileList.lastIndex) {
                imgPreviewState.showImageIndex = 0
            }
            else {
                imgPreviewState.showImageIndex++
            }

            scope.launch {
                imgPreviewState.lazyListState.animateScrollToItem(imgPreviewState.showImageIndex)
            }
        }
    }
}