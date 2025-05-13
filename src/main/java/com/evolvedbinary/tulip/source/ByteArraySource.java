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
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;

/**
 * A byte array to read unparsed text from.
 */
public class ByteArraySource implements Source {

    private final String identifier;
    @Nullable private byte[] unparsedText;
    int offset = 0;

    public ByteArraySource(final byte[] data) {
        this("ByteArraySource/0x" + Integer.toHexString(Arrays.hashCode(data)).toUpperCase(), data);
    }

    public ByteArraySource(final String identifier, final byte[] unparsedText) {
        if (unparsedText == null) {
            throw new IllegalArgumentException("Input unparsed text must not be null");
        }
        this.identifier = identifier;
        this.unparsedText = unparsedText;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public int read(final byte[] buffer) throws IOException {
        if (unparsedText == null) {
            throw new IOException("Source is closed");
        }

        final int available = unparsedText.length - offset;
        if (available == 0) {
            return -1;
        }

        final int copyLength = Math.min(available, buffer.length);
        System.arraycopy(unparsedText, offset, buffer, 0, copyLength);

        offset += copyLength;

        return copyLength;
    }

    @Override
    public void close() {
        this.unparsedText = null;
        this.offset = -1;
    }
}
