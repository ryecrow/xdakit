/**
 * Title:	XDADeflaterInputStream
 * Description:	以DeflaterInputStream为底层，实现XDAFilterInputStream提供压缩功能
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

import java.io.IOException
import java.util.zip.DeflaterInputStream

internal class XDADeflaterInputStream(theBaseStream: XDAInputStream?) : XDAFilterInputStream(theBaseStream!!) {
    private var delegate: DeflaterInputStream? = null

    @Throws(IOException::class)
    override fun available(): Int {
        return delegate!!.available()
    }

    @Throws(IOException::class)
    override fun close() {
        delegate!!.close()
    }

    override fun mark(readlimit: Int) {
        delegate!!.mark(readlimit)
    }

    override fun markSupported(): Boolean {
        return delegate!!.markSupported()
    }

    @Throws(IOException::class)
    override fun read(): Int {
        return delegate!!.read()
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray): Int {
        return delegate!!.read(b)
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        return delegate!!.read(b, off, len)
    }

    @Throws(IOException::class)
    override fun reset() {
        delegate!!.reset()
    }

    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        return delegate!!.skip(n)
    }

    @Throws(IOException::class)
    override fun open(): Boolean {
        if (delegate != null) return false
        if (baseStream.open() != true) return false
        delegate = DeflaterInputStream(baseStream)
        return true
    }

    @Throws(IOException::class)
    override fun nirvana(): XDAInputStream? {
        if (delegate != null) delegate!!.close()
        baseStream = baseStream.nirvana()!!
        return XDADeflaterInputStream(baseStream)
    }

    override fun canRead(): Boolean {
        return baseStream.canRead()
    }
}