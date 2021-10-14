/**
 * Title:	XDADocument
 * Description:	实现各种具体的操作，可以定义XDAView提供视图
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

import com.founderrd.xda.bean.ItemData;
import com.founderrd.xda.constant.Operator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import static com.founderrd.xda.XDAEntry.ITEMLIST_COMPRESS_MASK;
import static com.founderrd.xda.XDAEntry.NAMETABLE_COMPRESS_MASK;

class XDADocument implements AutoCloseable {

    static final Pattern PACKPATH_PATTERN = Pattern
            .compile("([\\\\/]([^\t:*?\"<>|\\\\/])+)+");
    static final int BUFFER_SIZE = 65536;
    static final byte[] BS_CLASSTYPE = {'C', '.', 'B', 'S'};
    static final byte COMPRESS_UNDEFINED_FLAG_MARKER = (byte) 0xfc;
    static final byte COMPRESS_NAMETABLE_MASK = 0x01;
    static final byte COMPRESS_ITEMLIST_MASK = 0x02;
    static final int CHECKSUM_LENGTH = 16;
    static final String XDAFLAG = "xda";
    static final int ENTRY_NAMEVALUE_LENGTH = 16;
    private static final int SIGNATURE_END = 4;
    final byte[] buffer = new byte[BUFFER_SIZE];
    private final List<XDAEntry> entries;
    private final HashMap<String, XDAItemInfo> itemsMap;
    private final HashSet<String> changedItemPathSet;
    private final List<XDAView> viewList;
    private XDAHeader header;
    private RandomAccessFile xdaDoc;
    private int nameValue;

    XDADocument() {
        this.header = new XDAHeader();
        this.entries = new LinkedList<>();
        this.itemsMap = new HashMap<>();
        this.changedItemPathSet = new HashSet<>();
        this.xdaDoc = null;
        this.nameValue = 1;
        this.viewList = new LinkedList<>();
    }

    XDADocument(RandomAccessFile file) {
        this.header = new XDAHeader();
        this.entries = new LinkedList<>();
        this.itemsMap = new HashMap<>();
        this.changedItemPathSet = new HashSet<>();
        this.xdaDoc = file;
        this.nameValue = 1;
        this.viewList = new LinkedList<>();
    }

    XDADocument(RandomAccessFile file, byte majorVersion, byte minorVersion, byte entryNameTableType, byte bitsParam)
            throws XDAException {
        this.header = new XDAHeader(majorVersion, minorVersion, entryNameTableType, bitsParam);
        this.entries = new LinkedList<>();
        this.itemsMap = new HashMap<>();
        this.changedItemPathSet = new HashSet<>();
        this.xdaDoc = file;
        this.nameValue = 1;
        this.viewList = new LinkedList<>();
        parseItems();
        updateViewAfterParseItems();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        unload();
    }

    public void open(File file) throws IOException {
        try {
            xdaDoc = new RandomAccessFile(file, "rw");
            header.parse(xdaDoc);
            parseItems();
            updateViewAfterParseItems();
        } catch (XDAException e) {
            unload();
            throw new IOException(e);
        } catch (IOException e) {
            unload();
            throw e;
        }
    }

    public void create(File theXDAFile, byte theMajorVersion,
                       byte theMinorVersion, byte theEntryNameTableType, byte theBitsParam)
            throws FileNotFoundException, XDAException {
        try {
            if (theXDAFile.exists())
                theXDAFile.delete();

            file = theXDAFile;
            xdaDoc = new RandomAccessFile(theXDAFile, "rw");
            header.create(theMajorVersion, theMinorVersion,
                    theEntryNameTableType, theBitsParam);

        } catch (FileNotFoundException | XDAException e) {
            unload();
            throw e;
        }
    }

    public XDAHeader getHeader() {
        return header;
    }

    public List<XDAEntry> getEntries() {
        return entries;
    }

    public void extractItemStream(final String pathInXDA,
                                  OutputStream outPutStream, XDADecorator dec) throws IOException,
            XDAException {
        validate();
        if (!IsValidPackPath(pathInXDA))
            throw new XDAException(XDAException.INVALID_PACK_PATH);

        XDAItemInfo itemInfo = itemsMap.get(pathInXDA);
        if (itemInfo == null)
            throw new XDAException(XDAException.INEXISTENT_ITEM);
        int index = calcItemFirstStreamIndex(itemInfo);
        doExtractItemStream(index, itemInfo, outPutStream, dec);
    }

    // 插入文件，在调用saveChanges前，不能在类外部对itemStream进行操作
    public void insertItem(final String pathInXDA, XDAInputStream inputStream,
                           byte[] ecs) throws XDAException {
        validate();
        if (!IsValidPackPath(pathInXDA))
            throw new XDAException(XDAException.INVALID_PACK_PATH);

        doInsertItem(pathInXDA, inputStream, ecs);
    }

    // 替换文件，在调用saveChanges前，不能在类外部对itemStream进行操作
    public void replaceItem(final String pathInXDA, XDAInputStream inputStream,
                            byte[] ecs) throws XDAException {
        validate();
        if (!IsValidPackPath(pathInXDA))
            throw new XDAException(XDAException.INVALID_PACK_PATH);

        doReplaceItem(pathInXDA, inputStream, ecs);
    }

    // 追加文件，在调用saveChanges前，不能在类外部对itemStream进行操作
    public void appendItem(final String pathInXDA, XDAInputStream inputStream,
                           byte[] ecs) throws XDAException {
        validate();
        if (!IsValidPackPath(pathInXDA))
            throw new XDAException(XDAException.INVALID_PACK_PATH);

        doAppendItem(pathInXDA, inputStream, ecs);
    }

    // 删除项
    public void deleteItem(final String pathInXDA) throws XDAException {
        validate();
        if (!IsValidPackPath(pathInXDA))
            throw new XDAException(XDAException.INVALID_PACK_PATH);

        checkNewOperationValid(pathInXDA, XDADefine.OPERATOR_DELETE);

        doDeleteItem(pathInXDA);
        updateView(pathInXDA, XDADefine.OPERATOR_DELETE);
    }

    public Vector<String> getAllLogicExistedItem() {
        Vector<String> allLogicExistedItem = new Vector<>(itemsMap.size());
        Collection<XDAItemInfo> col = itemsMap.values();

        for (XDAItemInfo itemInfo : col) {
            if (itemInfo.histories.lastElement().operator != XDADefine.OPERATOR_DELETE) {
                allLogicExistedItem.add(itemInfo.fullPath);
            }
        }

        return allLogicExistedItem;
    }

    public void registerView(XDAView theView) {
        if (theView != null)
            viewList.add(theView);

        Collection<XDAItemInfo> col = itemsMap.values();

        for (XDAItemInfo itemInfo : col) {
            if (itemInfo.histories.lastElement().operator != XDADefine.OPERATOR_DELETE)
                theView.update(itemInfo.fullPath, XDADefine.OPERATOR_NEW);
        }
    }

    public void saveChanges(byte entryCompress) throws XDAException,
            IOException, NoSuchAlgorithmException {
        validate();
        xdaDoc.close();
        xdaDoc = new RandomAccessFile(file, "rwd");

        writeHeader();
        Vector<SaveHelper> saveHelpers = tidyChangedItems();
        if (saveHelpers.isEmpty())
            return;

        long bSPosition = doSaveChangesIntoBS(saveHelpers);
        long entryPosition = doSaveChangesIntoEntry(saveHelpers, bSPosition,
                entryCompress);

        writeBackPriorEntryNext(entryPosition);
        writeBackHeader(entryPosition);

        changedItemPathSet.clear();
        nameValue = 1;
        xdaDoc.close();
        xdaDoc = new RandomAccessFile(file, "r");
    }

    public void saveAs(File path, byte majorVersion, byte minorVersion,
                       byte entryNameTableType, byte bitsParam, byte entryCompress)
            throws XDAException, IOException, NoSuchAlgorithmException {
        RandomAccessFile newfile = new RandomAccessFile(path, "rw");
        XDAHeader newHeader = new XDAHeader(majorVersion, minorVersion, entryNameTableType, bitsParam);
        writeHeader(newfile);
        long bSOffset = newfile.length();
        Vector<BSInfo> bSInfos = writeBS4NewFile(newfile, bitsParam);
        long firstEntryPos = newfile.length();
        writeEntry4NewFile(newfile, entryCompress, bSInfos, bitsParam, bSOffset);
        newHeader.setFirstEntryOffset(firstEntryPos);
        updateFirstEntryOffset(newfile, bitsParam, firstEntryPos);
        if (!bSInfos.isEmpty()) {
            newHeader.setEntryCount(1);
            updateEntryCount(newfile, 1);
        }
        newfile.close();
    }

    public void unregisterView(XDAView theView) {
        viewList.remove(theView);
    }

    public boolean hasItem(final String pathInXDA) {
        if (!IsValidPackPath(pathInXDA))
            return false;

        return itemsMap.containsKey(pathInXDA);
    }

    private void parseHeader() throws IOException, XDAException {
        this.xdaDoc.seek(0);
        validateSignature();
        byte[] data = new byte[8];
        if (this.xdaDoc.read(data) != data.length) {
            throw new XDAException("Failed to read xda header. The file might be corrupted");
        }
        byte major = data[0];
        byte minor = data[1];
        int entryCount = Utils.readInt(data, 2);
        byte bitsParam = data[6];
        byte entryNameTableType = data[7];
        long firstEntryOffset = parseFirstEntryOffset(bitsParam)
        this.header = new XDAHeader(major, minor, entryNameTableType, bitsParam, entryCount, firstEntryOffset);
    }

    private void parseEntry(final long position, final byte bitsParam, final int index,
                            HashMap<String, XDAItemInfo> itemsMap) throws XDAException, IOException {
        this.position = position;
        this.index = index;
        if (bitsParam != 0x02 && bitsParam != 0x04 && bitsParam != 0x08)
            throw new XDAException(XDAException.INVALID_BITSPARAM);

        validateClassType();

        xdaDoc.seek(position);
        int entryLength = Utils.readInt(xdaDoc);
        long bsOffset = Utils.readIntegerAccording2BitsParam(xdaDoc, bitsParam);
        long next = Utils.readIntegerAccording2BitsParam(xdaDoc, bitsParam);
        byte compress = xdaDoc.readByte();
        byte[] checksum = readChecksum();
        int nameTableLength = Utils.readInt(xdaDoc);

        XDAEntry entry = new XDAEntry(position, index, entryLength, bsOffset, next, compress, checksum, nameTableLength)

        parseEntryLength(fileXDA);
        parseBSOffset(fileXDA, bitsParam);
        parseNext(fileXDA, bitsParam);
        parseCompress(fileXDA);
        parseCheckSum(fileXDA);
        parseNameTableLength(fileXDA);
        calcItemListLength(bitsParam);
        parseNameTableAndItemList(fileXDA, bitsParam, itemsMap);

        isParse = true;
    }

    private void parseNameTableAndItemList(int itemListLength, byte compress,
                                           final byte theBitsParam, HashMap<String, XDAItemInfo> itemsMap)
            throws IOException, XDAException {
        DataInputStream nameTableStream = createTableStream(XDAEntry.NAMETABLELENGTH_LENGTH, compress, NAMETABLE_COMPRESS_MASK);
        DataInputStream itemListStream = createTableStream(itemListLength, compress, ITEMLIST_COMPRESS_MASK);

        List<XDAEntry.NamePathPair> nameTable = parseNameTable(nameTableStream);
        HashMap<BigInteger, XDAEntry.OperItem> itemList = parseItemList(itemListStream, theBitsParam);
        updateItemsMap(theBitsParam, nameTable, itemList, itemsMap);
    }

    private DataInputStream createTableStream(int length, byte compress, byte mask)
            throws IOException, XDAException {
        byte[] data = new byte[length];
        if (xdaDoc.read(data) < length) {
            throw new XDAException("Failed to read table data");
        }

        InputStream stream = new ByteArrayInputStream(data);
        if ((compress & mask) != 0) {
            return new DataInputStream(new InflaterInputStream(stream));
        } else {
            return new DataInputStream(stream);
        }
    }

    private List<XDAEntry.NamePathPair> parseNameTable(DataInputStream nameTableStream)
            throws IOException {
        int nameCount = Utils.readInt(nameTableStream);
        List<XDAEntry.NamePathPair> nameTable = new ArrayList<>(nameCount);
        for (int i = 0; i < nameCount; ++i) {
            BigInteger nameValueInNameTable = Utils.readBigInteger(nameTableStream, XDAEntry.NAMEVALUE_LENGTH);
            String path = Utils.readUTF8(nameTableStream, XDAEntry.PATH_LENGTH);
            nameTable.add(new XDAEntry.NamePathPair(path, nameValueInNameTable));
        }

        return nameTable;
    }

    HashMap<BigInteger, XDAEntry.OperItem> parseItemList(DataInputStream itemListStream,
                                                         final byte theBitsParam) throws IOException {
        // 先解析itemListStream
        HashMap<BigInteger, XDAEntry.OperItem> operItems = new HashMap<>(); // 哈希表初始大小为个数*2

        ItemData oneItemData;
        try {
            Byte operator = itemListStream.readByte();
            Long itemOffset = Utils
                    .readIntegerAccording2BitsParam(itemListStream, theBitsParam);
            BigInteger nameValueInItemList = Utils.readBigInteger(
                    itemListStream, XDAEntry.NAMEVALUE_LENGTH);
            oneItemData = new ItemData(operator, itemOffset, nameValueInItemList);
        } catch (IOException e) {
            return operItems;
        }

        XDAEntry.OperItem oneOperItem = new XDAEntry.OperItem();

        XDAEntry.OperItem.OpAndOffSet oneOpAndOffSet = oneOperItem.new OpAndOffSet(
                oneItemData.getOperator(), oneItemData.getItemOffset());
        if (oneOpAndOffSet.operator == XDADefine.OPERATOR_END)
            return operItems;

        oneOperItem.nameValue = oneItemData.getNameValueInItemList(); // nameValueInItemList
        oneOperItem.opAndOffset.add(oneOpAndOffSet);
        operItems.put(oneOperItem.nameValue, oneOperItem);
        int i = 0;

        while (true) {
            ++i;
            if (i == 83) {
                int k = 0;
                ++k;
            }

            try {
                Byte operator = itemListStream.readByte();
                Long itemOffset = Utils
                        .readIntegerAccording2BitsParam(itemListStream, theBitsParam);
                BigInteger nameValueInItemList = Utils.readBigInteger(
                        itemListStream, XDAEntry.NAMEVALUE_LENGTH);
                oneItemData = new ItemData(operator, itemOffset, nameValueInItemList);
            } catch (IOException e) {
                break;
            }

            oneOpAndOffSet = oneOperItem.new OpAndOffSet(oneItemData.getOperator(), oneItemData.getItemOffset());
            if (oneOpAndOffSet.operator == XDADefine.OPERATOR_END)
                break;

            BigInteger oneNameValue = oneItemData.getNameValueInItemList();
            if (oneNameValue.equals(oneOperItem.nameValue))
                oneOperItem.opAndOffset.add(oneOpAndOffSet);
            else {
                oneOperItem = new XDAEntry.OperItem();
                oneOperItem.nameValue = oneNameValue;
                oneOperItem.opAndOffset.add(oneOpAndOffSet);
                operItems.put(oneOperItem.nameValue, oneOperItem);
            }
        }

        return operItems;
    }

    private void updateItemsMap(byte bitsParam,
                                List<XDAEntry.NamePathPair> nameTable,
                                HashMap<BigInteger, XDAEntry.OperItem> itemList,
                                HashMap<String, XDAItemInfo> itemsMap) throws IOException, XDAException {

        for (XDAEntry.NamePathPair namePathPair : nameTable) {
            XDAEntry.OperItem oneOperItem = itemList.get(namePathPair.nameValue);
            if (oneOperItem == null) {
                throw new XDAException("Invalid name value");
            }
            XDAItemInfo oneItemInfo = itemsMap.get(namePathPair.path);
            // 路径名已经存在
            if (oneItemInfo != null)
                updateExistedItemOp2ItemsMap(oneOperItem, oneItemInfo, bitsParam);
                // 路径名第一次出现
            else {
                // oneItemInfo = new XDAItemInfo(namePathPair.path);
                updateUnexistedItemOp2ItemsMap(oneOperItem, namePathPair.path,
                        itemsMap, fileXDA, bitsParam);
            }
        }
    }

    private void updateExistedItemOp2ItemsMap(XDAEntry.OperItem theOperItem,
                                              XDAItemInfo theItemInfo, byte theBitsParam) throws FooE, IOException {
        for (XDAEntry.OperItem.OpAndOffSet opAndOffSet : theOperItem.opAndOffset) {
            XDAHistory lastHistroy = theItemInfo.histories.lastElement();
            CheckOperatorSequence(lastHistroy.operator,
                    (byte) (opAndOffSet.operator & XDADefine.OPERATOR_MASK));
            theItemInfo
                    .addHistory(
                            index,
                            (byte) (opAndOffSet.operator & XDADefine.OPERATOR_MASK),
                            xdaDoc, position + opAndOffSet.itemOffset,
                            theBitsParam);
        }
    }

    private void validateClassType() throws IOException, XDAException {
        byte[] c = new byte[XDAEntry.CLASSTYPE_LENGTH];
        int actualRead = xdaDoc.read(c);
        if ((actualRead != XDAEntry.CLASSTYPE_LENGTH) || !Arrays.equals(c, XDAEntry.CLASSTYPE_CONTENT)) {
            throw new XDAException("The entry has an invalid class type");
        }
    }

    private byte[] readChecksum() throws IOException, XDAException {
        byte[] cs = new byte[XDAEntry.CHECKSUM_LENGTH];
        if (xdaDoc.read(cs) != XDAEntry.CHECKSUM_LENGTH) {
            throw new XDAException("Failed to read checksum");
        }
        return cs;
    }

    private void writeHeader() throws IOException {
        writeHeader(xdaDoc);
    }

    private void writeHeader(RandomAccessFile file) throws IOException {
        if (header.getEntryCount() != 0) {
            return;
        }
        file.seek(0);
        file.write(XDAHeader.SIGNATURE);
        file.writeByte(header.getMajorVersion());
        file.writeByte(header.getMinorVersion());
        Utils.writeInt(file, header.getEntryCount());
        file.writeByte(header.getEntryNameTableType());
        byte bitsParam = header.getBitsParam();
        file.writeByte(bitsParam);
        Utils.writeIntegerAccording2BitsParam(file, bitsParam, header.getFirstEntryOffset());
    }

    private long writeBSHeader4NewFile(RandomAccessFile newfile)
            throws IOException {
        long bSHeaderPos = newfile.length();
        newfile.seek(bSHeaderPos);
        newfile.write(BS_CLASSTYPE);

        return bSHeaderPos;
    }

    private BSInfo writeOneBSFileStream4NewFile(RandomAccessFile newfile,
                                                byte bitsParam, XDAItemInfo itemInfo, int index, long bSHeaderPos)
            throws IOException, XDAException {
        BSInfo bSInfo = new BSInfo();
        bSInfo.path = itemInfo.fullPath;
        byte[] checkSum = new byte[1];
        while (index < itemInfo.histories.size()) {
            BSInfo.FileStreamInfo fileStreamInfo = bSInfo.new FileStreamInfo();
            XDAHistory history = itemInfo.histories.get(index);
            long length = 0;
            long writeBackPosition = newfile.length();

            // FileStream信息
            fileStreamInfo.offset = writeBackPosition - bSHeaderPos;
            fileStreamInfo.op = history.getOperator();
            bSInfo.fileStreams.add(fileStreamInfo);

            // 预留checkSum和length位置
            newfile.seek(writeBackPosition);
            newfile.writeByte(checkSum[0]);
            Utils.writeIntegerAccording2BitsParam(newfile,
                    bitsParam, length);
            newfile.write(history.ecs);
            // 写FileStream
            length = history.writeTo(newfile, bitsParam, buffer, checkSum);
            newfile.seek(writeBackPosition);
            newfile.writeByte(checkSum[0]);
            Utils.writeIntegerAccording2BitsParam(newfile,
                    bitsParam, length);
            ++index;
        }

        return bSInfo;
    }

    private void writeEntry4NewFile(RandomAccessFile newfile,
                                    byte entryCompress, Vector<BSInfo> bSInfos, byte bitsParam,
                                    long bSOffset) throws IOException, XDAException,
            NoSuchAlgorithmException {
        long writeBackPos = newfile.length();
        newfile.seek(writeBackPos);

        // 预写
        writeEntryClassType(newfile);
        long entryLength = 0;
        writeEntryLength(newfile, entryLength);
        writeEntryBSOffset(newfile, bitsParam, bSOffset);
        writeEntryNext(newfile, bitsParam, 0);
        writeEntryCompress(newfile, entryCompress);
        byte[] checkSum = new byte[CHECKSUM_LENGTH];
        writeEntryCheckSum(newfile, checkSum);
        long nameTableLength = 0;
        writeEntryNameTableLength(newfile, nameTableLength);

        // 写NameTable和ItemList
        MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        nameTableLength = writeEntryNameTableAndItemList4NewFile(newfile,
                bSInfos, entryCompress, bitsParam, md);

        // 回填
        entryLength = newfile.length() - writeBackPos;
        newfile.seek(writeBackPos + BS_CLASSTYPE.length);
        writeEntryLength(newfile, entryLength);
        writeEntryBSOffset(newfile, bitsParam, bSOffset);
        writeEntryNext(newfile, bitsParam, 0);
        writeEntryCompress(newfile, entryCompress);
        checkSum = md.digest();
        writeEntryCheckSum(newfile, checkSum);
        writeEntryNameTableLength(newfile, nameTableLength);
    }

    /*
     * private:
     */
    private void doDeleteItem(final String pathInXDA) throws XDAException {
        XDAItemInfo itemInfo = markChangedItem(pathInXDA);
        addItemNewInfo(itemInfo, XDADefine.OPERATOR_DELETE, null, null);
    }

    private long writeEntryNameTableAndItemList4NewFile(
            RandomAccessFile newfile, Vector<BSInfo> bSInfos,
            byte entryCompress, byte bitsParam, MessageDigest md)
            throws IOException {
        ByteArrayOutputStream nmTableData = new ByteArrayOutputStream();
        ByteArrayOutputStream itemListData = new ByteArrayOutputStream();
        OutputStream nmTable;
        OutputStream itemList;
        if ((entryCompress & COMPRESS_NAMETABLE_MASK) != 0) {
            nmTable = new BufferedOutputStream(new DeflaterOutputStream(
                    nmTableData));
        } else
            nmTable = nmTableData;

        DataOutputStream nammTableStream = new DataOutputStream(nmTable);
        if ((entryCompress & COMPRESS_ITEMLIST_MASK) != 0) {
            itemList = new BufferedOutputStream(new DeflaterOutputStream(
                    itemListData));
        } else
            itemList = itemListData;

        DataOutputStream itemListStream = new DataOutputStream(itemList);

        Utils.writeInt(nammTableStream, bSInfos.size());
        Iterator<BSInfo> iter = bSInfos.iterator();
        int i = 1;
        byte[] nmVal = new byte[ENTRY_NAMEVALUE_LENGTH];
        while (iter.hasNext()) {
            BSInfo oneBSInfo = iter.next();
            byte[] val = Utils.converIntBigEndian2LittleEndian(i);
            int j = 0;
            for (; j < val.length; ++j)
                nmVal[j] = val[val.length - j - 1];
            for (; j < nmVal.length; ++j)
                nmVal[j] = 0x00;
            nammTableStream.write(nmVal);
            byte[] pathByte = oneBSInfo.path.getBytes();
            nammTableStream.write(pathByte, 0, pathByte.length);
            nammTableStream.write(0x00);

            for (BSInfo.FileStreamInfo fileStreamHistory : oneBSInfo.fileStreams) {
                itemListStream.write(fileStreamHistory.op);
                Utils.writeIntegerAccording2BitsParam(
                        itemListStream, bitsParam, fileStreamHistory.offset);
                itemListStream.write(nmVal);
            }

            ++i;
        }

        long posMark = newfile.length();
        newfile.seek(posMark);
        long nameTableLength = Utils.copyFromSrcToDst(nmTableData
                .toByteArray(), newfile, md);
        Utils.copyFromSrcToDst(itemListData.toByteArray(), newfile,
                md);

        return nameTableLength;
    }

    private void doInsertItem(final String pathInXDA,
                              XDAInputStream maintainedStream, byte[] ecs) throws XDAException {
        checkNewOperationValid(pathInXDA, XDADefine.OPERATOR_NEW);
        XDAItemInfo itemInfo = markChangedItem(pathInXDA);
        addItemNewInfo(itemInfo, XDADefine.OPERATOR_NEW, maintainedStream, ecs);
    }

    private void doReplaceItem(final String pathInXDA,
                               XDAInputStream maintainedStream, byte[] ecs) throws XDAException {
        checkNewOperationValid(pathInXDA, XDADefine.OPERATOR_REPLACE);
        XDAItemInfo itemInfo = markChangedItem(pathInXDA);
        addItemNewInfo(itemInfo, XDADefine.OPERATOR_REPLACE, maintainedStream,
                ecs);
    }

    private void doAppendItem(final String pathInXDA,
                              XDAInputStream maintainedStream, byte[] ecs) throws XDAException {
        checkNewOperationValid(pathInXDA, XDADefine.OPERATOR_APPEND);
        XDAItemInfo itemInfo = markChangedItem(pathInXDA);
        addItemNewInfo(itemInfo, XDADefine.OPERATOR_APPEND, maintainedStream,
                ecs);
    }

    private void tidyLastOperationDelete(XDAItemInfo itemInfo,
                                         Vector<SaveHelper> saveHelpers) throws XDAException {
        Vector<XDAHistory> histories = itemInfo.histories;
        int i = histories.size() - 1;
        for (; i >= 0; --i) {
            if (histories.elementAt(i).entryNo < header.getEntryCount() + 1)
                break;
        }

        if (i == -1) {
            histories.removeAllElements();
        } else if (histories.elementAt(i).getOperator() == XDADefine.OPERATOR_DELETE) {
            histories.setSize(i + 1);
        } else {
            histories.setElementAt(histories.lastElement(), i + 1);
            histories.setSize(i + 2);
            saveHelpers.add(new SaveHelper(itemInfo, i + 1));
        }
    }

    private void tidyLastOperationNew(XDAItemInfo itemInfo,
                                      Vector<SaveHelper> saveHelpers) throws XDAException {
        Vector<XDAHistory> histories = itemInfo.histories;
        int i = histories.size() - 1;
        // 找到上一个entry的最后一个history
        for (; i >= 0; --i) {
            if (histories.elementAt(i).entryNo < header.getEntryCount() + 1)
                break;
        }

        if (i == -1) {
            histories.setElementAt(histories.lastElement(), i + 1);
            histories.setSize(i + 2);
        } else {
            if (histories.elementAt(i).getOperator() != XDADefine.OPERATOR_DELETE)
                histories.lastElement().operator = XDADefine.OPERATOR_REPLACE;
            histories.setElementAt(histories.lastElement(), i + 1);
            histories.setSize(i + 2);
        }

        saveHelpers.add(new SaveHelper(itemInfo, i + 1));
    }

    private void tidyLastOperationAppend(XDAItemInfo itemInfo,
                                         Vector<SaveHelper> saveHelpers) throws XDAException {
        Vector<XDAHistory> histories = itemInfo.histories;
        int i = histories.size() - 1;

        // 找到本entry最开始的append
        for (; i >= 0; --i) {
            if (histories.elementAt(i).operator != XDADefine.OPERATOR_APPEND
                    || histories.elementAt(i).entryNo < header.getEntryCount() + 1)
                break;
        }

        // 再上一个history, 逻辑上保证了j>=0
        int k = i;
        // 本entry最开始的append的上一个history的也是这次操作中的
        if (histories.elementAt(i).entryNo == header.getEntryCount() + 1) {
            if (histories.elementAt(i).operator == XDADefine.OPERATOR_NEW) {
                for (; k >= 0; --k)
                    if (histories.elementAt(k).entryNo < header.getEntryCount() + 1)
                        break;
                if (k >= 0
                        && histories.elementAt(k).operator != XDADefine.OPERATOR_DELETE)
                    histories.elementAt(i).operator = XDADefine.OPERATOR_REPLACE;
                ++k;
                saveHelpers.add(new SaveHelper(itemInfo, k));
                for (; i < histories.size(); ++i, ++k)
                    histories.setElementAt(histories.elementAt(i), k);
                histories.setSize(k);
            }
            // 必然是replace
            else {
                for (; k >= 0; --k)
                    if (histories.elementAt(k).entryNo < header.getEntryCount() + 1)
                        break;
                if (k >= 0
                        && histories.elementAt(k).operator == XDADefine.OPERATOR_DELETE)
                    histories.elementAt(i).operator = XDADefine.OPERATOR_NEW;
                ++k;
                saveHelpers.add(new SaveHelper(itemInfo, k));
                for (; i < histories.size(); ++i, ++k)
                    histories.setElementAt(histories.elementAt(i), k);
                histories.setSize(k);
            }
        }
        // 本次操作都是append
        else
            saveHelpers.add(new SaveHelper(itemInfo, k));
    }

    private void tidyLastOperationReplace(XDAItemInfo itemInfo,
                                          Vector<SaveHelper> saveHelpers) throws XDAException {
        Vector<XDAHistory> histories = itemInfo.histories;
        int i = histories.size() - 1;
        for (; i >= 0; --i) {
            if (histories.elementAt(i).entryNo < header.getEntryCount() + 1)
                break;
        }

        if (i == -1) {
            histories.lastElement().operator = XDADefine.OPERATOR_NEW;
            histories.setElementAt(histories.lastElement(), i + 1);
            histories.setSize(i + 2);
            saveHelpers.add(new SaveHelper(itemInfo, i + 1));
        } else {
            if (histories.elementAt(i).getOperator() == XDADefine.OPERATOR_DELETE)
                histories.lastElement().operator = XDADefine.OPERATOR_NEW;
            histories.setElementAt(histories.lastElement(), i + 1);
            histories.setSize(i + 2);
            saveHelpers.add(new SaveHelper(itemInfo, i + 1));
        }
    }

    private void writeBSClassType() throws IOException {
        xdaDoc.write(BS_CLASSTYPE);
    }

    private void tidyChangedItem(final String changedItemPath,
                                 Vector<SaveHelper> saveHelpers) throws XDAException {
        XDAItemInfo itemInfo = itemsMap.get(changedItemPath);
        XDAHistory history = itemInfo.histories.lastElement();
        switch (history.getOperator()) {
            case XDADefine.OPERATOR_DELETE:
                tidyLastOperationDelete(itemInfo, saveHelpers);
                break;
            case XDADefine.OPERATOR_APPEND:
                tidyLastOperationAppend(itemInfo, saveHelpers);
                break;
            case XDADefine.OPERATOR_NEW:
                tidyLastOperationNew(itemInfo, saveHelpers);
                break;
            case XDADefine.OPERATOR_REPLACE:
                tidyLastOperationReplace(itemInfo, saveHelpers);
                break;
            default:
                throw new XDAException(XDAException.INVALID_OPERATION_TYPE);
        }

        if (itemInfo.histories.isEmpty())
            itemsMap.remove(changedItemPath);
    }

    private void writeBackPriorEntryNext(long lastEntryPosition)
            throws XDAException, IOException {
        int priorEntryIndex = entries.size() - 2;
        if (priorEntryIndex >= 0) {
            XDAEntry priorEntry = entries.get(priorEntryIndex);
            priorEntry.modifyNext(xdaDoc, lastEntryPosition, header
                    .getBitsParam());
        }
    }

    private void writeBackHeader(long lastEntryPosition) throws XDAException,
            IOException {
        if (header.getEntryCount() == 0) {
            header.setFirstEntryOffset(lastEntryPosition);
            updateFirstEntryOffset(xdaDoc, header.getBitsParam(), lastEntryPosition);
        }

        int entryCount = header.getEntryCount() + 1;
        header.setEntryCount(entryCount);
        updateEntryCount(xdaDoc, entryCount);
    }

    private Vector<BSInfo> writeBS4NewFile(RandomAccessFile newfile, byte bitsParam) throws XDAException, IOException {
        long bSHeaderPos = writeBSHeader4NewFile(newfile);
        Collection<XDAItemInfo> col = itemsMap.values();
        Iterator<XDAItemInfo> iter = col.iterator();

        Vector<BSInfo> bSInfos = new Vector<>();
        while (iter.hasNext()) {
            int index;
            XDAItemInfo itemInfo = iter.next();
            try {
                index = calcItemFirstStreamIndex(itemInfo);
            } catch (XDAException e) {
                continue;
            }

            BSInfo oneBSInfo = writeOneBSFileStream4NewFile(newfile, bitsParam,
                    itemInfo, index, bSHeaderPos);
            bSInfos.add(oneBSInfo);
        }

        return bSInfos;
    }

    private long doSaveChangesIntoEntry(Vector<SaveHelper> saveHelpers,
                                        long bSPosition, byte entryCompress) throws IOException,
            XDAException, NoSuchAlgorithmException {

        long entryWritePosition = xdaDoc.length();

        xdaDoc.seek(entryWritePosition);
        writeEntryClassType();
        long writeBackLengthPosition = xdaDoc.getFilePointer();
        writeEntryLength(0);
        writeEntryBSOffset(bSPosition);
        writeEntryNext(0);
        writeEntryCompress(entryCompress);
        long writeBackCheckSumPosition = xdaDoc.getFilePointer();
        byte[] checkSum = new byte[CHECKSUM_LENGTH];
        writeEntryCheckSum(checkSum);
        writeEntryNameTableLength(0);
        MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        long nameTableLength = writeEntryNameTableAndItemList(saveHelpers,
                bSPosition, entryCompress, checkSum, md);
        long entryLength = xdaDoc.getFilePointer() - entryWritePosition;
        xdaDoc.seek(writeBackLengthPosition);
        writeEntryLength(entryLength);
        xdaDoc.seek(writeBackCheckSumPosition);
        checkSum = md.digest();
        writeEntryCheckSum(checkSum);
        writeEntryNameTableLength(nameTableLength);

        XDAEntry newEntry = new XDAEntry(entries.size(), entryWritePosition,
                (int) entryLength, bSPosition, 0, entryCompress, checkSum,
                (int) nameTableLength, saveHelpers.size(), header
                .getBitsParam());
        entries.add(newEntry);

        return entryWritePosition;
    }

    private void writeEntryLength(long entryLength) throws IOException {
        writeEntryLength(xdaDoc, entryLength);
    }

    private void writeEntryLength(RandomAccessFile file, long entryLength)
            throws IOException {
        Utils.writeInt(file, entryLength);
    }

    private long writeEntryNameTableAndItemList(Vector<SaveHelper> saveHelpers,
                                                long bSPosition, byte entryCompress, byte[] checkSum,
                                                MessageDigest md) throws IOException, XDAException,
            NoSuchAlgorithmException {
        // 先生成nametable itemlist的文件，写入内容。
        File nameTableFile = File.createTempFile(XDAFLAG, null);
        File itemListFile = File.createTempFile(XDAFLAG, null);

        OutputStream nameTableOutputStream = new FileOutputStream(nameTableFile);
        OutputStream itemListOutputStream = new FileOutputStream(itemListFile);

        if ((entryCompress & COMPRESS_NAMETABLE_MASK) != 0) {
            nameTableOutputStream = new DeflaterOutputStream(
                    nameTableOutputStream);
        }

        if ((entryCompress & COMPRESS_NAMETABLE_MASK) != 0) {
            itemListOutputStream = new DeflaterOutputStream(
                    itemListOutputStream);
        }

        // 先写入namecount
        Utils.writeInt(nameTableOutputStream, saveHelpers.size());

        byte[] nameValue = new byte[ENTRY_NAMEVALUE_LENGTH];
        for (SaveHelper saveHelper : saveHelpers) {
            calcNameValue(saveHelper.itemInfo.fullPath, nameValue);
            nameTableOutputStream.write(nameValue);
            nameTableOutputStream.write(saveHelper.itemInfo.fullPath
                    .getBytes(StandardCharsets.UTF_8));
            nameTableOutputStream.write(0x00); // 写0(结尾)

            for (int i = saveHelper.index; i < saveHelper.itemInfo.histories
                    .size(); ++i) {
                XDAOldHistory history = (XDAOldHistory) saveHelper.itemInfo.histories
                        .get(i);
                itemListOutputStream
                        .write(saveHelper.itemInfo.histories.get(i).operator);
                Utils.writeIntegerAccording2BitsParam(
                        itemListOutputStream, header.getBitsParam(), history
                                .getPosition()
                                - bSPosition);
                itemListOutputStream.write(nameValue);
            }
        }
        itemListOutputStream.write(XDADefine.OPERATOR_END);
        Utils.writeIntegerAccording2BitsParam(itemListOutputStream,
                header.getBitsParam(), 0);
        int i = 0;
        nameValue[i++] = (byte) 0x7f;
        for (; i < ENTRY_NAMEVALUE_LENGTH; ++i)
            nameValue[i] = (byte) 0xff;
        itemListOutputStream.write(nameValue);

        nameTableOutputStream.close();
        itemListOutputStream.close();

        InputStream nameTableInputStream = new FileInputStream(nameTableFile);
        InputStream itemListInputStream = new FileInputStream(itemListFile);

        long nameTableLength = Utils.copyFromSrcToDst(
                nameTableInputStream, xdaDoc, buffer, md);
        Utils.copyFromSrcToDst(itemListInputStream, xdaDoc,
                buffer, md);

        nameTableInputStream.close();
        itemListInputStream.close();

        nameTableFile.delete();
        itemListFile.delete();

        return nameTableLength;
    }

    private void calcNameValue(String path, byte[] nameValue) {
        byte[] theNameValue = Utils
                .converIntBigEndian2LittleEndian(this.nameValue);
        int i = 0;
        for (; i < theNameValue.length; ++i)
            nameValue[i] = theNameValue[theNameValue.length - i - 1];
        for (; i < nameValue.length; ++i)
            nameValue[i] = 0x00;

        ++this.nameValue;
    }

    private void writeEntryNameTableLength(long nameTableLength)
            throws IOException {
        writeEntryNameTableLength(xdaDoc, nameTableLength);
    }

    private void writeEntryNameTableLength(RandomAccessFile file,
                                           long nameTableLength) throws IOException {
        Utils.writeInt(file, nameTableLength);
    }

    private void writeEntryCheckSum(byte[] checkSum) throws IOException {
        writeEntryCheckSum(xdaDoc, checkSum);
    }

    private void writeEntryCheckSum(RandomAccessFile file, byte[] checkSum)
            throws IOException {
        file.write(checkSum);
    }

    private void writeEntryCompress(byte entryCompress) throws IOException {
        writeEntryCompress(xdaDoc, entryCompress);
    }

    private void writeEntryCompress(RandomAccessFile file, byte entryCompress)
            throws IOException {
        file.write(entryCompress & (~COMPRESS_UNDEFINED_FLAG_MARKER));
    }

    private void writeEntryClassType() throws IOException {
        writeEntryClassType(xdaDoc);
    }

    private void writeEntryClassType(RandomAccessFile file) throws IOException {
        file.write(XDAEntry.CLASSTYPE_CONTENT);
    }

    private void writeEntryBSOffset(long bSOffset) throws XDAException,
            IOException {
        writeEntryBSOffset(xdaDoc, header.getBitsParam(), bSOffset);
    }

    private void writeEntryBSOffset(RandomAccessFile file, byte bitsParam,
                                    long bSOffset) throws IOException {
        Utils.writeIntegerAccording2BitsParam(file, bitsParam,
                bSOffset);
    }

    private void writeEntryNext(long next) throws XDAException, IOException {
        writeEntryNext(xdaDoc, header.getBitsParam(), next);
    }

    private void writeEntryNext(RandomAccessFile file, byte bitsParam, long next)
            throws IOException {
        Utils
                .writeIntegerAccording2BitsParam(file, bitsParam, next);
    }


    private Vector<SaveHelper> tidyChangedItems() throws XDAException {
        Vector<SaveHelper> saveHelpers = new Vector<>();
        for (String changedItemPath : changedItemPathSet) {
            tidyChangedItem(changedItemPath, saveHelpers);
        }

        return saveHelpers;
    }

    private long doSaveChangesIntoBS(Vector<SaveHelper> saveHelpers)
            throws IOException, XDAException {
        long positon = xdaDoc.length();
        xdaDoc.seek(positon);
        writeBSClassType();

        for (int i = 0; i < saveHelpers.size(); ++i) {
            SaveHelper saveHelper = saveHelpers.elementAt(i);

            switch (saveHelper.itemInfo.histories.lastElement().operator) {
                case XDADefine.OPERATOR_DELETE:
                    doSaveDeleteChange(saveHelper);
                    break;
                case XDADefine.OPERATOR_APPEND:
                    doSaveAppendChange(saveHelper);
                    break;
                default:
                    doSaveNewOrReplaceChange(saveHelper);
                    break;
            }
        }

        return positon;
    }

    private void doSaveDeleteChange(SaveHelper saveHelper) throws IOException {
        setHistoryOfSaveHelper(saveHelper, saveHelper.index, -1, 0);
    }

    private void doSaveAppendChange(SaveHelper saveHelper) throws IOException,
            XDAException {
        long position = xdaDoc.length();
        xdaDoc.seek(position);
        for (int i = saveHelper.index; i < saveHelper.itemInfo.histories.size(); ++i) {
            XDANewHistory newHistory = (XDANewHistory) saveHelper.itemInfo.histories
                    .elementAt(i);
            long length = writeBSFileStream(newHistory);
            setHistoryOfSaveHelper(saveHelper, i, position, length);
        }
    }

    private void doSaveNewOrReplaceChange(SaveHelper saveHelper)
            throws IOException, XDAException {
        long position = xdaDoc.length();
        xdaDoc.seek(position);
        XDANewHistory newHistory = (XDANewHistory) saveHelper.itemInfo.histories
                .elementAt(saveHelper.index);
        long length = writeBSFileStream(newHistory);
        setHistoryOfSaveHelper(saveHelper, saveHelper.index, position, length);
    }

    private long writeBSFileStream(XDANewHistory newHistory)
            throws IOException, XDAException {
        long length = 0L;
        byte checkSum = (byte) 0x00;
        long position = xdaDoc.length();
        xdaDoc.seek(position);

        writeBSCheckSum(checkSum);
        writeBSLength(length);
        writeBSECS(newHistory.getECS());
        Pair<Long, Byte> lengthAndCheckSum = writeBSFileData(newHistory);

        xdaDoc.seek(position);
        writeBSCheckSum(lengthAndCheckSum.getRight());
        writeBSLength(lengthAndCheckSum.getLeft());

        return length;
    }

    private Pair<Long, Byte> writeBSFileData(XDANewHistory newHistory) throws IOException,
            XDAException {
        byte[] checkSum = {0x00};
        long length = newHistory.writeTo(xdaDoc, header.getBitsParam(), buffer, checkSum);
        return new ImmutablePair<>(length, checkSum[0]);
    }

    private void writeBSCheckSum(byte checksum) throws IOException {
        xdaDoc.writeByte(checksum);
    }

    private void writeBSLength(long length) throws XDAException, IOException {
        Utils.writeIntegerAccording2BitsParam(xdaDoc, header
                .getBitsParam(), length);
    }

    private void writeBSECS(byte[] ecs) throws IOException {
        xdaDoc.write(ecs);
    }

    // 可以改成二分法求异或。。。以后再改
    @SuppressWarnings("unused")
    private byte calcCheckSum(byte[] src, int from, int length) {
        byte checksum = 0x00;
        for (; from < length; ++from)
            checksum ^= src[from];
        return checksum;
    }

    private void setHistoryOfSaveHelper(SaveHelper saveHelper, int index,
                                        long position, long length) {
        XDAHistory newHistory = saveHelper.itemInfo.histories.elementAt(index);
        XDAHistory oldHistory = new XDAOldHistory(newHistory, position,
                xdaDoc, length);
        saveHelper.itemInfo.histories.set(index, oldHistory);
    }

    private void addItemNewInfo(XDAItemInfo itemInfo, byte operator,
                                XDAInputStream maintainedStream, byte[] ecs) throws XDAException {
        itemInfo.addHistory(header.getEntryCount() + 1, operator,
                maintainedStream, ecs);
    }

    private void doExtractItemStream(final int itemItemFirstStreamIndex,
                                     final XDAItemInfo itemInfo, OutputStream outPutStream,
                                     XDADecorator dec) throws IOException, XDAException {
        if (itemItemFirstStreamIndex == itemInfo.histories.size() - 1)
            doExtractItemJustOneStream(itemInfo.histories.lastElement(),
                    outPutStream, dec);
        else
            doExtractItemManyStream(itemItemFirstStreamIndex, itemInfo,
                    outPutStream, dec);
    }

    private void doExtractItemJustOneStream(XDAHistory history,
                                            OutputStream outPutStream, XDADecorator dec) throws IOException,
            XDAException {
        OutputStream targetOutputStream = outPutStream;
        if (dec != null && history.ecs[0] != XDADefine.OPERATOR_END)
            targetOutputStream = dec.InflateDecorate(outPutStream, history.ecs,
                    history.ecs.length - 2);
        history.writeTo(targetOutputStream, header.getBitsParam(), buffer);
    }

    private XDAItemInfo markChangedItem(final String pathInXDA) {
        changedItemPathSet.add(pathInXDA);
        XDAItemInfo itemInfo = itemsMap.get(pathInXDA);
        if (itemInfo == null) {
            itemInfo = new XDAItemInfo(pathInXDA);
            itemsMap.put(itemInfo.fullPath, itemInfo);
        }

        return itemInfo;
    }

    private void checkNewOperationValid(final String pathInXDA, byte operation)
            throws XDAException {
        boolean valid = false;
        XDAItemInfo itemInfo = itemsMap.get(pathInXDA);
        switch (operation) {
            case XDADefine.OPERATOR_NEW:
                if (itemInfo != null) {
                    if (itemInfo.histories.lastElement().operator != XDADefine.OPERATOR_DELETE)
                        break;
                }
                valid = true;
                break;

            case XDADefine.OPERATOR_REPLACE:
            case XDADefine.OPERATOR_APPEND:
            case XDADefine.OPERATOR_DELETE:
                if (itemInfo == null)
                    break;
                if (itemInfo.histories.lastElement().operator == XDADefine.OPERATOR_DELETE)
                    break;
                valid = true;
                break;

            default:
                break;
        }

        if (!valid)
            throw new XDAException(XDAException.INVALID_OPERATION);
    }

    private void parseItems() throws XDAException {
        long position = header.getFirstEntryOffset();
        for (int i = 0; i < header.getEntryCount(); ++i) {
            XDAEntry oneEntry = new XDAEntry();
            oneEntry.parse(xdaDoc, position, header.getBitsParam(), i + 1,
                    itemsMap);
            position = oneEntry.getNext();
            entries.add(oneEntry);
        }

        if (position != 0)
            throw new XDAException(FooE.INVALID_NEXT_FIELD_OF_LAST_ENTRY);
    }

    private int calcItemFirstStreamIndex(final XDAItemInfo itemInfo)
            throws XDAException {
        int index = itemInfo.histories.size() - 1;
        XDAHistory currentHistory = itemInfo.histories.elementAt(index);
        if (currentHistory.operator == XDADefine.OPERATOR_DELETE)
            throw new XDAException(XDAException.INVALID_ITEM_CONTENT);

        while (index >= 0) {
            if (currentHistory.operator != XDADefine.OPERATOR_APPEND)
                break;
            currentHistory = itemInfo.histories.elementAt(--index);
        }

        if (index < 0
                || (currentHistory.operator != XDADefine.OPERATOR_NEW && currentHistory.operator != XDADefine.OPERATOR_REPLACE))
            throw new XDAException(XDAException.INVALID_ITEM_CONTENT);

        return index;
    }

    private void updateView(String path, byte operator) {
        for (XDAView theView : viewList)
            theView.update(path, operator);
    }

    private void updateViewAfterParseItems() {
        if (viewList.isEmpty()) {
            return;
        }
        itemsMap.values().stream()
                .filter(item -> Operator.DELETE.compareTo()item.histories.lastElement())
        Collection<XDAItemInfo> col = itemsMap.values();

        for (XDAItemInfo itemInfo : col) {
            if (itemInfo.histories.lastElement().operator != XDADefine.OPERATOR_DELETE)
                updateView(itemInfo.fullPath, XDADefine.OPERATOR_NEW);
        }
    }

    private void doExtractItemManyStream(final int itemItemFirstStreamIndex,
                                         final XDAItemInfo itemInfo, OutputStream outPutStream,
                                         XDADecorator dec) throws IOException, XDAException {
        int index = itemItemFirstStreamIndex;
        for (; index < itemInfo.histories.size(); ++index) {
            XDAHistory currentHistory = itemInfo.histories.elementAt(index);
            if (currentHistory.ecs[0] == XDADefine.OPERATOR_END || dec == null) {
                currentHistory.writeTo(outPutStream, header.getBitsParam(),
                        buffer);
            } else {
                File tmpFile = File.createTempFile("xda", null);
                OutputStream tmpStream = new FileOutputStream(tmpFile);
                tmpStream = dec.InflateDecorate(tmpStream, currentHistory.ecs,
                        currentHistory.ecs.length - 2);
                currentHistory
                        .writeTo(tmpStream, header.getBitsParam(), buffer);
                tmpStream.close();

                InputStream unCodeStream = new FileInputStream(tmpFile);
                Utils.copyFromSrcToDst(unCodeStream, outPutStream,
                        buffer);
                unCodeStream.close();
                tmpFile.delete();
            }
        }
    }

    private boolean IsValidPackPath(String path) {
        return PACKPATH_PATTERN.matcher(path).matches();
    }

    private void unload() throws IOException {
        if (xdaDoc != null) {
            xdaDoc.close();
        }

        entries.clear();
        itemsMap.clear();
        changedItemPathSet.clear();
        viewList.clear();
    }

    private void validate() throws XDAException {
        if (this.xdaDoc == null) {
            throw new XDAException("This document is not properly initialized");
        }
    }

    private void validateSignature() throws XDAException {
        byte[] signature = new byte[XDAHeader.SIGNATURE_LENGTH];
        int actualRead;
        try {
            actualRead = xdaDoc.read(signature);
        } catch (IOException e) {
            throw new XDAException("Failed to read file signature", e);
        }
        if ((actualRead != XDAHeader.SIGNATURE_LENGTH) || Arrays.equals(signature, XDAHeader.SIGNATURE)) {
            throw new XDAException("The file seems not to have a valid signature. Maybe it is not a valid xda file");
        }
    }

    private long parseFirstEntryOffset(byte bitsParam) throws IOException {
        switch (bitsParam) {
            case 0x02:
                return Utils.readShort(xdaDoc);
            case 0x04:
                return Utils.readInt(xdaDoc);
            case 0x08:
                return Utils.readLong(xdaDoc);
            default:
                return -1;
        }
    }

    private void updateEntryCount(RandomAccessFile file, int entryCount) throws IOException {
        file.seek(XDAHeader.ENTRY_COUNT_OFFSET);
        Utils.writeInt(file, entryCount);
    }

    private void updateFirstEntryOffset(RandomAccessFile file, byte bitsParam, long firstEntryOffset) throws IOException {
        file.seek(XDAHeader.FIRST_ENTRY_OFFSET);
        Utils.writeIntegerAccording2BitsParam(file, bitsParam, firstEntryOffset);
    }

    class SaveHelper {
        final XDAItemInfo itemInfo;
        final int index;

        SaveHelper(XDAItemInfo theItemInfo, int theIndex) {
            itemInfo = theItemInfo;
            index = theIndex;
        }
    }

    class BSInfo {
        final Vector<FileStreamInfo> fileStreams;
        String path;

        BSInfo() {
            path = null;
            fileStreams = new Vector<>();
        }

        class FileStreamInfo {
            long offset;
            byte op;

            FileStreamInfo() {
                offset = 0;
                op = (byte) 0x00;
            }
        }
    }
}
