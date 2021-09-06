/**
 * Title:	XDADecorator
 * Description:	实现一个装饰类，按要求装饰流
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

import com.founderrd.xda.XDAException
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.InflaterOutputStream

class XDADecorator {

    fun decorate(baseStream: InputStream?, ecs: ByteArray?): InputStream? {
        return null
    }

    fun decorate(file: File?, ecs: ByteArray): XDAInputStream {
        val `in`: XDAInputStream = XDAFileInputStream(file!!)
        return doDecorate(`in`, ecs)
    }

    @Throws(XDAException::class)
    fun decorate(path: String?, ecs: ByteArray): XDAInputStream {
        val file = File(path)
        if (!file.exists() || file.isDirectory) throw XDAException(XDAException.Companion.INVALID_FILE_PATH)
        return decorate(file, ecs)
    }

    fun decorate(buff: ByteArray, ecs: ByteArray): XDAInputStream {
        val `in`: XDAInputStream = XDAByteArrayInputStream(buff)
        return doDecorate(`in`, ecs)
    }

    fun InflateDecorate(
        baseStream: OutputStream?,
        ecs: ByteArray, index: Int
    ): OutputStream? {
        var index = index
        if (index < 0) return baseStream
        var result: OutputStream? = null
        when (ecs[index]) {
            0x02 -> result = InflaterOutputStream(baseStream)
            else -> {
            }
        }
        return InflateDecorate(result, ecs, --index)
    }

    private fun doDecorate(`in`: XDAInputStream, ecs: ByteArray): XDAInputStream {
        var `in` = `in`
        for (i in ecs.size - 2 downTo 0) {
            when (ecs[i]) {
                0x02 -> `in` = XDADeflaterInputStream(`in`)
                else -> {
                }
            }
        }
        return `in`
    }
}