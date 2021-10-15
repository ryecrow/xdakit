package io.technicrow.xdakit.model;

import io.technicrow.xdakit.constant.Operator;
import lombok.Data;

import java.math.BigInteger;

/**
 * Item in the {@link ItemList}
 */
@Data
public class Item {

    private Operator operator;

    private Byte reserved;

    private Long itemOffset;

    private BigInteger nameValue;
}
