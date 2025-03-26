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

public class Token implements AutoCloseable {

    private final AbstractLexer lexer;
    TokenType tokenType;

    int lineNumber;
    int columnNumber;

    byte[] lexeme;

    Token(final AbstractLexer lexer) {
        this.lexer = lexer;
    }

    TokenType getTokenType() {
        return tokenType;
    }

    public byte[] getLexeme() {
        return lexeme;
    }


    @Override
    public void close() {
        releaseToken();
    }

    private void releaseToken() {
//        System.out.println("Releasing one of the used tokens for future use");
        lexer.reuseToken(this);
    }
}
