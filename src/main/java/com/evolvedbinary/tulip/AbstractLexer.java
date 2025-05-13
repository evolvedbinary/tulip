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

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Base class for a Lexer.
 */
abstract class AbstractLexer implements Lexer {

    final Source source;
    final int bufferSize;
    @Nullable byte[] buffer1;
    @Nullable byte[] buffer2;

    @Nullable byte[] currentBuffer;
    @Nullable int currentBufferLength = 0;

    protected final XmlSpecification xmlSpecification;

    int lexemeBegin = 0;
    int forward = -1;

    final Deque<Token> freeTokens = new ArrayDeque<>();

    /**
     * @param source the source to read from.
     * @param bufferSize the size of the buffer to use for reading. The lexer will potentially allocate two of these.
     * @param xmlSpecification the XML specification that we should lex for.
     */
    protected AbstractLexer(final Source source, final int bufferSize, final XmlSpecification xmlSpecification) {
        this.source = source;
        this.bufferSize = bufferSize;
        this.xmlSpecification = xmlSpecification;
    }

    /**
     * Advance the forward pointer one character.
     */
    protected void readNextChar() throws IOException {
        readNextChars(1);
    }

    /**
     * Advance the forward pointer by a number of characters.
     *
     * @param count the number of characters to try and advance the forward pointer.
     */
    protected void readNextChars(final int count) throws IOException {
        if (forward + count > currentBufferLength) {
            if (buffer1 == null) {
                // Buffer1 is empty so read into it and set it as the current buffer
                buffer1 = new byte[bufferSize];
                currentBufferLength = loadBuffer(buffer1);
                currentBuffer = buffer1;
                if (currentBufferLength > forward + count) {
                    forward += count;
                    return;
                }
            }

            if (buffer2 == null) {
                // Buffer1 is not empty, but Buffer2 is so read into it set it as the current buffer
                buffer2 = new byte[bufferSize];
                currentBufferLength = loadBuffer(buffer2);
                currentBuffer = buffer2;
                if (buffer1.length + currentBufferLength > forward + count) {
                    forward += count;
                    return;
                }
            }

            throw new IOException("Out of buffer space");
        }

        forward += count;
    }

    private int loadBuffer(final byte[] buffer) throws IOException {
        return source.read(buffer);
    }

    @Override
    public void close() {
        freeTokens.clear();
    }

    /**
     * Creates a token of a specific type.
     *
     * @param tokenType the type of the token to create.
     *
     * @return the token.
     */
    protected Token token(final TokenType tokenType) {
        final Token token = getFreeToken();
        token.tokenType = tokenType;
        token.lexemeBegin = lexemeBegin;
        token.lexemeEnd = forward;
        token.buffer = currentBuffer;
        // TODO(AR) set line number, column number
        return token;
    }

    /**
     * Get or create a token object.
     *
     * Tries to find a free Token object first and return it,
     * if not, a new Token object is created.
     *
     * When the token is finished with {@link Token#close()}
     * should be called so that the token may be reused in
     * the future.
     *
     * @return a token object.
     */
    protected Token getFreeToken() {
        @Nullable Token freeToken = freeTokens.pollFirst();
        if (freeToken != null) {
            freeToken.reset();
        } else {
            freeToken = new Token(this);
        }
        return freeToken;
    }

    /**
     * Provide a token for reuse by the lexer.
     *
     * @param freeToken a token that is free and can be reused.
     */
    void reuseToken(final Token freeToken) {
        freeTokens.push(freeToken);
    }

    protected static final byte QUOTATION_MARK = 0x22;
    protected static final byte APOSTROPHE     = 0x27;
    protected static final byte FULL_STOP      = 0x2E;
    protected static final byte ZERO           = 0x30;
    protected static final byte NINE           = 0x39;
    protected static final byte Q              = 0x51;
}
