/**
 * Title:	XDAContainer
 * Description:	实现XDAInterface接口，底层使用了XDADocument和XDADecorator
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

import com.founderrd.xda.constant.COMPRESS_ITEMLIST_MASK
import com.founderrd.xda.constant.COMPRESS_NAMETABLE_MASK
import com.founderrd.xda.util.and
import com.founderrd.xda.util.or
import java.io.*
import java.security.NoSuchAlgorithmException

class XDAContainer : XDA {

    val dec: XDADecorator = XDADecorator()
    private val doc: XDADocument = XDADocument()

    // XDA文档操作
    override fun create(filePath: String, bitsParam: Byte) {
        val xdaFile = File(filePath)
        doc.create(xdaFile, 0x01.toByte(), 0x00.toByte(), 0x00.toByte(), bitsParam)
    }

    override fun create(filePath: String) {
        create(filePath, 0x04.toByte())
    }

    override fun open(filePath: String) {
        val xdaFile = File(filePath)
        if (!xdaFile.exists() || xdaFile.isDirectory) throw XDAException(XDAException.INVALID_FILE_PATH)
        doc.open(xdaFile)
    }

    override fun close() {
        doc.close()
    }

    override fun save(compressNameTable: Boolean, compressItemList: Boolean) {
        var entryCompressMark: Byte = 0x00
        if (compressNameTable) entryCompressMark = entryCompressMark or COMPRESS_NAMETABLE_MASK
        if (compressItemList) entryCompressMark = entryCompressMark or COMPRESS_ITEMLIST_MASK
        doc.saveChanges(entryCompressMark)
    }

    override fun save() {
        save(compressNameTable = true, compressItemList = true)
    }

    override fun saveAs(
        path: String,
        bitsParam: Byte,
        compressNameTable: Boolean,
        compressItemList: Boolean
    ) {
        var entryCompress: Byte = 0
        if (compressNameTable) entryCompress = entryCompress and 0x01
        if (compressItemList) entryCompress = entryCompress and 0x02
        val newFile = File(path)
        doc.saveAs(newFile, 0x01.toByte(), 0x00.toByte(), 0x00.toByte(), bitsParam, entryCompress)
    }

    @Throws(NoSuchAlgorithmException::class, XDAException::class, IOException::class)
    override fun saveAs(path: String) {
        saveAs(path, 0x04.toByte(), compressNameTable = true, compressItemList = true)
    }

    override fun appendFile(internalPath: String, sourceFilePath: String, ecs: ByteArray) {
        val inputSteam = dec.decorate(sourceFilePath, ecs)
        doc.insertItem(internalPath, inputSteam, ecs)
    }

    override fun appendFile(internalPath: String, data: ByteArray, ecs: ByteArray) {
        val inputSteam = dec.decorate(data, ecs)
        doc.insertItem(internalPath, inputSteam, ecs)
    }

    override fun replaceFile(internalPath: String, sourceFilePath: String, ecs: ByteArray) {
        val inputSteam = dec.decorate(sourceFilePath, ecs)
        doc.replaceItem(internalPath, inputSteam, ecs)
    }

    override fun replaceFile(internalPath: String, data: ByteArray, ecs: ByteArray) {
        val inputSteam = dec.decorate(data, ecs)
        doc.replaceItem(internalPath, inputSteam, ecs)
    }

    override fun removeFile(internalPath: String) {
        doc.deleteItem(internalPath)
    }

    override fun appendDirectory(attachPath: String, directory: String, ecs: ByteArray) {
        val dir = File(directory)
        if (!dir.exists() || !dir.isDirectory) return
        val parentPath = attachPath + "\\"
        doAppendDirectory(parentPath, dir, ecs)
    }

    override fun removeDirectory(internalPath: String) {
        // 检查pathInXDA
        var pathDir = internalPath
        if (!(pathDir.endsWith('\\') || pathDir.endsWith('/'))) {
            pathDir += "\\"
        }
        for (path in doc.allLogicExistedItem) {
            if (path!!.startsWith(pathDir)) try {
                doc.deleteItem(path)
            } catch (e: XDAException) {
                continue
            }
        }
    }

    override fun extractFileTo(internalPath: String, destination: String) {
        val targetStream: OutputStream = FileOutputStream(destination)
        doc.extractItemStream(internalPath, targetStream, dec)
    }

    override fun extract(internalPath: String): ByteArray {
        val targetStream: OutputStream = ByteArrayOutputStream()
        doc.extractItemStream(internalPath, targetStream, dec)
        return (targetStream as ByteArrayOutputStream).toByteArray()
    }

    override fun extractTo(destination: String) {
        val allLogicExistedItem = doc.allLogicExistedItem
        for (pathInXDA in allLogicExistedItem) {
            val targetFilePath = destination + pathInXDA
            File(targetFilePath).parentFile.mkdirs()
            extractFileTo(pathInXDA, targetFilePath)
        }
    }

    override fun extractTo(internalPath: String, destination: String) {
        var pathInXDA = internalPath
        var dir = destination
        if (XDADocument.PACKPATH_PATTERN.matcher(pathInXDA)
                .matches()
        ) throw XDAException(XDAException.INVALID_PACK_PATH)
        var ch = pathInXDA[pathInXDA.length - 1]
        if (ch != '\\' && ch != '/') {
            pathInXDA += "\\"
        }
        ch = dir[dir.length - 1]
        if (ch == '\\' || ch == '/') {
            dir = dir.substring(0, dir.length - 2)
        }

        for (oneItemPath in doc.allLogicExistedItem) {
            if (oneItemPath.startsWith(pathInXDA)) {
                try {
                    val extractFile = dir + oneItemPath
                    createFile(extractFile)
                    extractFileTo(oneItemPath, extractFile)
                } catch (e: IOException) {
                    continue
                } catch (e: XDAException) {
                    continue
                }
            }
        }
    }

    override val contents: List<String>
        get() = doc.allLogicExistedItem

    override fun validateFile(): Boolean {
        return false
    }

    override val majorVersion: Int = doc.header.getMajorVersion().toInt()

    override val minorVersion: Int = doc.header.getMinorVersion().toInt()

    private fun doAppendDirectory(parentPath: String, directory: File, ecs: ByteArray) {
        val children = directory.listFiles() ?: return
        for (child in children) {
            if (child.isDirectory) {
                doAppendDirectory(
                    parentPath + child.name + "\\",
                    child, ecs
                )
            } else {
                appendFile(
                    parentPath + child.name, child
                        .absolutePath, ecs
                )
            }
        }
    }

    private fun createFile(filePath: String): Boolean {
        val file = File(filePath)
        val folder = file.parentFile
        folder.mkdirs()
        return file.createNewFile()
    }
}