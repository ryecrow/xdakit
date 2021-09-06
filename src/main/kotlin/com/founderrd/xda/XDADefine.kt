/**
 * Title:	XDADefine
 * Description:	基本常量定义
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

interface XDADefine {
    companion object {
        /**
         * 定义操作常量
         */
        const val OPERATOR_NEW: Byte = 0x01
        const val OPERATOR_APPEND: Byte = 0x02
        const val OPERATOR_REPLACE: Byte = 0x03
        const val OPERATOR_DELETE: Byte = 0x04
        const val OPERATOR_END: Byte = 0x0f
        const val OPERATOR_MASK: Byte = 0x0f

        /**
         * 定义ECS常量
         */
        const val ECS_END_FLAG = 0xff.toByte()
        const val ECS_ZIP_FLAG = 0x02.toByte()
        const val ECS_XMI_FLAG = 0x10.toByte()

        /**
         * 定义ECS缓冲容量
         */
        const val ECS_BUFFER_CAPACITY = 0x10.toByte()
    }
}