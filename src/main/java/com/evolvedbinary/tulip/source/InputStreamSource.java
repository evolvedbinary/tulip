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
import java.io.InputStream;

/**
 * An input stream to read unparsed text from.
 */
public class InputStreamSource implements Source {

    private final String identifier;
    @Nullable private InputStream unparsedText;

    public InputStreamSource(final String identifier, final InputStream unparsedText) {
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

        return unparsedText.read(buffer);
    }

    @Override
    public void close() throws IOException {
        if (this.unparsedText != null) {
            this.unparsedText.close();
            this.unparsedText = null;
        }
    }
}
