/**
 * Title:	XDAFilterInputStream
 * Description:	定义一个fileter继承于XDAInputStream。提供了和FilterInputStream类似的功能
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

import java.io.IOException;

abstract class XDAFilterInputStream extends XDAInputStream {
    protected XDAInputStream baseStream;

    XDAFilterInputStream(XDAInputStream theBaseStream) {
        baseStream = theBaseStream;
    }

    public boolean open() throws IOException {
        return baseStream.open();
    }

    public boolean canRead() {
        return baseStream.canRead();
    }
}
