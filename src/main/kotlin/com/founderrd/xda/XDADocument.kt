/**
 * Title:	XDADocument
 * Description:	实现各种具体的操作，可以定义XDAView提供视图
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */
package com.founderrd.xda

import com.founderrd.xda.XDACommonFunction
import com.founderrd.xda.XDADocument.BSInfo.FileStreamInfo
import com.founderrd.xda.util.writeIntegerAccording2BitsParam
import java.io.*
import java.nio.charset.Charset
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.regex.Pattern
import java.util.zip.DeflaterOutputStream

internal class XDADocument {

    var buffer = ByteArray(BUFFER_SIZE)
    private var theFile: RandomAccessFile?
    val header: XDAHeader = XDAHeader()
    private val entries: Vector<XDAEntry> = Vector()
    private val itemsMap: HashMap<String?, XDAItemInfo> = HashMap()
    private val changedItemPathSet: HashSet<String?> = HashSet()
    private var nameValue: Int
    private val viewList: MutableList<XDAView>

    /*
     * public:
     */
    @Throws(IOException::class)
    fun close() {
        uninit()
    }

    @Throws(IOException::class, XDAException::class)
    fun open(file: File) {
        try {
            theFile = RandomAccessFile(file, "rw")
            header.parse(theFile!!)
            parseItems()
            updateViewAfterParseItems()
        } catch (e: IOException) {
            uninit()
            throw e
        } catch (e: XDAException) {
            uninit()
            throw e
        }
    }

    @Throws(FileNotFoundException::class, XDAException::class)
    fun create(
        theXDAFile: File, theMajorVersion: Byte, theMinorVersion: Byte,
        theEntryNameTableType: Byte, theBitsParam: Byte
    ) {
        try {
            if (theXDAFile.exists()) theXDAFile.delete()
            theFile = RandomAccessFile(theXDAFile, "rw")
            header.create(
                theMajorVersion, theMinorVersion,
                theEntryNameTableType, theBitsParam
            )
        } catch (e: FileNotFoundException) {
            uninit()
            throw e
        } catch (e: XDAException) {
            uninit()
            throw e
        }
    }

    @Throws(IOException::class, XDAException::class)
    fun extractItemStream(
        pathInXDA: String?,
        outPutStream: OutputStream, dec: XDADecorator
    ) {
        checkHaveXDA()
        if (!IsValidPackPath(pathInXDA)) throw XDAException(XDAException.Companion.INVALID_PACK_PATH)
        val itemInfo = itemsMap[pathInXDA] ?: throw XDAException(XDAException.Companion.INEXISTENT_ITEM)
        val index = calcItemFirstStreamIndex(itemInfo)
        doExtractItemStream(index, itemInfo, outPutStream, dec)
    }

    // 插入文件，在调用saveChanges前，不能在类外部对itemStream进行操作
    @Throws(XDAException::class)
    fun insertItem(
        pathInXDA: String?, inputStream: XDAInputStream?,
        ecs: ByteArray
    ) {
        checkHaveXDA()
        if (!IsValidPackPath(pathInXDA)) throw XDAException(XDAException.Companion.INVALID_PACK_PATH)
        doInsertItem(pathInXDA, inputStream, ecs)
    }

    // 替换文件，在调用saveChanges前，不能在类外部对itemStream进行操作
    @Throws(XDAException::class)
    fun replaceItem(
        pathInXDA: String?, inputStream: XDAInputStream?,
        ecs: ByteArray
    ) {
        checkHaveXDA()
        if (!IsValidPackPath(pathInXDA)) throw XDAException(XDAException.Companion.INVALID_PACK_PATH)
        doReplaceItem(pathInXDA, inputStream, ecs)
    }

    // 追加文件，在调用saveChanges前，不能在类外部对itemStream进行操作
    @Throws(XDAException::class)
    fun appendItem(
        pathInXDA: String?, inputStream: XDAInputStream,
        ecs: ByteArray
    ) {
        checkHaveXDA()
        if (!IsValidPackPath(pathInXDA)) throw XDAException(XDAException.Companion.INVALID_PACK_PATH)
        doAppendItem(pathInXDA, inputStream, ecs)
    }

    // 删除项
    @Throws(XDAException::class)
    fun deleteItem(pathInXDA: String?) {
        checkHaveXDA()
        if (!IsValidPackPath(pathInXDA)) throw XDAException(XDAException.Companion.INVALID_PACK_PATH)
        checkNewOperationValid(pathInXDA, XDADefine.Companion.OPERATOR_DELETE)
        doDeleteItem(pathInXDA)
        updateView(pathInXDA, XDADefine.Companion.OPERATOR_DELETE)
    }

    val allLogicExistedItem: Vector<String>
        get() {
            val allLogicExistedItem = Vector<String>(itemsMap.size)
            val col: Collection<XDAItemInfo> = itemsMap.values
            val iter = col.iterator()
            while (iter.hasNext()) {
                val itemInfo = iter.next()
                if (itemInfo.histories.lastElement().operator != XDADefine.Companion.OPERATOR_DELETE) {
                    allLogicExistedItem.add(itemInfo.fullPath)
                }
            }
            return allLogicExistedItem
        }

    fun registerView(theView: XDAView?) {
        if (theView != null) viewList.add(theView)
        val col: Collection<XDAItemInfo> = itemsMap.values
        val iter = col.iterator()
        while (iter.hasNext()) {
            val itemInfo = iter.next()
            if (itemInfo.histories.lastElement().operator != XDADefine.Companion.OPERATOR_DELETE) theView!!.update(
                itemInfo.fullPath,
                XDADefine.Companion.OPERATOR_NEW
            )
        }
    }

