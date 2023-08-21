package utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.LogOutputStream
import view.widget.PictureModel
import java.io.File

object Picture2Video {
    private const val PictureListFileName = "pictureList.txt"
    private const val GenerateVideoFileName = "timelapse_video_"


    suspend fun picture2Video(
        fileList: List<PictureModel>,
        savePath: File,
        ffmpegRunnable: String,
        pictureKeepTime: Double,
        videoRate: Int,
        videoCode: String,
        videoSize: String,
        onProgress: (msg: String) -> Unit,
        onResult: (result: Result<String>) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            onProgress("正在生成列表文件")
            val pictureListFile = createPictureListFile(savePath, fileList, pictureKeepTime)

            onProgress("开始创建视频")

            val videoFile = runPicture2Video(
                File(savePath, "${GenerateVideoFileName}${System.currentTimeMillis()}.mp4"),
                pictureListFile,
                ffmpegRunnable,
                videoRate,
                videoCode,
                videoSize,
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
        fileList: List<PictureModel>,
        pictureKeepTime: Double
    ): File {
        val pictureListFile = File(savePath, PictureListFileName)
        if (!pictureListFile.exists()) {
            pictureListFile.createNewFile()
        }
        else {
            pictureListFile.writeText("")
        }

        for (file in fileList) {
            pictureListFile.appendText("file '${file.file.absolutePath}'\nduration $pictureKeepTime\n")
        }

        return pictureListFile
    }

    private fun runPicture2Video(
        outputFile: File,
        pictureListFile: File,
        ffmpegRunnable: String,
        videoRate: Int,
        videoCode: String,
        videoSize: String,
        onProgress: (msg: String) -> Unit
    ): File {
        val cmd: MutableList<String> = mutableListOf()
        cmd.add(ffmpegRunnable)
        cmd.addAll("-y -f concat -safe 0 -i".split(" "))
        cmd.add(pictureListFile.absolutePath)
        // 似乎是这个文件的编码有问题，在 macOS 上识别不到转义的 " ，所以这里索性分开写了
        cmd.addAll("-r $videoRate -c:v $videoCode -vf format=yuv420p".split(" "))
        // see: https://stackoverflow.com/questions/25891342/creating-a-video-from-a-single-image-for-a-specific-duration-in-ffmpeg
        cmd.addAll("-vf scale=${videoSize}:force_original_aspect_ratio=decrease:eval=frame,pad=${videoSize}:-1:-1:color=black".split(" "))
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