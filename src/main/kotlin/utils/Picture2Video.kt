package utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.LogOutputStream
import java.io.File

object Picture2Video {
    private const val PictureListFileName = "pictureList.txt"
    private const val GenerateVideoFileName = "timelapse_video_"


    suspend fun orderFileListByTime(
        fileList: List<FileWithDate>
    ): List<File>  = withContext(Dispatchers.IO) {
        return@withContext fileList
            .sortedBy {
                it.date
            }
            .map { it.file }
    }

    suspend fun picture2Video(
        fileList: List<File>,
        savePath: File,
        ffmpegRunnable: String,
        pictureKeepTime: Double,
        videoRate: Int,
        onProgress: (msg: String) -> Unit,
        onResult: (result: Result<String>) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onProgress("正在生成列表文件")
            val pictureListFile = createPictureListFile(savePath, fileList)

            onProgress("开始创建视频")

            val videoFile = runPicture2Video(
                File(savePath, "${GenerateVideoFileName}${System.currentTimeMillis()}.mp4"),
                pictureListFile,
                ffmpegRunnable,
                pictureKeepTime,
                videoRate,
                onProgress
            )

            pictureListFile.delete()

            onResult(Result.success("生成完成，视频保存在 $videoFile \n"))

        } catch (tr: Throwable) {
            tr.printStackTrace()
            onResult(Result.failure(tr))
        }
    }

    private fun createPictureListFile(
        savePath: File,
        fileList: List<File>,
    ): File {
        val pictureListFile = File(savePath, PictureListFileName)
        if (!pictureListFile.exists()) {
            pictureListFile.createNewFile()
        }
        else {
            pictureListFile.writeText("")
        }

        for (file in fileList) {
            pictureListFile.appendText("file '${file.absolutePath}'\n")
        }

        return pictureListFile
    }

    private fun runPicture2Video(
        outputFile: File,
        pictureListFile: File,
        ffmpegRunnable: String,
        pictureKeepTime: Double,
        videoRate: Int,
        onProgress: (msg: String) -> Unit
    ): File {
        val cmd: MutableList<String> = mutableListOf()
        cmd.add(ffmpegRunnable)
        cmd.addAll("-y -r $pictureKeepTime -f concat -safe 0 -i".split(" "))
        cmd.add(pictureListFile.absolutePath)
        cmd.addAll("-c:v libx264 -vf \"fps=$videoRate,format=yuv420p\"".split(" "))
        cmd.add(outputFile.absolutePath)

        ProcessExecutor()
            .command(cmd)
            .redirectOutput(object : LogOutputStream() {
                override fun processLine(line: String?) {
                    onProgress("$line")
                }
            })
            .redirectError(object : LogOutputStream() {
                override fun processLine(line: String?) {
                    onProgress("$line")
                }
            })
            .exitValues(0)
            .execute()

        return outputFile
    }
}