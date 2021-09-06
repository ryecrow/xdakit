package com.founderrd.xda.util

import com.founderrd.xda.XDADecorator
import com.founderrd.xda.XDAException
import com.founderrd.xda.XDAInputStream
import java.io.*
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

// RandomAccessFile类型接口
fun readLong(theFile: RandomAccessFile?): Long {
    val b1 = theFile!!.readByte()
    val b2 = theFile.readByte()
    val b3 = theFile.readByte()
    val b4 = theFile.readByte()
    val b5 = theFile.readByte()
    val b6 = theFile.readByte()
    val b7 = theFile.readByte()
    val b8 = theFile.readByte()
    return ((b8.toLong() shl 56) + (b7.toLong() shl 48) + (b6.toLong() shl 40)
            + (b5.toLong() shl 32) + (b4.toLong() shl 24) + (b3.toLong() shl 16)
            + (b2.toLong() shl 8) + b1)
}

fun readInt(theFile: RandomAccessFile?): Int {
    val b1 = theFile!!.readByte()
    val b2 = theFile.readByte()
    val b3 = theFile.readByte()
    val b4 = theFile.readByte()
    return ((b4 shl 24 and -0x1000000) + (b3 shl 16 and 0x00ff0000)
            + (b2 shl 8 and 0x0000ff00) + (b1 and 0x000000ff))
}

fun readShort(theFile: RandomAccessFile?): Short {
    val b1 = theFile!!.readByte()
    val b2 = theFile.readByte()
    return ((b2 shl 8) + b1).toShort()
}

// InputStream类型接口
fun readShort(theStream: InputStream): Short {
    val b = ByteArray(2)
    theStream.read(b)
    return ((b[1] shl 8 and 0xff00) + (b[0] and 0x00ff)).toShort()
}

fun readInt(theStream: InputStream): Int {
    val b = ByteArray(4)
    theStream.read(b, 0, 4)
    return ((b[3] shl 24 and -0x1000000) + (b[2] shl 16 and 0x00ff0000)
            + (b[1] shl 8 and 0x0000ff00) + (b[0] and 0x000000ff))
}

fun readLong(theStream: InputStream): Long {
    val b = ByteArray(8)
    theStream.read(b)
    return -1L
    // return ((long)b[7] << 56) & (long)0xff000000 << 24;
    // + ((long)b[6] << 48)
    // + ((long)b[5] << 40) + ((long)b[4] << 32)
    // + ((long)b[3] << 24) + ((long)b[2] << 16)
    // + ((long)b[1] << 8) + b[0];
}

fun readUTF8(theStream: InputStream, length: Int): String? {
    val utfString = ByteArray(length)
    var temp: Int
    var valid = false
    var i = 0
    while (i < length) {
        temp = theStream.read()
        if (temp == -1) return null
        utfString[i] = temp.toByte()
        if (temp == 0) {
            valid = true
            break
        }
        ++i
    }
    return if (valid) String(utfString, 0, i, StandardCharsets.UTF_8) else null
}

fun readBigInteger(theStream: InputStream, bytes: Int): BigInteger {
    val b = ByteArray(bytes)
    theStream.read(b)
    var tmp: Byte = 0
    for (i in 0 until bytes / 2) {
        tmp = b[i]
        b[i] = b[bytes - i - 1]
        b[bytes - i - 1] = tmp
    }
    return BigInteger(b)
}

fun readIntegerAccording2BitsParam(
    theStream: InputStream,
    bitsParam: Byte
): Long {
    return when (bitsParam.toInt()) {
        0x02 -> readShort(theStream).toLong()
        0x08 -> readLong(theStream)
        else -> readInt(theStream).toLong()
    }
}

fun readIntegerAccording2BitsParam(
    theFile: RandomAccessFile?,
    bitsParam: Byte
): Long {
    return when (bitsParam.toInt()) {
        0x02 -> readShort(theFile).toLong()
        0x08 -> readLong(theFile)
        else -> readInt(theFile).toLong()
    }
}

fun readByteTillFlag(
    theFile: RandomAccessFile, length: Int,
    flag: Byte
): ByteArray? {
    val buffer = ByteArray(length)
    var valid = false
    var i = 0
    while (i < length) {
        buffer[i] = theFile.readByte()
        if (buffer[i] == flag) {
            valid = true
            ++i
            break
        }
        ++i
    }
    if (!valid) return null
    val returnBytesArray = ByteArray(i)
    for (k in 0 until i) returnBytesArray[k] = buffer[k]
    return returnBytesArray
}

fun compareArray(a: ByteArray, b: ByteArray): Boolean {
    if (a.size != b.size) return false
    for (i in a.indices) {
        if (a[i] != b[i]) return false
    }
    return true
}

