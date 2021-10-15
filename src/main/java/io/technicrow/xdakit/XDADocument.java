package io.technicrow.xdakit;

import io.technicrow.xdakit.constant.Operator;
import io.technicrow.xdakit.model.*;
import lombok.Value;

import javax.annotation.Nonnull;
import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.InflaterInputStream;

/**
 * The XDA Document
 */
public class XDADocument implements XDA {

    private static final byte[] RIGHTS_INFO = {'@', 'X', 'D', 'A', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] ENTRY_CLASS_TYPE = {'C', '.', 'E', 'n'};
    private static final byte[] BIT_STREAM_CLASS_TYPE = {'C', '.', 'B', 'S'};
    private static final int CHECKSUM_LENGTH = 16;
    private static final int NAME_VALUE_LENGTH = 16;
    private static final byte NAME_TABLE_COMPRESS_MASK = 0x01;
    private static final byte ITEM_LIST_COMPRESS_MASK = 0x02;

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
        try {
            doParse();
        } catch (IOException | XDAException e) {
            file.close();
            throw e;
        }
    }

    public static XDADocument open(@Nonnull String filePath) throws IOException, XDAException {
        File file = new File(filePath);
        return open(file);
    }

    public static XDADocument open(@Nonnull File file) throws IOException, XDAException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        if (file.isDirectory()) {
            throw new FileNotFoundException("Designated path is a directory: " + file.getAbsolutePath());
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
        parseEntries();
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
        long firstEntryOffset = Utils.readByBitsParam(file, bitsParam);
        this.header = new XDAHeader(majorVersion, minorVersion, entryCount, entryNameTableType, bitsParam, firstEntryOffset);
    }

    private void parseEntries() throws XDAException, IOException {
        List<XDAEntry> xdaEntries = new LinkedList<>();
        long position = header.getFirstEntryOffset();
        for (int i = 0; i < header.getEntryCount(); ++i) {
            XDAEntry entry = parseEntry(i, position);
            position = entry.getNext();
            xdaEntries.add(entry);
        }
        this.entries = xdaEntries;
    }

    private XDAEntry parseEntry(final int index, long position) throws XDAException,
            IOException {
        file.seek(position);
        validateClassType(ENTRY_CLASS_TYPE);
        int entryLength = Utils.readInt(file);
        long bsOffset = Utils.readByBitsParam(file, header.getBitsParam());
        long next = Utils.readByBitsParam(file, header.getBitsParam());
        byte compress = file.readByte();
        byte[] checkSum = new byte[CHECKSUM_LENGTH];
        int actualRead = file.read(checkSum);
        if (actualRead != CHECKSUM_LENGTH) {
            throw new XDAException("Failed to read checkSum. Actual bytes read: " + actualRead);
        }
        int nameTableLength = Utils.readInt(file);
        NameTable nameTable = parseNameTable(index, nameTableLength, compress);
        int itemListLength = getItemListLength(entryLength, nameTableLength);
        List<Item> itemList = parseItemList(itemListLength, compress, nameTable);
        return new XDAEntry(index, position, entryLength, bsOffset, next, compress, checkSum,
                nameTableLength, nameTable.getNameCount(), nameTable.getNameMappings(), itemList);
    }

    private NameTable parseNameTable(int index, int length, byte compress) throws IOException, XDAException {
        byte[] nameTableData = new byte[length];
        int actualRead = file.read(nameTableData);
        if (actualRead != length) {
            throw new XDAException("Failed to read name table. Actual bytes read: " + actualRead);
        }
        InputStream raw = new ByteArrayInputStream(nameTableData);
        InputStream source;
        if ((compress & NAME_TABLE_COMPRESS_MASK) != 0) {
            source = new InflaterInputStream(raw);
        } else {
            source = raw;
        }

        try (InputStream data = new DataInputStream(source)) {
            int nameCount = Utils.readInt(source);
            List<NameMapping> nameTable = new ArrayList<>(nameCount);
            for (int i = 0; i < nameCount; ++i) {
                BigInteger nameValue = Utils.readBigInteger(data, NAME_VALUE_LENGTH);
                String path = Utils.readString(data);
                nameTable.add(new NameMapping(nameValue, path));
            }
            return new NameTable(nameCount, nameTable);
        }
    }

    private List<Item> parseItemList(int itemListLength, byte compress, NameTable nameTable) throws IOException, XDAException {
        byte[] itemListData = new byte[itemListLength];
        if (file.read(itemListData) < itemListLength) {
            throw new XDAException("Failed to read item list");
        }

        InputStream raw = new ByteArrayInputStream(itemListData);
        InputStream source;
        if ((compress & ITEM_LIST_COMPRESS_MASK) != 0) {
            source = new InflaterInputStream(raw);
        } else {
            source = new DataInputStream(raw);
        }
        try (DataInputStream data = new DataInputStream(source)) {
            return doParseItemList(data, nameTable.getNameCount());
        }
    }

    private List<Item> doParseItemList(DataInputStream itemListStream, int size) throws IOException, XDAException {
        List<Item> itemList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Item item = parseItem(itemListStream);
            itemList.add(item);
        }
        return itemList;
    }

    private Item parseItem(DataInputStream source) throws IOException {
        byte operatorAndReserved = source.readByte();
        Operator operator = Operator.ofValue(operatorAndReserved);
        int reserved;
        if (header.getMinorVersion() == 0x00) {
            reserved = (byte) 0x00;
        } else {
            reserved = operatorAndReserved & 0xf0;
        }
        long itemOffset = Utils.readByBitsParam(source, header.getBitsParam());
        BigInteger nameValue = Utils.readBigInteger(source, NAME_VALUE_LENGTH);

        return new Item(operator, (byte) reserved, itemOffset, nameValue);
    }

    private void validateRightsInfo() throws IOException, XDAException {
        int length = RIGHTS_INFO.length;
        byte[] rightsInfo = new byte[length];
        if ((file.read(rightsInfo) != length) || !Arrays.equals(rightsInfo, RIGHTS_INFO)) {
            throw new XDAException("The file has an invalid rights info. It might not be a valid xda file");
        }
    }

    private void validateClassType(byte[] classType) throws IOException, XDAException {
        int length = classType.length;
        byte[] theClassType = new byte[length];
        if ((file.read(theClassType) != length) || !Arrays.equals(theClassType, classType)) {
            throw new XDAException("Class type is incorrect");
        }
    }

    private int getItemListLength(int entryLength, int nameTableLength) {
        return entryLength
                - 4 // Class Type
                - 4 // Entry Length
                - header.getBitsParam() // BSOffset
                - header.getBitsParam() // Next
                - 1 // Compress
                - 16 // CheckSum
                - 4 // NameTableLength
                - nameTableLength;
    }

    @Value
    private static class IndexAndNameValue {
        Integer index;
        BigInteger nameValue;
    }

    @Value
    private static class IndexAndOffset {
        Integer index;
        Long offset;
    }
}
