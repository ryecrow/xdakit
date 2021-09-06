/**
 * Title:	XDAItemInfo
 * Description:	定义XDADocument中的项信息类，提供对项信息的添加历史，提取历史记录的功能
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

import com.founderrd.xda.XDACommonFunction.copyFromSrcToDst
import com.founderrd.xda.XDACommonFunction.readByteTillFlag
import com.founderrd.xda.XDACommonFunction.readIntegerAccording2BitsParam
import com.founderrd.xda.XDAException
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.util.*

internal class XDAItemInfo(itemFullPath: String?) {
    var fullPath: String
    var histories: Vector<XDAHistory>

    @Throws(IOException::class)
    fun addHistory(
        theEntryNo: Int, theOperator: Byte,
        theFileXDA: RandomAccessFile, thePosition: Long, bitsParam: Byte
    ) {
        val oldHistory: XDAHistory = XDAOldHistory(
            theEntryNo, theOperator,
            theFileXDA, thePosition, bitsParam
        )
        histories.addElement(oldHistory)
    }

    fun addHistory(
        entryNo: Int, operator: Byte,
        maintainedStream: XDAInputStream?, ecs: ByteArray?
    ) {
        val newHistory: XDAHistory = XDANewHistory(
            entryNo, operator,
            maintainedStream, ecs
        )
        histories.addElement(newHistory)
    }

    init {
        fullPath = String(itemFullPath)
        histories = Vector()
    }
}

internal abstract class XDAHistory {
    var entryNo: Int
    var operator: Byte
    var ecs: ByteArray?

    constructor() {
        entryNo = 0
        operator = 0
        ecs = null
    }

    constructor(theEntryNo: Int, theOperator: Byte) {
        entryNo = theEntryNo
        operator = theOperator
    }

    val eCS: ByteArray
        get() = ecs!!.clone()

    // 不进行任何的流装饰直接将当前History下的流写入target
    @Throws(IOException::class, XDAException::class)
    abstract fun writeTo(target: OutputStream?, bitsParam: Byte, buffer: ByteArray?): Long

    @Throws(IOException::class, XDAException::class)
    abstract fun writeTo(target: RandomAccessFile?, bitsParam: Byte, buffer: ByteArray?): Long

    @Throws(XDAException::class, IOException::class)
    abstract fun writeTo(
        target: RandomAccessFile?, bitsParam: Byte,
        buffer: ByteArray?, checkSum: ByteArray?
    ): Long

    companion object {
        const val ITEM_CHECKSUM_LENGTH = 1
    }
}

internal class XDAOldHistory : XDAHistory {
    var position: Long
        private set
    private var fileXDA: RandomAccessFile
    var length: Long
        private set

    constructor(
        newHistory: XDAHistory, thePosition: Long,
        theFileXDA: RandomAccessFile, theLength: Long
    ) {
        super.ecs = newHistory.ecs
        super.entryNo = newHistory.entryNo
        super.operator = newHistory.operator
        position = thePosition
        fileXDA = theFileXDA
        length = theLength
    }

    constructor(
        theEntryNo: Int, theOperator: Byte,
        theFileXDA: RandomAccessFile, thePosition: Long, bitsParam: Byte
    ) : super(theEntryNo, theOperator) {
        position = thePosition
        fileXDA = theFileXDA
        val currentPosition = theFileXDA.filePointer
        fileXDA.seek(position + ITEM_CHECKSUM_LENGTH)
        length = readIntegerAccording2BitsParam(
            theFileXDA,
            bitsParam
        )
        ecs = readByteTillFlag(
            theFileXDA,
            XDADefine.ECS_BUFFER_CAPACITY.toInt(), XDADefine.ECS_END_FLAG
        )
        fileXDA.seek(currentPosition)
    }

    @Throws(IOException::class, XDAException::class)
    override fun writeTo(target: OutputStream?, bitsParam: Byte, buffer: ByteArray?): Long {
        if (operator == XDADefine.OPERATOR_DELETE) throw XDAException(XDAException.CANNOT_EXTRACT_STREAM)
        seek(bitsParam)
        return copyFromSrcToDst(
            fileXDA, target!!, length,
            buffer!!
        )
    }

    @Throws(IOException::class, XDAException::class)
    override fun writeTo(target: RandomAccessFile?, bitsParam: Byte, buffer: ByteArray?): Long {
        if (operator == XDADefine.OPERATOR_DELETE) throw XDAException(XDAException.CANNOT_EXTRACT_STREAM)
        seek(bitsParam)
        return copyFromSrcToDst(
            fileXDA, target!!, length,
            buffer!!
        )
    }

    @Throws(XDAException::class, IOException::class)
    override fun writeTo(
        target: RandomAccessFile?, bitsParam: Byte, buffer: ByteArray?,
        checkSum: ByteArray?
    ): Long {
        if (operator == XDADefine.OPERATOR_DELETE) throw XDAException(XDAException.CANNOT_EXTRACT_STREAM)
        seek(bitsParam)
        return copyFromSrcToDst(
            fileXDA, target!!, length,
            buffer!!, checkSum!!
        )
    }

    @Throws(IOException::class)
    private fun seek(bitsParam: Byte) {
        fileXDA.seek(position + bitsParam + ITEM_CHECKSUM_LENGTH + ecs!!.size)
    }
}

internal class XDANewHistory(
    theEntryNo: Int, theOperator: Byte,
    var targetStream: XDAInputStream?, theECS: ByteArray?
) : XDAHistory(theEntryNo, theOperator) {
    @Throws(IOException::class, XDAException::class)
    override fun writeTo(target: OutputStream?, bitsParam: Byte, buffer: ByteArray?): Long {
        if (operator == XDADefine.OPERATOR_DELETE) throw XDAException(XDAException.CANNOT_EXTRACT_STREAM)
        targetStream!!.open()
        val result = copyFromSrcToDst(
            targetStream!!,
            target!!, buffer
        )
        targetStream = targetStream!!.nirvana()
        return result
    }

    @Throws(IOException::class, XDAException::class)
    override fun writeTo(target: RandomAccessFile?, bitsParam: Byte, buffer: ByteArray?): Long {
        if (operator == XDADefine.OPERATOR_DELETE) throw XDAException(XDAException.CANNOT_EXTRACT_STREAM)
        targetStream!!.open()
        val result = copyFromSrcToDst(
            targetStream!!,
            target!!, buffer
        )
        targetStream = targetStream!!.nirvana()
        return result
    }

    @Throws(XDAException::class, IOException::class)
    override fun writeTo(
        target: RandomAccessFile?, bitsParam: Byte, buffer: ByteArray?,
        checkSum: ByteArray?
    ): Long {
        if (operator == XDADefine.OPERATOR_DELETE) throw XDAException(XDAException.CANNOT_EXTRACT_STREAM)
        targetStream!!.open()
        val result = copyFromSrcToDst(
            targetStream!!,
            target!!, buffer!!, checkSum!!
        )
        targetStream = targetStream!!.nirvana()
        return result
    }

    init {
        if (theECS != null) ecs = theECS.clone()
    }
}