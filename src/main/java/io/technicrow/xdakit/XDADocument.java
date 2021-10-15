package io.technicrow.xdakit;

import io.technicrow.xdakit.model.XDAEntry;
import io.technicrow.xdakit.model.XDAHeader;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The XDA Document
 */
public class XDADocument implements XDA {

    private static final byte[] RIGHTS_INFO = {'@', 'X', 'D', 'A', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] ENTRY_CLASS_TYPE = {'C', '.', 'E', 'n'};
    private static final byte[] BIT_STREAM_CLASS_TYPE = {'C', '.', 'B', 'S'};

    private XDAHeader header;
    private List<XDAEntry> entries;
    private RandomAccessFile file;

    XDADocument() {
        this.header = new XDAHeader();
        this.entries = new LinkedList<>();
        this.file = null;
    }

    private XDADocument(RandomAccessFile file) throws IOException, XDAException {
        this.file = file;
        doParse();
    }

    public static XDADocument open(@Nonnull File file) throws IOException, XDAException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        return new XDADocument(new RandomAccessFile(file, "rw"));
    }

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public int getMajorVersion() {
        return this.header.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return this.header.getMinorVersion();
    }

    @Override
    public void close() throws IOException {
        if (this.file != null) {
            this.file.close();
        }
    }

    private void doParse() throws IOException, XDAException {
        parseHeader();
    }

    private void parseHeader() throws IOException, XDAException {
        file.seek(0);
        validateRightsInfo();
        byte majorVersion = file.readByte();
        byte minorVersion = file.readByte();
        int entryCount = Utils.readInt(file);
        byte entryNameTableType = file.readByte();
        if (entryNameTableType != 0x00) {
            throw new XDAException("Invalid entry nameTable type: " + entryNameTableType);
        }
        byte bitsParam = file.readByte();
        long firstEntryOffset = readFirstEntryOffset(bitsParam);
        this.header = new XDAHeader(majorVersion, minorVersion, entryCount, entryNameTableType, bitsParam, firstEntryOffset);
    }

    private void validateRightsInfo() throws IOException, XDAException {
        int length = RIGHTS_INFO.length;
        byte[] rightsInfo = new byte[length];
        if ((file.read(rightsInfo) != length) || !Arrays.equals(rightsInfo, RIGHTS_INFO)) {
            throw new XDAException("The file has an invalid rights info. It might not be a valid xda file");
        }
    }

    private long readFirstEntryOffset(byte bitsParam) throws IOException, XDAException {
        switch (bitsParam) {
            case 0x02:
                return Utils.readShort(file);
            case 0x04:
                return Utils.readInt(file);
            case 0x08:
                return Utils.readLong(file);
            default:
                throw new XDAException("Invalid bitsParam: " + bitsParam);
        }
    }
}
