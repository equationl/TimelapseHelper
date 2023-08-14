package view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.zeroturnaround.exec.InvalidExitValueException
import org.zeroturnaround.exec.ProcessExecutor
import state.ApplicationState
import ui.CardColor
import ui.CardSize
import utils.TextPos
import view.widget.showFileSelector
import javax.swing.JFileChooser

@Composable
fun ControlContent(
    applicationState: ApplicationState
) {
    val state = applicationState.controlState

    Card(
        modifier = Modifier.size(CardSize).padding(16.dp).verticalScroll(rememberScrollState()),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
        backgroundColor = CardColor
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            // horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("输出路径：")
                OutlinedTextField(
                    value = state.outputPath,
                    onValueChange = { state.outputPath = it },
                    modifier = Modifier.width(CardSize.width / 3),
                    enabled = !state.isUsingSourcePath
                )
                Button(
                    onClick = {
                        showFileSelector(
                            isMultiSelection = false,
                            selectionMode = JFileChooser.DIRECTORIES_ONLY,
                            selectionFileFilter = null
                        ) {
                            state.outputPath = it[0].absolutePath
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp),
                    enabled = !state.isUsingSourcePath
                ) {
                    Text("选择")
                }
                Checkbox(
                    checked = state.isUsingSourcePath,
                    onCheckedChange = {
                        state.isUsingSourcePath = it
                        state.outputPath = if (it) "原路径" else ""
                    }
                )
                Text("输出至原路径", fontSize = 12.sp)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = state.isAddTimeText,
                    onCheckedChange = {
                        state.isAddTimeText = it
                    }
                )

                Text(
                    "添加时间水印",
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.clickable {
                        state.isAddTimeText = !state.isAddTimeText
                    }
                )
            }

            AnimatedVisibility(state.isAddTimeText) {
                Column {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("导出图像质量（0.0-1.0）：")
                        OutlinedTextField(
                            value = state.outputQualityTextFilter.getInputValue(),
                            onValueChange = state.outputQualityTextFilter.onValueChange(),
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
                                RadioButton(state.textPos == TextPos.LEFT_TOP, { state.textPos = TextPos.LEFT_TOP })
                                Text("左上角")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(state.textPos == TextPos.LEFT_BOTTOM, { state.textPos = TextPos.LEFT_BOTTOM })
                                Text("左下角")
                            }
                        }

                        Column {

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(state.textPos == TextPos.RIGHT_TOP, { state.textPos = TextPos.RIGHT_TOP })
                                Text("右上角")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(state.textPos == TextPos.RIGHT_BOTTOM, { state.textPos = TextPos.RIGHT_BOTTOM })
                                Text("右下角")
                            }

                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("水印设置：")
                        Column {
                            OutlinedTextField(
                                value = state.textColorFilter.getInputValue(),
                                onValueChange = state.textColorFilter.onValueChange(),
                                label = {
                                    Text("文字颜色")
                                }
                            )
                            OutlinedTextField(
                                value = state.textSizeFilter.getInputValue(),
                                onValueChange = state.textSizeFilter.onValueChange(),
                                label = {
                                    Text("文字尺寸")
                                }
                            )
                            OutlinedTextField(
                                value = state.dateFormat,
                                onValueChange = { state.dateFormat = it },
                                label = {
                                    Text("日期格式")
                                }
                            )
                            OutlinedTextField(
                                value = state.timeZoneFilter.getInputValue(),
                                onValueChange = state.timeZoneFilter.onValueChange(),
                                label = {
                                    Text("时区")
                                }
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = state.isGenerateVideo,
                    onCheckedChange = {
                        state.isGenerateVideo = it
                    }
                )

                Text(
                    "合成视频",
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.clickable {
                        state.isGenerateVideo = !state.isGenerateVideo
                    }
                )
            }

            AnimatedVisibility(state.isGenerateVideo) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("FFmpeg 路径：")
                        OutlinedTextField(
                            value = state.ffmpegPath,
                            onValueChange = { state.ffmpegPath = it },
                            modifier = Modifier.width(CardSize.width / 3),
                            enabled = !state.isUsingSystemFFmpegPath
                        )
                        Button(
                            onClick = {
                                showFileSelector(
                                    isMultiSelection = false,
                                    selectionMode = JFileChooser.FILES_ONLY,
                                    selectionFileFilter = null
                                ) {
                                    state.ffmpegPath = it[0].absolutePath
                                }
                            },
                            modifier = Modifier.padding(start = 8.dp),
                            enabled = !state.isUsingSystemFFmpegPath
                        ) {
                            Text("选择")
                        }
                        Checkbox(
                            checked = state.isUsingSystemFFmpegPath,
                            onCheckedChange = {
                                state.isUsingSystemFFmpegPath = it
                                state.ffmpegPath = if (it) "跟随系统" else ""
                            }
                        )
                        Text("使用系统默认", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            applicationState.dialogText = "正在测试 FFmpeg 是否可用……"
                            applicationState.scope.launch(Dispatchers.IO) {
                                try {
                                    val cmd = if (state.isUsingSystemFFmpegPath) "ffmpeg" else state.ffmpegPath
                                    val output = ProcessExecutor().command(cmd, "-version")
                                        .readOutput(true)
                                        .exitValues(0)
                                        .execute()
                                        .outputUTF8()
                                    applicationState.dialogText = "FFmpeg 正常！\n\n $output"
                                } catch (e: InvalidExitValueException) {
                                    println("Process exited with ${e.exitValue}")
                                    val output = e.result.outputUTF8()
                                    applicationState.dialogText = "FFmpeg 不可用！\n\n $output"
                                } catch (tr: Throwable) {
                                    println("Process exited with ${tr.stackTraceToString()}")
                                    applicationState.dialogText = "FFmpeg 不可用！\n\n ${tr.stackTraceToString()}"
                                }
                            }
                        },
                        enabled = state.ffmpegPath.isNotBlank()) {
                        Text("测试 FFmpeg")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        applicationState.onStartProgress()
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    enabled = applicationState.fileList.isNotEmpty() && (state.isAddTimeText || state.isGenerateVideo)
                ) {
                    Text("开始")
                }
            }
        }
    }
}