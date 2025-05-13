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
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class FileSourceTest {

    @TempDir static Path TEMP_DIR;

    @Test
    public void readNull() {
        assertThrows(IllegalArgumentException.class, () -> FileSource.open(null));
    }

    @Test
    public void readEmptySource() throws IOException {
        final Path srcBuffer = tempFile(new byte[0]);
        try (final Source source = FileSource.open(srcBuffer)) {
            final byte[] dstBuffer = new byte[1024];
            assertEquals(-1, source.read(dstBuffer));
        }
    }

    @Test
    public void readEmptyDest() throws IOException {
        final Path srcBuffer = tempFile(new byte[]{ 0x00, 0x01, 0x02, 0x03 });
        try (final Source source = FileSource.open(srcBuffer)) {
            final byte[] dstBuffer = new byte[0];
            assertEquals(0, source.read(dstBuffer));
        }
    }

    @Test
    public void readAfterCloseThrowsIOException() throws IOException {
        final Path srcBuffer = tempFile(new byte[]{ 0x00, 0x01, 0x02, 0x03 });
        final byte[] dstBuffer = new byte[1];

        try (final Source source = FileSource.open(srcBuffer)) {
            assertEquals(1, source.read(dstBuffer));

            source.close();

            assertThrows(IOException.class, () -> source.read(dstBuffer));
        }
    }

    @Test
    public void readSourceBufferSmallerThanDestBuffer() throws IOException {
        final byte[] srcData = new byte[]{ 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        final Path srcBuffer = tempFile(srcData);
        try (final FileSource source = FileSource.open(srcBuffer)) {
            final byte[] dstBuffer = new byte[16];
            assertEquals(srcData.length, source.read(dstBuffer));
            final byte[] expectedDstBuffer = Arrays.copyOf(srcData, dstBuffer.length);
            assertArrayEquals(expectedDstBuffer, dstBuffer);

            // try and read more, should get -1 bytes read as the source has reached its end
            assertEquals(-1, source.read(dstBuffer));
            assertArrayEquals(expectedDstBuffer, dstBuffer);
        }
    }

    @Test
    public void readSourceBufferLargerThanDestBuffer() throws IOException {
        final byte[] srcData = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        final Path srcBuffer = tempFile(srcData);
        try (final FileSource source = FileSource.open(srcBuffer)) {
            final byte[] dstBuffer = new byte[3];
            assertEquals(dstBuffer.length, source.read(dstBuffer));
            assertArrayEquals(new byte[] { 0x00, 0x01, 0x02 }, dstBuffer);

            // try and read more, should get three more bytes read
            assertEquals(dstBuffer.length, source.read(dstBuffer));
            assertArrayEquals(new byte[] { 0x03, 0x04, 0x05 }, dstBuffer);

            // try and read more, should get the last two bytes read
            assertEquals(2, source.read(dstBuffer));
            assertArrayEquals(new byte[] { 0x06, 0x07, 0x05 }, dstBuffer);
        }
    }

    @Test
    public void getIdentifier() throws IOException {
        final byte[] srcData = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07 };
        final Path srcBuffer = tempFile(srcData);
        final String expectedIdentifier = srcBuffer.normalize().toAbsolutePath().toString();
        try (final Source source = FileSource.open(srcBuffer)) {
            assertEquals(expectedIdentifier, source.getIdentifier());
        }
    }

    /**
     * Create a temporary file with the specified content.
     *
     * @param content the content for the file.
     *
     * @return the path to the temporary file.
     */
    private static Path tempFile(final byte[] content) throws IOException {
        final Path tempFile = Files.createTempFile(TEMP_DIR, FileSourceTest.class.getName(), "tmp");
        try (final OutputStream os = Files.newOutputStream(tempFile, StandardOpenOption.WRITE)) {
            os.write(content);
        }
        return tempFile;
    }
}
