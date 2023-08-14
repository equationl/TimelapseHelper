package view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import state.ApplicationState
import ui.CardColor
import ui.CardSize
import view.widget.legalSuffixList
import java.io.File

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageContent(
    applicationState: ApplicationState
) {

    val state = applicationState.imgPreviewState
    state.lazyListState = rememberLazyListState()

    Card(
        onClick = {
            applicationState.onClickImgChoose()
        },
        modifier = Modifier.size(CardSize).padding(16.dp),
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
                Image(
                    bitmap = applicationState.fileList[state.showImageIndex.coerceAtMost(applicationState.fileList.lastIndex)].inputStream().buffered()
                        .use(::loadImageBitmap),
                    contentDescription = null,
                    modifier = Modifier.height(CardSize.height / 2).fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )

                LazyColumn(
                    state = state.lazyListState,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            Button(onClick = {
                                applicationState.onClickImgChoose()
                            }) {
                                Text("添加")
                            }
                            Button(onClick = { applicationState.onDelImg(-1) }) {
                                Text("清空")
                            }
                        }

                    }

                    itemsIndexed(applicationState.fileList) { index: Int, item: File ->
                        Row(
                            modifier = Modifier.fillMaxWidth().then(
                                if (state.showImageIndex == index) Modifier.background(MaterialTheme.colors.secondary)
                                else Modifier
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                item.absolutePath,
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
        }
    }
}