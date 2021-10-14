package com.founderrd.xda;

/**
 * Header of an XDA file
 */
class XDAHeader {

    static final byte[] SIGNATURE = {'@', 'X', 'D', 'A', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static final int MAGIC_OFFSET = 0;
    static final int SIGNATURE_LENGTH = SIGNATURE.length;
    static final int ENTRY_COUNT_OFFSET = SIGNATURE_LENGTH + 2;
    static final int FIRST_ENTRY_OFFSET = ENTRY_COUNT_OFFSET + ENTRY_COUNT_LENGTH + 2;
    static final int ENTRY_COUNT_LENGTH = 4;
    static final int SIGNATURE_END = 4;

    private final byte majorVersion;
    private final byte minorVersion;
    private final byte entryNameTableType;
    private final byte bitsParam;
    private int entryCount;
    private long firstEntryOffset;

    XDAHeader() {
        this.majorVersion = 0x01;
        this.minorVersion = 0x00;
        this.entryCount = 0;
        this.entryNameTableType = 0x00;
        this.bitsParam = 0x04;
        this.firstEntryOffset = -1L;
    }

    XDAHeader(byte majorVersion, byte minorVersion, byte entryNameTableType, byte bitsParam) throws XDAException {
        this(majorVersion, minorVersion, entryNameTableType, bitsParam, 0, -1);
    }

    XDAHeader(byte majorVersion, byte minorVersion, byte entryNameTableType, byte bitsParam, int entryCount, long firstEntryOffset)
            throws XDAException {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.entryCount = Math.max(0, entryCount);
        this.entryNameTableType = validateEntryNameTableType(entryNameTableType);
        this.bitsParam = validateBitParam(bitsParam);
        this.firstEntryOffset = firstEntryOffset;
    }

    public final byte getMajorVersion() {
        return majorVersion;
    }

    public final byte getMinorVersion() {
        return minorVersion;
    }

    public final int getEntryCount() {
        return entryCount;
    }

    public void setEntryCount(int entryCount) {
        this.entryCount = entryCount;
    }

    public final byte getEntryNameTableType() {
        return entryNameTableType;
    }

    public final byte getBitsParam() {
        return bitsParam;
    }

    public final long getFirstEntryOffset() {
        return firstEntryOffset;
    }

    public void setFirstEntryOffset(long firstEntryOffset) {
        this.firstEntryOffset = firstEntryOffset;
    }

    private byte validateEntryNameTableType(byte entryNameTableType) throws XDAException {
        if ((entryNameTableType != 0x00) && (entryNameTableType != 0x08)) {
            throw new XDAException("Invalid entry name table type: " + entryNameTableType);
        }
        return entryNameTableType;
    }

    private byte validateBitParam(byte bitsParam) throws XDAException {
        if ((bitsParam != 0x02) && (bitsParam != 0x04) && (bitsParam != 0x08)) {
            if (bitsParam == 0x00) {
                return 0x02;
            } else {
                throw new XDAException("Invalid bitsParam: " + bitsParam);
            }
        } else {
            return bitsParam;
        }
    }
}
