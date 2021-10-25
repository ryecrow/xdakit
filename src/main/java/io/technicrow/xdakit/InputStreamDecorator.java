package io.technicrow.xdakit;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

/**
 * Helper class for decorating (maybe inflated) input streams
 */
public class InputStreamDecorator extends InputStream {

    private final InputStream source;

    public InputStreamDecorator(InputStream source, boolean shouldDeflate) {
        if (shouldDeflate) {
            this.source = new InflaterInputStream(source);
        } else {
            this.source = source;
        }
    }

    InputStreamDecorator(InputStream source, byte[] ecs) throws IOException, XDAException {
        if ((ecs == null) || (ecs.length < 1)) {
            this.source = source;
        } else {
            InputStream result = source;
            for (int i = ecs.length - 1; i >= 0; i--) {
                byte encryption = ecs[i];
                if (encryption == 0) {
                    continue;
                }
                switch (encryption) {
                    case 0x02:
                        result = new InflaterInputStream(result);
                        break;
                    case 0x10:
                        result = new BZip2CompressorInputStream(result);
                        break;
                    default:
                        throw new XDAException("Invalid encryption mark: " + Integer.toHexString(encryption));
                }
            }
            this.source = result;
        }
    }

    @Override
    public int read() throws IOException {
        return source.read();
    }

    @Override
    public int read(@Nonnull byte[] b) throws IOException {
        return source.read(b);
    }

    @Override
    public int read(@Nonnull byte[] b, int off, int len) throws IOException {
        return source.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return source.skip(n);
    }

    @Override
    public int available() throws IOException {
        return source.available();
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        source.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        source.reset();
    }

    @Override
    public boolean markSupported() {
        return source.markSupported();
    }
}
