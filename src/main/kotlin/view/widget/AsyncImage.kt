package view.widget

import androidx.compose.foundation.Image
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xml.sax.InputSource
import utils.md5
import utils.saveImage
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.net.URL
import javax.imageio.ImageIO


@Composable
fun <T> AsyncImage(
    load: suspend () -> T,
    painterFor: @Composable (T) -> Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val image: T? by produceState<T?>(null) {
        value = withContext(Dispatchers.IO) {
            try {
                load()
            } catch (e: IOException) {
                // instead of printing to console, you can also write this to log,
                // or show some error placeholder
                e.printStackTrace()
                null
            }
        }
    }

    if (image != null) {
        Image(
            painter = painterFor(image!!),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
    else {
        Icon(
            Icons.Outlined.Image,
            null
        )
    }
}

/* Loading from file with java.io API */

fun loadThumbnailBitmapPainter(file: File, scale: Float = 0.2f, minSize: IntSize = IntSize(128, 128)): Painter {
    val cachePainter = readFromCache(file)
    if (cachePainter != null) {
        return cachePainter
    }

    val image = ImageIO.read(file)

    val newHeight = (image.height * scale).toInt()
    val newWidth = (image.width * scale).toInt()

    if (newHeight <= minSize.height || newWidth <= minSize.width) return image.toPainter()

    val scaleImg = image.resizeImage(newWidth, newHeight)

    saveToCache(scaleImg, file)

    return scaleImg.toPainter()
}

private fun readFromCache(file: File): Painter? {
    try {
        val cacheFile = getCacheFile(file)
        if (cacheFile.exists()) {
            val image = ImageIO.read(cacheFile)
            return image.toPainter()
        }
        else {
            println("cache not exist!")
            return null
        }
    } catch (tr: Throwable) {
        tr.printStackTrace()
        return null
    }
}

private fun saveToCache(scaleImg: BufferedImage, file: File) {
    try {
        saveImage(scaleImg, getCacheFile(file), 0.7f)
    } catch (tr: Throwable) {
        tr.printStackTrace()
    }
}

private fun getCacheFile(file: File): File {
    return File(getCachePath(), "cache-${file.absolutePath.md5()}")
}

fun loadImageBitmap(file: File): ImageBitmap =
    file.inputStream().buffered().use(::loadImageBitmap)

fun loadSvgPainter(file: File, density: Density): Painter =
    file.inputStream().buffered().use { loadSvgPainter(it, density) }

fun loadXmlImageVector(file: File, density: Density): ImageVector =
    file.inputStream().buffered().use { loadXmlImageVector(InputSource(it), density) }

/* Loading from network with java.net API */

fun loadImageBitmap(url: String): ImageBitmap =
    URL(url).openStream().buffered().use(::loadImageBitmap)

fun loadSvgPainter(url: String, density: Density): Painter =
    URL(url).openStream().buffered().use { loadSvgPainter(it, density) }

fun loadXmlImageVector(url: String, density: Density): ImageVector =
    URL(url).openStream().buffered().use { loadXmlImageVector(InputSource(it), density) }

/* Loading from network with Ktor client API (https://ktor.io/docs/client.html). */

/*

suspend fun loadImageBitmap(url: String): ImageBitmap =
    urlStream(url).use(::loadImageBitmap)

suspend fun loadSvgPainter(url: String, density: Density): Painter =
    urlStream(url).use { loadSvgPainter(it, density) }

suspend fun loadXmlImageVector(url: String, density: Density): ImageVector =
    urlStream(url).use { loadXmlImageVector(InputSource(it), density) }

@OptIn(KtorExperimentalAPI::class)
private suspend fun urlStream(url: String) = HttpClient(CIO).use {
    ByteArrayInputStream(it.get(url))
}

 */

@Throws(IOException::class)
private fun BufferedImage.resizeImage(targetWidth: Int, targetHeight: Int): BufferedImage {
    val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
    val graphics2D = resizedImage.createGraphics()
    graphics2D.drawImage(this, 0, 0, targetWidth, targetHeight, null)
    graphics2D.dispose()
    return resizedImage
}

fun getCachePath(): File? {
    return try {
        val cachePath = File(System.getProperty("compose.application.resources.dir")).resolve("cache/img/")
        cachePath.mkdirs()
        cachePath
    } catch (tr: Throwable) {
        tr.printStackTrace()
        null
    }
}