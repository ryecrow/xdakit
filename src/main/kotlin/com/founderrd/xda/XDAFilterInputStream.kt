/**
 * Title:	XDAFilterInputStream
 * Description:	定义一个fileter继承于XDAInputStream。提供了和FilterInputStream类似的功能
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

import java.io.IOException

/**
 *
 */
internal abstract class XDAFilterInputStream(protected var baseStream: XDAInputStream) : XDAInputStream() {

    @Throws(IOException::class)
    override fun open(): Boolean {
        return baseStream.open()
    }

    override fun canRead(): Boolean {
        return baseStream.canRead()
    }
}