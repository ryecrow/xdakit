package io.technicrow.xdakit.model;

import lombok.Data;

import java.util.List;

/**
 * An XDA entry
 */
@Data
public class XDAEntry {

    private Integer index;

    private Long position;

    private Integer entryLength;

    private Long bsOffset;

    private Long next;

    private Byte compress;

    private byte[] checkSum;

    private int nameTableLength;

    private Integer nameCount;

    private List<NameMapping> nameTable;

    private List<Item> itemList;
}
