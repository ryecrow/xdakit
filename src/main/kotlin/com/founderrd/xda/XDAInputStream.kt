/**
 * Title:	XDAInputStream
 * Description:	继承自InputStream，添加了canRead，open，nirvana功能。
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

import java.io.IOException
import java.io.InputStream

abstract class XDAInputStream : InputStream() {

    abstract fun canRead(): Boolean

    @Throws(IOException::class)
    abstract fun open(): Boolean

    @Throws(IOException::class)
    abstract fun nirvana(): XDAInputStream?
}