package utils

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifSubIFDDirectory
import constant.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import view.widget.PictureModel
import java.awt.image.BufferedImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO

object AddText {
    suspend fun startAdd(
        outputPath: String,
        isUsingSourcePath: Boolean,
        textPos: TextPos,
        textColor: String,
        textSize: String,
        dateFormat: String,
        fileList: List<PictureModel>,
        timeZone: String,
        outputQualityText: String,
        onProgress: (msg: String) -> Unit
    ): Result<AddTextResult> {
        val failFileList = mutableListOf<String>()
        val successFileList = mutableListOf<PictureModel>()

        onProgress("检查参数")
        val checkValueResult = withContext(Dispatchers.IO) {
            checkValue(outputPath, textColor, textSize, dateFormat, isUsingSourcePath, outputQualityText)
        }

        if (checkValueResult.isFailure) return Result.failure(checkValueResult.exceptionOrNull()?: AddTextException())

        onProgress("开始处理")

        withContext(Dispatchers.IO) {
            fileList.forEachIndexed { index, file ->
                onProgress("正在处理第 ${index+1} 张图片")
                val result = addWatermark(file, outputPath, isUsingSourcePath, textPos, textColor, textSize, dateFormat, timeZone, outputQualityText)
                if (result.isFailure) {
                    failFileList.add("$file : ${result.exceptionOrNull()}")
                    onProgress("第 ${index+1} 张处理失败：${result.exceptionOrNull()}")
                    result.exceptionOrNull()?.printStackTrace()
                }
                else {
                    successFileList.add(result.getOrNull()!!)
                }
            }
        }

        return Result.success(AddTextResult(failFileList, successFileList))
    }
}

private fun addWatermark(
    file: PictureModel,
    outputPath: String,
    isUsingSourcePath: Boolean,
    textPos: TextPos,
    textColor: String,
    textSize: String,
    dateFormat: String,
    timeZone: String,
    outputQualityText: String
): Result<PictureModel> {

    var date = file.date

    if (timeZone != Constant.DefaultTimeZone) { // 只有时区变了才需要重新获取时间
        date = getDateFromExif(file.file, timeZone) ?: return Result.failure(AddTextException("获取时间戳失败！"))
    }

    if (date == null) {
        return Result.failure(AddTextException("获取时间戳失败！"))
    }

    val dateString = getDateString(date, dateFormat) ?: return Result.failure(AddTextException("转换时间失败！"))

    val saveFile = if (isUsingSourcePath) file.file.getUniqueFile() else File(outputPath).getUniqueFile(file.file)

    addTextToJpg(file.file, saveFile, textColor, textSize, textPos, dateString, outputQualityText).fold(
        onSuccess = {
            return Result.success(PictureModel(it, date, file.resolution))
        },
        onFailure = {
            return Result.failure(it)
        }
    )
}

private fun addTextToJpg(
    file: File,
    outputFile: File,
    textColor: String,
    textSize: String,
    textPos: TextPos,
    text: String,
    outputQualityText: String
): Result<File> {
    val color = textColor.toAwtColor ?: return Result.failure(AddTextException("水印文字颜色错误"))
    val size = try {
        textSize.toInt()
    } catch (tr: Throwable) {
        return Result.failure(AddTextException("水印文字尺寸错误"))
    }
    val outputQuality = try {
        outputQualityText.toFloat()
    } catch (tr: Throwable) {
        return Result.failure(AddTextException("压缩质量错误"))
    }

    return try {
        val image: BufferedImage = ImageIO.read(file)
        addTextWaterMark(image, color, size, text, outputFile, textPos, outputQuality)
        Result.success(outputFile)
    } catch (tr: Throwable) {
        Result.failure(tr)
    }
}

private fun File.getUniqueFile(sourceFile: File = File("")): File {
    var newFile = this

    if (newFile.isDirectory) {
        newFile = File(newFile, sourceFile.name)
    }

    var index = 1
    while (newFile.exists()) {
        newFile = File(newFile.parentFile, "${newFile.nameWithoutExtension}($index).${newFile.extension}")
        index++
    }

    return newFile
}

private fun getDateFromExif(
    file: File,
    timeZoneID: String
): Date? {
    return try {
        val timeZone = TimeZone.getTimeZone(timeZoneID)
        val metadata = ImageMetadataReader.readMetadata(file)
        val directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory::class.java)
        directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, timeZone)
    } catch (tr: Throwable) {
        tr.printStackTrace()
        null
    }
}

private fun checkValue(
    outputPath: String,
    textColor: String,
    textSize: String,
    dateFormat: String,
    isUsingSourcePath: Boolean,
    outputQualityText: String
): Result<Boolean> {
    if (!isUsingSourcePath) {
        val outFile = File(outputPath)
        if (!outFile.isDirectory || !outFile.isAbsolute) {
            return Result.failure(AddTextException("参数错误：输出路径不可用"))
        }
    }

    try {
        textColor.toAwtColor ?: return Result.failure(AddTextException("参数错误：水印文字颜色错误"))
    } catch (tr: Throwable) {
        tr.printStackTrace()
        return Result.failure(AddTextException("参数错误：水印文字颜色错误"))
    }

    try {
        textSize.toInt()
    } catch (tr: Throwable) {
        tr.printStackTrace()
        return Result.failure(AddTextException("参数错误：水印文字尺寸错误"))
    }

    try {
        outputQualityText.toFloat()
    } catch (tr: Throwable) {
        tr.printStackTrace()
        return Result.failure(AddTextException("参数错误：导出质量错误"))
    }

    try {
        val simpleDateFormat = SimpleDateFormat(dateFormat)
        simpleDateFormat.format(Date())
    } catch (tr: Throwable) {
        tr.printStackTrace()
        return Result.failure(AddTextException("参数错误：水印日期格式错误"))
    }


    return Result.success(true)
}

data class AddTextResult(
    val failFileList: List<String>,
    val successFile: List<PictureModel>,
)

class AddTextException(msg: String = "No Msg"): Exception(msg)