    @Throws(XDAException::class, IOException::class, NoSuchAlgorithmException::class)
    fun saveChanges(entryCompress: Byte) {
        checkHaveXDA()
        writeHeader()
        val saveHelpers = tidyChangedItems()
        if (saveHelpers.size == 0) return
        val bSPosition = doSaveChangesIntoBS(saveHelpers)
        val entryPosition = doSaveChangesIntoEntry(
            saveHelpers, bSPosition,
            entryCompress
        )
        writeBackPriorEntryNext(entryPosition)
        writeBackHeader(entryPosition)
        changedItemPathSet.clear()
        nameValue = 1
    }

    @Throws(XDAException::class, IOException::class, NoSuchAlgorithmException::class)
    fun saveAs(
        path: File?, majorVersion: Byte, minorVersion: Byte,
        entryNameTableType: Byte, bitsParam: Byte, entryCompress: Byte
    ) {
        checkHaveXDA()
        val newFile = RandomAccessFile(path, "rw")
        val newHeader = XDAHeader()
        newHeader.create(
            majorVersion, minorVersion, entryNameTableType,
            bitsParam
        )
        newHeader.write(newFile)
        val bSOffset = newFile.length()
        val bSInfos = writeBS4NewFile(newFile, bitsParam)
        val firstEntryPos = newFile.length()
        writeEntry4NewFile(newFile, entryCompress, bSInfos, bitsParam, bSOffset)
        newHeader.writeBackFirstEntryOffset(newFile, firstEntryPos)
        if (!bSInfos.isEmpty()) newHeader.writeBackEntryCount(newFile, 1)
        newFile.close()
    }

    fun unregisterView(theView: XDAView) {
        viewList.remove(theView)
    }

    fun hasItem(pathInXDA: String?): Boolean {
        return if (!IsValidPackPath(pathInXDA)) false else itemsMap.containsKey(pathInXDA)
    }

    @Throws(IOException::class)
    private fun writeBSHeader4NewFile(newfile: RandomAccessFile): Long {
        val bSHeaderPos = newfile.length()
        newfile.seek(bSHeaderPos)
        newfile.write(BS_CLASSTYPE)
        return bSHeaderPos
    }

    @Throws(IOException::class, XDAException::class)
    private fun writeOneBSFileStream4NewFile(
        newfile: RandomAccessFile,
        bitsParam: Byte, itemInfo: XDAItemInfo, index: Int, bSHeaderPos: Long
    ): BSInfo {
        var idx = index
        val bSInfo = BSInfo()
        bSInfo.path = itemInfo.fullPath
        val checkSum = ByteArray(1)
        while (idx < itemInfo.histories.size) {
            val fileStreamInfo = bSInfo.FileStreamInfo()
            val history = itemInfo.histories[idx]
            var length: Long = 0
            val writeBackPosition = newfile.length()

            // FileStream信息
            fileStreamInfo.offset = writeBackPosition - bSHeaderPos
            fileStreamInfo.op = history.operator
            bSInfo.fileStreams.add(fileStreamInfo)

            // 预留checkSum和length位置
            newfile.seek(writeBackPosition)
            newfile.writeByte(checkSum[0].toInt())
            writeIntegerAccording2BitsParam(
                newfile,
                bitsParam, length
            )
            newfile.write(history.ecs)
            // 写FileStream
            length = history.writeTo(newfile, bitsParam, buffer, checkSum)
            newfile.seek(writeBackPosition)
            newfile.writeByte(checkSum[0].toInt())
            writeIntegerAccording2BitsParam(
                newfile,
                bitsParam, length
            )
            ++idx
        }
        return bSInfo
    }

    @Throws(IOException::class, XDAException::class, NoSuchAlgorithmException::class)
    private fun writeEntry4NewFile(
        newfile: RandomAccessFile,
        entryCompress: Byte,
        bSInfos: Vector<BSInfo>,
        bitsParam: Byte,
        bSOffset: Long
    ) {
        val writeBackPos = newfile.length()
        newfile.seek(writeBackPos)

        // 预写
        writeEntryClassType(newfile)
        var entryLength: Long = 0
        writeEntryLength(newfile, entryLength)
        writeEntryBSOffset(newfile, bitsParam, bSOffset)
        writeEntryNext(newfile, bitsParam, 0)
        writeEntryCompress(newfile, entryCompress)
        var checkSum = ByteArray(CHECKSUM_LENGTH)
        writeEntryCheckSum(newfile, checkSum)
        var nameTableLength: Long = 0
        writeEntryNameTableLength(newfile, nameTableLength)

        // 写NameTable和ItemList
        val md = MessageDigest.getInstance("MD5")
        nameTableLength = writeEntryNameTableAndItemList4NewFile(
            newfile,
            bSInfos, entryCompress, bitsParam, md
        )

        // 回填
        entryLength = newfile.length() - writeBackPos
        newfile.seek(writeBackPos + BS_CLASSTYPE.size)
        writeEntryLength(newfile, entryLength)
        writeEntryBSOffset(newfile, bitsParam, bSOffset)
        writeEntryNext(newfile, bitsParam, 0)
        writeEntryCompress(newfile, entryCompress)
        checkSum = md.digest()
        writeEntryCheckSum(newfile, checkSum)
        writeEntryNameTableLength(newfile, nameTableLength)
    }

