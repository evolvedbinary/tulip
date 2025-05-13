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

/**
 * Base class for an XML Specification.
 */
public abstract class AbstractXmlSpecification implements XmlSpecification {

    // Constants for characters
    protected static final byte SPACE           = 0x20;
    protected static final byte TAB             = 0x09;
    protected static final byte CARRIAGE_RETURN = 0x0D;
    protected static final byte LINE_FEED       = 0x0A;
}