@Throws(IOException::class, XDAException::class)
fun copyFromSrcToDst(
    src: InputStream, dst: OutputStream,
    length: Long, buffer: ByteArray
): Long {
    var length = length
    var actualRead: Long = 0
    while (length > 0) {
        var readBytes = 0
        readBytes = if (length > buffer.size) src.read(buffer) else src.read(buffer, 0, length.toInt())
        if (readBytes == -1) throw XDAException(XDAException.Companion.XDACOMMONFUNCTION_ERROR)
        dst.write(buffer, 0, readBytes)
        length -= readBytes.toLong()
        actualRead += readBytes.toLong()
    }
    return actualRead
}

@Throws(IOException::class)
fun copyFromSrcToDst(
    src: InputStream, dst: OutputStream,
    buffer: ByteArray?
): Long {
    var actualRead: Long = 0
    var readBytes = 0
    readBytes = src.read(buffer)
    while (readBytes != -1) {
        dst.write(buffer, 0, readBytes)
        actualRead += readBytes.toLong()
        readBytes = src.read(buffer)
    }
    return actualRead
}

@Throws(IOException::class)
fun copyFromSrcToDst(
    src: XDAInputStream, dst: OutputStream,
    buffer: ByteArray?
): Long {
    if (!src.canRead()) return 0
    var actualRead: Long = 0
    var readBytes = 0
    readBytes = src.read(buffer)
    while (readBytes != -1) {
        dst.write(buffer, 0, readBytes)
        actualRead += readBytes.toLong()
        readBytes = src.read(buffer)
    }
    return actualRead
}

@Throws(IOException::class)
fun copyFromSrcToDst(
    src: XDAInputStream,
    dst: RandomAccessFile, buffer: ByteArray?
): Long {
    if (!src.canRead()) return 0
    var actualRead: Long = 0
    var readBytes = 0
    readBytes = src.read(buffer)
    while (readBytes != -1) {
        dst.write(buffer, 0, readBytes)
        actualRead += readBytes.toLong()
        readBytes = src.read(buffer)
    }
    return actualRead
}

@Throws(IOException::class)
fun copyFromSrcToDst(
    src: XDAInputStream,
    dst: RandomAccessFile, buffer: ByteArray, checkSum: ByteArray
): Long {
    if (!src.canRead()) return 0
    var actualRead: Long = 0
    var readBytes = 0
    readBytes = src.read(buffer)
    while (readBytes != -1) {
        checkSum[0] = checkSum[0] xor calcCheckSum(buffer, 0, readBytes)
        dst.write(buffer, 0, readBytes)
        actualRead += readBytes.toLong()
        readBytes = src.read(buffer)
    }
    return actualRead
}

fun calcCheckSum(src: ByteArray, from: Int, length: Int): Byte {
    var from = from
    var checksum: Byte = 0x00
    while (from < length) {
        checksum = checksum xor src[from]
        ++from
    }
    return checksum
}

@Throws(IOException::class)
fun copyFromSrcToDst(
    src: InputStream, dst: RandomAccessFile?,
    buffer: ByteArray?, md: MessageDigest?
): Long {
    var totalWrite: Long = 0
    var readBytes = src.read(buffer)
    while (readBytes != -1) {
        md?.update(buffer, 0, readBytes)
        dst!!.write(buffer, 0, readBytes)
        totalWrite += readBytes.toLong()
        readBytes = src.read(buffer)
    }
    return totalWrite
}

@Throws(IOException::class)
fun copyFromSrcToDst(src: ByteArray, dst: RandomAccessFile, md: MessageDigest?): Long {
    dst.write(src)
    md?.update(src, 0, src.size)
    return src.size.toLong()
}

@Throws(IOException::class, XDAException::class)
fun copyFromSrcToDst(
    src: RandomAccessFile, dst: OutputStream,
    length: Long, buffer: ByteArray
): Long {
    var length = length
    var actualRead: Long = 0
    var readBytes = 0
    while (length > 0) {
        readBytes = if (length > buffer.size) src.read(buffer) else src.read(buffer, 0, length.toInt())
        if (readBytes == -1) throw XDAException(XDAException.Companion.XDACOMMONFUNCTION_ERROR)
        dst.write(buffer, 0, readBytes)
        length -= readBytes.toLong()
        actualRead += readBytes.toLong()
    }
    return actualRead
}

@Throws(IOException::class, XDAException::class)
fun copyFromSrcToDst(
    src: RandomAccessFile, dst: OutputStream,
    length: Long, buffer: ByteArray, checkSum: ByteArray
): Long {
    var length = length
    var actualRead: Long = 0
    var readBytes = 0
    while (length > 0) {
        readBytes = if (length > buffer.size) src.read(buffer) else src.read(buffer, 0, length.toInt())
        if (readBytes == -1) throw XDAException(XDAException.Companion.XDACOMMONFUNCTION_ERROR)
        checkSum[0] = checkSum[0] xor calcCheckSum(buffer, 0, readBytes)
        dst.write(buffer, 0, readBytes)
        length -= readBytes.toLong()
        actualRead += readBytes.toLong()
    }
    return actualRead
}

