package org.tarantool.core.proto;

import java.nio.ByteBuffer;

/**
 * Leb128 encoder/decoder
 *
 * 
 */
public class Leb128 {

    /**
     * <p> readUnsigned. </p>
     *
     * @param buffer a {@link java.nio.ByteBuffer} object.
     * @return a int.
     */
    public static int readUnsigned(ByteBuffer buffer) {

        byte b0 = buffer.get();
        if ((b0 & 0x80) != 0x80) {
            return b0 & 0x7f;
        }

        byte b1 = buffer.get();
        if ((b1 & 0x80) != 0x80) {
            return (b0 & 0x7f) << 7
                    | (b1 & 0x7f);
        }
        byte b2 = buffer.get();
        if ((b2 & 0x80) != 0x80) {
            return (b0 & 0x7f) << 14
                    | (b1 & 0x7f) << 7
                    | (b2 & 0x7f);
        }
        byte b3 = buffer.get();
        if ((b3 & 0x80) != 0x80) {
            return (b0 & 0x7f) << 21
                    | (b1 & 0x7f) << 14
                    | (b2 & 0x7f) << 7
                    | (b3 & 0x7f);
        }
        byte b4 = buffer.get();
        if ((b4 & 0x80) != 0x80) {
            return (b0 & 0x7f) << 28
                    | (b1 & 0x7f) << 21
                    | (b2 & 0x7f) << 14
                    | (b3 & 0x7f) << 7
                    | (b4 & 0x7f);
        }

        throw new IllegalArgumentException("Can't read LEB128 from buffer");

    }

    /**
     * <p> unsignedSize. </p>
     *
     * @param value a int.
     * @return a int.
     */
    public static int unsignedSize(int value) {

        if (value < (1 << 7)) {
            return 1;
        }
        if (value < (1 << 14)) {
            return 2;
        }
        if (value < (1 << 21)) {
            return 3;
        }
        if (value < (1 << 28)) {
            return 4;
        }
        return 5;
    }

    /**
     * <p> writeUnsigned. </p>
     *
     * @param buffer a {@link java.nio.ByteBuffer} object.
     * @param value a int.
     * @return a {@link java.nio.ByteBuffer} object.
     */
    public static ByteBuffer writeUnsigned(ByteBuffer buffer, int value) {

        if (value >= (1 << 7)) {
            if (value >= (1 << 14)) {
                if (value >= (1 << 21)) {
                    if (value >= (1 << 28)) {
                        buffer.put((byte) ((value >> 28) | 0x80));
                    }
                    buffer.put((byte) ((value >> 21) | 0x80));
                }
                buffer.put((byte) ((value >> 14) | 0x80));
            }
            buffer.put((byte) ((value >> 7) | 0x80));
        }
        buffer.put((byte) ((value) & 0x7F));
        return buffer;
    }
}
