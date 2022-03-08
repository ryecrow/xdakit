package io.technicrow.xdakit.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

/**
 * An XDA entry
 */
public class XDAEntry {

    private Integer index;

    private Long position;

    private Integer entryLength;

    private Long bsOffset;

    private Long next;

    private Byte compress;

    private byte[] checkSum;

    private Integer nameTableLength;

    private Integer nameCount;

    private List<NameMapping> nameTable;

    private List<Item> itemList;

    public XDAEntry(Integer index, Long position, Integer entryLength, Long bsOffset, Long next, Byte compress, byte[] checkSum, Integer nameTableLength, Integer nameCount, List<NameMapping> nameTable, List<Item> itemList) {
        this.index = index;
        this.position = position;
        this.entryLength = entryLength;
        this.bsOffset = bsOffset;
        this.next = next;
        this.compress = compress;
        this.checkSum = checkSum;
        this.nameTableLength = nameTableLength;
        this.nameCount = nameCount;
        this.nameTable = nameTable;
        this.itemList = itemList;
    }

    public XDAEntry() {
    }

    public Integer getIndex() {
        return this.index;
    }

    public Long getPosition() {
        return this.position;
    }

    public Integer getEntryLength() {
        return this.entryLength;
    }

    public Long getBsOffset() {
        return this.bsOffset;
    }

    public Long getNext() {
        return this.next;
    }

    public Byte getCompress() {
        return this.compress;
    }

    public byte[] getCheckSum() {
        return this.checkSum;
    }

    public Integer getNameTableLength() {
        return this.nameTableLength;
    }

    public Integer getNameCount() {
        return this.nameCount;
    }

    public List<NameMapping> getNameTable() {
        return this.nameTable;
    }

    public List<Item> getItemList() {
        return this.itemList;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public void setEntryLength(Integer entryLength) {
        this.entryLength = entryLength;
    }

    public void setBsOffset(Long bsOffset) {
        this.bsOffset = bsOffset;
    }

    public void setNext(Long next) {
        this.next = next;
    }

    public void setCompress(Byte compress) {
        this.compress = compress;
    }

    public void setCheckSum(byte[] checkSum) {
        this.checkSum = checkSum;
    }

    public void setNameTableLength(Integer nameTableLength) {
        this.nameTableLength = nameTableLength;
    }

    public void setNameCount(Integer nameCount) {
        this.nameCount = nameCount;
    }

    public void setNameTable(List<NameMapping> nameTable) {
        this.nameTable = nameTable;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        XDAEntry xdaEntry = (XDAEntry) o;

        return new EqualsBuilder().append(index, xdaEntry.index).append(position, xdaEntry.position).append(entryLength, xdaEntry.entryLength).append(bsOffset, xdaEntry.bsOffset).append(next, xdaEntry.next).append(compress, xdaEntry.compress).append(checkSum, xdaEntry.checkSum).append(nameTableLength, xdaEntry.nameTableLength).append(nameCount, xdaEntry.nameCount).append(nameTable, xdaEntry.nameTable).append(itemList, xdaEntry.itemList).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(index).append(position).append(entryLength).append(bsOffset).append(next).append(compress).append(checkSum).append(nameTableLength).append(nameCount).append(nameTable).append(itemList).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("index", index)
                .append("position", position)
                .append("entryLength", entryLength)
                .append("bsOffset", bsOffset)
                .append("next", next)
                .append("compress", compress)
                .append("checkSum", checkSum)
                .append("nameTableLength", nameTableLength)
                .append("nameCount", nameCount)
                .append("nameTable", nameTable)
                .append("itemList", itemList)
                .toString();
    }
}
