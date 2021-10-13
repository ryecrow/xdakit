package com.founderrd.xda;

/**
 * Title:	XDACommonFunction
 * Description:	提供一些公共函数
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */


import com.founderrd.xda.XDADecorator;
import com.founderrd.xda.XDAException;
import com.founderrd.xda.XDAInputStream;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

class Utils {

    private Utils() {
        throw new AssertionError("No instance of Utils for you!");
    }

    // RandomAccessFile类型接口
    public static long readLong(RandomAccessFile theFile) throws IOException {
        byte b1 = theFile.readByte();
        byte b2 = theFile.readByte();
        byte b3 = theFile.readByte();
        byte b4 = theFile.readByte();
        byte b5 = theFile.readByte();
        byte b6 = theFile.readByte();
        byte b7 = theFile.readByte();
        byte b8 = theFile.readByte();

        return ((long) b8 << 56) + ((long) b7 << 48) + ((long) b6 << 40)
                + ((long) b5 << 32) + ((long) b4 << 24) + ((long) b3 << 16)
                + ((long) b2 << 8) + b1;

    }

    public static int readInt(RandomAccessFile theFile) throws IOException {
        byte b1 = theFile.readByte();
        byte b2 = theFile.readByte();
        byte b3 = theFile.readByte();
        byte b4 = theFile.readByte();

        return ((b4 << 24) & 0xff000000) + ((b3 << 16) & 0x00ff0000)
                + ((b2 << 8) & 0x0000ff00) + (b1 & 0x000000ff);
    }

    public static short readShort(RandomAccessFile theFile) throws IOException {
        byte b1 = theFile.readByte();
        byte b2 = theFile.readByte();

        return (short) ((b2 << 8) + b1);

    }

    // InputStream类型接口
    public static short readShort(InputStream theStream) throws IOException {
        byte[] b = new byte[2];
        theStream.read(b);

        return (short) (((b[1] << 8) & 0xff00) + (b[0] & 0x00ff));
    }

    public static int readInt(InputStream theStream) throws IOException {
        byte[] b = new byte[4];
        theStream.read(b, 0, 4);

        return ((b[3] << 24) & 0xff000000) + ((b[2] << 16) & 0x00ff0000)
                + ((b[1] << 8) & 0x0000ff00) + (b[0] & 0x000000ff);
    }

    public static long readLong(InputStream theStream) throws IOException {
        byte[] b = new byte[8];
        theStream.read(b);
        return -1;
        //
        //
        //
        // return ((long)b[7] << 56) & (long)0xff000000 << 24;
        // + ((long)b[6] << 48)
        // + ((long)b[5] << 40) + ((long)b[4] << 32)
        // + ((long)b[3] << 24) + ((long)b[2] << 16)
        // + ((long)b[1] << 8) + b[0];

    }

    public static String readUTF8(InputStream theStream, int length)
            throws IOException {
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
        theStream.read(b);

        byte tmp;
        for (int i = 0; i < bytes / 2; ++i) {
            tmp = b[i];
            b[i] = b[bytes - i - 1];
            b[bytes - i - 1] = tmp;
        }

        return new BigInteger(b);
    }

    public static long readIntegerAccording2BitsParam(InputStream theStream,
                                                      byte bitsParam) throws IOException {
        switch (bitsParam) {
            case 0x02:
                return readShort(theStream);
            case 0x08:
                return readLong(theStream);
            default:
                return readInt(theStream);
        }
    }

    public static long readIntegerAccording2BitsParam(RandomAccessFile theFile,
                                                      byte bitsParam) throws IOException {
        switch (bitsParam) {
            case 0x02:
                return readShort(theFile);
            case 0x08:
                return readLong(theFile);
            default:
                return readInt(theFile);
        }
    }

    public static byte[] readByteTillFlag(RandomAccessFile theFile, int length,
                                          byte flag) throws IOException {
        byte[] buffer = new byte[length];
        boolean valid = false;

        int i = 0;
        for (; i < length; ++i) {
            buffer[i] = theFile.readByte();
            if (buffer[i] == flag) {
                valid = true;
                ++i;
                break;
            }
        }

        if (!valid)
            return null;

        byte[] returnBytesArray = new byte[i];
        System.arraycopy(buffer, 0, returnBytesArray, 0, i);
        return returnBytesArray;
    }

    public static boolean compareArray(final byte[] a, final byte[] b) {
        if (a.length != b.length)
            return false;

        for (int i = 0; i < a.length; ++i) {
            if (a[i] != b[i])
                return false;
        }

        return true;
    }

