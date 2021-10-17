package io.technicrow.xdakit.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Header of an XDA file
 */
@Data
@AllArgsConstructor
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
}
