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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Base class for a lexer.
 */
abstract class AbstractLexer implements Lexer {

    private final Source source;



    private final int bufferSize;
    @Nullable protected byte[] buffer1;
    @Nullable protected byte[] buffer2;

    @Nullable byte[] forwardBuffer;
    @Nullable byte[] beginBuffer;

    protected final XmlSpecification xmlSpecification;

    int lexemeBegin = 0;
    int forward = -1;
    private int lexemeBeginOriginal = 0;
    private int forwardOriginal = -1;
    private int beginOffset = 0;
    private int forwardOffset = 0;


    private final Deque<Token> freeTokens = new ArrayDeque<>();
    ExecutorService executorService = Executors.newFixedThreadPool(2);

    /**
     * @param source the source to read from.
     * @param bufferSize the size of the buffer to use for reading. The lexer will potentially allocate two of these.
     */
    protected AbstractLexer(final Source source, final int bufferSize, final XmlSpecification xmlSpecification) throws IOException {
        this.source = source;
        this.bufferSize = bufferSize;
        this.xmlSpecification = xmlSpecification;
        buffer1 = new byte[bufferSize];
        buffer2 = new byte[bufferSize];
        loadBuffer(buffer1);
        loadBuffer(buffer2);
        beginBuffer = buffer1;
        forwardBuffer = buffer1;
    }

    /**
     * Advance the forward pointer one character.
     */
    protected void readNextChar() throws IOException {
        readNextChars(1);
    }

    public void decrementForward() {
        forward--;
        forwardOriginal--;
    }

    public void decrementBegin() {
        lexemeBegin--;
        lexemeBeginOriginal--;
    }

    /**
     * Advance the forward pointer by a number of characters.
     *
     * @param count the number of characters to try and advance the forward pointer.
     */
    protected void readNextChars(final int count) throws IOException {
        incrementForwardPointer(count);
    }

    private void incrementForwardPointer(int count) {
        if(forward+count>=bufferSize) {
            forwardOffset += bufferSize;
            switchForwardBuffer();
        }
        forwardOriginal += count;
        forward = forwardOriginal - forwardOffset;
    }

    private void switchForwardBuffer() {
        if(forwardBuffer==buffer1) {
            forwardBuffer = buffer2;
        } else {
            forwardBuffer = buffer1;
        }
    }

    private void incrementBeginPointer(int count) throws IOException {
        if(lexemeBegin+count>=bufferSize) {
            beginOffset += bufferSize;
            switchBeginBuffer();
        }
        lexemeBeginOriginal += count;
        lexemeBegin = lexemeBeginOriginal - beginOffset;
    }

    void resetLexemeBegin() throws IOException {
        if(forwardOffset>beginOffset) {
            switchBeginBuffer();
            beginOffset = forwardOffset;
        }
        lexemeBeginOriginal = forwardOriginal + 1;
        lexemeBegin = lexemeBeginOriginal - beginOffset;
    }

    private void switchBeginBuffer() throws IOException {
        if(beginBuffer==buffer1) {
            beginBuffer=buffer2;
            loadBuffer(buffer1);
        } else {
            beginBuffer=buffer1;
            loadBuffer(buffer2);
        }
    }

    private void loadBuffer(final byte[] buffer) throws IOException {
        source.read(buffer);
    }

    public String getCurrentLexeme() {
        byte[] str = new byte[forwardOriginal - lexemeBeginOriginal + 1];
        if(beginOffset == forwardOffset) {
            for(int i=lexemeBegin;i<=forward;i++) {
                str[i-lexemeBegin] = forwardBuffer[i];
            }
        } else {
            int count = 0;
            for(int i=lexemeBegin;i<bufferSize;i++) {
                str[count++] = beginBuffer[i];
            }
            for(int i=0;i<=forward;i++) {
                str[count++] = forwardBuffer[i];
            }
        }
        return new String(str);
    }
    @Override
    public void close() {
        freeTokens.clear();
    }

    /**
     * Create a token.
     *
     * @return a token.
     */
    protected Token getFreeToken() {
        @Nullable Token freeToken = freeTokens.peek();
        if (freeToken == null) {
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

    public int getBufferSize() {
        return bufferSize;
    }

    protected static final byte QUOTATION_MARK = 0x22;
    protected static final byte APOSTROPHE     = 0x27;
    protected static final byte FULL_STOP      = 0x2E;
    protected static final byte ZERO           = 0x30;
    protected static final byte NINE           = 0x39;
    protected static final byte Q              = 0x51;
}