    public static long copyFromSrcToDst(InputStream src, OutputStream dst,
                                        long length, byte[] buffer) throws IOException, XDAException {
        long actualRead = 0;
        while (length > 0) {
            int readBytes;
            if (length > buffer.length)
                readBytes = src.read(buffer);
            else
                readBytes = src.read(buffer, 0, (int) length);

            if (readBytes == -1)
                throw new XDAException(XDAException.XDACOMMONFUNCTION_ERROR);

            dst.write(buffer, 0, readBytes);
            length -= readBytes;
            actualRead += readBytes;
        }
        return actualRead;
    }

    public static long copyFromSrcToDst(InputStream src, OutputStream dst,
                                        byte[] buffer) throws IOException {
        long actualRead = 0;
        int readBytes;
        readBytes = src.read(buffer);
        while (readBytes != -1) {
            dst.write(buffer, 0, readBytes);
            actualRead += readBytes;
            readBytes = src.read(buffer);
        }
        return actualRead;
    }

    public static long copyFromSrcToDst(XDAInputStream src, OutputStream dst,
                                        byte[] buffer) throws IOException {
        if (!src.canRead())
            return 0;

        long actualRead = 0;
        int readBytes;
        readBytes = src.read(buffer);
        while (readBytes != -1) {
            dst.write(buffer, 0, readBytes);
            actualRead += readBytes;
            readBytes = src.read(buffer);
        }
        return actualRead;
    }

    public static long copyFromSrcToDst(XDAInputStream src,
                                        RandomAccessFile dst, byte[] buffer) throws IOException {
        if (!src.canRead())
            return 0;

        long actualRead = 0;
        int readBytes;
        readBytes = src.read(buffer);
        while (readBytes != -1) {
            dst.write(buffer, 0, readBytes);
            actualRead += readBytes;
            readBytes = src.read(buffer);
        }
        return actualRead;
    }

    public static long copyFromSrcToDst(XDAInputStream src,
                                        RandomAccessFile dst, byte[] buffer, byte[] checkSum)
            throws IOException {
        if (!src.canRead())
            return 0;

        long actualRead = 0;
        int readBytes;

        readBytes = src.read(buffer);
        while (readBytes != -1) {
            checkSum[0] ^= calcCheckSum(buffer, 0, readBytes);
            dst.write(buffer, 0, readBytes);
            actualRead += readBytes;
            readBytes = src.read(buffer);
        }

        return actualRead;
    }

    public static byte calcCheckSum(byte[] src, int from, int length) {
        byte checksum = 0x00;
        for (; from < length; ++from)
            checksum ^= src[from];
        return checksum;
    }

    public static long copyFromSrcToDst(InputStream src, RandomAccessFile dst,
                                        byte[] buffer, MessageDigest md) throws IOException {
        long totalWrite = 0;
        int readBytes = src.read(buffer);
        while (readBytes != -1) {
            if (md != null)
                md.update(buffer, 0, readBytes);
            dst.write(buffer, 0, readBytes);
            totalWrite += readBytes;
            readBytes = src.read(buffer);
        }
        return totalWrite;
    }

    public static long copyFromSrcToDst(byte[] src, RandomAccessFile dst, MessageDigest md) throws IOException {
        dst.write(src);
        if (md != null)
            md.update(src, 0, src.length);
        return src.length;
    }

    public static long copyFromSrcToDst(RandomAccessFile src, OutputStream dst,
                                        long length, byte[] buffer) throws IOException, XDAException {
        long actualRead = 0;
        int readBytes;
        while (length > 0) {
            if (length > buffer.length)
                readBytes = src.read(buffer);
            else
                readBytes = src.read(buffer, 0, (int) length);

            if (readBytes == -1)
                throw new XDAException(XDAException.XDACOMMONFUNCTION_ERROR);

            dst.write(buffer, 0, readBytes);
            length -= readBytes;
            actualRead += readBytes;
        }
        return actualRead;
    }

    public static long copyFromSrcToDst(RandomAccessFile src, OutputStream dst,
                                        long length, byte[] buffer, byte[] checkSum) throws IOException,
            XDAException {
        long actualRead = 0;
        int readBytes;
        while (length > 0) {
            if (length > buffer.length)
                readBytes = src.read(buffer);
            else
                readBytes = src.read(buffer, 0, (int) length);

            if (readBytes == -1)
                throw new XDAException(XDAException.XDACOMMONFUNCTION_ERROR);

            checkSum[0] ^= calcCheckSum(buffer, 0, readBytes);
            dst.write(buffer, 0, readBytes);
            length -= readBytes;
            actualRead += readBytes;
        }
        return actualRead;
    }

