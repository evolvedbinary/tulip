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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A string to read unparsed text from.
 */
public class StringSource extends ByteArraySource {

    public StringSource(final String unparsedText) {
        this("StringSource/0x" + Integer.toHexString(unparsedText != null ? unparsedText.hashCode() : 0).toUpperCase(), unparsedText);
    }

    public StringSource(final String identifier, final String unparsedText) {
        this(identifier, unparsedText, StandardCharsets.UTF_8);
    }

    public StringSource(final String identifier, final String unparsedText, final Charset charset) {
        super(identifier, unparsedText != null ? unparsedText.getBytes(charset) : null);
    }
}
