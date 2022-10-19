package utils

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.FileOutputStream
import javax.imageio.ImageIO

enum class TextPos {
    Left_Top,
    Left_Bottom,
    Right_Top,
    Right_Bottom
}

fun addTextWaterMark(
    targetImg: BufferedImage,
    textColor: Color,
    fontSize: Int,
    text: String,
    outPath: String
) {
    try {
        val width: Int = targetImg.width //ͼƬ��
        val height: Int = targetImg.height //ͼƬ��

        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_BGR)

        val graphics: Graphics2D = bufferedImage.createGraphics()
        graphics.drawImage(targetImg, 0, 0, width, height, null)
        graphics.color = textColor //ˮӡ��ɫ
        graphics.font = Font(null, Font.PLAIN, fontSize)

        // ˮӡ���ݷ��������½�
        val x = width - (text.length + 1) * fontSize
        val y = height - fontSize * 2
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