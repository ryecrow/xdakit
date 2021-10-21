package io.technicrow.xdakit.constant;

/**
 * BSG Data Type
 */
public enum DataType {

    BOOL(0x00),
    INTEGER(0x01),
    FLOAT(0x02),
    DOUBLE(0x03),
    STRING(0x04),
    HEX(0x05),
    ENUM(0x06);

    private final byte type;

    DataType(int type) {
        this.type = (byte) type;
    }

    public static DataType ofType(byte type) {
        switch (type) {
            case 0x00:
                return BOOL;
            case 0x01:
                return INTEGER;
            case 0x02:
                return FLOAT;
            case 0x03:
                return DOUBLE;
            case 0x04:
                return STRING;
            case 0x05:
                return HEX;
            case 0x06:
                return ENUM;
            default:
                return null;
        }
    }

    public boolean isType(byte type) {
        return type == this.type;
    }
}
