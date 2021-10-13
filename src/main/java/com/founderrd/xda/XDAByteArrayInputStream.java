/**
 * Title:	XDAByteArrayInputStream
 * Description:	以ByteArrayInputStream为底层实现XDAInputStream
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;

class XDAByteArrayInputStream extends XDAInputStream {
    private final byte[] buffer;
    private ByteArrayInputStream buffStream;

    XDAByteArrayInputStream(byte[] buffer) {
        this.buffer = buffer;
        buffStream = null;
    }

    public int available() throws IOException {
        return buffStream.available();
    }

    public void close() throws IOException {
        buffStream.close();
    }

    public void mark(int readlimit) {
        buffStream.mark(readlimit);
    }

    public boolean markSupported() {
        return buffStream.markSupported();
    }

    public int read() throws IOException {
        return buffStream.read();
    }

    public int read(@Nonnull byte[] b) throws IOException {
        return buffStream.read(b);
    }

    public int read(@Nonnull byte[] b, int off, int len) throws IOException {
        return buffStream.read(b, off, len);
    }

    public void reset() throws IOException {
        buffStream.reset();
    }

    public long skip(long n) throws IOException {
        return buffStream.skip(n);
    }

    public boolean open() throws IOException {
        if (buffStream != null)
            return false;
        buffStream = new ByteArrayInputStream(this.buffer);
        return true;
    }

    public XDAInputStream nirvana() throws IOException {
        if (buffStream != null)
            buffStream.close();

        return new XDAByteArrayInputStream(this.buffer);
    }

    public boolean canRead() {
        return (buffStream != null);
    }
}
