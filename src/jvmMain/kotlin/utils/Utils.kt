package utils

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.FileOutputStream
import javax.imageio.ImageIO

enum class TextPos {
    LEFT_TOP,
    LEFT_BOTTOM,
    RIGHT_TOP,
    RIGHT_BOTTOM
}

val String.toAwtColor: Color?
    get() {
        val a = substring(1, 3).toIntOrNull(16) ?: return null
        val r = substring(3, 5).toIntOrNull(16) ?: return null
        val g = substring(5, 7).toIntOrNull(16) ?: return null
        val b = substring(7, 9).toIntOrNull(16) ?: return null
        return Color(r, g, b, a)
    }

fun addTextWaterMark(
    targetImg: BufferedImage,
    textColor: Color,
    fontSize: Int,
    text: String,
    outPath: String,
    textPos: TextPos
) {
    try {
        val width: Int = targetImg.width //Í¼Æ¬¿í
        val height: Int = targetImg.height //Í¼Æ¬¸ß

        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_BGR)

        val graphics: Graphics2D = bufferedImage.createGraphics()
        graphics.drawImage(targetImg, 0, 0, width, height, null)
        graphics.color = textColor //Ë®Ó¡ÑÕÉ«
        graphics.font = Font(null, Font.PLAIN, fontSize)

        // Ë®Ó¡×ø±êÎ»ÖÃ
        val textWidth = graphics.fontMetrics.stringWidth(text)
        val textHeight = graphics.fontMetrics.height
        val point = textPos.getPoint(width, height, textWidth, textHeight)
        val x = point.x
        val y = point.y

        graphics.drawString(text, x, y)

        val outImgStream = FileOutputStream(outPath)
        ImageIO.write(bufferedImage, "jpg", outImgStream)
        outImgStream.flush()
        outImgStream.close()
        graphics.dispose()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun TextPos.getPoint(
    width: Int,
    height: Int,
    textWidth: Int,
    textHeight: Int,
    padding: Int = 10
): Point {
    return when (this) {
        TextPos.LEFT_TOP -> {
            Point(padding, textHeight)
        }
        TextPos.LEFT_BOTTOM -> {
            Point(
                padding,
                (height - padding).coerceAtLeast(0)
            )
        }
        TextPos.RIGHT_TOP -> {
            Point(
                (width - textWidth - padding).coerceAtLeast(0),
                textHeight
            )
        }
        TextPos.RIGHT_BOTTOM -> {
            Point(
                (width - textWidth - padding).coerceAtLeast(0),
                (height - padding).coerceAtLeast(0)
            )
        }
    }
}