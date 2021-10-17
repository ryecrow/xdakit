package io.technicrow.xdakit;

import io.technicrow.xdakit.constant.Operator;
import io.technicrow.xdakit.model.*;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.zip.DeflaterInputStream;
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
    private final RandomAccessFile file;

    private final List<String> paths = new LinkedList<>();
    private final Map<String, Long> fileToOffsetMap = new HashMap<>();

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
    @Nonnull
    public List<String> listAllFiles() {
        return Collections.unmodifiableList(paths);
    }

    @Override
    public FileStream getFile(@Nonnull String filePath) throws IOException, XDAException {
        if (!fileToOffsetMap.containsKey(filePath)) {
            throw new FileNotFoundException(String.format("This XDA file doesn't contain such path: %s", filePath));
        }
        long offset = fileToOffsetMap.get(filePath);
        file.seek(offset);
        byte checkSum = file.readByte();
        long length = Utils.readByBitsParam(file, header.getBitsParam());
        ByteBuffer ecsBuffer = ByteBuffer.allocate(8);
        byte b;
        int ecsLength = 0;
        while (((b = file.readByte()) != (byte) 0xff) && ecsLength < 8) {
            if (b == 0x00) {
                throw new XDAException("Invalid ECS value: 0x00");
            }
            ecsLength++;
            ecsBuffer.put(b);
        }
        byte[] ecs = new byte[ecsLength];
        System.arraycopy(ecsBuffer.array(), 0, ecs, 0, ecsLength);
        return new FileStream(FilenameUtils.getName(filePath),
                checkSum, length, ecs, readFileData(length, ecs));
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
            xdaEntries.add(entry);
            position = entry.getNext();
            if (position == 0) {
                break;
            }
        }
        this.entries = xdaEntries;
        updateFileToOffsetMap();
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
        NameTable nameTable = parseNameTable(nameTableLength, compress);
        int itemListLength = getItemListLength(entryLength, nameTableLength);
        List<Item> itemList = parseItemList(itemListLength, compress, nameTable);
        file.seek(bsOffset);
        validateClassType(BIT_STREAM_CLASS_TYPE);
        return new XDAEntry(index, position, entryLength, bsOffset, next, compress, checkSum,
                nameTableLength, nameTable.getNameCount(), nameTable.getNameMappings(), itemList);
    }

    private NameTable parseNameTable(int length, byte compress) throws IOException, XDAException {
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
                if (!paths.contains(path)) {
                    paths.add(path);
                }
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

    private List<Item> doParseItemList(DataInputStream itemListStream, int size) throws IOException {
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

    private void updateFileToOffsetMap() {
        Map<BigInteger, String> nameValues = new HashMap<>();
        for (XDAEntry entry : entries) {
            long entryOffset = entry.getBsOffset();
            for (NameMapping nm : entry.getNameTable()) {
                nameValues.put(nm.getNameValue(), nm.getPath());
            }
            for (Item item : entry.getItemList()) {
                long fileOffset = item.getItemOffset() + entryOffset;
                String path = nameValues.get(item.getNameValue());
                fileToOffsetMap.put(path, fileOffset);
            }
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

    private InputStream readFileData(long length, byte[] ecs) throws IOException, XDAException {
        byte[] fileData = new byte[Math.toIntExact(length)];
        file.read(fileData);
        InputStream result = new ByteArrayInputStream(fileData);
        for (int i = ecs.length - 1; i >= 0; i--) {
            byte encryption = ecs[i];
            if (encryption == 0) {
                continue;
            }
            switch (encryption) {
                case 0x02:
                    result = new DeflaterInputStream(result);
                    break;
                case 0x10:
                    result = new BZip2CompressorInputStream(result);
                    break;
                default:
                    throw new XDAException("Invalid encryption mark: " + Integer.toHexString(encryption));
            }
        }
        return result;
    }
}
