package view.widget

import androidx.compose.ui.awt.ComposeWindow
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter

val legalSuffixList: Array<String> = arrayOf("jpg", "jpeg")

fun showFileSelector(
    suffixList: Array<String> = arrayOf("jpg", "jpeg"),
    isMultiSelection: Boolean = true,
    selectionMode: Int = JFileChooser.FILES_AND_DIRECTORIES, // 可以选择目录和文件
    selectionFileFilter: FileNameExtensionFilter? = FileNameExtensionFilter("图片(.jpg .jpeg)", *suffixList), // 文件过滤
    onFileSelected: (Array<File>) -> Unit,
    ) {
    JFileChooser().apply {
        try {
            val lookAndFeel = UIManager.getSystemLookAndFeelClassName()
            UIManager.setLookAndFeel(lookAndFeel)
            SwingUtilities.updateComponentTreeUI(this)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        fileSelectionMode = selectionMode
        isMultiSelectionEnabled = isMultiSelection
        fileFilter = selectionFileFilter

        val result = showOpenDialog(ComposeWindow())
        if (result == JFileChooser.APPROVE_OPTION) {
            if (isMultiSelection) {
                onFileSelected(this.selectedFiles)
            }
            else {
                val resultArray = arrayOf(this.selectedFile)
                onFileSelected(resultArray)
            }
        }
    }
}

fun dropFileTarget(
    onFileDrop: (List<String>) -> Unit
): DropTarget {
    return object : DropTarget() {
        override fun drop(event: DropTargetDropEvent) {

            event.acceptDrop(DnDConstants.ACTION_REFERENCE)
            val dataFlavors = event.transferable.transferDataFlavors
            dataFlavors.forEach {
                if (it == DataFlavor.javaFileListFlavor) {
                    val list = event.transferable.getTransferData(it) as List<*>

                    val pathList = mutableListOf<String>()
                    list.forEach { filePath ->
                        pathList.add(filePath.toString())
                    }
                    onFileDrop(pathList)
                }
            }
            event.dropComplete(true)
        }
    }
}

fun filterFileList(fileList: List<String>): List<File> {
    val newFile = mutableListOf<File>()
    fileList.map {path ->
        newFile.add(File(path))
    }

    return filterFileList(newFile.toTypedArray())
}

fun filterFileList(fileList: Array<File>): List<File> {
    val newFileList = mutableListOf<File>()

    for (file in fileList) {
        if (file.isDirectory) {
            newFileList.addAll(getAllFile(file))
        }
        else {
            if (file.extension in legalSuffixList) {
                newFileList.add(file)
            }
        }
    }

    return newFileList
}

private fun getAllFile(file: File): List<File> {
    val newFileList = mutableListOf<File>()
    val fileTree = file.walk()
    fileTree.maxDepth(Int.MAX_VALUE)
        .filter { it.isFile }
        .filter { it.extension in legalSuffixList }
        .forEach {
            newFileList.add(it)
        }

    return newFileList
}