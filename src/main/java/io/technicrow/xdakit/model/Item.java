package io.technicrow.xdakit.model;

import io.technicrow.xdakit.constant.Operator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * Item in the ItemList
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    private Operator operator;

    private Byte reserved;

    private Long itemOffset;

    private BigInteger nameValue;
}
