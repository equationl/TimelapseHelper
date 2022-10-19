package utils

import java.io.File

object AddText {
    suspend fun startAdd(
        outputPath: String,
        isUsingSourcePath: Boolean,
        textPos: TextPos,
        textColor: String,
        textSize: String,
        dateFormat: String,
        fileList: List<File>
    ): Boolean {
        // TODO
        if (!checkValue(outputPath, isUsingSourcePath, textPos, textColor, textSize, dateFormat, fileList)) {
            return false
        }

        // ��ȡ exif
        /*val metadata = ImageMetadataReader.readMetadata(it[0])
        val directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
        val date: Date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
        println(date.toLocaleString())*/

        // д��ˮӡ
        /*val image: BufferedImage = ImageIO.read(it[0])
        addTextWaterMark(image, Color.LIGHT_GRAY, 80, "�����ı�ˮӡ", it[0].absolutePath+".jpg")*/

        return true
    }
}

private fun checkValue(
    outputPath: String,
    isUsingSourcePath: Boolean,
    textPos: TextPos,
    textColor: String,
    textSize: String,
    dateFormat: String,
    fileList: List<File>
): Boolean {

}
