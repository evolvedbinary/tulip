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
package com.evolvedbinary.tulip.lexer;

import org.jspecify.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;

public interface Lexer extends Closeable {


    /**
     * Read the next token.
     *
     * @return the next token, or null if we have reached the end of the source.
     */
    @Nullable Token next() throws IOException;

    @Override
    void close();
}
