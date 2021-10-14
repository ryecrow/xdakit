/**
 * Title:	XDADecorator
 * Description:	实现一个装饰类，按要求装饰流
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.InflaterOutputStream;

class XDADecorator {
    public InputStream decorate(InputStream baseStream, byte[] ecs) {
        return null;
    }

    public XDAInputStream decorate(File file, byte[] ecs) {
        XDAInputStream in = new XDAFileInputStream(file);
        return doDecorate(in, ecs);
    }

    public XDAInputStream decorate(String path, byte[] ecs) throws FooE {
        File file = new File(path);
        if (!file.exists() || file.isDirectory())
            throw new FooE(FooE.INVALID_FILE_PATH);

        return decorate(file, ecs);
    }

    public XDAInputStream decorate(byte[] buff, byte[] ecs) {
        XDAInputStream in = new XDAByteArrayInputStream(buff);
        return doDecorate(in, ecs);
    }

    public OutputStream InflateDecorate(OutputStream baseStream,
                                        final byte[] ecs, int index) {
        if (index < 0)
            return baseStream;

        OutputStream result = null;
        switch (ecs[index]) {
            case 0x02:
                result = new InflaterOutputStream(baseStream);
                break;
            default:
                break;
        }

        return InflateDecorate(result, ecs, --index);
    }

    private XDAInputStream doDecorate(XDAInputStream in, byte[] ecs) {
        for (int i = ecs.length - 2; i >= 0; --i) {
            switch (ecs[i]) {
                case 0x02:
                    in = new XDADeflaterInputStream(in);
                    break;
                default:
                    break;
            }
        }

        return in;
    }
}
