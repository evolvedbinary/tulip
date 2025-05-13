/*
 * Tulip - XPath and XQuery Parser
 * Copyright (c) 2025 Evolved Binary
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE file and at www.mariadb.com/bsl11.
 *
 * Change Date: 2028-03-23
 *
 * On the date above, in accordance with the Business Source License, use
 * of this software will be governed by the Apache License, Version 2.0.
 *
 * Additional Use Grant: None
 */
package com.evolvedbinary.tulip.source;

import com.evolvedbinary.tulip.Source;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ByteArraySourceTest {

    @Test
    public void readNull() {
        assertThrows(IllegalArgumentException.class, () -> new ByteArraySource(null));
    }

    @Test
    public void readEmptySource() throws IOException {
        final byte[] srcBuffer = new byte[0];
        try (final Source source = new ByteArraySource(srcBuffer)) {
            final byte[] dstBuffer = new byte[1024];
            assertEquals(-1, source.read(dstBuffer));
        }
    }

    @Test
    public void readEmptyDest() throws IOException {
        final byte[] srcBuffer = { 0x00, 0x01, 0x02, 0x03 };
        try (final Source source = new ByteArraySource(srcBuffer)) {
            final byte[] dstBuffer = new byte[0];
            assertEquals(0, source.read(dstBuffer));
        }
    }

    @Test
    public void readAfterCloseThrowsIOException() throws IOException {
        final byte[] srcBuffer = { 0x00, 0x01, 0x02, 0x03 };
        final byte[] dstBuffer = new byte[1];

        try (final Source source = new ByteArraySource(srcBuffer)) {
            assertEquals(1, source.read(dstBuffer));

            source.close();

            assertThrows(IOException.class, () -> source.read(dstBuffer));
        }
    }

    @Test
    public void readSourceBufferSmallerThanDestBuffer() throws IOException {
        final byte[] srcBuffer = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        try (final ByteArraySource source = new ByteArraySource(srcBuffer)) {
            final byte[] dstBuffer = new byte[16];
            assertEquals(srcBuffer.length, source.read(dstBuffer));
            assertEquals(8, source.offset);
            final byte[] expectedDstBuffer = Arrays.copyOf(srcBuffer, dstBuffer.length);
            assertArrayEquals(expectedDstBuffer, dstBuffer);

            // try and read more, should get -1 bytes read as the source has reached its end
            assertEquals(-1, source.read(dstBuffer));
            assertEquals(8, source.offset);
            assertArrayEquals(expectedDstBuffer, dstBuffer);
        }
    }

    @Test
    public void readSourceBufferLargerThanDestBuffer() throws IOException {
        final byte[] srcBuffer = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        try (final ByteArraySource source = new ByteArraySource(srcBuffer)) {
            final byte[] dstBuffer = new byte[3];
            assertEquals(dstBuffer.length, source.read(dstBuffer));
            assertEquals(3, source.offset);
            assertArrayEquals(new byte[] { 0x00, 0x01, 0x02 }, dstBuffer);

            // try and read more, should get three more bytes read
            assertEquals(dstBuffer.length, source.read(dstBuffer));
            assertEquals(6, source.offset);
            assertArrayEquals(new byte[] { 0x03, 0x04, 0x05 }, dstBuffer);

            // try and read more, should get the last two bytes read
            assertEquals(2, source.read(dstBuffer));
            assertEquals(8, source.offset);
            assertArrayEquals(new byte[] { 0x06, 0x07, 0x05 }, dstBuffer);
        }
    }

    @Test
    public void getDefaultIdentifier() throws IOException {
        final byte[] srcBuffer = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        try (final Source source = new ByteArraySource(srcBuffer)) {
            assertEquals("ByteArraySource/0xCCC08705", source.getIdentifier());
        }
    }

    @Test
    public void getIdentifier() throws IOException {
        final byte[] srcBuffer = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        try (final Source source = new ByteArraySource("my source", srcBuffer)) {
            assertEquals("my source", source.getIdentifier());
        }
    }
}
