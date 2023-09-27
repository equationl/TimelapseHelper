package utils

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter

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
    outPath: File,
    textPos: TextPos,
    outputQuality: Float
) {
    try {
        val width: Int = targetImg.width //ͼƬ��
        val height: Int = targetImg.height //ͼƬ��

        val graphics: Graphics2D = targetImg.createGraphics()
        graphics.color = textColor //ˮӡ��ɫ
        graphics.font = Font(null, Font.PLAIN, fontSize)

        // ˮӡ����λ��
        val textWidth = graphics.fontMetrics.stringWidth(text)
        val textHeight = graphics.fontMetrics.height
        val point = textPos.getPoint(width, height, textWidth, textHeight)
        val x = point.x
        val y = point.y

        graphics.drawString(text, x, y)

        // д�������
        saveImage(targetImg, outPath, outputQuality)
        graphics.dispose()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Throws(IOException::class)
fun saveImage(image: BufferedImage?, saveFile: File?, quality: Float) {
    val outputStream = ImageIO.createImageOutputStream(saveFile)
    val jpgWriter: ImageWriter = ImageIO.getImageWritersByFormatName("jpg").next()
    val jpgWriteParam: ImageWriteParam = jpgWriter.defaultWriteParam
    jpgWriteParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
    jpgWriteParam.compressionQuality = quality
    jpgWriter.output = outputStream
    val outputImage = IIOImage(image, null, null)
    jpgWriter.write(null, outputImage, jpgWriteParam)
    jpgWriter.dispose()
    outputStream.flush()
    outputStream.close()
}

fun getDateString(date: Date, dateFormat: String): String? {
    return try {
        val simpleDateFormat = SimpleDateFormat(dateFormat)
        simpleDateFormat.format(date)
    } catch (tr: Throwable) {
        null
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

fun String.md5(): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(32, '0')
}