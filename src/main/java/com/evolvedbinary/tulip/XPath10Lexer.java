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

public class XPath10Lexer extends AbstractLexer {

    protected XPath10Lexer(final Source source, final int bufferSize, final XmlSpecification xmlSpecification) throws IOException {
        super(source, bufferSize, xmlSpecification);
    }

    @Override
    public Token next() throws IOException {

        readNextChar();
        byte b = forwardBuffer[forward];
        System.out.println(b);

        TokenType tokenType = null;

        while (xmlSpecification.isWhiteSpace(b)) {
            // XML white-space
            readNextChar();
            b = forwardBuffer[forward];
            resetLexemeBegin();
            decrementBegin();
        }

        if(b==117) {
            System.out.println("Reached end of file");
            tokenType = TokenType.EOF;
        } else if (isDigit(b)) {
            // IntegerLiteral or (DecimalLiteral or Double Literal) starting with a digit
            // consumeNumber
            tokenType = TokenType.INTEGER_LITERAL;
            do {
                readNextChar();
            }while(isDigit(forwardBuffer[forward]));
            decrementForward(); //since last byte is not a digit
        } else if (b == FULL_STOP) {
            // Decimal Literal or Double Literal starting with a '.'

        } else if (b == QUOTATION_MARK || b == APOSTROPHE) {
            // Literal
            // consume string literal
            readLiteral(b);
            tokenType = TokenType.STRING_LITERAL;

        } else if (b == Q) {
            // URIQualifiedName

        }

        final Token token = getFreeToken();
        token.tokenType = tokenType;
        // TODO(AR) set line number, column number
        token.lexeme = getCurrentLexeme();
        resetLexemeBegin();
        return token;
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
        } while (forwardBuffer[forward] != startChar);
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
