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

public interface XmlSpecification {

    /**
     * Determine whether the supplied argument is white-space or not,
     * as defined by {@see https://www.w3.org/TR/xml/#NT-S}.
     *
     * @param b a byte.
     *
     * @return true of the supplied argument is whitespace.
     */
    boolean isWhiteSpace(byte b);
}
