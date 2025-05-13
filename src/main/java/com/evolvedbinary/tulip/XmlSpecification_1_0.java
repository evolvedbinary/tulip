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
 * Functions related to the XML 1.0 specification {@see https://www.w3.org/TR/xml}.
 */
public class XmlSpecification_1_0 extends AbstractXmlSpecification {

    public static final XmlSpecification INSTANCE = new XmlSpecification_1_0();

    private XmlSpecification_1_0() {
    }

    @Override
    public boolean isWhiteSpace(final byte b) {
       return b == SPACE
            || b == TAB
            || b == CARRIAGE_RETURN
            || b == LINE_FEED;
    }
}