    public static long copyFromSrcToDst(RandomAccessFile src,
                                        RandomAccessFile dst, long length, byte[] buffer)
            throws IOException, XDAException {
        long actualRead = 0;
        int readBytes;
        while (length > 0) {
            if (length > buffer.length)
                readBytes = src.read(buffer);
            else
                readBytes = src.read(buffer, 0, (int) length);

            if (readBytes == -1)
                throw new XDAException(XDAException.XDACOMMONFUNCTION_ERROR);

            dst.write(buffer, 0, readBytes);
            length -= readBytes;
            actualRead += readBytes;
        }
        return actualRead;
    }

    public static long copyFromSrcToDst(RandomAccessFile src,
                                        RandomAccessFile dst, long length, byte[] buffer, byte[] checkSum)
            throws IOException, XDAException {
        long actualRead = 0;
        int readBytes;
        while (length > 0) {
            if (length > buffer.length)
                readBytes = src.read(buffer);
            else
                readBytes = src.read(buffer, 0, (int) length);

            checkSum[0] ^= calcCheckSum(buffer, 0, readBytes);
            if (readBytes == -1)
                throw new XDAException(XDAException.XDACOMMONFUNCTION_ERROR);

            dst.write(buffer, 0, readBytes);
            length -= readBytes;
            actualRead += readBytes;
        }
        return actualRead;
    }

    public static void skipTillFlag(RandomAccessFile theFile, byte flag)
            throws IOException {
        byte read;
        while (true) {
            read = theFile.readByte();
            if (read == flag)
                return;
        }
    }

    public static void writeIntegerAccording2BitsParam(
            RandomAccessFile theFile, byte bitsParam, long integer)
            throws IOException {
        switch (bitsParam) {
            case 0x02:
                writeShort(theFile, integer);
            case 0x08:
                writeLong(theFile, integer);
            default:
                writeInt(theFile, integer);
        }
    }

    public static void writeIntegerAccording2BitsParam(OutputStream theStream,
                                                       byte bitsParam, long integer) throws IOException {
        switch (bitsParam) {
            case 0x02:
                writeShort(theStream, integer);
            case 0x08:
                writeLong(theStream, integer);
            default:
                writeInt(theStream, integer);
        }
    }

    public static void writeShort(OutputStream theStream, long integer)
            throws IOException {
        byte[] b = convertEndian(integer, 2);
        theStream.write(b, 0, b.length);
    }

    public static void writeInt(OutputStream theStream, long integer)
            throws IOException {
        byte[] b = converIntBigEndian2LittleEndian((int) integer);
        theStream.write(b, 0, b.length);
    }

    public static void writeLong(OutputStream theStream, long integer)
            throws IOException {
        byte[] b = convertEndian(integer, 8);
        theStream.write(b, 0, b.length);
    }

    public static void writeShort(RandomAccessFile theFile, long integer)
            throws IOException {
        byte[] b = convertEndian(integer, 2);
        theFile.write(b, 0, b.length);
    }

    public static void writeInt(RandomAccessFile theFile, long integer)
            throws IOException {
        byte[] b = converIntBigEndian2LittleEndian((int) integer);
        theFile.write(b, 0, b.length);
    }

    public static void writeLong(RandomAccessFile theFile, long integer)
            throws IOException {
        byte[] b = convertEndian(integer, 8);
        theFile.write(b, 0, b.length);
    }

    public static byte[] convertEndian(long integer, int bits) {
        byte[] b = new byte[bits];
        for (int i = 0; i < bits; ++i)
            b[i] = (byte) (integer << (i * 8));
        return b;
    }

    public static byte[] converIntBigEndian2LittleEndian(int integer) {
        byte[] b = new byte[4];

        b[3] = (byte) ((integer & 0xff000000) >>> 24);
        b[2] = (byte) ((integer & 0xff0000) >>> 16);
        b[1] = (byte) ((integer & 0xff00) >>> 8);
        b[0] = (byte) (integer & 0xff);

        return b;
    }

    public static XDAInputStream createXDAInputStream(File theFile, byte[] ecs,
                                                      XDADecorator decorator) {
        return null;
    }
}
