package io.technicrow.xdakit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class Utils {

    private static final int MAX_STRING_SIZE = 2048;

    private Utils() {
        throw new AssertionError("No instance of Utils for you!");
    }

    public static long readLong(RandomAccessFile file) throws IOException {
        byte[] b = new byte[8];
        if (file.read(b) != 8) {
            throw new IOException("Cannot read long from file");
        }
        return ((long) b[7] << 56) + ((long) b[6] << 48) + ((long) b[5] << 40)
                + ((long) b[4] << 32) + ((long) b[3] << 24) + ((long) b[2] << 16)
                + ((long) b[1] << 8) + b[0];
    }

    public static int readInt(RandomAccessFile file) throws IOException {
        byte[] b = new byte[4];
        if (file.read(b) != 4) {
            throw new IOException("Cannot read int from file");
        }
        return ((b[3] << 24) & 0xff000000) + ((b[2] << 16) & 0x00ff0000)
                + ((b[1] << 8) & 0x0000ff00) + (b[0] & 0x000000ff);
    }

    public static short readShort(RandomAccessFile file) throws IOException {
        byte[] b = new byte[2];
        if (file.read(b) != 2) {
            throw new IOException("Cannot read short from file");
        }
        return (short) ((b[1] << 8) + (b[0] & 0xff));
    }

    public static long readLong(InputStream in) throws IOException {
        byte[] b = new byte[8];
        if (in.read(b) != 8) {
            throw new IOException("Cannot read long from input stream");
        }
        return ((long) b[7] << 56) + ((long) b[6] << 48) + ((long) b[5] << 40)
                + ((long) b[4] << 32) + ((long) b[3] << 24) + ((long) b[2] << 16)
                + ((long) b[1] << 8) + b[0];
    }

    public static int readInt(InputStream in) throws IOException {
        byte[] b = new byte[4];
        if (in.read(b) != 4) {
            throw new IOException("Cannot read int from input stream");
        }
        return ((b[3] << 24) & 0xff000000) + ((b[2] << 16) & 0x00ff0000)
                + ((b[1] << 8) & 0x0000ff00) + (b[0] & 0x000000ff);
    }

    public static short readShort(InputStream in) throws IOException {
        byte[] b = new byte[2];
        if (in.read(b) != 2) {
            throw new IOException("Cannot read short from input stream");
        }
        return (short) ((b[1] << 8) + (b[0] & 0xff));
    }

    public static String readString(InputStream source) throws IOException {
        boolean valid = false;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int i = 0;
            int b;
            while ((i < MAX_STRING_SIZE) && ((b = source.read()) != -1)) {
                if (b == 0) {
                    valid = true;
                    break;
                }
                baos.write(b);
                i++;
            }
            if (valid) {
                return new String(baos.toByteArray(), 0, i, StandardCharsets.UTF_8);
            }
            throw new IOException("Unfinished string");
        }
    }

    public static BigInteger readBigInteger(InputStream theStream, int bytes)
            throws IOException {
        byte[] b = new byte[bytes];
        if (theStream.read(b) != bytes) {
            throw new IOException("Cannot read big integer");
        }

        byte tmp;
        for (int i = 0; i < bytes / 2; ++i) {
            tmp = b[i];
            b[i] = b[bytes - i - 1];
            b[bytes - i - 1] = tmp;
        }

        return new BigInteger(b);
    }

    public static long readByBitsParam(RandomAccessFile file, byte bitsParam) throws IOException {
        switch (bitsParam) {
            case 0x02:
                return Utils.readShort(file);
            case 0x04:
                return Utils.readInt(file);
            case 0x08:
                return Utils.readLong(file);
            default:
                throw new IllegalArgumentException("Invalid bitsParam: " + bitsParam);
        }
    }

    public static long readByBitsParam(InputStream source, byte bitsParam) throws IOException {
        switch (bitsParam) {
            case 0x02:
                return Utils.readShort(source);
            case 0x04:
                return Utils.readInt(source);
            case 0x08:
                return Utils.readLong(source);
            default:
                throw new IllegalArgumentException("Invalid bitsParam: " + bitsParam);
        }
    }
}
