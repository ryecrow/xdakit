/**
 * Title:	XDADocument
 * Description:	实现各种具体的操作，可以定义XDAView提供视图
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;

class XDADocument {
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
	static final int NAMECOUNT_LENGTH = 4;
	final byte[] buffer = new byte[BUFFER_SIZE];
	private final XDAHeader header;
	private final Vector<XDAEntry> entrys;
	private final HashMap<String, XDAItemInfo> itemsMap;
	private final HashSet<String> changedItemPathSet;
	private final List<XDAView> viewList;
	private RandomAccessFile xdaDoc;
	private File file;
	private int nameValue;

	XDADocument() {
		header = new XDAHeader();
		entrys = new Vector<>();
		itemsMap = new HashMap<>();
		changedItemPathSet = new HashSet<>();
		xdaDoc = null;
		nameValue = 1;
		viewList = new LinkedList<>();
	}

	/*
	 * public:
	 */
	public void close() throws IOException {
		uninit();
	}

	public void open(File theXDAFile) throws IOException, XDAException {
		try {
			file = theXDAFile;
			xdaDoc = new RandomAccessFile(theXDAFile, "rw");
			header.parse(xdaDoc);
			parseItems();
			updateViewAfterParseItems();
		} catch (IOException | XDAException e) {
			uninit();
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
			uninit();
			throw e;
		}
	}

	public XDAHeader getHeader() {
		return header;
	}

	public Vector<XDAEntry> getEntrys() {
		return entrys;
	}

	public void extractItemStream(final String pathInXDA,
								  OutputStream outPutStream, XDADecorator dec) throws IOException,
			XDAException {
		checkHaveXDA();
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
		checkHaveXDA();
		if (!IsValidPackPath(pathInXDA))
			throw new XDAException(XDAException.INVALID_PACK_PATH);

		doInsertItem(pathInXDA, inputStream, ecs);
	}

	// 替换文件，在调用saveChanges前，不能在类外部对itemStream进行操作
	public void replaceItem(final String pathInXDA, XDAInputStream inputStream,
							byte[] ecs) throws XDAException {
		checkHaveXDA();
		if (!IsValidPackPath(pathInXDA))
			throw new XDAException(XDAException.INVALID_PACK_PATH);

		doReplaceItem(pathInXDA, inputStream, ecs);
	}

	// 追加文件，在调用saveChanges前，不能在类外部对itemStream进行操作
	public void appendItem(final String pathInXDA, XDAInputStream inputStream,
						   byte[] ecs) throws XDAException {
		checkHaveXDA();
		if (!IsValidPackPath(pathInXDA))
			throw new XDAException(XDAException.INVALID_PACK_PATH);

		doAppendItem(pathInXDA, inputStream, ecs);
	}

	// 删除项
	public void deleteItem(final String pathInXDA) throws XDAException {
		checkHaveXDA();
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
		checkHaveXDA();
		xdaDoc.close();
		xdaDoc = new RandomAccessFile(file, "rwd");

		writeHeader();
		Vector<SaveHelper> saveHelpers = tidyChangedItems();
		if (saveHelpers.size() == 0)
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
		checkHaveXDA();
		RandomAccessFile newfile = new RandomAccessFile(path, "rw");
		XDAHeader newHeader = new XDAHeader();
		newHeader.create(majorVersion, minorVersion, entryNameTableType,
				bitsParam);
		newHeader.write(newfile);
		long bSOffset = newfile.length();
		Vector<BSInfo> bSInfos = writeBS4NewFile(newfile, bitsParam);
		long firstEntryPos = newfile.length();
		writeEntry4NewFile(newfile, entryCompress, bSInfos, bitsParam, bSOffset);
		newHeader.writeBackFirstEntryOffset(newfile, firstEntryPos);
		if (!bSInfos.isEmpty())
			newHeader.writeBackEntryCount(newfile, 1);
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
			XDACommonFunction.writeIntegerAccording2BitsParam(newfile,
					bitsParam, length);
			newfile.write(history.ecs);
			// 写FileStream
			length = history.writeTo(newfile, bitsParam, buffer, checkSum);
			newfile.seek(writeBackPosition);
			newfile.writeByte(checkSum[0]);
			XDACommonFunction.writeIntegerAccording2BitsParam(newfile,
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

		XDACommonFunction.writeInt(nammTableStream, bSInfos.size());
		Iterator<BSInfo> iter = bSInfos.iterator();
		int i = 1;
		byte[] nmVal = new byte[ENTRY_NAMEVALUE_LENGTH];
		while (iter.hasNext()) {
			BSInfo oneBSInfo = iter.next();
			byte[] val = XDACommonFunction.converIntBigEndian2LittleEndian(i);
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
				XDACommonFunction.writeIntegerAccording2BitsParam(
						itemListStream, bitsParam, fileStreamHistory.offset);
				itemListStream.write(nmVal);
			}

			++i;
		}

		long posMark = newfile.length();
		newfile.seek(posMark);
		long nameTableLength = XDACommonFunction.copyFromSrcToDst(nmTableData
				.toByteArray(), newfile, md);
		XDACommonFunction.copyFromSrcToDst(itemListData.toByteArray(), newfile,
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
		int priorEntryIndex = entrys.size() - 2;
		if (priorEntryIndex >= 0) {
			XDAEntry priorEntry = entrys.elementAt(priorEntryIndex);
			priorEntry.modifyNext(xdaDoc, lastEntryPosition, header
					.getBitsParam());
		}
	}

	private void writeBackHeader(long lastEntryPosition) throws XDAException,
			IOException {
		if (header.getEntryCount() == 0)
			header.writeBackFirstEntryOffset(xdaDoc, lastEntryPosition);

		header.writeBackEntryCount(xdaDoc, header.getEntryCount() + 1);
	}

	private Vector<BSInfo> writeBS4NewFile(RandomAccessFile newfile,
										   byte bitsParam) throws XDAException, IOException {
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

		XDAEntry newEntry = new XDAEntry(entrys.size(), entryWritePosition,
				(int) entryLength, bSPosition, 0, entryCompress, checkSum,
				(int) nameTableLength, saveHelpers.size(), header
				.getBitsParam());
		entrys.add(newEntry);

		return entryWritePosition;
	}

	private void writeEntryLength(long entryLength) throws IOException {
		writeEntryLength(xdaDoc, entryLength);
	}

	private void writeEntryLength(RandomAccessFile file, long entryLength)
			throws IOException {
		XDACommonFunction.writeInt(file, entryLength);
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
		XDACommonFunction.writeInt(nameTableOutputStream, saveHelpers.size());

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
				XDACommonFunction.writeIntegerAccording2BitsParam(
						itemListOutputStream, header.getBitsParam(), history
								.getPosition()
								- bSPosition);
				itemListOutputStream.write(nameValue);
			}
		}
		itemListOutputStream.write(XDADefine.OPERATOR_END);
		XDACommonFunction.writeIntegerAccording2BitsParam(itemListOutputStream,
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

		long nameTableLength = XDACommonFunction.copyFromSrcToDst(
				nameTableInputStream, xdaDoc, buffer, md);
		XDACommonFunction.copyFromSrcToDst(itemListInputStream, xdaDoc,
				buffer, md);

		nameTableInputStream.close();
		itemListInputStream.close();

		nameTableFile.delete();
		itemListFile.delete();

		return nameTableLength;
	}

	private void calcNameValue(String path, byte[] nameValue) {
		byte[] theNameValue = XDACommonFunction
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
		XDACommonFunction.writeInt(file, nameTableLength);
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
		XDACommonFunction.writeIntegerAccording2BitsParam(file, bitsParam,
				bSOffset);
	}

	private void writeEntryNext(long next) throws XDAException, IOException {
		writeEntryNext(xdaDoc, header.getBitsParam(), next);
	}

	private void writeEntryNext(RandomAccessFile file, byte bitsParam, long next)
			throws IOException {
		XDACommonFunction
				.writeIntegerAccording2BitsParam(file, bitsParam, next);
	}

	private void writeHeader() throws XDAException, IOException {
		if (header.getEntryCount() == 0)
			header.write(xdaDoc);
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
		XDACommonFunction.writeIntegerAccording2BitsParam(xdaDoc, header
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

	private void parseItems() throws XDAException, IOException {
		long position = header.getFirstEntryOffset();
		for (int i = 0; i < header.getEntryCount(); ++i) {
			XDAEntry oneEntry = new XDAEntry();
			oneEntry.parse(xdaDoc, position, header.getBitsParam(), i + 1,
					itemsMap);
			position = oneEntry.getNext();
			entrys.add(oneEntry);
		}

		if (position != 0)
			throw new XDAException(XDAException.INVALID_NEXT_FIELD_OF_LAST_ENTRY);
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
		if (viewList.isEmpty())
			return;

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
				XDACommonFunction.copyFromSrcToDst(unCodeStream, outPutStream,
						buffer);
				unCodeStream.close();
				tmpFile.delete();
			}
		}
	}

	private boolean IsValidPackPath(String path) {
		return PACKPATH_PATTERN.matcher(path).matches();
	}

	private void uninit() {
		if (xdaDoc != null)
			try {
				xdaDoc.close();
			} catch (IOException e) {
			}

		entrys.clear();
		itemsMap.clear();
		changedItemPathSet.clear();
		nameValue = 1;
		viewList.clear();
		file = null;
	}

	private void checkHaveXDA() throws XDAException {
		if (xdaDoc == null)
			throw new XDAException(XDAException.NO_XDA_FILE);

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
