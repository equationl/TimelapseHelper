package view

import androidx.compose.foundation.*
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.unit.dp
import state.ApplicationState
import ui.CardColor
import view.common.rememberLazyFGridScrollbarAdapter
import view.widget.ToolTip
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageGridContent(applicationState: ApplicationState) {
    val state = applicationState.imgPreviewState
    val lazyStaggeredGridState = rememberLazyStaggeredGridState()

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
                state = lazyStaggeredGridState
            ) {
                itemsIndexed(
                    applicationState.fileList,
                    key = {index: Int, item: File -> item.absolutePath }
                ) { index: Int, item: File ->
                    ToolTip(
                        tipText = item.absolutePath,
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .combinedClickable(
                                    onDoubleClick = {
                                        applicationState.showPicture(item)
                                    }
                                ) {
                                    // onclick
                                    applicationState.showPicture(item)
                                }
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    bitmap = item.inputStream().buffered()
                                        .use(::loadImageBitmap),
                                    contentDescription = null,
                                    modifier = Modifier,
                                    contentScale = ContentScale.Fit
                                )
                                Text(item.name)
                            }
                        }
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberLazyFGridScrollbarAdapter(
                    scrollState = lazyStaggeredGridState
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