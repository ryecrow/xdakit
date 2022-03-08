package io.technicrow.xdakit.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Header of an XDA file
 */
public class XDAHeader {

    private Byte majorVersion;

    private Byte minorVersion;

    private Integer entryCount;

    private Byte entryNameTableType;

    private Byte bitsParam;

    private Long firstEntryOffset;

    public XDAHeader() {
        this.majorVersion = (byte) 0x01;
        this.minorVersion = (byte) 0x00;
        this.entryCount = 0;
        this.entryNameTableType = 0x00;
        this.bitsParam = (byte) 0x04;
        this.firstEntryOffset = -1L;
    }

    public XDAHeader(Byte majorVersion, Byte minorVersion, Integer entryCount, Byte entryNameTableType, Byte bitsParam, Long firstEntryOffset) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.entryCount = entryCount;
        this.entryNameTableType = entryNameTableType;
        this.bitsParam = bitsParam;
        this.firstEntryOffset = firstEntryOffset;
    }

    public Byte getMajorVersion() {
        return this.majorVersion;
    }

    public Byte getMinorVersion() {
        return this.minorVersion;
    }

    public Integer getEntryCount() {
        return this.entryCount;
    }

    public Byte getEntryNameTableType() {
        return this.entryNameTableType;
    }

    public Byte getBitsParam() {
        return this.bitsParam;
    }

    public Long getFirstEntryOffset() {
        return this.firstEntryOffset;
    }

    public void setMajorVersion(Byte majorVersion) {
        this.majorVersion = majorVersion;
    }

    public void setMinorVersion(Byte minorVersion) {
        this.minorVersion = minorVersion;
    }

    public void setEntryCount(Integer entryCount) {
        this.entryCount = entryCount;
    }

    public void setEntryNameTableType(Byte entryNameTableType) {
        this.entryNameTableType = entryNameTableType;
    }

    public void setBitsParam(Byte bitsParam) {
        this.bitsParam = bitsParam;
    }

    public void setFirstEntryOffset(Long firstEntryOffset) {
        this.firstEntryOffset = firstEntryOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        XDAHeader xdaHeader = (XDAHeader) o;

        return new EqualsBuilder().append(majorVersion, xdaHeader.majorVersion).append(minorVersion, xdaHeader.minorVersion).append(entryCount, xdaHeader.entryCount).append(entryNameTableType, xdaHeader.entryNameTableType).append(bitsParam, xdaHeader.bitsParam).append(firstEntryOffset, xdaHeader.firstEntryOffset).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(majorVersion).append(minorVersion).append(entryCount).append(entryNameTableType).append(bitsParam).append(firstEntryOffset).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("majorVersion", majorVersion)
                .append("minorVersion", minorVersion)
                .append("entryCount", entryCount)
                .append("entryNameTableType", entryNameTableType)
                .append("bitsParam", bitsParam)
                .append("firstEntryOffset", firstEntryOffset)
                .toString();
    }
}
