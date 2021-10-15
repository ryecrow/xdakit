package io.technicrow.xdakit.constant;

/**
 * Item operator
 */
public enum Operator implements ByteWiseComparable {

    NO_OP(0x00),
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

    public static Operator ofValue(int value) {
        switch (value & 0x0f) {
            case 0x01:
                return NEW;
            case 0x02:
                return APPEND;
            case 0x03:
                return REPLACE;
            case 0x04:
                return DELETE;
            case 0x0f:
                return END;
            default:
                return NO_OP;
        }
    }

    public byte getValue() {
        return value;
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
