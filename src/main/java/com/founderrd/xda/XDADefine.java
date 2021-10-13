/**
 * Title:	XDADefine
 * Description:	基本常量定义
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

public interface XDADefine {
    /**
     * 定义操作常量
     */
    byte OPERATOR_NEW = 0x01;
    byte OPERATOR_APPEND = 0x02;
    byte OPERATOR_REPLACE = 0x03;
    byte OPERATOR_DELETE = 0x04;
    byte OPERATOR_END = 0x0f;
    byte OPERATOR_MASK = 0x0f;

    /**
     * 定义ECS常量
     */
    byte ECS_END_FLAG = (byte) 0xff;
    byte ECS_ZIP_FLAG = (byte) 0x02;
    byte ECS_XMI_FLAG = (byte) 0x10;

    /**
     * 定义ECS缓冲容量
     */
    byte ECS_BUFFER_CAPACITY = (byte) 0x10;

}
