package io.technicrow.xdakit.model;

import lombok.Data;

import java.math.BigInteger;

/**
 * NameMapping entity in the {@link NameTable}
 */
@Data
public class NameMapping {

    private BigInteger nameValue;

    private String path;
}
