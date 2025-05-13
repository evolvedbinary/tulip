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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InputStreamSourceTest {

    @Test
    public void readNull() {
        assertThrows(IllegalArgumentException.class, () -> new InputStreamSource("my source", null));
    }

    @Test
    public void readEmptySource() throws IOException {
        try (final InputStream srcBuffer = new ByteArrayInputStream(new byte[0])) {
            try (final Source source = new InputStreamSource("my source", srcBuffer)) {
                final byte[] dstBuffer = new byte[1024];
                assertEquals(-1, source.read(dstBuffer));
            }
        }
    }

    @Test
    public void readEmptyDest() throws IOException {
        try (final InputStream srcBuffer = new ByteArrayInputStream(new byte[]{ 0x00, 0x01, 0x02, 0x03 })) {
            try (final Source source = new InputStreamSource("my source", srcBuffer)) {
                final byte[] dstBuffer = new byte[0];
                assertEquals(0, source.read(dstBuffer));
            }
        }
    }

    @Test
    public void readAfterCloseThrowsIOException() throws IOException {
        try (final InputStream srcBuffer = new ByteArrayInputStream(new byte[]{ 0x00, 0x01, 0x02, 0x03 })) {
            final byte[] dstBuffer = new byte[1];

            try (final Source source = new InputStreamSource("my source", srcBuffer)) {
                assertEquals(1, source.read(dstBuffer));

                source.close();

                assertThrows(IOException.class, () -> source.read(dstBuffer));
            }
        }
    }

    @Test
    public void readSourceBufferSmallerThanDestBuffer() throws IOException {
        final byte[] srcData = new byte[]{ 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        try (final InputStream srcBuffer = new ByteArrayInputStream(srcData)) {
            try (final InputStreamSource source = new InputStreamSource("my source", srcBuffer)) {
                final byte[] dstBuffer = new byte[16];
                assertEquals(srcData.length, source.read(dstBuffer));
                final byte[] expectedDstBuffer = Arrays.copyOf(srcData, dstBuffer.length);
                assertArrayEquals(expectedDstBuffer, dstBuffer);

                // try and read more, should get -1 bytes read as the source has reached its end
                assertEquals(-1, source.read(dstBuffer));
                assertArrayEquals(expectedDstBuffer, dstBuffer);
            }
        }
    }

    @Test
    public void readSourceBufferLargerThanDestBuffer() throws IOException {
        final byte[] srcData = new byte[]{ 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        try (final InputStream srcBuffer = new ByteArrayInputStream(srcData)) {
            try (final InputStreamSource source = new InputStreamSource("my source", srcBuffer)) {
                final byte[] dstBuffer = new byte[3];
                assertEquals(dstBuffer.length, source.read(dstBuffer));
                assertArrayEquals(new byte[]{0x00, 0x01, 0x02}, dstBuffer);

                // try and read more, should get three more bytes read
                assertEquals(dstBuffer.length, source.read(dstBuffer));
                assertArrayEquals(new byte[]{0x03, 0x04, 0x05}, dstBuffer);

                // try and read more, should get the last two bytes read
                assertEquals(2, source.read(dstBuffer));
                assertArrayEquals(new byte[]{0x06, 0x07, 0x05}, dstBuffer);
            }
        }
    }

    @Test
    public void getIdentifier() throws IOException {
        final byte[] srcData = new byte[]{ 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        try (final InputStream srcBuffer = new ByteArrayInputStream(srcData)) {
            try (final Source source = new InputStreamSource("my source", srcBuffer)) {
                assertEquals("my source", source.getIdentifier());
            }
        }
    }
}
