package io.technicrow.xdakit.constant;

/**
 * BSG Schema Type
 */
public enum SchemaType {

    UNKNOWN(0x00),
    ATTRIBUTE(0x01),
    ELEMENT(0x02),
    SEQUENCE(0x03),
    CHOICE(0x04),
    ALL(0x05);

    private final byte type;

    SchemaType(int type) {
        this.type = (byte) type;
    }

    public static SchemaType ofType(byte type) {
        switch (type) {
            case 0x00:
                return UNKNOWN;
            case 0x01:
                return ATTRIBUTE;
            case 0x02:
                return ELEMENT;
            case 0x03:
                return SEQUENCE;
            case 0x04:
                return CHOICE;
            case 0x05:
                return ALL;
            default:
                return null;
        }
    }

    public boolean isType(byte type) {
        return type == this.type;
    }
}
