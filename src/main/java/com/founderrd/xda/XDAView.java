/**
 * Title:	XDAView
 * Description:	定义视图抽象父类
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

public abstract class XDAView {
    abstract void update(String path, byte operator);
}
