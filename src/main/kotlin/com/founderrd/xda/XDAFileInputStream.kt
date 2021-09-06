/**
 * Title:	XDAFileInputStream
 * Description:	以FileInputStream为底层实现XDAInputStream
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

import java.io.File
import java.io.FileInputStream
import java.io.IOException

internal class XDAFileInputStream(private val file: File) : XDAInputStream() {
    private var fileStream: FileInputStream? = null

    @Throws(IOException::class)
    override fun available(): Int {
        return fileStream!!.available()
    }

    @Throws(IOException::class)
    override fun close() {
        fileStream!!.close()
    }

    override fun mark(readlimit: Int) {
        fileStream!!.mark(readlimit)
    }

    override fun markSupported(): Boolean {
        return fileStream!!.markSupported()
    }

    @Throws(IOException::class)
    override fun read(): Int {
        return fileStream!!.read()
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int {
        return fileStream!!.read(b)
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return fileStream!!.read(b, off, len)
    }

    @Throws(IOException::class)
    override fun reset() {
        fileStream!!.reset()
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        return fileStream!!.skip(n)
    }

    @Throws(IOException::class)
    override fun open(): Boolean {
        if (fileStream != null) return false
        fileStream = FileInputStream(file)
        return true
    }

    @Throws(IOException::class)
    override fun nirvana(): XDAInputStream? {
        if (fileStream != null) fileStream!!.close()
        return XDAFileInputStream(file)
    }

    override fun canRead(): Boolean {
        return fileStream != null
    }
}