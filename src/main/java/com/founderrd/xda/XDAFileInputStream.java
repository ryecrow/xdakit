/**
 * Title:	XDAFileInputStream
 * Description:	以FileInputStream为底层实现XDAInputStream
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

class XDAFileInputStream extends XDAInputStream {
    private final File file;
    private FileInputStream fileStream;

    XDAFileInputStream(File theFile) {
        file = theFile;
        fileStream = null;
    }

    public int available() throws IOException {
        return fileStream.available();
    }

    public void close() throws IOException {
        fileStream.close();
    }

    public void mark(int readlimit) {
        fileStream.mark(readlimit);
    }

    public boolean markSupported() {
        return fileStream.markSupported();
    }

    public int read() throws IOException {
        return fileStream.read();
    }

    public int read(@Nonnull byte[] b) throws IOException {
        return fileStream.read(b);
    }

    public int read(@Nonnull byte[] b, int off, int len) throws IOException {
        return fileStream.read(b, off, len);
    }

    public void reset() throws IOException {
        fileStream.reset();
    }

    public long skip(long n) throws IOException {
        return fileStream.skip(n);
    }

    public boolean open() throws IOException {
        if (fileStream != null)
            return false;
        fileStream = new FileInputStream(file);
        return true;
    }

    public XDAInputStream nirvana() throws IOException {
        if (fileStream != null)
            fileStream.close();

        return new XDAFileInputStream(file);
    }

    public boolean canRead() {
        return (fileStream != null);
    }
}
