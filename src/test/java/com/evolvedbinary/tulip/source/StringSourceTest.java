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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class StringSourceTest {

    @Test
    public void readNull() {
        assertThrows(IllegalArgumentException.class, () -> new StringSource(null));
    }

    @Test
    public void readEmptySource() throws IOException {
        final String srcBuffer = "";
        try (final Source source = new StringSource(srcBuffer)) {
            final byte[] dstBuffer = new byte[1024];
            assertEquals(-1, source.read(dstBuffer));
        }
    }

    @Test
    public void readEmptyDest() throws IOException {
        final String srcBuffer = "abcd";
        try (final Source source = new StringSource(srcBuffer)) {
            final byte[] dstBuffer = new byte[0];
            assertEquals(0, source.read(dstBuffer));
        }
    }

    @Test
    public void readAfterCloseThrowsIOException() throws IOException {
        final String srcBuffer = "abcd";
        final byte[] dstBuffer = new byte[1];

        try (final Source source = new StringSource(srcBuffer)) {
            assertEquals(1, source.read(dstBuffer));

            source.close();

            assertThrows(IOException.class, () -> source.read(dstBuffer));
        }
    }

    @Test
    public void readSourceBufferSmallerThanDestBuffer() throws IOException {
        final String srcBuffer = "abcdefgh";
        try (final StringSource source = new StringSource(srcBuffer)) {
            final byte[] dstBuffer = new byte[16];
            assertEquals(srcBuffer.length(), source.read(dstBuffer));
            assertEquals(8, source.offset);
            final byte[] expectedDstBuffer = Arrays.copyOf(srcBuffer.getBytes(StandardCharsets.UTF_8), dstBuffer.length);
            assertArrayEquals(expectedDstBuffer, dstBuffer);

            // try and read more, should get -1 bytes read as the source has reached its end
            assertEquals(-1, source.read(dstBuffer));
            assertEquals(8, source.offset);
            assertArrayEquals(expectedDstBuffer, dstBuffer);
        }
    }

    @Test
    public void readSourceBufferLargerThanDestBuffer() throws IOException {
        final String srcBuffer = "abcdefgh";
        try (final StringSource source = new StringSource(srcBuffer)) {
            final byte[] dstBuffer = new byte[3];
            assertEquals(dstBuffer.length, source.read(dstBuffer));
            assertEquals(3, source.offset);
            byte[] expectedDstBuffer = srcBuffer.substring(0, 3).getBytes(StandardCharsets.UTF_8);
            assertArrayEquals(expectedDstBuffer, dstBuffer);

            // try and read more, should get three more bytes read
            assertEquals(dstBuffer.length, source.read(dstBuffer));
            assertEquals(6, source.offset);
            expectedDstBuffer = srcBuffer.substring(3, 6).getBytes(StandardCharsets.UTF_8);
            assertArrayEquals(expectedDstBuffer, dstBuffer);

            // try and read more, should get the last two bytes read
            assertEquals(2, source.read(dstBuffer));
            assertEquals(8, source.offset);
            expectedDstBuffer[0] = (byte) srcBuffer.charAt(6);
            expectedDstBuffer[1] = (byte) srcBuffer.charAt(7);
            assertArrayEquals(expectedDstBuffer, dstBuffer);
        }
    }

    @Test
    public void getDefaultIdentifier() throws IOException {
        final String srcBuffer = "abcdefgh";
        try (final Source source = new StringSource(srcBuffer)) {
            assertEquals("StringSource/0x4B151884", source.getIdentifier());
        }
    }

    @Test
    public void getIdentifier() throws IOException {
        final String srcBuffer = "abcdefgh";
        try (final Source source = new StringSource("my source", srcBuffer)) {
            assertEquals("my source", source.getIdentifier());
        }
    }
}
