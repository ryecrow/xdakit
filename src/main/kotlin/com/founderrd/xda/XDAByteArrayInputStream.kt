/**
 * Title:	XDAByteArrayInputStream
 * Description:	以ByteArrayInputStream为底层实现XDAInputStream
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

import java.io.ByteArrayInputStream
import java.io.IOException

internal class XDAByteArrayInputStream(private val buffer: ByteArray) : XDAInputStream() {

    private var buffStream: ByteArrayInputStream? = null

    @Throws(IOException::class)
    override fun available(): Int {
        return buffStream!!.available()
    }

    @Throws(IOException::class)
    override fun close() {
        buffStream!!.close()
    }

    override fun mark(readlimit: Int) {
        buffStream!!.mark(readlimit)
    }

    override fun markSupported(): Boolean {
        return buffStream!!.markSupported()
    }

    @Throws(IOException::class)
    override fun read(): Int {
        return buffStream!!.read()
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int {
        return buffStream!!.read(b)
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return buffStream!!.read(b, off, len)
    }

    @Throws(IOException::class)
    override fun reset() {
        buffStream!!.reset()
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        return buffStream!!.skip(n)
    }

    @Throws(IOException::class)
    override fun open(): Boolean {
        if (buffStream != null) return false
        buffStream = ByteArrayInputStream(buffer)
        return true
    }

    @Throws(IOException::class)
    override fun nirvana(): XDAInputStream? {
        if (buffStream != null) buffStream!!.close()
        return XDAByteArrayInputStream(buffer)
    }

    override fun canRead(): Boolean {
        return buffStream != null
    }
}