    /*
     * private:
     */
    @Throws(XDAException::class)
    private fun doDeleteItem(pathInXDA: String?) {
        val itemInfo = markChangedItem(pathInXDA)
        addItemNewInfo(itemInfo, XDADefine.Companion.OPERATOR_DELETE, null, null)
    }

    @Throws(IOException::class)
    private fun writeEntryNameTableAndItemList4NewFile(
        newfile: RandomAccessFile, bSInfos: Vector<BSInfo>,
        entryCompress: Byte, bitsParam: Byte, md: MessageDigest
    ): Long {
        val nmTableData = ByteArrayOutputStream()
        val itemListData = ByteArrayOutputStream()
        var nmTable: OutputStream? = null
        var itemList: OutputStream? = null
        nmTable = if (entryCompress and COMPRESS_NAMETABLE_MASK != 0) {
            BufferedOutputStream(
                DeflaterOutputStream(
                    nmTableData
                )
            )
        } else nmTableData
        val nammTableStream = DataOutputStream(nmTable)
        itemList = if (entryCompress and COMPRESS_ITEMLIST_MASK != 0) {
            BufferedOutputStream(
                DeflaterOutputStream(
                    itemListData
                )
            )
        } else itemListData
        val itemListStream = DataOutputStream(itemList)
        XDACommonFunction.writeInt(nammTableStream, bSInfos.size.toLong())
        val iter: Iterator<BSInfo> = bSInfos.iterator()
        var i = 1
        val nmVal = ByteArray(ENTRY_NAMEVALUE_LENGTH)
        while (iter.hasNext()) {
            val oneBSInfo = iter.next()
            val `val` = XDACommonFunction.converIntBigEndian2LittleEndian(i)
            var j = 0
            while (j < `val`!!.size) {
                nmVal[j] = `val`[`val`.size - j - 1]
                ++j
            }
            while (j < nmVal.size) {
                nmVal[j] = 0x00
                ++j
            }
            nammTableStream.write(nmVal)
            val pathByte = oneBSInfo.path!!.toByteArray()
            nammTableStream.write(pathByte, 0, pathByte.size)
            nammTableStream.write(0x00)
            val historyIter: Iterator<FileStreamInfo> = oneBSInfo.fileStreams
                .iterator()
            while (historyIter.hasNext()) {
                val fileStreamHistory = historyIter.next()
                itemListStream.write(fileStreamHistory.op.toInt())
                XDACommonFunction.writeIntegerAccording2BitsParam(
                    itemListStream, bitsParam, fileStreamHistory.offset
                )
                itemListStream.write(nmVal)
            }
            ++i
        }
        val posMark = newfile.length()
        newfile.seek(posMark)
        val nameTableLength = XDACommonFunction.copyFromSrcToDst(
            nmTableData
                .toByteArray(), newfile, md
        )
        XDACommonFunction.copyFromSrcToDst(
            itemListData.toByteArray(), newfile,
            md
        )
        return nameTableLength
    }

    @Throws(XDAException::class)
    private fun doInsertItem(
        pathInXDA: String?,
        maintainedStream: XDAInputStream?, ecs: ByteArray
    ) {
        checkNewOperationValid(pathInXDA, XDADefine.Companion.OPERATOR_NEW)
        val itemInfo = markChangedItem(pathInXDA)
        addItemNewInfo(itemInfo, XDADefine.Companion.OPERATOR_NEW, maintainedStream, ecs)
    }

    @Throws(XDAException::class)
    private fun doReplaceItem(
        pathInXDA: String?,
        maintainedStream: XDAInputStream?, ecs: ByteArray
    ) {
        checkNewOperationValid(pathInXDA, XDADefine.Companion.OPERATOR_REPLACE)
        val itemInfo = markChangedItem(pathInXDA)
        addItemNewInfo(
            itemInfo, XDADefine.Companion.OPERATOR_REPLACE, maintainedStream,
            ecs
        )
    }

    @Throws(XDAException::class)
    private fun doAppendItem(
        pathInXDA: String?,
        maintainedStream: XDAInputStream, ecs: ByteArray
    ) {
        checkNewOperationValid(pathInXDA, XDADefine.Companion.OPERATOR_APPEND)
        val itemInfo = markChangedItem(pathInXDA)
        addItemNewInfo(
            itemInfo, XDADefine.Companion.OPERATOR_APPEND, maintainedStream,
            ecs
        )
    }

    @Throws(XDAException::class)
    private fun tidyLastOperationDelete(
        itemInfo: XDAItemInfo?,
        saveHelpers: Vector<SaveHelper>
    ) {
        val histories = itemInfo!!.histories
        var i = histories.size - 1
        while (i >= 0) {
            if (histories.elementAt(i).entryNo < header.getEntryCount() + 1) break
            --i
        }
        if (i == -1) {
            histories.removeAllElements()
        } else if (histories.elementAt(i).operator == XDADefine.Companion.OPERATOR_DELETE) {
            histories.setSize(i + 1)
        } else {
            histories.setElementAt(histories.lastElement(), i + 1)
            histories.setSize(i + 2)
            saveHelpers.add(SaveHelper(itemInfo, i + 1))
        }
    }

