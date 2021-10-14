/**
 * Title:	XDAEntry
 * Description:	提供解析Entry和获取Entry信息功能
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

import com.founderrd.xda.bean.ItemData;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.InflaterInputStream;

class XDAEntry {
    static final int CLASSTYPE_LENGTH = 4;
    static final byte[] CLASSTYPE_CONTENT = new byte[]{'C', '.', 'E', 'n'};
    static final int ENTRYLENGTH_LENGTH = 4;
    static final int COMPRESS_LENGTH = 1;
    static final int CHECKSUM_LENGTH = 16;
    static final int NAMETABLELENGTH_LENGTH = 4;
    static final int NAMEVALUE_LENGTH = 16;
    static final int PATH_LENGTH = 256;
    static final byte COMPRESS_UNDEFINED_FLAG_MARKER = (byte) 0xfc;
    static final byte NAMETABLE_COMPRESS_MASK = 0x01;
    static final byte ITEMLIST_COMPRESS_MASK = 0x02;
    private int index;
    private long position;
    private int entryLength;
    private long bsOffset;
    private long next;
    private byte compress;
    private byte[] checkSum;
    private int nameTableLength;
    private int itemListLength;
    private int nameCount;
    private boolean isParse;

    XDAEntry() {
        checkSum = new byte[CHECKSUM_LENGTH];
        isParse = false;
    }

    XDAEntry(int theIndex, long thePosition, int theEntryLength,
             long theBSOffset, long theNext, byte theCompress,
             byte[] theCheckSum, int theNameTableLength, int theNameCount,
             byte bitsParam) {
        index = theIndex;
        position = thePosition;

        entryLength = theEntryLength;
        bsOffset = theBSOffset;
        next = theNext;
        compress = theCompress;
        checkSum = theCheckSum.clone();
        nameTableLength = theNameTableLength;

        nameCount = theNameCount;
        itemListLength = theEntryLength - CLASSTYPE_LENGTH - ENTRYLENGTH_LENGTH
                - bitsParam - bitsParam - COMPRESS_LENGTH - NAMEVALUE_LENGTH
                - theNameTableLength;

        isParse = true;
    }

    void parse(RandomAccessFile fileXDA, final long position,
               final byte bitsParam, final int index,
               HashMap<String, XDAItemInfo> itemsMap) throws FooE,
            IOException {
        this.position = position;
        this.index = index;
        if (bitsParam != 0x02 && bitsParam != 0x04 && bitsParam != 0x08)
            throw new FooE(FooE.INVALID_BITSPARAM);

        fileXDA.seek(this.position);
        parseClassType(fileXDA);
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

    long getPosition() throws FooE {
        if (!isParse)
            throw new FooE(FooE.NEVER_PARSE_ENTRY);

        return position;
    }

    public void setPosition(long position) throws FooE {
        if (!isParse)
            throw new FooE(FooE.NEVER_PARSE_ENTRY);

        this.position = position;
    }

    int getEntryLength() throws FooE {
        if (!isParse)
            throw new FooE(FooE.NEVER_PARSE_ENTRY);

        return entryLength;
    }

    public void setEntryLength(int entryLength) {
        this.entryLength = entryLength;
    }

    long getBSOffset() throws FooE {
        if (!isParse)
            throw new FooE(FooE.NEVER_PARSE_ENTRY);

        return bsOffset;
    }

    long getNext() throws FooE {
        if (!isParse)
            throw new FooE(FooE.NEVER_PARSE_ENTRY);

        return next;
    }

    public void setNext(long next) {
        this.next = next;
    }

    byte getCompress() throws FooE {
        if (!isParse)
            throw new FooE(FooE.NEVER_PARSE_ENTRY);

        return compress;
    }

    public void setCompress(byte compress) {
        this.compress = compress;
    }

    // @SuppressWarnings("unchecked")
    // private void parseAllItems(RandomAccessFile theFileXDA, final byte
    // theBitsParam, HashMap<String, XDAItemInfo> itemsMap) throws IOException,
    // XDAException {
    // DataInputStream nameTableStream = createNameTableStream(theFileXDA);
    // DataInputStream itemListStream = createItemListStream(theFileXDA);
    //
    // for (int i = 0; i < nameCount; ++i) {
    // List itemInfos = parseOneItem(nameTableStream, itemListStream,
    // theBitsParam);
    //
    // BigInteger nameValueInNameTable = (BigInteger)itemInfos.get(0);
    // String path = (String)itemInfos.get(1);
    //
    // byte operator = ((Byte)itemInfos.get(2)).byteValue();
    // long itemOffset = ((Long)itemInfos.get(3)).longValue();
    // BigInteger nameValueInItemList = (BigInteger)itemInfos.get(4);
    //
    // if (!nameValueInNameTable.equals(nameValueInItemList))
    // throw new
    // XDAException("Namevalues in NameTable & in ItmeList are not same.");
    //
    // updateItemsMap(itemsMap, theFileXDA, nameValueInNameTable, path,
    // operator, itemOffset, theBitsParam);
    // }
    // }

    byte[] getCheckSum() throws FooE {
        if (!isParse)
            throw new FooE(FooE.NEVER_PARSE_ENTRY);

        return checkSum;
    }

    public void setCheckSum(byte[] checkSum) {
        this.checkSum = checkSum;
    }

    int getNameTableLength() throws FooE {
        if (!isParse)
            throw new FooE(FooE.NEVER_PARSE_ENTRY);

        return nameTableLength;
    }

    public void setNameTableLength(int nameTableLength) {
        this.nameTableLength = nameTableLength;
    }

    void modifyNext(RandomAccessFile theXDA, long theNext,
                    byte mutableFieldLength) throws IOException {
        theXDA.seek(position + CLASSTYPE_LENGTH + ENTRYLENGTH_LENGTH
                + mutableFieldLength);
        next = theNext;
        Utils.writeIntegerAccording2BitsParam(theXDA,
                mutableFieldLength, next);
    }

    private DataInputStream createNameTableStream(RandomAccessFile theFileXDA)
            throws IOException, FooE {
        byte[] nameTableData = new byte[nameTableLength];
        if (theFileXDA.read(nameTableData) < nameTableLength)
            throw new FooE(FooE.INVALID_NAMETABLE);

        InputStream pureNameTableDataStream = new ByteArrayInputStream(
                nameTableData);

        // NameTable被压缩
        if ((compress & NAMETABLE_COMPRESS_MASK) != 0)
            return new DataInputStream(new InflaterInputStream(
                    pureNameTableDataStream));
            // NameTable没有被压缩
        else
            return new DataInputStream(pureNameTableDataStream);
    }

    private void calcItemListLength(final byte bitsParam) {
        itemListLength = entryLength - CLASSTYPE_LENGTH - ENTRYLENGTH_LENGTH
                - bitsParam - bitsParam - COMPRESS_LENGTH - CHECKSUM_LENGTH
                - NAMETABLELENGTH_LENGTH - nameTableLength;
    }

    private DataInputStream createItemListStream(RandomAccessFile theFileXDA)
            throws IOException, FooE {
        byte[] itemListData = new byte[itemListLength];
        if (theFileXDA.read(itemListData) < itemListLength)
            throw new FooE(FooE.INVALID_ITEMLIST);

        InputStream pureItemListDataStream = new ByteArrayInputStream(
                itemListData);

        // NameTable被压缩
        if ((compress & ITEMLIST_COMPRESS_MASK) != 0)
            return new DataInputStream(new BufferedInputStream(
                    new InflaterInputStream(pureItemListDataStream)));
            // NameTable没有被压缩
        else
            return new DataInputStream(pureItemListDataStream);
    }

    // private void parseNameCount(RandomAccessFile theFileXDA) throws
    // IOException {
    // nameCount = Utils.readInt(theFileXDA);
    // }

    ItemData parseOneItemInItemList(DataInputStream itemListStream,
                                    final byte theBitsParam) throws IOException {
        Byte operator = itemListStream.readByte();
        Long itemOffset = Utils
                .readIntegerAccording2BitsParam(itemListStream, theBitsParam);
        BigInteger nameValueInItemList = Utils.readBigInteger(
                itemListStream, NAMEVALUE_LENGTH);
        return new ItemData(operator, itemOffset, nameValueInItemList);
    }

    HashMap<BigInteger, OperItem> parseItemList(DataInputStream itemListStream,
                                                final byte theBitsParam) throws IOException {
        // 先解析itemListStream
        HashMap<BigInteger, OperItem> operItems = new HashMap<>(); // 哈希表初始大小为个数*2

        ItemData oneItemData;
        try {
            oneItemData = parseOneItemInItemList(itemListStream, theBitsParam);
        } catch (IOException e) {
            return operItems;
        }

        OperItem oneOperItem = new OperItem();

        OperItem.OpAndOffSet oneOpAndOffSet = oneOperItem.new OpAndOffSet(
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
                oneItemData = parseOneItemInItemList(itemListStream,
                        theBitsParam);
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
                oneOperItem = new OperItem();
                oneOperItem.nameValue = oneNameValue;
                oneOperItem.opAndOffset.add(oneOpAndOffSet);
                operItems.put(oneOperItem.nameValue, oneOperItem);
            }
        }

        return operItems;
    }

    private Vector<NamePathPair> parseNameTable(DataInputStream nameTableStream)
            throws IOException {
        nameCount = Utils.readInt(nameTableStream);
        Vector<NamePathPair> nameTable = new Vector<>(nameCount);
        for (int i = 0; i < nameCount; ++i) {
            // ItemInfo in NameTable
            BigInteger nameValueInNameTable = Utils.readBigInteger(
                    nameTableStream, NAMEVALUE_LENGTH);
            String path = Utils.readUTF8(nameTableStream, PATH_LENGTH);
            nameTable.add(new NamePathPair(path, nameValueInNameTable));
        }

        return nameTable;
    }

    private void parseNameTableAndItemList(RandomAccessFile theFileXDA,
                                           final byte theBitsParam, HashMap<String, XDAItemInfo> itemsMap)
            throws IOException, FooE {
        DataInputStream nameTableStream = createNameTableStream(theFileXDA);
        DataInputStream itemListStream = createItemListStream(theFileXDA);

        Vector<NamePathPair> nameTable = parseNameTable(nameTableStream);
        HashMap<BigInteger, OperItem> itemList = parseItemList(itemListStream,
                theBitsParam);
        updateItemsMap(theFileXDA, theBitsParam, nameTable, itemList, itemsMap);
    }

    private void updateExistedItemOp2ItemsMap(OperItem theOperItem,
                                              XDAItemInfo theItemInfo, RandomAccessFile theFileXDA,
                                              byte theBitsParam) throws FooE, IOException {
        for (OperItem.OpAndOffSet opAndOffSet : theOperItem.opAndOffset) {
            XDAHistory lastHistroy = theItemInfo.histories.lastElement();
            CheckOperatorSequence(lastHistroy.operator,
                    (byte) (opAndOffSet.operator & XDADefine.OPERATOR_MASK));
            theItemInfo
                    .addHistory(
                            index,
                            (byte) (opAndOffSet.operator & XDADefine.OPERATOR_MASK),
                            theFileXDA, position + opAndOffSet.itemOffset,
                            theBitsParam);
        }
    }

    private void updateUnexistedItemOp2ItemsMap(OperItem operItem, String path,
                                                HashMap<String, XDAItemInfo> itemsMap, RandomAccessFile fileXDA,
                                                byte bitsParam) throws FooE, IOException {
        if ((byte) (operItem.opAndOffset.firstElement().operator & XDADefine.OPERATOR_MASK) != XDADefine.OPERATOR_NEW)
            throw new FooE(FooE.INEXISTENT_ITEM);

        XDAItemInfo newItemInfo = new XDAItemInfo(path);
        int i = 0;
        OperItem.OpAndOffSet opAndOffset = operItem.opAndOffset.get(i++);
        newItemInfo.addHistory(index,
                (byte) (opAndOffset.operator & XDADefine.OPERATOR_MASK),
                fileXDA, bsOffset + opAndOffset.itemOffset, bitsParam);

        for (; i < operItem.opAndOffset.size(); ++i) {
            XDAHistory lastHistroy = newItemInfo.histories.lastElement();
            CheckOperatorSequence(
                    lastHistroy.operator,
                    (byte) (operItem.opAndOffset.get(i).operator & XDADefine.OPERATOR_MASK));
            newItemInfo
                    .addHistory(
                            index,
                            (byte) (operItem.opAndOffset.get(i).operator & XDADefine.OPERATOR_MASK),
                            fileXDA, bsOffset
                                    + operItem.opAndOffset.get(i).itemOffset,
                            bitsParam);
        }

        itemsMap.put(path, newItemInfo);
    }

    private void updateItemsMap(RandomAccessFile fileXDA, byte bitsParam,
                                Vector<NamePathPair> nameTable,
                                HashMap<BigInteger, OperItem> itemList,
                                HashMap<String, XDAItemInfo> itemsMap) throws FooE,
            IOException {

        for (NamePathPair namePathPair : nameTable) {
            OperItem oneOperItem = itemList.get(namePathPair.nameValue);
            if (oneOperItem == null)
                throw new FooE(FooE.INVALID_NAMEVALUE);

            XDAItemInfo oneItemInfo = itemsMap.get(namePathPair.path);
            // 路径名已经存在
            if (oneItemInfo != null)
                updateExistedItemOp2ItemsMap(oneOperItem, oneItemInfo, fileXDA,
                        bitsParam);
                // 路径名第一次出现
            else {
                // oneItemInfo = new XDAItemInfo(namePathPair.path);
                updateUnexistedItemOp2ItemsMap(oneOperItem, namePathPair.path,
                        itemsMap, fileXDA, bitsParam);
            }
        }
    }

    private void CheckOperatorSequence(byte lastOperator, byte currentOperator)
            throws FooE {
        if ((lastOperator == XDADefine.OPERATOR_DELETE && currentOperator != XDADefine.OPERATOR_NEW)
                || (lastOperator == XDADefine.OPERATOR_NEW && currentOperator == XDADefine.OPERATOR_NEW))
            throw new FooE(FooE.INVALID_OPERATION_SEQUENCE);
    }

    private void parseClassType(RandomAccessFile theFileXDA)
            throws IOException, FooE {
        byte[] theClassType = new byte[CLASSTYPE_LENGTH];
        theFileXDA.read(theClassType);
        if (!Utils.compareArray(theClassType, CLASSTYPE_CONTENT))
            throw new FooE(FooE.INVALID_ENTRY_CLASSTYPE);
    }

    private void parseEntryLength(RandomAccessFile theFileXDA)
            throws IOException {
        entryLength = Utils.readInt(theFileXDA);
    }

    private void parseBSOffset(RandomAccessFile theFileXDA,
                               final byte theBitsParam) throws IOException, FooE {
        bsOffset = Utils.readIntegerAccording2BitsParam(theFileXDA,
                theBitsParam);
    }

    private void parseNext(RandomAccessFile theFileXDA, final byte theBitsParam)
            throws IOException, FooE {
        next = Utils.readIntegerAccording2BitsParam(theFileXDA,
                theBitsParam);
    }

    private void parseCompress(RandomAccessFile theFileXDA) throws IOException,
            FooE {
        compress = theFileXDA.readByte();
    }

    private void parseCheckSum(RandomAccessFile theFileXDA) throws IOException {
        theFileXDA.read(checkSum);
    }

    private void parseNameTableLength(RandomAccessFile theFileXDA)
            throws IOException {
        nameTableLength = Utils.readInt(theFileXDA);
    }

    public int getIndex() throws FooE {
        if (!isParse)
            throw new FooE(FooE.NEVER_PARSE_ENTRY);

        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setBsOffset(long bsOffset) {
        this.bsOffset = bsOffset;
    }

    public int getItemListLength() throws FooE {
        if (!isParse)
            throw new FooE(FooE.NEVER_PARSE_ENTRY);

        return itemListLength;
    }

    public void setItemListLength(int itemListLength) {
        this.itemListLength = itemListLength;
    }

    public int getNameCount() throws FooE {
        if (!isParse)
            throw new FooE(FooE.NEVER_PARSE_ENTRY);

        return nameCount;
    }

    public void setNameCount(int nameCount) {
        this.nameCount = nameCount;
    }

    // 操作项
    static class OperItem {
        final Vector<OpAndOffSet> opAndOffset;
        BigInteger nameValue;

        OperItem() {
            opAndOffset = new Vector<>();
            nameValue = null;
        }

        class OpAndOffSet {
            final byte operator;
            final long itemOffset;

            OpAndOffSet(byte theOperator, long theItemOffset) {
                operator = theOperator;
                itemOffset = theItemOffset;
            }
        }
    }

    static class NamePathPair {
        final String path;
        final BigInteger nameValue;

        NamePathPair(String thePath, BigInteger theNameValue) {
            path = thePath;
            nameValue = theNameValue;
        }
    }
}
