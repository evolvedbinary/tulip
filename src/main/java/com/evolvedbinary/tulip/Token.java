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

import org.jspecify.annotations.Nullable;

public class Token implements AutoCloseable {

    private final AbstractLexer lexer;
    TokenType tokenType;

    int lineNumber;
    int columnNumber;

    byte[] forwardBuffer;
    byte[] beginBuffer;
    int forward;
    int lexemeBegin;
    int beginOffset = 0;
    int forwardOffset = 0;

    Token(final AbstractLexer lexer) {
        this.lexer = lexer;
    }

    TokenType getTokenType() {
        return tokenType;
    }

    public String getLexeme() {
        if(beginOffset == forwardOffset) {
            return new String(forwardBuffer, lexemeBegin, forward - lexemeBegin + 1);
        } else {
            return (new String(beginBuffer, lexemeBegin, beginBuffer.length-lexemeBegin)).concat(new String(forwardBuffer, 0, forward+1));
        }
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