    @Throws(XDAException::class)
    private fun tidyLastOperationNew(
        itemInfo: XDAItemInfo?,
        saveHelpers: Vector<SaveHelper>
    ) {
        val histories = itemInfo!!.histories
        var i = histories.size - 1
        // 找到上一个entry的最后一个history
        while (i >= 0) {
            if (histories.elementAt(i).entryNo < header.getEntryCount() + 1) break
            --i
        }
        if (i == -1) {
            histories.setElementAt(histories.lastElement(), i + 1)
            histories.setSize(i + 2)
        } else {
            if (histories.elementAt(i)
                    .operator != XDADefine.Companion.OPERATOR_DELETE
            ) histories.lastElement().operator = XDADefine.Companion.OPERATOR_REPLACE
            histories.setElementAt(histories.lastElement(), i + 1)
            histories.setSize(i + 2)
        }
        saveHelpers.add(SaveHelper(itemInfo, i + 1))
    }

    @Throws(XDAException::class)
    private fun tidyLastOperationAppend(
        itemInfo: XDAItemInfo?,
        saveHelpers: Vector<SaveHelper>
    ) {
        val histories = itemInfo!!.histories
        var i = histories.size - 1

        // 找到本entry最开始的append
        while (i >= 0) {
            if (histories.elementAt(i).operator != XDADefine.Companion.OPERATOR_APPEND
                || histories.elementAt(i).entryNo < header.getEntryCount() + 1
            ) break
            --i
        }

        // 再上一个history, 逻辑上保证了j>=0
        var k = i
        // 本entry最开始的append的上一个history的也是这次操作中的
        if (histories.elementAt(i).entryNo == header.getEntryCount() + 1) {
            if (histories.elementAt(i).operator == XDADefine.Companion.OPERATOR_NEW) {
                while (k >= 0) {
                    if (histories.elementAt(k).entryNo < header.getEntryCount() + 1) break
                    --k
                }
                if (k >= 0
                    && histories.elementAt(k).operator != XDADefine.Companion.OPERATOR_DELETE
                ) histories.elementAt(i).operator = XDADefine.Companion.OPERATOR_REPLACE
                ++k
                saveHelpers.add(SaveHelper(itemInfo, k))
                while (i < histories.size) {
                    histories.setElementAt(histories.elementAt(i), k)
                    ++i
                    ++k
                }
                histories.setSize(k)
            } else {
                while (k >= 0) {
                    if (histories.elementAt(k).entryNo < header.getEntryCount() + 1) break
                    --k
                }
                if (k >= 0
                    && histories.elementAt(k).operator == XDADefine.Companion.OPERATOR_DELETE
                ) histories.elementAt(i).operator = XDADefine.Companion.OPERATOR_NEW
                ++k
                saveHelpers.add(SaveHelper(itemInfo, k))
                while (i < histories.size) {
                    histories.setElementAt(histories.elementAt(i), k)
                    ++i
                    ++k
                }
                histories.setSize(k)
            }
        } else saveHelpers.add(SaveHelper(itemInfo, k))
    }

    @Throws(XDAException::class)
    private fun tidyLastOperationReplace(
        itemInfo: XDAItemInfo?,
        saveHelpers: Vector<SaveHelper>
    ) {
        val histories = itemInfo!!.histories
        var i = histories.size - 1
        while (i >= 0) {
            if (histories.elementAt(i).entryNo < header.getEntryCount() + 1) break
            --i
        }
        if (i == -1) {
            histories.lastElement().operator = XDADefine.Companion.OPERATOR_NEW
            histories.setElementAt(histories.lastElement(), i + 1)
            histories.setSize(i + 2)
            saveHelpers.add(SaveHelper(itemInfo, i + 1))
        } else {
            if (histories.elementAt(i)
                    .operator == XDADefine.Companion.OPERATOR_DELETE
            ) histories.lastElement().operator = XDADefine.Companion.OPERATOR_NEW
            histories.setElementAt(histories.lastElement(), i + 1)
            histories.setSize(i + 2)
            saveHelpers.add(SaveHelper(itemInfo, i + 1))
        }
    }

    @Throws(IOException::class)
    private fun writeBSClassType() {
        theFile!!.write(BS_CLASSTYPE)
    }

    @Throws(XDAException::class)
    private fun tidyChangedItem(
        changedItemPath: String?,
        saveHelpers: Vector<SaveHelper>
    ) {
        val itemInfo = itemsMap[changedItemPath]
        val history = itemInfo!!.histories.lastElement()
        when (history.operator) {
            XDADefine.Companion.OPERATOR_DELETE -> tidyLastOperationDelete(itemInfo, saveHelpers)
            XDADefine.Companion.OPERATOR_APPEND -> tidyLastOperationAppend(itemInfo, saveHelpers)
            XDADefine.Companion.OPERATOR_NEW -> tidyLastOperationNew(itemInfo, saveHelpers)
            XDADefine.Companion.OPERATOR_REPLACE -> tidyLastOperationReplace(itemInfo, saveHelpers)
            else -> throw XDAException(XDAException.Companion.INVALID_OPERATION_TYPE)
        }
        if (itemInfo.histories.isEmpty()) itemsMap.remove(changedItemPath)
    }

    @Throws(XDAException::class, IOException::class)
    private fun writeBackPriorEntryNext(lastEntryPosition: Long) {
        val priorEntryIndex = entries.size - 2
        if (priorEntryIndex >= 0) {
            val priorEntry = entries.elementAt(priorEntryIndex)
            priorEntry.modifyNext(
                theFile, lastEntryPosition, header
                    .getBitsParam()
            )
        }
    }

