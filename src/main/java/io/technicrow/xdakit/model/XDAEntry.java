package io.technicrow.xdakit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * An XDA entry
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
