import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.kotlin.dsl.support.unzipTo
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.equationl"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        buildTypes.release.proguard {
            configurationFiles.from(project.file("compose-desktop.pro"))
        }
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TimelapseHelper"
            packageVersion = "1.0.0"
            copyright = "© 2023 likehide.com. All rights reserved."
            vendor = "equationl"

            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))

            windows {
                menuGroup = "Likehide"
            }

            macOS {
                bundleID = "com.likehide.timelapseHelper"
            }
        }
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation("com.drewnoakes:metadata-extractor:2.18.0")
    implementation("org.zeroturnaround:zt-exec:1.12")
}

gradle.afterProject {
    downloadFFmpeg()
}

fun downloadFFmpeg() {
    println("downloadFFmpeg: Downloading and extracting ffmpeg binary file ...")

    if (!System.getProperty("os.arch").contains("64")) {
        println("downloadFFmpeg: Not support Current System arch(${System.getProperty("os.arch")}), You may need download for your system by yourself at: https://ffmpeg.org/download.html, then copy to '<RESOURCES_ROOT_DIR>/<OS_NAME>', such as `resources/macos/`")
        return
    }

    val windowsDownloadLink = "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip"
    val macDownloadLink = "https://evermeet.cx/ffmpeg/ffmpeg-6.0.zip"

    val cachePath = file("cache")
    val windowsFile = "resources/windows/ffmpeg.exe"
    val macFile = "resources/macos/ffmpeg"

    val ffmpegFile = if (Os.isFamily(Os.FAMILY_WINDOWS)) file(windowsFile) else file(macFile)

    println("downloadFFmpeg: Check $ffmpegFile ...")

    if (!ffmpegFile.exists()) {
        println("downloadFFmpeg: '$ffmpegFile' not exist, start downloading...")

        if (!cachePath.exists()) {
            mkdir(cachePath)
        }

        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            executeDownload(windowsDownloadLink, cachePath.resolve("ffmpeg.zip"))
            unzipWindowsFFmpeg(cachePath.resolve("ffmpeg.zip"), file(windowsFile))
        }
        else if (Os.isFamily(Os.FAMILY_MAC)) {
            executeDownload(macDownloadLink, cachePath.resolve("ffmpeg.zip"))
            unzipMacFFmpeg(cachePath.resolve("ffmpeg.zip"), file(macDownloadLink))
        }
        else {
            println("downloadFFmpeg: Not support Current System, You may need download for your system by yourself at: https://ffmpeg.org/download.html, then copy to '<RESOURCES_ROOT_DIR>/<OS_NAME>', such as `resources/macos/ffmpeg`")
        }
    }
    else {
        println("downloadFFmpeg: '$ffmpegFile' already exist, all done!\nTip: if you want download again, just remove '$ffmpegFile' then build again.")
    }
}

fun executeDownload(link: String, cachePath: File) {
    println("downloadFFmpeg: Download from `$link` to `$cachePath` ...")

    ant.invokeMethod("get", mapOf("src" to link, "dest" to cachePath))

    println("downloadFFmpeg: `$cachePath` downloaded, start unzip...")
}

fun unzipWindowsFFmpeg(cacheFile: File, saveFile: File) {
    unzipTo(cacheFile.parentFile, cacheFile)

    println("downloadFFmpeg: unzip finish, start copy...")

    cacheFile.parentFile.resolve("ffmpeg-master-latest-win64-gpl/bin/ffmpeg.exe").copyTo(saveFile)

    println("downloadFFmpeg: copy finish! start remove cache...")

    cacheFile.parentFile.deleteRecursively()

    println("downloadFFmpeg: All done!")
}

fun unzipMacFFmpeg(cacheFile: File, saveFile: File) {
    unzipTo(cacheFile.parentFile, cacheFile)

    println("downloadFFmpeg: unzip finish, start copy...")

    cacheFile.parentFile.resolve("ffmpeg-6.0/ffmpeg").copyTo(saveFile)

    println("downloadFFmpeg: copy finish! start remove cache...")

    cacheFile.parentFile.deleteRecursively()

    println("downloadFFmpeg: All done!")
}