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
package com.evolvedbinary.tulip;

import java.io.Closeable;
import java.io.IOException;

/**
 * A source to read unparsed text from.
 */
public interface Source extends Closeable {

    /**
     * Get an identifier for this source.
     *
     * @return an identifier for this source.
     */
    String getIdentifier();

    /**
     * Reads data from the source into the provided buffer.
     *
     * @param buffer the buffer to read data into.
     *
     * @return the number of bytes read into the buffer.
     */
    int read(byte[] buffer) throws IOException;
}
