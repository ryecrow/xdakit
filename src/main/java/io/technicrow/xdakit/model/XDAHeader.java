package io.technicrow.xdakit.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Header of an XDA file
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class XDAHeader {

    @Builder.Default
    private Byte majorVersion = (byte) 0x01;

    @Builder.Default
    private Byte minorVersion = (byte) 0x00;

    @Builder.Default
    private Integer entryCount = 0;

    @Builder.Default
    private Byte entryNameTableType = 0x00;

    @Builder.Default
    private Byte bitsParam = (byte) 0x04;

    @Builder.Default
    private Long firstEntryOffset = -1L;
}
