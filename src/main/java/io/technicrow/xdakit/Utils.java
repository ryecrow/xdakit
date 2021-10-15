package io.technicrow.xdakit;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

class Utils {

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

    public static String readUTF8(InputStream theStream, int length) throws IOException {
        byte[] utfString = new byte[length];
        int temp;
        boolean valid = false;
        int i;
        for (i = 0; i < length; ++i) {
            temp = theStream.read();
            if (temp == -1)
                return null;
            utfString[i] = (byte) temp;
            if (utfString[i] == 0) {
                valid = true;
                break;
            }
        }

        if (!valid)
            return null;

        return new String(utfString, 0, i, StandardCharsets.UTF_8);
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
}