    @Throws(XDAException::class, IOException::class)
    private fun writeBackHeader(lastEntryPosition: Long) {
        if (header.getEntryCount() == 0) header.writeBackFirstEntryOffset(theFile!!, lastEntryPosition)
        header.writeBackEntryCount(theFile!!, header.getEntryCount() + 1)
    }

    @Throws(XDAException::class, IOException::class)
    private fun writeBS4NewFile(
        newfile: RandomAccessFile,
        bitsParam: Byte
    ): Vector<BSInfo> {
        val bSHeaderPos = writeBSHeader4NewFile(newfile)
        val col: Collection<XDAItemInfo> = itemsMap.values
        val iter = col.iterator()
        val bSInfos = Vector<BSInfo>()
        while (iter.hasNext()) {
            var index = 0
            val itemInfo = iter.next()
            index = try {
                calcItemFirstStreamIndex(itemInfo)
            } catch (e: XDAException) {
                continue
            }
            val oneBSInfo = writeOneBSFileStream4NewFile(
                newfile, bitsParam,
                itemInfo, index, bSHeaderPos
            )
            bSInfos.add(oneBSInfo)
        }
        return bSInfos
    }

    @Throws(IOException::class, XDAException::class, NoSuchAlgorithmException::class)
    private fun doSaveChangesIntoEntry(
        saveHelpers: Vector<SaveHelper>,
        bSPosition: Long,
        entryCompress: Byte
    ): Long {
        val entryWritePosition = theFile!!.length()
        theFile!!.seek(entryWritePosition)
        writeEntryClassType()
        val writeBackLengthPosition = theFile!!.filePointer
        writeEntryLength(0)
        writeEntryBSOffset(bSPosition)
        writeEntryNext(0)
        writeEntryCompress(entryCompress)
        val writeBackCheckSumPosition = theFile!!.filePointer
        var checkSum = ByteArray(CHECKSUM_LENGTH)
        writeEntryCheckSum(checkSum)
        writeEntryNameTableLength(0)
        val md = MessageDigest.getInstance("MD5")
        val nameTableLength = writeEntryNameTableAndItemList(
            saveHelpers,
            bSPosition, entryCompress, checkSum, md
        )
        val entryLength = theFile!!.filePointer - entryWritePosition
        theFile!!.seek(writeBackLengthPosition)
        writeEntryLength(entryLength)
        theFile!!.seek(writeBackCheckSumPosition)
        checkSum = md.digest()
        writeEntryCheckSum(checkSum)
        writeEntryNameTableLength(nameTableLength)
        val newEntry = XDAEntry(
            entries.size,
            entryWritePosition,
            entryLength.toInt(),
            bSPosition,
            0,
            entryCompress,
            checkSum,
            nameTableLength.toInt(),
            saveHelpers.size,
            header
                .getBitsParam()
        )
        entries.add(newEntry)
        return entryWritePosition
    }

    @Throws(IOException::class)
    private fun writeEntryLength(entryLength: Long) {
        writeEntryLength(theFile, entryLength)
    }

    @Throws(IOException::class)
    private fun writeEntryLength(file: RandomAccessFile?, entryLength: Long) {
        XDACommonFunction.writeInt(file, entryLength)
    }

    @Throws(IOException::class, XDAException::class, NoSuchAlgorithmException::class)
    private fun writeEntryNameTableAndItemList(
        saveHelpers: Vector<SaveHelper>,
        bSPosition: Long,
        entryCompress: Byte,
        checkSum: ByteArray,
        md: MessageDigest
    ): Long {
        // 先生成nametable itemlist的文件，写入内容。
        val nameTableFile = File.createTempFile(XDAFLAG, null)
        val itemListFile = File.createTempFile(XDAFLAG, null)
        var nameTableOutputStream: OutputStream = FileOutputStream(nameTableFile)
        var itemListOutputStream: OutputStream = FileOutputStream(itemListFile)
        if (entryCompress and COMPRESS_NAMETABLE_MASK != 0) {
            nameTableOutputStream = DeflaterOutputStream(
                nameTableOutputStream
            )
        }
        if (entryCompress and COMPRESS_NAMETABLE_MASK != 0) {
            itemListOutputStream = DeflaterOutputStream(
                itemListOutputStream
            )
        }

        // 先写入namecount
        XDACommonFunction.writeInt(nameTableOutputStream, saveHelpers.size.toLong())
        val nameValue = ByteArray(ENTRY_NAMEVALUE_LENGTH)
        for (saveHelper in saveHelpers) {
            calcNameValue(saveHelper.itemInfo!!.fullPath, nameValue)
            nameTableOutputStream.write(nameValue)
            nameTableOutputStream.write(
                saveHelper.itemInfo!!.fullPath
                    .toByteArray(Charset.forName("UTF-8"))
            )
            nameTableOutputStream.write(0x00) // 写0(结尾)
            for (i in saveHelper.index until saveHelper.itemInfo!!.histories
                .size) {
                val history = saveHelper.itemInfo!!.histories[i] as XDAOldHistory
                itemListOutputStream
                    .write(saveHelper.itemInfo!!.histories[i].operator.toInt())
                XDACommonFunction.writeIntegerAccording2BitsParam(
                    itemListOutputStream, header.getBitsParam(), history
                        .position
                            - bSPosition
                )
                itemListOutputStream.write(nameValue)
            }
        }
        itemListOutputStream.write(XDADefine.Companion.OPERATOR_END.toInt())
        XDACommonFunction.writeIntegerAccording2BitsParam(
            itemListOutputStream,
            header.getBitsParam(), 0
        )
        var i = 0
        nameValue[i++] = 0x7f.toByte()
        while (i < ENTRY_NAMEVALUE_LENGTH) {
            nameValue[i] = 0xff.toByte()
            ++i
        }
        itemListOutputStream.write(nameValue)
        nameTableOutputStream.close()
        itemListOutputStream.close()
        val nameTableInputStream: InputStream = FileInputStream(nameTableFile)
        val itemListInputStream: InputStream = FileInputStream(itemListFile)
        val nameTableLength = XDACommonFunction.copyFromSrcToDst(
            nameTableInputStream, theFile, buffer, md
        )
        XDACommonFunction.copyFromSrcToDst(
            itemListInputStream, theFile,
            buffer, md
        )
        nameTableInputStream.close()
        itemListInputStream.close()
        nameTableFile.delete()
        itemListFile.delete()
        return nameTableLength
    }

