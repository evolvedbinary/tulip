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

import com.evolvedbinary.tulip.source.Source;
import com.evolvedbinary.tulip.spec.XmlSpecification;

import java.io.IOException;

public class XPath31Lexer extends XPath30Lexer {

    protected XPath31Lexer(final Source source, final int bufferSize, final XmlSpecification xmlSpecification) throws IOException {
        super(source, bufferSize, xmlSpecification);
    }

    // TODO(AR) override relevant rules from XPath 3.1
}