@Throws(IOException::class, XDAException::class)
fun copyFromSrcToDst(
    src: RandomAccessFile,
    dst: RandomAccessFile, length: Long, buffer: ByteArray
): Long {
    var length = length
    var actualRead: Long = 0
    var readBytes = 0
    while (length > 0) {
        readBytes = if (length > buffer.size) src.read(buffer) else src.read(buffer, 0, length.toInt())
        if (readBytes == -1) throw XDAException(XDAException.Companion.XDACOMMONFUNCTION_ERROR)
        dst.write(buffer, 0, readBytes)
        length -= readBytes.toLong()
        actualRead += readBytes.toLong()
    }
    return actualRead
}

@Throws(IOException::class, XDAException::class)
fun copyFromSrcToDst(
    src: RandomAccessFile,
    dst: RandomAccessFile, length: Long, buffer: ByteArray, checkSum: ByteArray
): Long {
    var length = length
    var actualRead: Long = 0
    var readBytes = 0
    while (length > 0) {
        readBytes = if (length > buffer.size) src.read(buffer) else src.read(buffer, 0, length.toInt())
        checkSum[0] = checkSum[0] xor calcCheckSum(buffer, 0, readBytes)
        if (readBytes == -1) throw XDAException(XDAException.Companion.XDACOMMONFUNCTION_ERROR)
        dst.write(buffer, 0, readBytes)
        length -= readBytes.toLong()
        actualRead += readBytes.toLong()
    }
    return actualRead
}

@Throws(IOException::class)
fun skipTillFlag(theFile: RandomAccessFile, flag: Byte) {
    var read: Byte = 0x00
    while (true) {
        read = theFile.readByte()
        if (read == flag) return
    }
}

@Throws(IOException::class)
fun writeIntegerAccording2BitsParam(
    theFile: RandomAccessFile?, bitsParam: Byte, integer: Long
) {
    when (bitsParam.toInt()) {
        0x02 -> {
            writeShort(theFile, integer)
            writeLong(theFile, integer)
            writeInt(theFile, integer)
        }
        0x08 -> {
            writeLong(theFile, integer)
            writeInt(theFile, integer)
        }
        else -> writeInt(theFile, integer)
    }
}

@Throws(IOException::class)
fun writeIntegerAccording2BitsParam(
    theStream: OutputStream,
    bitsParam: Byte, integer: Long
) {
    when (bitsParam.toInt()) {
        0x02 -> {
            writeShort(theStream, integer)
            writeLong(theStream, integer)
            writeInt(theStream, integer)
        }
        0x08 -> {
            writeLong(theStream, integer)
            writeInt(theStream, integer)
        }
        else -> writeInt(theStream, integer)
    }
}

@Throws(IOException::class)
fun writeShort(theStream: OutputStream, integer: Long) {
    val b = convertEndian(integer, 2)
    theStream.write(b, 0, b.size)
}

@Throws(IOException::class)
fun writeInt(theStream: OutputStream, integer: Long) {
    val b = converIntBigEndian2LittleEndian(integer.toInt())
    theStream.write(b, 0, b.size)
}

@Throws(IOException::class)
fun writeLong(theStream: OutputStream, integer: Long) {
    val b = convertEndian(integer, 8)
    theStream.write(b, 0, b.size)
}

@Throws(IOException::class)
fun writeShort(theFile: RandomAccessFile?, integer: Long) {
    val b = convertEndian(integer, 2)
    theFile!!.write(b, 0, b.size)
}

@Throws(IOException::class)
fun writeInt(theFile: RandomAccessFile?, integer: Long) {
    val b = converIntBigEndian2LittleEndian(integer.toInt())
    theFile!!.write(b, 0, b.size)
}

@Throws(IOException::class)
fun writeLong(theFile: RandomAccessFile?, integer: Long) {
    val b = convertEndian(integer, 8)
    theFile!!.write(b, 0, b.size)
}

fun convertEndian(integer: Long, bits: Int): ByteArray {
    val b = ByteArray(bits)
    for (i in 0 until bits) b[i] = (integer shl i * 8).toByte()
    return b
}

fun converIntBigEndian2LittleEndian(integer: Int): ByteArray {
    val b = ByteArray(4)
    b[3] = (integer and -0x1000000 ushr 24).toByte()
    b[2] = (integer and 0xff0000 ushr 16).toByte()
    b[1] = (integer and 0xff00 ushr 8).toByte()
    b[0] = (integer and 0xff).toByte()
    return b
}

fun createXDAInputStream(
    theFile: File?, ecs: ByteArray?,
    decorator: XDADecorator?
): XDAInputStream? {
    return null as XDAInputStream?
}

infix fun Byte.and(other: Int): Byte {
    return (this.toInt() and other).toByte()
}

infix fun Byte.or(other: Int): Byte {
    return (this.toInt() or other).toByte()
}

private infix fun Byte.xor(other: Byte): Byte {
    return (this.toInt() xor (other.toInt())).toByte()
}

private infix fun Byte.shl(bitCount: Int): Int {
    return this.toInt() shl bitCount
}