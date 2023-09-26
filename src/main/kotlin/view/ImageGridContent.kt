package view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.defaultScrollbarStyle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TableRows
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import state.ApplicationState
import ui.CardColor
import view.common.rememberLazyFGridScrollbarAdapter
import view.widget.AsyncImage
import view.widget.PictureModel
import view.widget.ToolTip
import view.widget.loadImageBitmap

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageGridContent(applicationState: ApplicationState) {
    val state = applicationState.imgPreviewGridState
    state.lazyState = rememberLazyStaggeredGridState()

    Card(
        //modifier = Modifier.size(CardSize).padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
        backgroundColor = CardColor,
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(state.imgRowNum),
                modifier = Modifier.fillMaxSize(),
                state = state.lazyState
            ) {
                itemsIndexed(
                    applicationState.fileList,
                    key = {index: Int, item: PictureModel -> "$index - ${item.file.absolutePath}" }
                ) { index: Int, item: PictureModel ->
                    ToolTip(
                        tipText = item.file.absolutePath,
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .combinedClickable(
                                    onDoubleClick = {
                                        applicationState.showPicture(item.file)
                                    }
                                ) {
                                    // onclick
                                    applicationState.showPicture(item.file)
                                }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    load = { loadImageBitmap(item.file) },
                                    painterFor = { remember { BitmapPainter(it) } },
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit
                                )
                                Text("${index+1}. ${item.file.name}")
                            }
                        }
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberLazyFGridScrollbarAdapter(
                    scrollState = state.lazyState
                ),
                style = defaultScrollbarStyle().copy(
                    unhoverColor = Color.Black.copy(alpha = 0.50f),
                    hoverColor = Color.Black
                )
            )

            Row(
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                IconButton(
                    onClick = {
                        state.changeRowNum()
                    }
                ) {
                    Icon(
                        state.getRowIcon(state.imgRowNum),
                        null,
                        tint = MaterialTheme.colors.primaryVariant
                    )
                }

                IconButton(
                    onClick = {
                        applicationState.imageShowModel = ApplicationState.ImgShowModel.List
                    }
                ) {
                    Icon(
                        Icons.Outlined.TableRows,
                        null,
                        tint = MaterialTheme.colors.primaryVariant
                    )
                }
            }

        }
    }
}