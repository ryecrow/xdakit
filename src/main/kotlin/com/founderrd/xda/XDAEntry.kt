/**
 * Title:	XDAEntry
 * Description:	提供解析Entry和获取Entry信息功能
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

import com.founderrd.xda.XDACommonFunction
import com.founderrd.xda.XDAEntry.OperItem.OpAndOffSet
import com.founderrd.xda.XDAException
import java.io.*
import java.math.BigInteger
import java.util.*
import java.util.zip.InflaterInputStream

internal class XDAEntry {
    private var index = 0
    private var position: Long = 0
    private var entryLength = 0
    private var bsOffset: Long = 0
    private var next: Long = 0
    private var compress: Byte = 0
    private var checkSum: ByteArray
    private var nameTableLength = 0
    private var itemListLength = 0
    private var nameCount = 0
    private var isParse: Boolean

    constructor() {
        checkSum = ByteArray(CHECKSUM_LENGTH)
        isParse = false
    }

    constructor(
        theIndex: Int, thePosition: Long, theEntryLength: Int,
        theBSOffset: Long, theNext: Long, theCompress: Byte,
        theCheckSum: ByteArray, theNameTableLength: Int, theNameCount: Int,
        bitsParam: Byte
    ) {
        index = theIndex
        position = thePosition
        entryLength = theEntryLength
        bsOffset = theBSOffset
        next = theNext
        compress = theCompress
        checkSum = theCheckSum.clone()
        nameTableLength = theNameTableLength
        nameCount = theNameCount
        itemListLength = (theEntryLength - CLASSTYPE_LENGTH - ENTRYLENGTH_LENGTH
                - bitsParam - bitsParam - COMPRESS_LENGTH - NAMEVALUE_LENGTH
                - theNameTableLength)
        isParse = true
    }

    @Throws(XDAException::class, IOException::class)
    fun parse(
        fileXDA: RandomAccessFile?, position: Long,
        bitsParam: Byte, index: Int,
        itemsMap: HashMap<String?, XDAItemInfo>
    ) {
        this.position = position
        this.index = index
        if (bitsParam.toInt() != 0x02 && bitsParam.toInt() != 0x04 && bitsParam.toInt() != 0x08) throw XDAException(
            XDAException.Companion.INVALID_BITSPARAM
        )
        fileXDA!!.seek(this.position)
        parseClassType(fileXDA)
        parseEntryLength(fileXDA)
        parseBSOffset(fileXDA, bitsParam)
        parseNext(fileXDA, bitsParam)
        parseCompress(fileXDA)
        parseCheckSum(fileXDA)
        parseNameTableLength(fileXDA)
        calcItemListLength(bitsParam)
        parseNameTableAndItemList(fileXDA, bitsParam, itemsMap)
        isParse = true
    }

    @Throws(XDAException::class)
    fun getPosition(): Long {
        if (!isParse) throw XDAException(XDAException.Companion.NEVER_PARSE_ENTRY)
        return position
    }

    @Throws(XDAException::class)
    fun setPosition(position: Long) {
        if (!isParse) throw XDAException(XDAException.Companion.NEVER_PARSE_ENTRY)
        this.position = position
    }

    @Throws(XDAException::class)
    fun getEntryLength(): Int {
        if (!isParse) throw XDAException(XDAException.Companion.NEVER_PARSE_ENTRY)
        return entryLength
    }

    fun setEntryLength(entryLength: Int) {
        this.entryLength = entryLength
    }

    @get:Throws(XDAException::class)
    val bSOffset: Long
        get() {
            if (!isParse) throw XDAException(XDAException.Companion.NEVER_PARSE_ENTRY)
            return bsOffset
        }

    @Throws(XDAException::class)
    fun getNext(): Long {
        if (!isParse) throw XDAException(XDAException.Companion.NEVER_PARSE_ENTRY)
        return next
    }

    fun setNext(next: Long) {
        this.next = next
    }

    @Throws(XDAException::class)
    fun getCompress(): Byte {
        if (!isParse) throw XDAException(XDAException.Companion.NEVER_PARSE_ENTRY)
        return compress
    }

    fun setCompress(compress: Byte) {
        this.compress = compress
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
    @Throws(XDAException::class)
    fun getCheckSum(): ByteArray {
        if (!isParse) throw XDAException(XDAException.Companion.NEVER_PARSE_ENTRY)
        return checkSum
    }

    fun setCheckSum(checkSum: ByteArray) {
        this.checkSum = checkSum
    }

    @Throws(XDAException::class)
    fun getNameTableLength(): Int {
        if (!isParse) throw XDAException(XDAException.Companion.NEVER_PARSE_ENTRY)
        return nameTableLength
    }

    fun setNameTableLength(nameTableLength: Int) {
        this.nameTableLength = nameTableLength
    }

    @Throws(IOException::class)
    fun modifyNext(
        theXDA: RandomAccessFile?, theNext: Long,
        mutableFieldLength: Byte
    ) {
        theXDA!!.seek(
            position + CLASSTYPE_LENGTH + ENTRYLENGTH_LENGTH
                    + mutableFieldLength
        )
        next = theNext
        XDACommonFunction.writeIntegerAccording2BitsParam(
            theXDA,
            mutableFieldLength, next
        )
    }

    @Throws(IOException::class, XDAException::class)
    private fun createNameTableStream(theFileXDA: RandomAccessFile?): DataInputStream {
        val nameTableData = ByteArray(nameTableLength)
        if (theFileXDA!!.read(nameTableData) < nameTableLength) throw XDAException(XDAException.Companion.INVALID_NAMETABLE)
        val pureNameTableDataStream: InputStream = ByteArrayInputStream(
            nameTableData
        )

        // NameTable被压缩
        return if (compress and NAMETABLE_COMPRESS_MASK != 0) DataInputStream(
            InflaterInputStream(
                pureNameTableDataStream
            )
        ) else DataInputStream(pureNameTableDataStream)
    }

    private fun calcItemListLength(bitsParam: Byte) {
        itemListLength = (entryLength - CLASSTYPE_LENGTH - ENTRYLENGTH_LENGTH
                - bitsParam - bitsParam - COMPRESS_LENGTH - CHECKSUM_LENGTH
                - NAMETABLELENGTH_LENGTH - nameTableLength)
    }

    @Throws(IOException::class, XDAException::class)
    private fun createItemListStream(theFileXDA: RandomAccessFile?): DataInputStream {
        val itemListData = ByteArray(itemListLength)
        if (theFileXDA!!.read(itemListData) < itemListLength) throw XDAException(XDAException.Companion.INVALID_ITEMLIST)
        val pureItemListDataStream: InputStream = ByteArrayInputStream(
            itemListData
        )

        // NameTable被压缩
        return if (compress and ITEMLIST_COMPRESS_MASK != 0) DataInputStream(
            BufferedInputStream(
                InflaterInputStream(pureItemListDataStream)
            )
        ) else DataInputStream(pureItemListDataStream)
    }

    // private void parseNameCount(RandomAccessFile theFileXDA) throws
    // IOException {
    // nameCount = XDACommonFunction.readInt(theFileXDA);
    // }
    @Throws(IOException::class)
    fun parseOneItemInItemList(
        itemListStream: DataInputStream,
        theBitsParam: Byte
    ): List<*> {
        val operator = java.lang.Byte.valueOf(itemListStream.readByte())
        val itemOffset =
            java.lang.Long.valueOf(XDACommonFunction.readIntegerAccording2BitsParam(itemListStream, theBitsParam))
        val nameValueInItemList = XDACommonFunction.readBigInteger(
            itemListStream, NAMEVALUE_LENGTH
        )
        val returnList: MutableList<*> = ArrayList<Any?>()
        returnList.add(operator)
        returnList.add(itemOffset)
        returnList.add(nameValueInItemList)
        return returnList
    }

    @Throws(IOException::class)
    fun parseItemList(
        itemListStream: DataInputStream,
        theBitsParam: Byte
    ): HashMap<BigInteger?, OperItem> {
        // 先解析itemListStream
        val operItems = HashMap<BigInteger?, OperItem>() // 哈希表初始大小为个数*2
        var oneItemData: List<*>? = null
        oneItemData = try {
            parseOneItemInItemList(itemListStream, theBitsParam)
        } catch (e: IOException) {
            return operItems
        }
        var oneOperItem = OperItem()
        var oneOpAndOffSet = oneOperItem.OpAndOffSet(
            (oneItemData.get(0) as Byte).toByte(), (oneItemData
                .get(1) as Long).toLong()
        )
        if (oneOpAndOffSet.operator == XDADefine.Companion.OPERATOR_END) return operItems
        oneOperItem.nameValue = oneItemData.get(2) as BigInteger? // nameValueInItemList
        oneOperItem.opAndOffset.add(oneOpAndOffSet)
        operItems[oneOperItem.nameValue] = oneOperItem
        var i = 0
        while (true) {
            ++i
            if (i == 83) {
                var k = 0
                ++k
            }
            oneItemData = try {
                parseOneItemInItemList(
                    itemListStream,
                    theBitsParam
                )
            } catch (e: IOException) {
                break
            }
            oneOpAndOffSet = oneOperItem.OpAndOffSet(
                (oneItemData
                    .get(0) as Byte).toByte(), (oneItemData.get(1) as Long)
                    .toLong()
            )
            if (oneOpAndOffSet.operator == XDADefine.Companion.OPERATOR_END) break
            val oneNameValue = oneItemData.get(2) as BigInteger
            if (oneNameValue == oneOperItem.nameValue) oneOperItem.opAndOffset.add(oneOpAndOffSet) else {
                oneOperItem = OperItem()
                oneOperItem.nameValue = oneNameValue
                oneOperItem.opAndOffset.add(oneOpAndOffSet)
                operItems[oneOperItem.nameValue] = oneOperItem
            }
        }
        return operItems
    }

    @Throws(IOException::class)
    private fun parseNameTable(nameTableStream: DataInputStream): Vector<NamePathPair> {
        nameCount = XDACommonFunction.readInt(nameTableStream)
        val nameTable = Vector<NamePathPair>(nameCount)
        for (i in 0 until nameCount) {
            // ItemInfo in NameTable
            val nameValueInNameTable = XDACommonFunction.readBigInteger(
                nameTableStream, NAMEVALUE_LENGTH
            )
            val path = XDACommonFunction.readUTF8(
                nameTableStream,
                PATH_LENGTH
            )
            nameTable.add(NamePathPair(path, nameValueInNameTable))
        }
        return nameTable
    }

    @Throws(IOException::class, XDAException::class)
    private fun parseNameTableAndItemList(
        theFileXDA: RandomAccessFile?,
        theBitsParam: Byte, itemsMap: HashMap<String?, XDAItemInfo>
    ) {
        val nameTableStream = createNameTableStream(theFileXDA)
        val itemListStream = createItemListStream(theFileXDA)
        val nameTable = parseNameTable(nameTableStream)
        val itemList = parseItemList(
            itemListStream,
            theBitsParam
        )
        updateItemsMap(theFileXDA, theBitsParam, nameTable, itemList, itemsMap)
    }

    @Throws(XDAException::class, IOException::class)
    private fun updateExistedItemOp2ItemsMap(
        theOperItem: OperItem,
        theItemInfo: XDAItemInfo, theFileXDA: RandomAccessFile?,
        theBitsParam: Byte
    ) {
        val opAndOffSets: Iterator<OpAndOffSet> = theOperItem.opAndOffset
            .iterator()
        while (opAndOffSets.hasNext()) {
            val opAndOffSet = opAndOffSets.next()
            val lastHistroy = theItemInfo.histories.lastElement()
            CheckOperatorSequence(
                lastHistroy.operator,
                (opAndOffSet.operator and XDADefine.Companion.OPERATOR_MASK).toByte()
            )
            theItemInfo
                .addHistory(
                    index, (opAndOffSet.operator and XDADefine.Companion.OPERATOR_MASK).toByte(),
                    theFileXDA, position + opAndOffSet.itemOffset,
                    theBitsParam
                )
        }
    }

    @Throws(XDAException::class, IOException::class)
    private fun updateUnexistedItemOp2ItemsMap(
        operItem: OperItem, path: String?,
        itemsMap: HashMap<String?, XDAItemInfo>, fileXDA: RandomAccessFile?,
        bitsParam: Byte
    ) {
        if ((operItem.opAndOffset.firstElement().operator and XDADefine.Companion.OPERATOR_MASK).toByte() != XDADefine.Companion.OPERATOR_NEW) throw XDAException(
            XDAException.Companion.INEXISTENT_ITEM
        )
        val newItemInfo = XDAItemInfo(path)
        var i = 0
        val opAndOffset = operItem.opAndOffset[i++]
        newItemInfo.addHistory(
            index, (opAndOffset.operator and XDADefine.Companion.OPERATOR_MASK).toByte(),
            fileXDA, bsOffset + opAndOffset.itemOffset, bitsParam
        )
        while (i < operItem.opAndOffset.size) {
            val lastHistroy = newItemInfo.histories.lastElement()
            CheckOperatorSequence(
                lastHistroy.operator, (operItem.opAndOffset[i].operator and XDADefine.Companion.OPERATOR_MASK).toByte()
            )
            newItemInfo
                .addHistory(
                    index, (operItem.opAndOffset[i].operator and XDADefine.Companion.OPERATOR_MASK).toByte(),
                    fileXDA, bsOffset
                            + operItem.opAndOffset[i].itemOffset,
                    bitsParam
                )
            ++i
        }
        itemsMap[path] = newItemInfo
    }

    @Throws(XDAException::class, IOException::class)
    private fun updateItemsMap(
        fileXDA: RandomAccessFile?, bitsParam: Byte,
        nameTable: Vector<NamePathPair>,
        itemList: HashMap<BigInteger?, OperItem>,
        itemsMap: HashMap<String?, XDAItemInfo>
    ) {
        val iter: Iterator<NamePathPair> = nameTable.iterator()
        while (iter.hasNext()) {
            val namePathPair = iter.next()
            val oneOperItem = itemList[namePathPair.nameValue]
                ?: throw XDAException(XDAException.Companion.INVALID_NAMEVALUE)
            val oneItemInfo = itemsMap[namePathPair.path]
            // 路径名已经存在
            if (oneItemInfo != null) updateExistedItemOp2ItemsMap(
                oneOperItem, oneItemInfo, fileXDA,
                bitsParam
            ) else {
                // oneItemInfo = new XDAItemInfo(namePathPair.path);
                updateUnexistedItemOp2ItemsMap(
                    oneOperItem, namePathPair.path,
                    itemsMap, fileXDA, bitsParam
                )
            }
        }
    }

    @Throws(XDAException::class)
    private fun CheckOperatorSequence(lastOperator: Byte, currentOperator: Byte) {
        if (lastOperator == XDADefine.Companion.OPERATOR_DELETE && currentOperator != XDADefine.Companion.OPERATOR_NEW
            || lastOperator == XDADefine.Companion.OPERATOR_NEW && currentOperator == XDADefine.Companion.OPERATOR_NEW
        ) throw XDAException(XDAException.Companion.INVALID_OPERATION_SEQUENCE)
    }

    @Throws(IOException::class, XDAException::class)
    private fun parseClassType(theFileXDA: RandomAccessFile?) {
        val theClassType = ByteArray(CLASSTYPE_LENGTH)
        theFileXDA!!.read(theClassType)
        if (!XDACommonFunction.compareArray(
                theClassType,
                CLASSTYPE_CONTENT
            )
        ) throw XDAException(XDAException.Companion.INVALID_ENTRY_CLASSTYPE)
    }

    @Throws(IOException::class)
    private fun parseEntryLength(theFileXDA: RandomAccessFile?) {
        entryLength = XDACommonFunction.readInt(theFileXDA)
    }

    @Throws(IOException::class, XDAException::class)
    private fun parseBSOffset(
        theFileXDA: RandomAccessFile?,
        theBitsParam: Byte
    ) {
        bsOffset = XDACommonFunction.readIntegerAccording2BitsParam(
            theFileXDA,
            theBitsParam
        )
    }

    @Throws(IOException::class, XDAException::class)
    private fun parseNext(theFileXDA: RandomAccessFile?, theBitsParam: Byte) {
        next = XDACommonFunction.readIntegerAccording2BitsParam(
            theFileXDA,
            theBitsParam
        )
    }

    @Throws(IOException::class, XDAException::class)
    private fun parseCompress(theFileXDA: RandomAccessFile?) {
        compress = theFileXDA!!.readByte()
    }

    @Throws(IOException::class)
    private fun parseCheckSum(theFileXDA: RandomAccessFile?) {
        theFileXDA!!.read(checkSum)
    }

    @Throws(IOException::class)
    private fun parseNameTableLength(theFileXDA: RandomAccessFile?) {
        nameTableLength = XDACommonFunction.readInt(theFileXDA)
    }

    @Throws(XDAException::class)
    fun getIndex(): Int {
        if (!isParse) throw XDAException(XDAException.Companion.NEVER_PARSE_ENTRY)
        return index
    }

    fun setIndex(index: Int) {
        this.index = index
    }

    fun setBsOffset(bsOffset: Long) {
        this.bsOffset = bsOffset
    }

    @Throws(XDAException::class)
    fun getItemListLength(): Int {
        if (!isParse) throw XDAException(XDAException.Companion.NEVER_PARSE_ENTRY)
        return itemListLength
    }

    fun setItemListLength(itemListLength: Int) {
        this.itemListLength = itemListLength
    }

    @Throws(XDAException::class)
    fun getNameCount(): Int {
        if (!isParse) throw XDAException(XDAException.Companion.NEVER_PARSE_ENTRY)
        return nameCount
    }

    fun setNameCount(nameCount: Int) {
        this.nameCount = nameCount
    }

    // 操作项
    internal inner class OperItem {
        var opAndOffset: Vector<OpAndOffSet>
        var nameValue: BigInteger?

        internal inner class OpAndOffSet(var operator: Byte, var itemOffset: Long)

        init {
            opAndOffset = Vector()
            nameValue = null
        }
    }

    internal inner class NamePathPair(var path: String?, var nameValue: BigInteger?)
    companion object {
        const val CLASSTYPE_LENGTH = 4
        val CLASSTYPE_CONTENT = byteArrayOf('C'.code.toByte(), '.'.code.toByte(), 'E'.code.toByte(), 'n'.code.toByte())
        const val ENTRYLENGTH_LENGTH = 4
        const val COMPRESS_LENGTH = 1
        const val CHECKSUM_LENGTH = 16
        const val NAMETABLELENGTH_LENGTH = 4
        const val NAMEVALUE_LENGTH = 16
        const val PATH_LENGTH = 256
        const val COMPRESS_UNDEFINED_FLAG_MARKER = 0xfc.toByte()
        const val NAMETABLE_COMPRESS_MASK: Byte = 0x01
        const val ITEMLIST_COMPRESS_MASK: Byte = 0x02
    }
}