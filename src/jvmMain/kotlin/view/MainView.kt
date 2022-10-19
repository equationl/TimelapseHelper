package view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ui.BackgroundColor
import ui.CardColor
import ui.CardSize
import utils.AddText
import utils.TextPos
import view.widget.dropFileTarget
import view.widget.showFileSelector
import java.io.File
import java.util.*


@Composable
fun MainPager(window: ComposeWindow) {
    val fileList = mutableStateListOf<File>()
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        // 拖拽文件
        window.contentPane.dropTarget = dropFileTarget {
            val newFile = mutableListOf<File>()
            it.map {path ->
                // TODO 记得做文件过滤，以及如果是文件夹的话遍历文件夹
                newFile.add(File(path))
            }

            fileList.addAll(newFile)
        }

        onDispose {  }
    }

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
                            // TODO 记得做文件过滤，以及如果是文件夹的话遍历文件夹

                            fileList.addAll(it)
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
                onStart = { outputPath: String, isUsingSourcePath: Boolean, textPos: TextPos, textColor: String, textSize: String, dateFormat: String ->
                    coroutineScope.launch {
                        AddText.startAdd(outputPath, isUsingSourcePath, textPos, textColor, textSize, dateFormat, fileList)
                    }
                }
            )

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
        dateFormat: String
    ) -> Unit
) {
    var outputPath by remember { mutableStateOf("") }
    var isUsingSourcePath by remember { mutableStateOf(true) }
    var textPos by remember { mutableStateOf(TextPos.Left_Bottom) }
    var textColor by remember { mutableStateOf("#FF000000") }
    var textSize by remember { mutableStateOf("80") }
    var dateFormat by remember { mutableStateOf("yyyy:HH:dd hh:mm:ss") }


    Card(
        modifier = Modifier.size(CardSize).padding(vertical = 32.dp),
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
                    enabled = !isUsingSourcePath             )
                Button(
                    onClick = { /*TODO*/ },
                    modifier = Modifier.padding(start = 8.dp)
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 18.dp)
            ) {
                Text("水印位置：")

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(textPos == TextPos.Left_Top, { textPos = TextPos.Left_Top })
                        Text("左上角")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(textPos == TextPos.Left_Bottom, { textPos = TextPos.Left_Bottom })
                        Text("左下角")
                    }
                }

                Column {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(textPos == TextPos.Right_Top, { textPos = TextPos.Right_Top })
                        Text("右上角")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(textPos == TextPos.Right_Bottom, { textPos = TextPos.Right_Bottom })
                        Text("右下角")
                    }

                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("水印设置：")
                Column {
                    OutlinedTextField(
                        value = textColor,
                        onValueChange = { textColor = it},
                        label = {
                            Text("文字颜色")
                        }
                    )
                    OutlinedTextField(
                        value = textSize,
                        onValueChange = { textSize = it },
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
                }
            }

            Button(
                onClick = {
                    onStart(outputPath, isUsingSourcePath, textPos, textColor, textSize, dateFormat)
                          },
                modifier = Modifier.padding(top = 8.dp)
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
                Text(text = "请点击选择文件（夹）或拖拽文件（夹）至此")
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