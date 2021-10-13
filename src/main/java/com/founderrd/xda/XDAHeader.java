/**
 * Title:	XDAHeader
 * Description:	提供解析XDA文件header，获取header信息功能。
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

import java.io.IOException;
import java.io.RandomAccessFile;

class XDAHeader {

    private static final byte[] MAGIC = {'@', 'X', 'D', 'A', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private static final int MAGIC_OFFSET = 0;
    private static final int MAGIC_LENGTH = MAGIC.length;
    private static final int ENTRY_COUNT_OFFSET = MAGIC_LENGTH + 2;
    private static final int FIRST_ENTRY_OFFSET = ENTRY_COUNT_OFFSET + ENTRY_COUNT_LENGTH + 2;
    private static final int MAGIC_END = 4;
    private static final int ENTRY_COUNT_LENGTH = 4;
    private byte majorVersion;
    private byte minorVersion;
    private int entryCount;
    private byte entryNameTableType;
    private byte bitsParam;
    private long firstEntryOffset;
    private boolean parsed;

    XDAHeader() {
        parsed = false;
    }

    void create(byte theMajorVersion, byte theMinorVersion,
                byte theEntryNameTableType, byte theBitsParam) throws XDAException {
        majorVersion = theMajorVersion;
        minorVersion = theMinorVersion;
        entryCount = 0;
        entryNameTableType = theEntryNameTableType;
        parseBitParam(theBitsParam);
        firstEntryOffset = -1;
        parsed = true;
    }

    void write(RandomAccessFile theXDAFile) throws IOException {
        theXDAFile.seek(MAGIC_OFFSET);
        writeRightsInfo(theXDAFile);
        writeMajorVersion(theXDAFile);
        writeMinorVersion(theXDAFile);
        writeEntryCount(theXDAFile);
        writeEntryNameTableType(theXDAFile);
        writeBitsParam(theXDAFile);
        writeFirstEntryOffset(theXDAFile);
    }

    void writeBackEntryCount(RandomAccessFile theXDAFile, int theEntryCount)
            throws IOException {
        entryCount = theEntryCount;
        theXDAFile.seek(ENTRY_COUNT_OFFSET);
        writeEntryCount(theXDAFile);
    }

    void writeBackFirstEntryOffset(RandomAccessFile theXDAFile,
                                   long theFirstEntryOffset) throws IOException {
        firstEntryOffset = theFirstEntryOffset;
        theXDAFile.seek(FIRST_ENTRY_OFFSET);
        XDACommonFunction.writeIntegerAccording2BitsParam(theXDAFile,
                bitsParam, firstEntryOffset);
    }

    void parse(RandomAccessFile fileXDA) throws IOException, XDAException {
        fileXDA.seek(MAGIC_OFFSET);
        parseRigthsInfo(fileXDA);
        parseMajorVersion(fileXDA);
        parseMinorVersion(fileXDA);
        parseEntryCount(fileXDA);
        parseEntryNameTableType(fileXDA);
        parseBitParam(fileXDA);
        parseFirstEntryOffset(fileXDA);
        parsed = true;
    }

    public final byte getMajorVersion() throws XDAException {
        if (!parsed)
            throw new XDAException(XDAException.NEVER_PARSE_HEADER);
        return majorVersion;
    }

    public final byte getMinorVersion() throws XDAException {
        if (!parsed)
            throw new XDAException(XDAException.NEVER_PARSE_HEADER);
        return minorVersion;
    }

    public final int getEntryCount() throws XDAException {
        if (!parsed)
            throw new XDAException(XDAException.NEVER_PARSE_HEADER);
        return entryCount;
    }

    public final byte getEntryNameTableType() throws XDAException {
        if (!parsed)
            throw new XDAException(XDAException.NEVER_PARSE_HEADER);
        return entryNameTableType;
    }

    public final byte getBitsParam() throws XDAException {
        if (!parsed)
            throw new XDAException(XDAException.NEVER_PARSE_HEADER);
        return bitsParam;
    }

    public final long getFirstEntryOffset() throws XDAException {
        if (!parsed)
            throw new XDAException(XDAException.NEVER_PARSE_HEADER);
        return firstEntryOffset;
    }

    private void parseRigthsInfo(RandomAccessFile fileXDA)
            throws IOException, XDAException {
        // 读rightsInfo
        byte[] rightsInfo = new byte[MAGIC_LENGTH];
        if (fileXDA.read(rightsInfo) != MAGIC_LENGTH)
            throw new XDAException(XDAException.INVALID_RIGHT_INFO);
        for (int i = 0; i < MAGIC_END; ++i)
            if (rightsInfo[i] != MAGIC[i])
                throw new XDAException(XDAException.INVALID_RIGHT_INFO);
    }

    private void parseMajorVersion(RandomAccessFile fileXDA)
            throws IOException {
        majorVersion = fileXDA.readByte();
    }

    private void parseMinorVersion(RandomAccessFile fileXDA)
            throws IOException {
        minorVersion = fileXDA.readByte();
    }

    private void parseEntryCount(RandomAccessFile fileXDA)
            throws IOException {
        entryCount = XDACommonFunction.readInt(fileXDA);
    }

    private void parseEntryNameTableType(RandomAccessFile fileXDA)
            throws IOException, XDAException {
        entryNameTableType = fileXDA.readByte();
        switch (entryNameTableType) {
            case 0x00:
            case 0x08:
                return;
            default:
                throw new XDAException(XDAException.INVALID_ENTRYNAMETABLETYPE);
        }
    }

    private void parseBitParam(RandomAccessFile fileXDA)
            throws IOException, XDAException {
        byte theBitsParam = fileXDA.readByte();
        parseBitParam(theBitsParam);
    }

    private void parseBitParam(byte theBitsParam) throws XDAException {
        bitsParam = theBitsParam;
        if (bitsParam == 0x00)
            bitsParam = 0x02;

        switch (bitsParam) {
            case 0x02:
            case 0x04:
            case 0x08:
                return;
            default:
                throw new XDAException(XDAException.INVALID_BITSPARAM);
        }
    }

    private void parseFirstEntryOffset(RandomAccessFile fileXDA)
            throws IOException, XDAException {
        switch (bitsParam) {
            case 0x02:
                firstEntryOffset = XDACommonFunction.readShort(fileXDA);
                break;
            case 0x04:
                firstEntryOffset = XDACommonFunction.readInt(fileXDA);
                break;
            case 0x08:
                firstEntryOffset = XDACommonFunction.readLong(fileXDA);
                break;
            default:
                throw new XDAException(XDAException.INVALID_BITSPARAM);
        }
    }

    private void writeRightsInfo(RandomAccessFile theXDAFile)
            throws IOException {
        theXDAFile.write(MAGIC);
    }

    private void writeMajorVersion(RandomAccessFile theXDAFile)
            throws IOException {
        theXDAFile.writeByte(majorVersion);
    }

    private void writeMinorVersion(RandomAccessFile theXDAFile)
            throws IOException {
        theXDAFile.writeByte(minorVersion);
    }

    private void writeEntryCount(RandomAccessFile theXDAFile)
            throws IOException {
        XDACommonFunction.writeInt(theXDAFile, entryCount);
    }

    private void writeEntryNameTableType(RandomAccessFile theXDAFile)
            throws IOException {
        theXDAFile.writeByte(entryNameTableType);
    }

    private void writeBitsParam(RandomAccessFile theXDAFile)
            throws IOException {
        theXDAFile.writeByte(bitsParam);
    }

    private void writeFirstEntryOffset(RandomAccessFile theXDAFile)
            throws IOException {
        XDACommonFunction.writeIntegerAccording2BitsParam(theXDAFile,
                bitsParam, firstEntryOffset);
    }
}
