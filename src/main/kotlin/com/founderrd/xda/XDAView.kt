package com.founderrd.xda

/**
 * 定义视图抽象父类
 *
 * @author 杨天航(tianhang.yang@gmail.com)
 * @version 1.0
 */
abstract class XDAView {
    abstract fun update(path: String, operator: Byte)
}