package view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import state.ControlState
import ui.CardColor
import utils.TextPos
import view.widget.ToolTip
import view.widget.showFileSelector
import javax.swing.JFileChooser

@Composable
fun ControlContent(
    applicationState: ApplicationState,
    modifier: Modifier
) {
    val state = applicationState.controlState
    val scrollState = rememberScrollState()

    Box(modifier) {
        Card(
            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState),
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("输出路径：")
                    OutlinedTextField(
                        value = state.outputPath,
                        onValueChange = { state.outputPath = it },
                        enabled = !state.isUsingSourcePath,
                        modifier = Modifier.fillMaxWidth(0.5f)
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
                        Text("FFmpeg 可执行文件来源：")

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.height(IntrinsicSize.Min)
                        ) {

                            RadioButton(
                                selected = state.ffmpegFrom == ControlState.FFmpegFrom.Bundle,
                                onClick = {
                                    state.ffmpegFrom = ControlState.FFmpegFrom.Bundle
                                }
                            )

                            ToolTip("使用软件已打包的 FFmpeg 文件，并非所有系统都支持，如果不可用请切换其他选项使用。") {
                                Text("使用软件自带", fontSize = 12.sp, modifier = Modifier.clickable {
                                    state.ffmpegFrom = ControlState.FFmpegFrom.Bundle
                                })
                            }

                            Divider(
                                modifier = Modifier.fillMaxHeight().padding(start = 24.dp).width(1.dp)
                            )

                            RadioButton(
                                selected = state.ffmpegFrom == ControlState.FFmpegFrom.System,
                                onClick = {
                                    state.ffmpegFrom = ControlState.FFmpegFrom.System
                                }
                            )

                            ToolTip("使用系统默认表示您已在当前系统 PATH 中添加 ffmpeg 路径， 可直接通过 “ffmpeg” 命令调用") {
                                Text("使用系统默认", fontSize = 12.sp, modifier = Modifier.clickable {
                                    state.ffmpegFrom = ControlState.FFmpegFrom.System
                                })
                            }

                            Divider(
                                modifier = Modifier.fillMaxHeight().padding(start = 24.dp).width(1.dp)
                            )

                            RadioButton(
                                selected = state.ffmpegFrom == ControlState.FFmpegFrom.Customize,
                                onClick = {
                                    state.ffmpegFrom = ControlState.FFmpegFrom.Customize
                                }
                            )

                            ToolTip("自定义 FFmpeg 可执行文件路径") {
                                Text("自定义", fontSize = 12.sp, modifier = Modifier.clickable {
                                    state.ffmpegFrom = ControlState.FFmpegFrom.Customize
                                })
                            }
                        }

                        AnimatedVisibility(
                            visible = state.ffmpegFrom == ControlState.FFmpegFrom.Customize
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("FFmpeg 路径：")
                                ToolTip("填写或选择已下载的 FFmpeg 可执行文件，在 Windows 上通常为 “ffmpeg.exe” ； 在 Linux 和 MacOs 上通常为 “ffmpeg”") {
                                    OutlinedTextField(
                                        value = state.ffmpegPath,
                                        onValueChange = { state.ffmpegPath = it },
                                    )
                                }

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
                                ) {
                                    Text("选择")
                                }
                            }
                        }

                        Button(
                            onClick = {
                                applicationState.changeDialogText("正在测试 FFmpeg 是否可用……")
                                applicationState.scope.launch(Dispatchers.IO) {
                                    try {
                                        val cmd = state.getFfmpegBinaryPath()
                                        val output = ProcessExecutor().command(cmd, "-version")
                                            .readOutput(true)
                                            .exitValues(0)
                                            .execute()
                                            .outputUTF8()
                                        applicationState.changeDialogText("FFmpeg 正常！\n\n $output")
                                    } catch (e: InvalidExitValueException) {
                                        println("Process exited with ${e.exitValue}")
                                        val output = e.result.outputUTF8()
                                        applicationState.changeDialogText("FFmpeg 不可用！\n\n $output")
                                    } catch (tr: Throwable) {
                                        println("Process exited with ${tr.stackTraceToString()}")
                                        applicationState.changeDialogText("FFmpeg 不可用！\n\n ${tr.stackTraceToString()}")
                                    }
                                }
                            },
                        ) {
                            Text("测试 FFmpeg")
                        }

                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("每张图片持续时间（s）：")

                            OutlinedTextField(
                                value = state.pictureKeepTime.getInputValue(),
                                onValueChange = state.pictureKeepTime.onValueChange(),
                                modifier = Modifier.fillMaxWidth(0.6f)
                            )

                            ToolTip("将持续时间以倒数的形式设置，这有助于计算帧率，例如勾选“倒数”后，持续时间填写 25，帧率填写 25，则表示每张图片持续一帧") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        state.isReciprocalPictureKeepTime = !state.isReciprocalPictureKeepTime
                                        state.pictureKeepTime.setValue((1.0 / (state.pictureKeepTime.getInputValue().text.toDoubleOrNull() ?: 1.0)).toString())
                                    }
                                ) {
                                    Checkbox(
                                        checked = state.isReciprocalPictureKeepTime,
                                        onCheckedChange = {
                                            state.isReciprocalPictureKeepTime = it
                                            state.pictureKeepTime.setValue((1.0 / (state.pictureKeepTime.getInputValue().text.toDoubleOrNull() ?: 1.0)).toString())
                                        }
                                    )
                                    Text("倒数", fontSize = 12.sp)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("视频帧率：")
                            OutlinedTextField(
                                value = state.videoRate.getInputValue(),
                                onValueChange = state.videoRate.onValueChange(),
                            )
                        }

                        Text("视频编码")
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.height(IntrinsicSize.Min)
                        ) {

                            RadioButton(
                                selected = state.videoCode == ControlState.VideoCode.X264,
                                onClick = {
                                    state.videoCode = ControlState.VideoCode.X264
                                }
                            )

                            ToolTip("体积、质量不如 X265，但是几乎兼容所有播放器") {
                                Text("x264", fontSize = 12.sp, modifier = Modifier.clickable {
                                    state.videoCode = ControlState.VideoCode.X264
                                })
                            }

                            Divider(
                                modifier = Modifier.fillMaxHeight().padding(start = 24.dp).width(1.dp)
                            )

                            RadioButton(
                                selected = state.videoCode == ControlState.VideoCode.X265,
                                onClick = {
                                    state.videoCode = ControlState.VideoCode.X265
                                }
                            )

                            ToolTip("体积更小、质量更高，但是部分播放器可能不支持") {
                                Text("x265", fontSize = 12.sp, modifier = Modifier.clickable {
                                    state.videoCode = ControlState.VideoCode.X265
                                })
                            }

                        }

                        Text("分辨率(宽x高)：")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = state.videoWidth.getInputValue(),
                                onValueChange = state.videoWidth.onValueChange(),
                                enabled = !state.isUsingDefaultVideoSize,
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                            Text("X")
                            OutlinedTextField(
                                value = state.videoHeight.getInputValue(),
                                onValueChange = state.videoHeight.onValueChange(),
                                enabled = !state.isUsingDefaultVideoSize,
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                Checkbox(
                                    checked = state.isUsingDefaultVideoSize,
                                    onCheckedChange = {
                                        state.isUsingDefaultVideoSize = it
                                    },
                                )
                                ToolTip("将会直接使用第一张图片的分辨率") {
                                    Text("默认", fontSize = 12.sp)
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = state.isOnlyGenerateVideoWithAddText,
                                onCheckedChange = {
                                    state.isOnlyGenerateVideoWithAddText = it
                                },
                                enabled = state.isAddTimeText
                            )
                            ToolTip("当同时添加水印和生成视频时该选项才可用；如果勾选该选项，则合成视频时不会合成添加水印失败的图片") {
                                Text("仅合成成功添加水印的图片", fontSize = 12.sp)
                            }
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
                        enabled = applicationState.fileList.isNotEmpty() && state.outputPath.isNotBlank() && (state.isAddTimeText || state.isGenerateVideo)
                    ) {
                        Text("开始")
                    }
                }
            }
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd)
                .fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState)
        )
    }
}