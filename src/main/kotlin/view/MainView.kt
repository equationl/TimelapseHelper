package view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import ui.BackgroundColor
import ui.CardColor
import ui.CardSize
import utils.*
import view.widget.dropFileTarget
import view.widget.filterFileList
import view.widget.legalSuffixList
import view.widget.showFileSelector
import java.io.File
import javax.swing.JFileChooser


@Composable
fun MainPager(window: ComposeWindow) {
    val dialogScrollState = rememberScrollState()
    val fileList = mutableStateListOf<File>()
    val coroutineScope = rememberCoroutineScope()

    var dialogText by remember { mutableStateOf("") }
    var isRunning by remember { mutableStateOf(false) }

    window.contentPane.dropTarget = dropFileTarget {
        fileList.addAll(filterFileList(it))
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
            ImageContent(
                onclick = {
                    showFileSelector(
                        onFileSelected = {
                            fileList.addAll(filterFileList(it))
                        }
                    )
                },
                onDel = {index: Int ->
                    if (index < 0) {
                        fileList.clear()
                    }
                    else {
                        fileList.removeAt(index)
                    }
                },
                fileList = fileList.toList()
            )

            ControlContent(
                onStart = { outputPath, isUsingSourcePath, textPos, textColor, textSize, dateFormat, timeZone, outputQualityText ->
                    coroutineScope.launch {
                        isRunning = true
                        dialogText = "正在处理中"
                        val result = AddText.startAdd(
                            outputPath, isUsingSourcePath, textPos, textColor, textSize,
                            dateFormat, fileList, timeZone, outputQualityText
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
                },
                enabled = fileList.isNotEmpty()
            )

        }
    }

    if (dialogText.isNotBlank()) {
        Dialog(
            onCloseRequest = { if (!isRunning) dialogText = "" },
            title = if (isRunning) "处理中" else "处理完成",
            resizable = false
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(dialogScrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(dialogText)
            }
        }
    }
}

@Composable
fun ControlContent(
    onStart: (
        outputPath: String,
        isUsingSourcePath: Boolean,
        textPos: TextPos,
        textColor: String,
        textSize: String,
        dateFormat: String,
        timeZone: String,
        outputQualityText: String,
    ) -> Unit,
    enabled: Boolean
) {
    var outputPath by remember { mutableStateOf("原路径") }
    var isUsingSourcePath by remember { mutableStateOf(true) }
    var textPos by remember { mutableStateOf(TextPos.LEFT_BOTTOM) }
    val textColorFilter = remember { FilterColorHex(defaultValue = TextFieldValue("#FFCCCCCC")) }
    val textSizeFilter = remember { FilterNumber(minValue = 1.0, decimalNumber = 0, defaultValue = TextFieldValue("80")) }
    var dateFormat by remember { mutableStateOf("yyyy.MM.dd HH:mm:ss") }
    val timeZoneFilter = remember { FilterGMT(TextFieldValue("GMT+8:00")) }
    val outputQualityTextFilter = remember { FilterNumber(minValue = 0.0, maxValue = 1.0, defaultValue = TextFieldValue("0.7")) }

    Card(
        modifier = Modifier.size(CardSize).padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
        backgroundColor = CardColor
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("输出路径：")
                OutlinedTextField(
                    value = outputPath,
                    onValueChange = { outputPath = it },
                    modifier = Modifier.width(CardSize.width / 3),
                    enabled = !isUsingSourcePath
                )
                Button(
                    onClick = {
                        showFileSelector(
                            isMultiSelection = false,
                            selectionMode = JFileChooser.DIRECTORIES_ONLY,
                            selectionFileFilter = null
                        ) {
                            outputPath = it[0].absolutePath
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp),
                    enabled = !isUsingSourcePath
                ) {
                    Text("选择")
                }
                Checkbox(
                    checked = isUsingSourcePath,
                    onCheckedChange = {
                        isUsingSourcePath = it
                        outputPath = if (it) "原路径" else ""
                    }
                )
                Text("输出至原路径", fontSize = 12.sp)
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("导出图像质量（0.0-1.0）：")
                OutlinedTextField(
                    value = outputQualityTextFilter.getInputValue(),
                    onValueChange = outputQualityTextFilter.onValueChange(),
                    modifier = Modifier.width(CardSize.width / 4)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 18.dp)
            ) {
                Text("水印位置：")

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(textPos == TextPos.LEFT_TOP, { textPos = TextPos.LEFT_TOP })
                        Text("左上角")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(textPos == TextPos.LEFT_BOTTOM, { textPos = TextPos.LEFT_BOTTOM })
                        Text("左下角")
                    }
                }

                Column {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(textPos == TextPos.RIGHT_TOP, { textPos = TextPos.RIGHT_TOP })
                        Text("右上角")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(textPos == TextPos.RIGHT_BOTTOM, { textPos = TextPos.RIGHT_BOTTOM })
                        Text("右下角")
                    }

                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("水印设置：")
                Column {
                    OutlinedTextField(
                        value = textColorFilter.getInputValue(),
                        onValueChange = textColorFilter.onValueChange(),
                        label = {
                            Text("文字颜色")
                        }
                    )
                    OutlinedTextField(
                        value = textSizeFilter.getInputValue(),
                        onValueChange = textSizeFilter.onValueChange(),
                        label = {
                            Text("文字尺寸")
                        }
                    )
                    OutlinedTextField(
                        value = dateFormat,
                        onValueChange = { dateFormat = it },
                        label = {
                            Text("日期格式")
                        }
                    )
                    OutlinedTextField(
                        value = timeZoneFilter.getInputValue(),
                        onValueChange = timeZoneFilter.onValueChange(),
                        label = {
                            Text("时区")
                        }
                    )
                }
            }

            Button(
                onClick = {
                    onStart(
                        outputPath,
                        isUsingSourcePath,
                        textPos,
                        textColorFilter.getInputValue().text,
                        textSizeFilter.getInputValue().text,
                        dateFormat,
                        timeZoneFilter.getInputValue().text,
                        outputQualityTextFilter.getInputValue().text
                    )
                          },
                modifier = Modifier.padding(top = 8.dp),
                enabled = enabled
            ) {
                Text("开始")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageContent(
    onclick: () -> Unit,
    onDel: (index: Int) -> Unit,
    fileList: List<File> = emptyList()
) {
    var showImageIndex by remember { mutableStateOf(0) }

    Card(
        onClick = onclick,
        modifier = Modifier.size(CardSize).padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
        backgroundColor = CardColor,
        enabled = fileList.isEmpty()
    ) {
        if (fileList.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "请点击选择文件（夹）或拖拽文件（夹）至此\n仅支持 ${legalSuffixList.contentToString()}",
                    textAlign = TextAlign.Center
                )
            }
        }
        else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    bitmap = fileList[showImageIndex.coerceAtMost(fileList.lastIndex)].inputStream().buffered().use(::loadImageBitmap),
                    contentDescription = null,
                    modifier = Modifier.height(CardSize.height / 2).fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Button(onClick = onclick ) {
                                Text("添加")
                            }
                            Button(onClick = { onDel(-1) }) {
                                Text("清空")
                            }
                        }

                    }

                    itemsIndexed(fileList) {index: Int, item: File ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                item.absolutePath,
                                modifier = Modifier.clickable {
                                    showImageIndex = index
                                }.weight(0.9f),
                            )

                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    onDel(index)
                                }.weight(0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}