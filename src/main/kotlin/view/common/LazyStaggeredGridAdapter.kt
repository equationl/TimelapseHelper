package view.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemInfo
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.v2.ScrollbarAdapter
import androidx.compose.foundation.v2.maxScrollOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun rememberLazyFGridScrollbarAdapter(
    scrollState: LazyStaggeredGridState,
): ScrollbarAdapter = remember(scrollState) {
    LazyStaggeredGridAdapter(scrollState)
}

@OptIn(ExperimentalFoundationApi::class)
class LazyStaggeredGridAdapter(
    private val scrollState: LazyStaggeredGridState
) : LazyLineContentAdapter() {
    override val lineSpacing: Int
        get() = scrollState.layoutInfo.mainAxisItemSpacing

    override val viewportSize: Double
        get() = with(scrollState.layoutInfo) {
            if (orientation == Orientation.Vertical)
                viewportSize.height
            else
                viewportSize.width
        }.toDouble()

    override fun firstVisibleLine(): VisibleLine? {
        return scrollState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index != -1 } // Skip exiting items
            ?.let { firstVisibleItem ->
                VisibleLine(
                    index = firstVisibleItem.index,
                    offset = firstVisibleItem.mainAxisOffset()
                )
            }
    }

    override fun totalLineCount() = scrollState.layoutInfo.totalItemsCount

    override fun contentPadding() = with(scrollState.layoutInfo){
        beforeContentPadding + afterContentPadding
    }

    override suspend fun snapToLine(lineIndex: Int, scrollOffset: Int) {
        scrollState.scrollToItem(lineIndex, scrollOffset)
    }

    override suspend fun scrollBy(value: Float) {
        scrollState.scrollBy(value)
    }

    override fun averageVisibleLineSize()= with(scrollState.layoutInfo.visibleItemsInfo){
        val firstFloatingIndex = 0
        val first = this[firstFloatingIndex]
        val last = last()
        val count = size - firstFloatingIndex
        (last.mainAxisOffset() + last.mainAxisSize() - first.mainAxisOffset() - (count-1)*lineSpacing).toDouble() / count
    }

    private val isVertical = scrollState.layoutInfo.orientation == Orientation.Vertical

    private fun LazyStaggeredGridItemInfo.mainAxisOffset() = with(offset) {
        if (isVertical) y else x
    }

    private fun LazyStaggeredGridItemInfo.mainAxisSize() = with(size) {
        if (isVertical) height else width
    }

}

abstract class LazyLineContentAdapter: ScrollbarAdapter{

    // Implement the adapter in terms of "lines", which means either rows,
    // (for a vertically scrollable widget) or columns (for a horizontally
    // scrollable one).
    // For LazyList this translates directly to items; for LazyGrid, it
    // translates to rows/columns of items.

    data class VisibleLine(
        val index: Int,
        val offset: Int
    )

    /**
     * Return the first visible line, if any.
     */
    protected abstract fun firstVisibleLine(): VisibleLine?

    /**
     * Return the total number of lines.
     */
    protected abstract fun totalLineCount(): Int

    /**
     * The sum of content padding (before+after) on the scrollable axis.
     */
    protected abstract fun contentPadding(): Int

    /**
     * Scroll immediately to the given line, and offset it by [scrollOffset] pixels.
     */
    protected abstract suspend fun snapToLine(lineIndex: Int, scrollOffset: Int)

    /**
     * Scroll from the current position by the given amount of pixels.
     */
    protected abstract suspend fun scrollBy(value: Float)

    /**
     * Return the average size (on the scrollable axis) of the visible lines.
     */
    protected abstract fun averageVisibleLineSize(): Double

    /**
     * The spacing between lines.
     */
    protected abstract val lineSpacing: Int

    private val averageVisibleLineSize by derivedStateOf {
        if (totalLineCount() == 0)
            0.0
        else
            averageVisibleLineSize()
    }

    private val averageVisibleLineSizeWithSpacing get() = averageVisibleLineSize + lineSpacing

    override val scrollOffset: Double
        get() {
            val firstVisibleLine = firstVisibleLine()
            return if (firstVisibleLine == null)
                0.0
            else {
                firstVisibleLine.index * averageVisibleLineSizeWithSpacing - firstVisibleLine.offset
            }
        }

    override val contentSize: Double
        get() {
            val totalLineCount = totalLineCount()
            return averageVisibleLineSize * totalLineCount +
                    lineSpacing * (totalLineCount - 1).coerceAtLeast(0) +
                    contentPadding()
        }

    override suspend fun scrollTo(scrollOffset: Double) {
        val distance = scrollOffset - this@LazyLineContentAdapter.scrollOffset

        // if we scroll less than viewport we need to use scrollBy function to avoid
        // undesirable scroll jumps (when an item size is different)
        //
        // if we scroll more than viewport we should immediately jump to this position
        // without recreating all items between the current and the new position
        if (abs(distance) <= viewportSize) {
            scrollBy(distance.toFloat())
        } else {
            snapTo(scrollOffset)
        }
    }

    private suspend fun snapTo(scrollOffset: Double) {
        val scrollOffsetCoerced = scrollOffset.coerceIn(0.0, maxScrollOffset)

        val index = (scrollOffsetCoerced / averageVisibleLineSizeWithSpacing)
            .toInt()
            .coerceAtLeast(0)
            .coerceAtMost(totalLineCount() - 1)

        val offset = (scrollOffsetCoerced - index * averageVisibleLineSizeWithSpacing)
            .toInt()
            .coerceAtLeast(0)

        snapToLine(lineIndex = index, scrollOffset = offset)
    }

}