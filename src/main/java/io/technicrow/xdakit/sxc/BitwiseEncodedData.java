package io.technicrow.xdakit.sxc;

import java.io.IOException;
import java.io.InputStream;

/**
 * Reader utility class for data encoded by prefix
 */
class BitwiseEncodedData implements AutoCloseable {

    private static final byte PREFIX_ONE_MASK = (byte) 0b10000000;
    private static final byte DATA_ONE_MASK = (byte) 0x7f;
    private static final byte PREFIX_TWO_MASK = (byte) 0b01000000;
    private static final byte DATA_TWO_MASK = (byte) 0x3f;
    private static final byte PREFIX_THREE_MASK = (byte) 0b00100000;
    private static final byte DATA_THREE_MASK = (byte) 0x1f;
    private static final byte PREFIX_FOUR_MASK = (byte) 0b00010000;
    private static final byte DATA_FOUR_MASK = (byte) 0x0f;
    private final InputStream source;

    BitwiseEncodedData(InputStream source) {
        this.source = source;
    }

    byte readByte() throws IOException {
        return (byte) source.read();
    }

    byte[] read() throws IOException {
        byte[] result;
        byte encode = readByte();
        if ((encode & PREFIX_ONE_MASK) == 0) {
            result = new byte[]{(byte) (encode & DATA_ONE_MASK)};
        } else if ((encode & PREFIX_TWO_MASK) == 0) {
            result = new byte[]{(byte) (encode & DATA_TWO_MASK), readByte()};
        } else if ((encode & PREFIX_THREE_MASK) == 0) {
            result = new byte[]{(byte) (encode & DATA_THREE_MASK), readByte(), readByte()};
        } else if ((encode & PREFIX_FOUR_MASK) == 0) {
            result = new byte[]{(byte) (encode & DATA_FOUR_MASK), readByte(), readByte(), readByte()};
        } else {
            throw new IOException("Invalid leading sequence");
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        source.close();
    }
}
