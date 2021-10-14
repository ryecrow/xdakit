/**
 * Title:	XDAItemInfo
 * Description:	定义XDADocument中的项信息类，提供对项信息的添加历史，提取历史记录的功能
 * Author:	杨天航(tianhang.yang@gmail.com)
 * version: 	1.0
 * time: 	2009.11.11
 */

package com.founderrd.xda;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Vector;

class XDAItemInfo {
	final String fullPath;
	final Vector<XDAHistory> histories;

	XDAItemInfo(String itemFullPath) {
		fullPath = itemFullPath;
		histories = new Vector<>();
	}

	void addHistory(int theEntryNo, byte theOperator,
					RandomAccessFile theFileXDA, long thePosition, byte bitsParam)
			throws IOException {
		XDAHistory oldHistory = new XDAOldHistory(theEntryNo, theOperator,
				theFileXDA, thePosition, bitsParam);
		histories.addElement(oldHistory);
	}

	void addHistory(int entryNo, byte operator,
					XDAInputStream maintainedStream, byte[] ecs) {
		XDAHistory newHistory = new XDANewHistory(entryNo, operator,
				maintainedStream, ecs);
		histories.addElement(newHistory);
	}
}

abstract class XDAHistory {
	static final int ITEM_CHECKSUM_LENGTH = 1;
	protected int entryNo;
	protected byte operator;
	protected byte[] ecs;

	XDAHistory() {
		entryNo = 0;
		operator = 0;
		ecs = null;
	}

	XDAHistory(int theEntryNo, byte theOperator) {
		entryNo = theEntryNo;
		operator = theOperator;
	}

	final int getEntryNo() {
		return entryNo;
	}

	final byte getOperator() {
		return operator;
	}

	final byte[] getECS() {
		return ecs.clone();
	}

	// 不进行任何的流装饰直接将当前History下的流写入target
	abstract long writeTo(OutputStream target, byte bitsParam, byte[] buffer)
			throws IOException, FooE;

	abstract long writeTo(RandomAccessFile target, byte bitsParam, byte[] buffer)
			throws IOException, FooE;

	abstract long writeTo(RandomAccessFile target, byte bitsParam,
						  byte[] buffer, byte[] checkSum) throws FooE, IOException;
}

class XDAOldHistory extends XDAHistory {
	private final long position;
	private final RandomAccessFile fileXDA;
	private final long length;

	XDAOldHistory(XDAHistory newHistory, long thePosition,
				  RandomAccessFile theFileXDA, long theLength) {
		super.ecs = newHistory.ecs;
		super.entryNo = newHistory.entryNo;
		super.operator = newHistory.operator;
		position = thePosition;
		fileXDA = theFileXDA;
		length = theLength;
	}

	XDAOldHistory(int theEntryNo, byte theOperator,
				  RandomAccessFile theFileXDA, long thePosition, byte bitsParam)
			throws IOException {
		super(theEntryNo, theOperator);
		position = thePosition;
		fileXDA = theFileXDA;
		long currentPosition = theFileXDA.getFilePointer();
		fileXDA.seek(position + ITEM_CHECKSUM_LENGTH);
		length = Utils.readIntegerAccording2BitsParam(theFileXDA,
				bitsParam);
		ecs = Utils.readByteTillFlag(theFileXDA,
				XDADefine.ECS_BUFFER_CAPACITY, XDADefine.ECS_END_FLAG);
		fileXDA.seek(currentPosition);
	}

	long writeTo(OutputStream target, byte bitsParam, byte[] buffer)
			throws IOException, FooE {
		if (operator == XDADefine.OPERATOR_DELETE)
			throw new FooE(FooE.CANNOT_EXTRACT_STREAM);

		seek(bitsParam);
		return Utils.copyFromSrcToDst(fileXDA, target, length,
				buffer);
	}

	long writeTo(RandomAccessFile target, byte bitsParam, byte[] buffer)
			throws IOException, FooE {
		if (operator == XDADefine.OPERATOR_DELETE)
			throw new FooE(FooE.CANNOT_EXTRACT_STREAM);

		seek(bitsParam);
		return Utils.copyFromSrcToDst(fileXDA, target, length,
				buffer);
	}

	long writeTo(RandomAccessFile target, byte bitsParam, byte[] buffer,
				 byte[] checkSum) throws FooE, IOException {
		if (operator == XDADefine.OPERATOR_DELETE)
			throw new FooE(FooE.CANNOT_EXTRACT_STREAM);

		seek(bitsParam);
		return Utils.copyFromSrcToDst(fileXDA, target, length,
				buffer, checkSum);
	}

	final long getPosition() {
		return position;
	}

	final long getLength() {
		return length;
	}

	private void seek(byte bitsParam) throws IOException {
		fileXDA.seek(position + bitsParam + ITEM_CHECKSUM_LENGTH + ecs.length);
	}
}

class XDANewHistory extends XDAHistory {
	private XDAInputStream maintainedStream;

	XDANewHistory(int theEntryNo, byte theOperator,
				  XDAInputStream theTargetStream, byte[] theECS) {
		super(theEntryNo, theOperator);
		maintainedStream = theTargetStream;
		if (theECS != null)
			ecs = theECS.clone();
	}

	long writeTo(OutputStream target, byte bitsParam, byte[] buffer)
			throws IOException, FooE {
		if (operator == XDADefine.OPERATOR_DELETE)
			throw new FooE(FooE.CANNOT_EXTRACT_STREAM);

		maintainedStream.open();
		long result = Utils.copyFromSrcToDst(maintainedStream,
				target, buffer);
		maintainedStream = maintainedStream.nirvana();
		return result;
	}

	long writeTo(RandomAccessFile target, byte bitsParam, byte[] buffer)
			throws IOException, FooE {
		if (operator == XDADefine.OPERATOR_DELETE)
			throw new FooE(FooE.CANNOT_EXTRACT_STREAM);

		maintainedStream.open();
		long result = Utils.copyFromSrcToDst(maintainedStream,
				target, buffer);
		maintainedStream = maintainedStream.nirvana();
		return result;
	}

	long writeTo(RandomAccessFile target, byte bitsParam, byte[] buffer,
				 byte[] checkSum) throws FooE, IOException {
		if (operator == XDADefine.OPERATOR_DELETE)
			throw new FooE(FooE.CANNOT_EXTRACT_STREAM);

		maintainedStream.open();
		long result = Utils.copyFromSrcToDst(maintainedStream,
				target, buffer, checkSum);
		maintainedStream = maintainedStream.nirvana();
		return result;
	}

	final XDAInputStream getTargetStream() {
		return maintainedStream;
	}
}
