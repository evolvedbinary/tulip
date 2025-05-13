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

import java.io.IOException;

/**
 * Implements an XPath 1.0 Lexer as described in
 * the W3C Recommendation "XML Path Language (XPath) Version 1.0".
 * Specifically {@see https://www.w3.org/TR/xpath-10/#exprlex}.
 */
public class XPath10Lexer extends AbstractLexer {

    protected XPath10Lexer(final Source source, final int bufferSize, final XmlSpecification xmlSpecification) {
        super(source, bufferSize, xmlSpecification);
    }

    @Override
    public Token next() throws IOException {

        readNextChar();
        final byte b = currentBuffer[forward];


        if (xmlSpecification.isWhiteSpace(b)) {
            // XML white-space

        } else if (isDigit(b)) {
            // Number starting with a digit

        } else if (b == FULL_STOP) {
            // Number starting with a '.'

        } else if (b == QUOTATION_MARK || b == APOSTROPHE) {
            // Literal
            readLiteral(b);
            return token(TokenType.LITERAL);

        } else if (b == Q) {
            // URIQualifiedName

        }

        // TODO(AR) what to do here? maybe we are at end of file, or an error state?
        return null;
    }

    /**
     * Reads a Literal.
     * {@see https://www.w3.org/TR/xpath-10/#NT-Literal}
     *
     * This state starts when a quotation mark of apostrophe was detected.
     *
     * @param startChar the starting character - either a quotation mark or apostrophe
     */
    void readLiteral(final byte startChar) throws IOException {
        // read chars until we find one that matches the startChar
        do {
            readNextChar();
        } while (currentBuffer[forward] != startChar);
    }

    /**
     * Determines if the character is a digit.
     * {@see https://www.w3.org/TR/xpath-10/#NT-Digits}
     *
     * @param b the character to test.
     *
     * @return true if the character is a digit, false otherwise.
     */
    boolean isDigit(final byte b) {
        return b >= ZERO && b <= NINE;
    }
}
