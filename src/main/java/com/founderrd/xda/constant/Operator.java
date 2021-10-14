package com.founderrd.xda.constant;

/**
 * TODO: Description
 *
 * @author fangyangsen
 * @version 1.0
 */
public enum Operator implements ByteWiseComparable {

    NEW(0x01),
    APPEND(0x02),
    REPLACE(0x03),
    DELETE(0x04),
    END(0x0f),
    MASK(0x0f),
    ;

    private final byte value;

    Operator(int value) {
        this.value = (byte) value;
    }

    @Override
    public int compareTo(byte other) {
        if (this.value == other) {
            return 0;
        } else {
            return (this.value < other) ? -1 : 1;
        }
    }
}
