/**
 * Title:	XDADeflaterInputStream
 * Description:	以DeflaterInputStream为底层，实现XDAFilterInputStream提供压缩功能
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.zip.DeflaterInputStream;

class XDADeflaterInputStream extends XDAFilterInputStream {
    private DeflaterInputStream delegate;

    XDADeflaterInputStream(XDAInputStream theBaseStream) {
        super(theBaseStream);
        delegate = null;
    }

    public int available() throws IOException {
        return delegate.available();
    }

    public void close() throws IOException {
        delegate.close();
    }

    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    public boolean markSupported() {
        return delegate.markSupported();
    }

    public int read() throws IOException {
        return delegate.read();
    }

    public int read(@Nonnull byte[] b) throws IOException {
        return delegate.read(b);
    }

    public int read(@Nonnull byte[] b, int off, int len) throws IOException {
        return delegate.read(b, off, len);
    }

    public void reset() throws IOException {
        delegate.reset();
    }

    public long skip(long n) throws IOException {
        return delegate.skip(n);
    }

    public boolean open() throws IOException {
        if (delegate != null)
            return false;
        if (!baseStream.open())
            return false;
        delegate = new DeflaterInputStream(baseStream);
        return true;
    }

    public XDAInputStream nirvana() throws IOException {
        if (delegate != null)
            delegate.close();

        baseStream = baseStream.nirvana();
        return new XDADeflaterInputStream(baseStream);
    }

    public boolean canRead() {
        return baseStream.canRead();
    }
}
