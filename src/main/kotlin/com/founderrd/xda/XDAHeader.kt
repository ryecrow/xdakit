/**
 * Title:	XDAHeader
 * Description:	提供解析XDA文件header，获取header信息功能。
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

import com.founderrd.xda.XDACommonFunction
import com.founderrd.xda.XDAException
import java.io.IOException
import java.io.RandomAccessFile

internal class XDAHeader {
    val RIGHTINFO_BEGIN = 0
    val RIGHTINFO_LENGTH = 14
    val RIGHTINFO_END = 4
    val RIGHTINFO_CONTENT = byteArrayOf(
        '@'.code.toByte(), 'X'.code.toByte(), 'D'.code.toByte(), 'A'.code.toByte(), 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0
    )
    val ENTRYCOUNT_POSITION = RIGHTINFO_LENGTH + 2
    val ENTRYCOUNT_LENGTH = 4
    val FIRSTENTRYOFFSET_POSITION = (ENTRYCOUNT_POSITION
            + ENTRYCOUNT_LENGTH + 2)
    private var majorVersion: Byte = 0
    private var minorVersion: Byte = 0
    private var entryCount = 0
    private var entryNameTableType: Byte = 0
    private var bitsParam: Byte = 0
    private var firstEntryOffset: Long = 0
    private var isParse = false

    @Throws(XDAException::class)
    fun create(
        theMajorVersion: Byte, theMinorVersion: Byte,
        theEntryNameTableType: Byte, theBitsParam: Byte
    ) {
        majorVersion = theMajorVersion
        minorVersion = theMinorVersion
        entryCount = 0
        entryNameTableType = theEntryNameTableType
        parseBitParam(theBitsParam)
        firstEntryOffset = -1
        isParse = true
    }

    @Throws(IOException::class)
    fun write(theXDAFile: RandomAccessFile) {
        theXDAFile.seek(RIGHTINFO_BEGIN.toLong())
        writeRightsInfo(theXDAFile)
        writeMajorVersion(theXDAFile)
        writeMinorVersion(theXDAFile)
        writeEntryCount(theXDAFile)
        writeEntryNameTableType(theXDAFile)
        writeBitsParam(theXDAFile)
        writeFirstEntryOffset(theXDAFile)
    }

    @Throws(IOException::class)
    fun writeBackEntryCount(theXDAFile: RandomAccessFile, theEntryCount: Int) {
        entryCount = theEntryCount
        theXDAFile.seek(ENTRYCOUNT_POSITION.toLong())
        writeEntryCount(theXDAFile)
    }

    @Throws(IOException::class)
    fun writeBackFirstEntryOffset(
        theXDAFile: RandomAccessFile,
        theFirstEntryOffset: Long
    ) {
        firstEntryOffset = theFirstEntryOffset
        theXDAFile.seek(FIRSTENTRYOFFSET_POSITION.toLong())
        XDACommonFunction.writeIntegerAccording2BitsParam(
            theXDAFile,
            bitsParam, firstEntryOffset
        )
    }

    @Throws(IOException::class, XDAException::class)
    fun parse(fileXDA: RandomAccessFile) {
        fileXDA.seek(RIGHTINFO_BEGIN.toLong())
        parseRigthsInfo(fileXDA)
        parseMajorVersion(fileXDA)
        parseMinorVersion(fileXDA)
        parseEntryCount(fileXDA)
        parseEntryNameTableType(fileXDA)
        parseBitParam(fileXDA)
        parseFirstEntryOffset(fileXDA)
        isParse = true
    }

    @Throws(XDAException::class)
    fun getMajorVersion(): Byte {
        if (!isParse) throw XDAException(XDAException.NEVER_PARSE_HEADER)
        return majorVersion
    }

    @Throws(XDAException::class)
    fun getMinorVersion(): Byte {
        if (!isParse) throw XDAException(XDAException.NEVER_PARSE_HEADER)
        return minorVersion
    }

    @Throws(XDAException::class)
    fun getEntryCount(): Int {
        if (!isParse) throw XDAException(XDAException.NEVER_PARSE_HEADER)
        return entryCount
    }

    @Throws(XDAException::class)
    fun getEntryNameTableType(): Byte {
        if (!isParse) throw XDAException(XDAException.NEVER_PARSE_HEADER)
        return entryNameTableType
    }

    @Throws(XDAException::class)
    fun getBitsParam(): Byte {
        if (!isParse) throw XDAException(XDAException.NEVER_PARSE_HEADER)
        return bitsParam
    }

    @Throws(XDAException::class)
    fun getFirstEntryOffset(): Long {
        if (!isParse) throw XDAException(XDAException.NEVER_PARSE_HEADER)
        return firstEntryOffset
    }

    @Throws(IOException::class, XDAException::class)
    private fun parseRigthsInfo(fileXDA: RandomAccessFile) {
        // 读rightsInfo
        val rightsInfo = ByteArray(RIGHTINFO_LENGTH)
        if (fileXDA.read(rightsInfo) != RIGHTINFO_LENGTH) throw XDAException(XDAException.INVALID_RIGHT_INFO)
        for (i in 0 until RIGHTINFO_END) if (rightsInfo[i] != RIGHTINFO_CONTENT[i]) throw XDAException(XDAException.INVALID_RIGHT_INFO)
    }

    @Throws(IOException::class)
    private fun parseMajorVersion(fileXDA: RandomAccessFile) {
        majorVersion = fileXDA.readByte()
    }

    @Throws(IOException::class)
    private fun parseMinorVersion(fileXDA: RandomAccessFile) {
        minorVersion = fileXDA.readByte()
    }

    @Throws(IOException::class)
    private fun parseEntryCount(fileXDA: RandomAccessFile) {
        entryCount = XDACommonFunction.readInt(fileXDA)
    }

    @Throws(IOException::class, XDAException::class)
    private fun parseEntryNameTableType(fileXDA: RandomAccessFile) {
        entryNameTableType = fileXDA.readByte()
        when (entryNameTableType) {
            0x00, 0x08 -> return
            else -> throw XDAException(XDAException.INVALID_ENTRYNAMETABLETYPE)
        }
    }

    @Throws(IOException::class, XDAException::class)
    private fun parseBitParam(fileXDA: RandomAccessFile) {
        val theBitsParam = fileXDA.readByte()
        parseBitParam(theBitsParam)
    }

    @Throws(XDAException::class)
    private fun parseBitParam(theBitsParam: Byte) {
        bitsParam = theBitsParam
        if (bitsParam.toInt() == 0x00) bitsParam = 0x02
        when (bitsParam) {
            0x02, 0x04, 0x08 -> return
            else -> throw XDAException(XDAException.INVALID_BITSPARAM)
        }
    }

    @Throws(IOException::class, XDAException::class)
    private fun parseFirstEntryOffset(fileXDA: RandomAccessFile) {
        firstEntryOffset = when (bitsParam) {
            0x02 -> XDACommonFunction.readShort(fileXDA).toLong()
            0x04 -> XDACommonFunction.readInt(fileXDA).toLong()
            0x08 -> XDACommonFunction.readLong(fileXDA)
            else -> throw XDAException(XDAException.INVALID_BITSPARAM)
        }
    }

    @Throws(IOException::class)
    private fun writeRightsInfo(theXDAFile: RandomAccessFile) {
        theXDAFile.write(RIGHTINFO_CONTENT)
    }

    @Throws(IOException::class)
    private fun writeMajorVersion(theXDAFile: RandomAccessFile) {
        theXDAFile.writeByte(majorVersion.toInt())
    }

    @Throws(IOException::class)
    private fun writeMinorVersion(theXDAFile: RandomAccessFile) {
        theXDAFile.writeByte(minorVersion.toInt())
    }

    @Throws(IOException::class)
    private fun writeEntryCount(theXDAFile: RandomAccessFile) {
        XDACommonFunction.writeInt(theXDAFile, entryCount.toLong())
    }

    @Throws(IOException::class)
    private fun writeEntryNameTableType(theXDAFile: RandomAccessFile) {
        theXDAFile.writeByte(entryNameTableType.toInt())
    }

    @Throws(IOException::class)
    private fun writeBitsParam(theXDAFile: RandomAccessFile) {
        theXDAFile.writeByte(bitsParam.toInt())
    }

    @Throws(IOException::class)
    private fun writeFirstEntryOffset(theXDAFile: RandomAccessFile) {
        XDACommonFunction.writeIntegerAccording2BitsParam(
            theXDAFile,
            bitsParam, firstEntryOffset
        )
    }
}