    private fun calcNameValue(path: String, nameValue: ByteArray) {
        val theNameValue = XDACommonFunction.converIntBigEndian2LittleEndian(this.nameValue)
        var i = 0
        while (i < theNameValue!!.size) {
            nameValue[i] = theNameValue[theNameValue.size - i - 1]
            ++i
        }
        while (i < nameValue.size) {
            nameValue[i] = 0x00
            ++i
        }
        ++this.nameValue
    }

    @Throws(IOException::class)
    private fun writeEntryNameTableLength(nameTableLength: Long) {
        writeEntryNameTableLength(theFile, nameTableLength)
    }

    @Throws(IOException::class)
    private fun writeEntryNameTableLength(
        file: RandomAccessFile?,
        nameTableLength: Long
    ) {
        XDACommonFunction.writeInt(file, nameTableLength)
    }

    @Throws(IOException::class)
    private fun writeEntryCheckSum(checkSum: ByteArray) {
        writeEntryCheckSum(theFile, checkSum)
    }

    @Throws(IOException::class)
    private fun writeEntryCheckSum(file: RandomAccessFile?, checkSum: ByteArray) {
        file!!.write(checkSum)
    }

    @Throws(IOException::class)
    private fun writeEntryCompress(entryCompress: Byte) {
        writeEntryCompress(theFile, entryCompress)
    }

    @Throws(IOException::class)
    private fun writeEntryCompress(file: RandomAccessFile?, entryCompress: Byte) {
        file.write(entryCompress and COMPRESS_UNDEFINED_FLAG_MARKER.inv())
    }

    @Throws(IOException::class)
    private fun writeEntryClassType(file: RandomAccessFile? = theFile) {
        file!!.write(XDAEntry.Companion.CLASSTYPE_CONTENT)
    }

    @Throws(XDAException::class, IOException::class)
    private fun writeEntryBSOffset(bSOffset: Long) {
        writeEntryBSOffset(theFile, header.getBitsParam(), bSOffset)
    }

    @Throws(IOException::class)
    private fun writeEntryBSOffset(
        file: RandomAccessFile?, bitsParam: Byte,
        bSOffset: Long
    ) {
        XDACommonFunction.writeIntegerAccording2BitsParam(
            file, bitsParam,
            bSOffset
        )
    }

    @Throws(XDAException::class, IOException::class)
    private fun writeEntryNext(next: Long) {
        writeEntryNext(theFile, header.getBitsParam(), next)
    }

    @Throws(IOException::class)
    private fun writeEntryNext(file: RandomAccessFile?, bitsParam: Byte, next: Long) {
        XDACommonFunction.writeIntegerAccording2BitsParam(file, bitsParam, next)
    }

    @Throws(XDAException::class, IOException::class)
    private fun writeHeader() {
        if (header.getEntryCount() == 0) header.write(theFile!!)
    }

    @Throws(XDAException::class)
    private fun tidyChangedItems(): Vector<SaveHelper> {
        val saveHelpers = Vector<SaveHelper>()
        val changedItemIter: Iterator<String?> = changedItemPathSet.iterator()
        while (changedItemIter.hasNext()) {
            val changedItemPath = changedItemIter.next()
            tidyChangedItem(changedItemPath, saveHelpers)
        }
        return saveHelpers
    }

    @Throws(IOException::class, XDAException::class)
    private fun doSaveChangesIntoBS(saveHelpers: Vector<SaveHelper>): Long {
        val positon = theFile!!.length()
        theFile!!.seek(positon)
        writeBSClassType()
        for (i in saveHelpers.indices) {
            val saveHelper = saveHelpers.elementAt(i)
            when (saveHelper.itemInfo!!.histories.lastElement().operator) {
                XDADefine.Companion.OPERATOR_DELETE -> doSaveDeleteChange(saveHelper)
                XDADefine.Companion.OPERATOR_APPEND -> doSaveAppendChange(saveHelper)
                else -> doSaveNewOrReplaceChange(saveHelper)
            }
        }
        return positon
    }

    @Throws(IOException::class)
    private fun doSaveDeleteChange(saveHelper: SaveHelper) {
        setHistoryOfSaveHelper(saveHelper, saveHelper.index, -1, 0)
    }

    @Throws(IOException::class, XDAException::class)
    private fun doSaveAppendChange(saveHelper: SaveHelper) {
        val position = theFile!!.length()
        theFile!!.seek(position)
        for (i in saveHelper.index until saveHelper.itemInfo!!.histories.size) {
            val newHistory = saveHelper.itemInfo!!.histories
                .elementAt(i) as XDANewHistory
            val length = writeBSFileStream(newHistory)
            setHistoryOfSaveHelper(saveHelper, i, position, length)
        }
    }

