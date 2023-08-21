package view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import constant.Constant
import kotlinx.coroutines.launch
import state.ApplicationState
import state.ImgPreviewState
import ui.CardColor
import utils.getDateString
import view.widget.legalSuffixList

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ImageContent(
    applicationState: ApplicationState,
    modifier: Modifier
) {

    val state = applicationState.imgPreviewState
    state.lazyListState = rememberLazyListState()

    Card(
        onClick = {
            applicationState.onClickImgChoose()
        },
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
        backgroundColor = CardColor,
        enabled = applicationState.fileList.isEmpty()
    ) {
        if (applicationState.fileList.isEmpty()) {
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
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    Image(
                        bitmap = applicationState.fileList[state.showImageIndex.coerceAtMost(applicationState.fileList.lastIndex)].file.inputStream().buffered()
                            .use(::loadImageBitmap),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                applicationState.showPicture(applicationState.fileList[state.showImageIndex.coerceAtMost(applicationState.fileList.lastIndex)].file)
                            },
                        contentScale = ContentScale.Fit
                    )

                    this@Column.AnimatedVisibility(
                        visible = applicationState.fileList.isNotEmpty(),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        IconButton(
                            onClick = {
                                applicationState.imageShowModel = ApplicationState.ImgShowModel.Grid
                            }
                        ) {
                            Icon(
                                Icons.Outlined.ViewModule,
                                null,
                                tint = MaterialTheme.colors.primaryVariant
                            )
                        }
                    }
                }


                Row(
                    modifier = Modifier.fillMaxSize().weight(0.15f),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        applicationState.onClickImgChoose()
                    }) {
                        Text("添加")
                    }

                    Text("${state.showImageIndex + 1}/${applicationState.fileList.size}")

                    Button(onClick = { applicationState.onDelImg(-1) }) {
                        Text("清空")
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    var lastTitle = remember { "" }
                    LazyColumn(
                        state = state.lazyListState,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.height(IntrinsicSize.Min)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp).clickable {
                                        when (state.sortType) {
                                            ImgPreviewState.ImgSortType.TimeAsc -> {
                                                state.sortType = ImgPreviewState.ImgSortType.TimeDesc
                                            }
                                            ImgPreviewState.ImgSortType.TimeDesc -> {
                                                state.sortType = ImgPreviewState.ImgSortType.TimeAsc
                                            }
                                            else -> {
                                                state.sortType = ImgPreviewState.ImgSortType.TimeAsc
                                            }
                                        }

                                        applicationState.scope.launch {
                                            applicationState.reSortFileList()
                                        }
                                    }
                                ) {
                                    Text("时间", color = if (state.sortType == ImgPreviewState.ImgSortType.TimeDesc || state.sortType == ImgPreviewState.ImgSortType.TimeAsc) MaterialTheme.colors.primary else Color.Unspecified)
                                    Icon(if (state.sortType.isAsc) Icons.Outlined.ArrowDropUp else Icons.Outlined.ArrowDropDown, null, tint = if (state.sortType == ImgPreviewState.ImgSortType.TimeDesc || state.sortType == ImgPreviewState.ImgSortType.TimeAsc) MaterialTheme.colors.primary else Color.Unspecified)
                                }

                                Divider(
                                    Modifier.fillMaxHeight().width(1.dp)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp).clickable {
                                        when (state.sortType) {
                                            ImgPreviewState.ImgSortType.NameAsc -> {
                                                state.sortType = ImgPreviewState.ImgSortType.NameDesc
                                            }
                                            ImgPreviewState.ImgSortType.NameDesc -> {
                                                state.sortType = ImgPreviewState.ImgSortType.NameAsc
                                            }
                                            else -> {
                                                state.sortType = ImgPreviewState.ImgSortType.NameAsc
                                            }
                                        }

                                        applicationState.scope.launch {
                                            applicationState.reSortFileList()
                                        }
                                    }
                                ) {
                                    Text("名称", color = if (state.sortType == ImgPreviewState.ImgSortType.NameDesc || state.sortType == ImgPreviewState.ImgSortType.NameAsc) MaterialTheme.colors.primary else Color.Unspecified)
                                    Icon(if (state.sortType.isAsc) Icons.Outlined.ArrowDropUp else Icons.Outlined.ArrowDropDown, null, tint = if (state.sortType == ImgPreviewState.ImgSortType.NameDesc || state.sortType == ImgPreviewState.ImgSortType.NameAsc) MaterialTheme.colors.primary else Color.Unspecified)
                                }
                            }
                        }

                        applicationState.fileList.forEachIndexed { index, pictureModel ->
                            if (lastTitle != pictureModel.file.parent) {
                                stickyHeader {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Surface {
                                            Text(pictureModel.file.parent)
                                        }
                                    }
                                }
                                lastTitle = pictureModel.file.parent
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().then(
                                        if (state.showImageIndex == index) Modifier.background(MaterialTheme.colors.secondary)
                                        else {
                                            if (index % 2 == 0) {
                                                Modifier.background(MaterialTheme.colors.secondaryVariant)
                                            }
                                            else {
                                                Modifier
                                            }
                                        }
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "${index+1}. [${pictureModel.date?.let { getDateString(it, Constant.DefaultDateFormat) }}, ${pictureModel.resolution?.let { "${it.height}x${it.width}" }}] ${pictureModel.file.name}",
                                        modifier = Modifier.clickable {
                                            state.showImageIndex = index
                                        }.weight(0.9f),
                                        color = if (state.showImageIndex == index) MaterialTheme.colors.onSecondary else Color.Unspecified
                                    )

                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.clickable {
                                            applicationState.onDelImg(index)
                                        }.weight(0.1f)
                                    )
                                }
                            }
                        }
                    }

                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(
                            scrollState = state.lazyListState
                        )
                    )
                }
            }
        }
    }
}