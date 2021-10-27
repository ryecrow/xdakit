package io.technicrow.xdakit.sxc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

class BitwiseEncodedDataTests {

    @Test
    void testReadData() throws IOException {
        byte[] b1 = {0b00001000, 0x55, 0x3f, 0x55, 0x3f, 0x55, 0x3f};
        try (InputStream is = new ByteArrayInputStream(b1);
             BitwiseEncodedData data = new BitwiseEncodedData(is)) {
            Assertions.assertEquals(1, data.read().length);
        }
        byte[] b2 = {(byte) 0b10001000, 0x55, 0x3f, 0x55, 0x3f, 0x55, 0x3f};
        try (InputStream is = new ByteArrayInputStream(b2);
             BitwiseEncodedData data = new BitwiseEncodedData(is)) {
            Assertions.assertEquals(2, data.read().length);
        }
        byte[] b3 = {(byte) 0b11011000, 0x55, 0x3f, 0x55, 0x3f, 0x55, 0x3f};
        try (InputStream is = new ByteArrayInputStream(b3);
             BitwiseEncodedData data = new BitwiseEncodedData(is)) {
            Assertions.assertEquals(3, data.read().length);
        }
        byte[] b4 = {(byte) 0b11101010, 0x55, 0x3f, 0x55, 0x3f, 0x55, 0x3f};
        try (InputStream is = new ByteArrayInputStream(b4);
             BitwiseEncodedData data = new BitwiseEncodedData(is)) {
            Assertions.assertEquals(4, data.read().length);
        }
        byte[] b5 = {(byte) 0b11111010, 0x55, 0x3f, 0x55, 0x3f, 0x55, 0x3f};
        try (InputStream is = new ByteArrayInputStream(b5);
             BitwiseEncodedData data = new BitwiseEncodedData(is)) {
            Assertions.assertThrows(IOException.class, data::read);
        }
    }
}