    @Throws(IOException::class, XDAException::class)
    private fun doSaveNewOrReplaceChange(saveHelper: SaveHelper) {
        val position = theFile!!.length()
        theFile!!.seek(position)
        val newHistory = saveHelper.itemInfo!!.histories
            .elementAt(saveHelper.index) as XDANewHistory
        val length = writeBSFileStream(newHistory)
        setHistoryOfSaveHelper(saveHelper, saveHelper.index, position, length)
    }

    @Throws(IOException::class, XDAException::class)
    private fun writeBSFileStream(newHistory: XDANewHistory): Long {
        val length: Long = 0
        val checkSum = 0x00.toByte()
        val position = theFile!!.length()
        theFile!!.seek(position)
        writeBSCheckSum(checkSum)
        writeBSLength(length)
        writeBSECS(newHistory.eCS)
        val checkSumAndLength = writeBSFileData(newHistory)
        theFile!!.seek(position)
        writeBSCheckSum((checkSumAndLength[0] as Byte).toByte())
        writeBSLength((checkSumAndLength[1] as Long).toLong())
        return length
    }

    @Throws(IOException::class, XDAException::class)
    private fun writeBSFileData(newHistory: XDANewHistory): List<*> {
        val resultList: MutableList<*> = ArrayList<Any?>()
        val checkSum = ByteArray(1)
        checkSum[0] = 0x00
        val length = newHistory.writeTo(
            theFile, header.getBitsParam(),
            buffer, checkSum
        )
        resultList.add(checkSum[0])
        resultList.add(length)
        return resultList
    }

    @Throws(IOException::class)
    private fun writeBSCheckSum(checksum: Byte) {
        theFile!!.writeByte(checksum.toInt())
    }

    @Throws(XDAException::class, IOException::class)
    private fun writeBSLength(length: Long) {
        XDACommonFunction.writeIntegerAccording2BitsParam(
            theFile, header
                .getBitsParam(), length
        )
    }

    @Throws(IOException::class)
    private fun writeBSECS(ecs: ByteArray) {
        theFile!!.write(ecs)
    }

    // 可以改成二分法求异或。。。以后再改
    private fun calcCheckSum(src: ByteArray, from: Int, length: Int): Byte {
        var from = from
        var checksum: Byte = 0x00
        while (from < length) {
            checksum = checksum xor src[from]
            ++from
        }
        return checksum
    }

    private fun setHistoryOfSaveHelper(
        saveHelper: SaveHelper, index: Int,
        position: Long, length: Long
    ) {
        val newHistory = saveHelper.itemInfo!!.histories.elementAt(index)
        val oldHistory: XDAHistory = XDAOldHistory(
            newHistory, position,
            theFile, length
        )
        saveHelper.itemInfo!!.histories[index] = oldHistory
    }

    @Throws(XDAException::class)
    private fun addItemNewInfo(
        itemInfo: XDAItemInfo, operator: Byte,
        maintainedStream: XDAInputStream?, ecs: ByteArray?
    ) {
        itemInfo.addHistory(
            header.getEntryCount() + 1, operator,
            maintainedStream, ecs
        )
    }

    @Throws(IOException::class, XDAException::class)
    private fun doExtractItemStream(
        itemItemFirstStreamIndex: Int,
        itemInfo: XDAItemInfo, outPutStream: OutputStream,
        dec: XDADecorator
    ) {
        if (itemItemFirstStreamIndex == itemInfo.histories.size - 1) doExtractItemJustOneStream(
            itemInfo.histories.lastElement(),
            outPutStream, dec
        ) else doExtractItemManyStream(
            itemItemFirstStreamIndex, itemInfo,
            outPutStream, dec
        )
    }

    @Throws(IOException::class, XDAException::class)
    private fun doExtractItemJustOneStream(
        history: XDAHistory,
        outPutStream: OutputStream, dec: XDADecorator?
    ) {
        var targetOutputStream: OutputStream? = outPutStream
        if (dec != null && history.ecs[0] != XDADefine.Companion.OPERATOR_END) targetOutputStream = dec.InflateDecorate(
            outPutStream, history.ecs,
            history.ecs.size - 2
        )
        history.writeTo(targetOutputStream, header.getBitsParam(), buffer)
    }

    private fun markChangedItem(pathInXDA: String?): XDAItemInfo {
        changedItemPathSet.add(pathInXDA)
        var itemInfo = itemsMap[pathInXDA]
        if (itemInfo == null) {
            itemInfo = XDAItemInfo(pathInXDA)
            itemsMap[itemInfo.fullPath] = itemInfo
        }
        return itemInfo
    }

    @Throws(XDAException::class)
    private fun checkNewOperationValid(pathInXDA: String?, operation: Byte) {
        var valid = false
        val itemInfo = itemsMap[pathInXDA]
        when (operation) {
            XDADefine.Companion.OPERATOR_NEW -> {
                if (itemInfo != null) {
                    if (itemInfo.histories.lastElement().operator != XDADefine.Companion.OPERATOR_DELETE) break
                }
                valid = true
            }
            XDADefine.Companion.OPERATOR_REPLACE, XDADefine.Companion.OPERATOR_APPEND, XDADefine.Companion.OPERATOR_DELETE -> {
                if (itemInfo == null) break
                if (itemInfo.histories.lastElement().operator == XDADefine.Companion.OPERATOR_DELETE) break
                valid = true
            }
            else -> {
            }
        }
        if (!valid) throw XDAException(XDAException.Companion.INVALID_OPERATION)
    }

