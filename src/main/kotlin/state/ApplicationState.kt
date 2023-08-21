package state

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.runtime.*
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.key.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import utils.AddText
import utils.Picture2Video
import view.widget.filterFileList
import view.widget.showFileSelector
import java.io.File

@Composable
fun rememberApplicationState(
    scope: CoroutineScope,
    dialogScrollState: ScrollState
) = remember {
    ApplicationState(scope, dialogScrollState)
}

class ApplicationState(val scope: CoroutineScope, val dialogScrollState: ScrollState) {
    lateinit var window: ComposeWindow

    val imgPreviewState = ImgPreviewState()
    val imgPreviewGridState = ImgPreviewGridState()
    val controlState = ControlState()

    val fileList = mutableStateListOf<File>()

    var dialogText by mutableStateOf("")
    var isRunning by mutableStateOf(false)

    var imageShowModel by mutableStateOf(ImgShowModel.List)

    var windowShowPicture: File? by mutableStateOf(null)

    fun onClickImgChoose() {
        showFileSelector(
            onFileSelected = {
                fileList.addAll(filterFileList(it))
            }
        )
    }

    fun onDelImg(index: Int) {
        if (index < 0) {
            imgPreviewState.showImageIndex = 0
            fileList.clear()
        } else {
            fileList.removeAt(index)
        }
    }

    fun onStartProgress() {
        scope.launch {
            var videoFileList = fileList.toList()
            var failText = ""

            if (controlState.isAddTimeText) {
                isRunning = true
                changeDialogText("正在处理中")
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
                    changeDialogText("正在处理中：$it", false)
                }
                result.fold(
                    { addTextResult ->
                        if (controlState.isGenerateVideo) { // 如果勾选了生成视频，则需要重新创建文件列表
                            addTextResult.failFileList.map { failText += "$it\n" }

                            videoFileList = Picture2Video.orderFileListByTime(addTextResult.successFile)
                        } else {
                            isRunning = false
                            changeDialogText("处理完成！", false)
                            addTextResult.failFileList.map { failText += "$it\n" }
                            if (addTextResult.failFileList.isNotEmpty()) changeDialogText("\n以下文件处理失败\n$failText")
                        }
                    },
                    {
                        isRunning = false
                        changeDialogText("错误：${result.exceptionOrNull().toString()}", false)
                    }
                )
            }

            if (controlState.isGenerateVideo) {
                Picture2Video.picture2Video(
                    videoFileList,
                    if (controlState.isUsingSourcePath) File(videoFileList[0].parent) else File(controlState.outputPath),
                    controlState.getFfmpegBinaryPath(),
                    if (controlState.isReciprocalPictureKeepTime) controlState.pictureKeepTime.getInputValue().text.toDouble() else (1.0 / (controlState.pictureKeepTime.getInputValue().text.toDoubleOrNull() ?: 1.0)),
                    controlState.videoRate.getInputValue().text.toInt(),
                    onProgress = {
                        changeDialogText(it)
                                 },
                    onResult = { result ->
                        isRunning = false

                        result.fold(
                            onSuccess = {
                                changeDialogText("$failText \n $it")
                                        },
                            onFailure = {
                                changeDialogText("$failText \n 处理失败：\n ${it.message}\n")
                            }
                        )
                    }
                )

            }
        }
    }

    fun onKeyEvent(keyEvent: KeyEvent): Boolean {

        if (keyEvent.type == KeyEventType.KeyDown) {
            when (keyEvent.key.nativeKeyCode) {
                37 -> { // 向左
                    if (imageShowModel == ImgShowModel.List) {
                        minImgIndex()
                    }
                    else {
                        scrollBackward()
                    }

                }

                38 -> { // 向上箭头
                    if (imageShowModel == ImgShowModel.List) {
                        minImgIndex()
                    }
                    else {
                        scrollBackward()
                    }

                }

                39 -> { // 向右
                    if (imageShowModel == ImgShowModel.List) {
                        plusImgIndex()
                    }
                    else {
                        scrollForward()
                    }

                }

                40 -> { // 向下箭头
                    if (imageShowModel == ImgShowModel.List) {
                        plusImgIndex()
                    }
                    else {
                        scrollForward()
                    }

                }
            }
        }

        return false
    }

    fun changeDialogText(newMsg: String, isAppend: Boolean = true, isScroll: Boolean = true) {
        dialogText = if (isAppend) "$dialogText\n$newMsg" else newMsg

        if (isScroll) {
            scope.launch {
                delay(100) // 需要等待重组完成才能 scroll
                dialogScrollState.scrollTo(dialogScrollState.maxValue)
            }
        }
    }

    fun showPicture(picture: File?) {
        windowShowPicture = picture
    }

    private fun minImgIndex() {
        if (fileList.isNotEmpty()) {
            if (imgPreviewState.showImageIndex == 0) {
                imgPreviewState.showImageIndex = fileList.lastIndex
            } else {
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
            } else {
                imgPreviewState.showImageIndex++
            }

            scope.launch {
                imgPreviewState.lazyListState.animateScrollToItem(imgPreviewState.showImageIndex)
            }
        }
    }

    /**
     * 向前滑动，每次滑动一个可见范围距离，到达最底部时返回顶部
     * */
    @OptIn(ExperimentalFoundationApi::class)
    private fun scrollForward() {
        if ((imgPreviewGridState.lazyState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 ) >= imgPreviewGridState.lazyState.layoutInfo.totalItemsCount - 1) {
            scope.launch {
                // 算不出来最大偏移值，所以改成跳转到最后一个 item
                imgPreviewGridState.lazyState.scrollToItem(0)
            }
        }
        else {
            val scrollTo = imgPreviewGridState.lazyState.firstVisibleItemScrollOffset + imgPreviewGridState.lazyState.layoutInfo.viewportEndOffset
            scope.launch {
                imgPreviewGridState.lazyState.animateScrollBy(scrollTo.toFloat())
            }
        }

    }

    /**
     * 向后滑动，每次滑动一个 item，达到最顶部时返回底部
     * */
    @OptIn(ExperimentalFoundationApi::class)
    private fun scrollBackward() {
        if (imgPreviewGridState.lazyState.firstVisibleItemIndex <= 1) {
            scope.launch {
                // 算不出来最大偏移值，所以改成跳转到最后一个 item
                imgPreviewGridState.lazyState.scrollToItem(imgPreviewGridState.lazyState.layoutInfo.totalItemsCount)
            }
        }
        else {
            scope.launch {
                imgPreviewGridState.lazyState.animateScrollToItem(imgPreviewGridState.lazyState.firstVisibleItemIndex - 1)
            }
        }

    }

    enum class ImgShowModel {
        List,
        Grid
    }
}