    @Throws(XDAException::class, IOException::class)
    private fun parseItems() {
        var position = header.getFirstEntryOffset()
        for (i in 0 until header.getEntryCount()) {
            val oneEntry = XDAEntry()
            oneEntry.parse(
                theFile, position, header.getBitsParam(), i + 1,
                itemsMap
            )
            position = oneEntry.next
            entries.add(oneEntry)
        }
        if (position != 0L) throw XDAException(XDAException.Companion.INVALID_NEXT_FIELD_OF_LAST_ENTRY)
    }

    @Throws(XDAException::class)
    private fun calcItemFirstStreamIndex(itemInfo: XDAItemInfo): Int {
        var index = itemInfo.histories.size - 1
        var currentHistory = itemInfo.histories.elementAt(index)
        if (currentHistory.operator == XDADefine.Companion.OPERATOR_DELETE) throw XDAException(XDAException.Companion.INVALID_ITEM_CONTENT)
        while (index >= 0) {
            if (currentHistory.operator != XDADefine.Companion.OPERATOR_APPEND) break
            currentHistory = itemInfo.histories.elementAt(--index)
        }
        if (index < 0
            || currentHistory.operator != XDADefine.Companion.OPERATOR_NEW && currentHistory.operator != XDADefine.Companion.OPERATOR_REPLACE
        ) throw XDAException(XDAException.Companion.INVALID_ITEM_CONTENT)
        return index
    }

    private fun updateView(path: String?, operator: Byte) {
        for (theView in viewList) theView.update(path!!, operator)
    }

    private fun updateViewAfterParseItems() {
        if (viewList.isEmpty()) return
        val col: Collection<XDAItemInfo> = itemsMap.values
        val iter = col.iterator()
        while (iter.hasNext()) {
            val itemInfo = iter.next()
            if (itemInfo.histories.lastElement().operator != XDADefine.Companion.OPERATOR_DELETE) updateView(
                itemInfo.fullPath,
                XDADefine.Companion.OPERATOR_NEW
            )
        }
    }

    @Throws(IOException::class, XDAException::class)
    private fun doExtractItemManyStream(
        itemItemFirstStreamIndex: Int,
        itemInfo: XDAItemInfo, outPutStream: OutputStream,
        dec: XDADecorator?
    ) {
        var index = itemItemFirstStreamIndex
        while (index < itemInfo.histories.size) {
            val currentHistory = itemInfo.histories.elementAt(index)
            if (currentHistory.ecs[0] == XDADefine.Companion.OPERATOR_END || dec == null) {
                currentHistory.writeTo(
                    outPutStream, header.getBitsParam(),
                    buffer
                )
            } else {
                val tmpFile = File.createTempFile("xda", null)
                var tmpStream: OutputStream? = FileOutputStream(tmpFile)
                tmpStream = dec.InflateDecorate(
                    tmpStream, currentHistory.ecs,
                    currentHistory.ecs.size - 2
                )
                currentHistory
                    .writeTo(tmpStream, header.getBitsParam(), buffer)
                tmpStream!!.close()
                val unCodeStream: InputStream = FileInputStream(tmpFile)
                XDACommonFunction.copyFromSrcToDst(
                    unCodeStream, outPutStream,
                    buffer
                )
                unCodeStream.close()
                tmpFile.delete()
            }
            ++index
        }
    }

    private fun IsValidPackPath(path: String?): Boolean {
        return PACKPATH_PATTERN.matcher(path).matches()
    }

    private fun uninit() {
        if (theFile != null) {
            try {
                theFile!!.close()
            } finally {
                entries.clear()
                itemsMap.clear()
                changedItemPathSet.clear()
                nameValue = 1
                viewList.clear()
                file = null
            }
        }
    }

    @Throws(XDAException::class)
    private fun checkHaveXDA() {
        if (theFile == null) throw XDAException(XDAException.Companion.NO_XDA_FILE)
    }

    internal inner class SaveHelper(var itemInfo: XDAItemInfo?, var index: Int)
    internal inner class BSInfo {
        var path: String? = null
        var fileStreams: Vector<FileStreamInfo>

        internal inner class FileStreamInfo {
            var offset: Long = 0
            var op: Byte

            init {
                op = 0x00.toByte()
            }
        }

        init {
            fileStreams = Vector()
        }
    }

    companion object {
        val PACKPATH_PATTERN = Pattern
            .compile("([\\\\/]([^\t:*?\"<>|\\\\/])+)+")
        const val BUFFER_SIZE = 65536
        val BS_CLASSTYPE = byteArrayOf('C'.code.toByte(), '.'.code.toByte(), 'B'.code.toByte(), 'S'.code.toByte())
        const val COMPRESS_UNDEFINED_FLAG_MARKER = 0xfc.toByte()
        const val COMPRESS_NAMETABLE_MASK: Byte = 0x01
        const val COMPRESS_ITEMLIST_MASK: Byte = 0x02
        const val CHECKSUM_LENGTH = 16
        const val XDAFLAG = "xda"
        const val ENTRY_NAMEVALUE_LENGTH = 16
        const val NAMECOUNT_LENGTH = 4
    }

    init {
        theFile = null
        nameValue = 1
        viewList = LinkedList()
    }
}