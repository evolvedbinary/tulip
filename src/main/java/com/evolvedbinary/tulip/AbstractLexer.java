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

import static com.evolvedbinary.tulip.LexerConstants.BUFFER_SIZE;

/**
 * Base class for a lexer.
 */
abstract class AbstractLexer implements Lexer {

    private final Source source;
    private final int bufferSize;
    protected final XmlSpecification xmlSpecification;

    // --- Buffering ---
    // The two buffers used for reading data from the source.
    private final byte[] buffer1;
    private final byte[] buffer2;

    // Pointers to the currently active buffers for forward scanning and lexeme beginning.
    // These will point to either buffer1 or buffer2.
    @Nullable // Nullable only before constructor finishes
    protected byte[] forwardBuffer;
    @Nullable // Nullable only before constructor finishes
    protected byte[] beginBuffer;

    // --- Pointer Management ---
    // Pointers relative to the start of their respective buffers (forwardBuffer, beginBuffer).
    protected int lexemeBegin = 0; // Start position of the current lexeme within beginBuffer
    protected int forward = -1;    // Current read position within forwardBuffer

    // Absolute character position from the start of the input stream.
    private int lexemeBeginOriginal = 0;
    private int forwardOriginal = -1;

    // Offset of the start of the current beginBuffer/forwardBuffer from the input stream start.
    int beginOffset = 0;
    int forwardOffset = 0;


    // --- Token Pooling ---
    private final Deque<Token> freeTokens = new ArrayDeque<>();



    /**
     * Constructs the AbstractLexer.
     *
     * @param source           The source to read characters from.
     * @param bufferSize       The size of each internal buffer. Must be large enough to hold any single token.
     * @param xmlSpecification Rules for character classification (e.g., whitespace).
     * @throws IOException If an error occurs during initial buffer loading.
     */
    protected AbstractLexer(final Source source, final int bufferSize, final XmlSpecification xmlSpecification) throws IOException {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be positive.");
        }
        this.source = source;
        this.bufferSize = bufferSize;
        this.xmlSpecification = xmlSpecification;

        // Allocate buffers
        this.buffer1 = new byte[bufferSize];
        this.buffer2 = new byte[bufferSize];

        // Initial load and setup
        loadBuffer(this.buffer1); // Load first chunk
        loadBuffer(this.buffer2); // Load second chunk (lookahead)

        // Initial buffer pointers
        this.beginBuffer = this.buffer1;
        this.forwardBuffer = this.buffer1;
    }

    // ========================================================================
    // Pointer Advancement and Management
    // ========================================================================

    /**
     * Advances the forward pointer one character, handling buffer switches if necessary.
     *
     * @throws IOException If an error occurs reading from the source.
     */
    protected void readNextChar() throws IOException {
        incrementForwardPointer(1);
    }

    /**
     * Advances the forward pointer by a specified number of characters.
     * Use with caution, mainly intended for internal lookahead mechanisms.
     *
     * @param count The number of characters to advance.
     * @throws IOException If an error occurs reading from the source.
     */
    protected void readNextChars(final int count) throws IOException {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative.");
        }
        incrementForwardPointer(count);
    }

    /**
     * Decrements the forward pointer by one position. Used for backtracking after lookahead.
     * Does not cross buffer boundaries backward (assumes lookahead is within current/next buffer).
     */
    public void decrementForward() {
        // TODO: Decrementing forward may push it to the begin buffer, this edge case hasn't been handled yet
        forward--;
        forwardOriginal--;
    }

    /**
     * Decrements the lexeme begin pointer by one position.
     * Primarily used internally, e.g., during whitespace skipping setup.
     */
    public void decrementBegin() {
        lexemeBegin--;
        lexemeBeginOriginal--;
    }

    /**
     * Core logic to advance the forward pointer, handling buffer switches.
     *
     * @param count Number of positions to advance.
     */
    private void incrementForwardPointer(int count) {
        if(forward+count >= bufferSize) { // Assumption being that the count wouldn't exceed the bufferSize itself
            forwardOffset += bufferSize;
            switchForwardBuffer();
        }
        forwardOriginal += count;
        forward = forwardOriginal - forwardOffset;
    }

    /**
     * Switches the `forwardBuffer` to the other buffer.
     * Assumes the other buffer was pre-loaded by `switchBeginBuffer`.
     */
    private void switchForwardBuffer() {
//        System.out.println("Switch buffers - descarding begin buffer which was: "+ new String(beginBuffer));
        if(forwardBuffer==buffer1) {
            forwardBuffer = buffer2;
        } else {
            forwardBuffer = buffer1;
        }
    }

    /**
     * Might have utility in future
     *
     * @param count Number of positions to advance.
     */
    private void incrementBeginPointer(int count) throws IOException {
        if(lexemeBegin+count>=bufferSize) {
            beginOffset += bufferSize;
            switchBeginBuffer();
        }
        lexemeBeginOriginal += count;
        lexemeBegin = lexemeBeginOriginal - beginOffset;
    }

    /**
     * Resets the `lexemeBegin` pointer to match the position *after* the `forward` pointer.
     * Typically called after a token is recognized or whitespace is skipped.
     * Handles switching the `beginBuffer` and loading new data if `forward` has
     * moved into the next buffer.
     *
     * @throws IOException If an error occurs loading data into the buffer.
     */
    void resetLexemeBegin() throws IOException {
        // If forward pointer has moved into the next buffer compared to begin pointer
        if(forwardOffset>beginOffset) {
            switchBeginBuffer(); // This loads data into the now inactive buffer
            beginOffset = forwardOffset;
        }
        lexemeBeginOriginal = forwardOriginal + 1;
        lexemeBegin = lexemeBeginOriginal - beginOffset;
    }

    /**
     * Switches the `beginBuffer` to the other buffer and triggers loading
     * data into the buffer that `beginBuffer` previously pointed to.
     *
     * @throws IOException If an error occurs during buffer loading.
     */
    private void switchBeginBuffer() throws IOException {
        if(beginBuffer==buffer1) {
            beginBuffer=buffer2;
            loadBuffer(buffer1);
        } else {
            beginBuffer=buffer1;
            loadBuffer(buffer2);
        }
    }

    /**
     * Loads data from the source into the specified buffer.
     * Marks the end of the stream within the buffer using -1 if a partial read occurs.
     *
     * @param buffer The byte buffer to load data into.
     * @throws IOException If an I/O error occurs reading from the source.
     */
    private void loadBuffer(final byte[] buffer) throws IOException {
        // TODO: Consider making this asynchronous using executorService if performance critical.
        // Current implementation is synchronous.
        if (source == null) {
            throw new IllegalStateException("Source is null");
        }
        int bytesRead = source.read(buffer);

        if(bytesRead != BUFFER_SIZE && bytesRead>=0) {
            buffer[bytesRead] = -1; // Use -1 to indicate EOF or end of valid data in the buffer
        }
    }

    // ========================================================================
    // Lexeme Extraction - Currently not in use
    // ========================================================================

    /**
     * Extracts the bytes of the current lexeme (from lexemeBegin to forward).
     * Handles cases where the lexeme spans across the two buffers.
     *
     * @return A byte array containing the lexeme data. Returns an empty array if pointers are invalid.
     */
    private byte[] getCurrentLexemeBytes() {
        int lexemeLength = (int) (forwardOriginal - lexemeBeginOriginal + 1);
        byte[] lexeme = new byte[lexemeLength];
        int lexemeIndex = 0;

        if (beginOffset == forwardOffset) {
            // Lexeme is entirely within a single buffer (the current forwardBuffer/beginBuffer)
            for (int i = lexemeBegin; i <= forward; i++) {
                if(i >= beginBuffer.length) break;
                lexeme[lexemeIndex++] = beginBuffer[i];
            }
        } else {
            // Lexeme spans across beginBuffer and forwardBuffer
            for (int i = lexemeBegin; i < bufferSize; i++) {
                if (lexemeIndex >= lexemeLength) break;
                if(i >= beginBuffer.length) break;
                lexeme[lexemeIndex++] = beginBuffer[i];
            }

            for (int i = 0; i <= forward; i++) {
                if (lexemeIndex >= lexemeLength) break;
                if(i >= forwardBuffer.length) break;
                lexeme[lexemeIndex++] = forwardBuffer[i];
            }
        }

        return lexeme;
    }

    /**
     * Populates a pre-allocated byte array with the current lexeme's content.
     * Note: Caller must ensure the provided array `lexeme` is large enough.
     * Consider using {@link #getCurrentLexeme()} for safer string extraction.
     *
     * @param lexeme The byte array to populate.
     */
    public void populateLexeme(byte[] lexeme) {
        byte[] currentLexemeBytes = getCurrentLexemeBytes();
        System.arraycopy(currentLexemeBytes, 0, lexeme, 0, Math.min(lexeme.length, currentLexemeBytes.length));
        // Consider warning or error if provided lexeme array is too small.
    }

    /**
     * Gets the current lexeme as a String.
     * Handles cases where the lexeme spans across the two buffers.
     *
     * @return The String representation of the current lexeme.
     */
    public String getCurrentLexeme() {
        // Assuming default charset is acceptable. For specific encoding, use new String(bytes, Charset).
        return new String(getCurrentLexemeBytes());
    }


    // Resource management
    @Override
    public void close() {
        freeTokens.clear();
    }

    // ========================================================================
    // Token Pooling
    // ========================================================================

    /**
     * Retrieves a reusable Token object from the pool or creates a new one if the pool is empty.
     *
     * @return A Token object ready for population.
     */
    protected Token getFreeToken() {
        @Nullable Token freeToken = freeTokens.peek(); // Either use an already existing object
        if (freeToken == null) { // Or create a new one if not present
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


    // ========================================================================
    // Constants (Moved from original location for better grouping)
    // ========================================================================

    protected static final byte QUOTATION_MARK = 0x22;
    protected static final byte APOSTROPHE     = 0x27;
    protected static final byte ZERO           = 0x30;
    protected static final byte NINE           = 0x39;
    protected static final byte LOWERCASE_A    = 0x61;
    protected static final byte LOWERCASE_Z    = 0x7A;
    protected static final byte UPPERCASE_A    = 0x41;
    protected static final byte UPPERCASE_Z    = 0x5A;

    // Arithmetic Operators
    protected static final byte PLUS        = 0x2B; // '+'
    protected static final byte MINUS       = 0x2D; // '-'
    protected static final byte UNDERSCORE       = 0x5F; // '-'
    protected static final byte MULTIPLY_OPERATOR    = 0x2A; // '*'

    // Comparison Operators
    protected static final byte EQUALS      = 0x3D; // '='
    protected static final byte NOT   = 0x21; // '!' (for "!=")
    protected static final byte LESS_THAN   = 0x3C; // '<'
    protected static final byte GREATER_THAN = 0x3E; // '>'


    // Path Operators
    protected static final byte SLASH       = 0x2F; // '/'
    protected static final byte FULL_STOP   = 0x2E;


    // Parentheses & Other Symbols
    protected static final byte LPAREN      = 0x28; // '('
    protected static final byte RPAREN      = 0x29; // ')'
    protected static final byte LBRACKET    = 0x5B; // '['
    protected static final byte RBRACKET    = 0x5D; // ']'
    protected static final byte AT_OPERATOR = 0x40; // '@'
    protected static final byte COMMA       = 0x2C; // ','
    protected static final byte UNION_OPERATOR = 0x7C; // '|'
    protected static final byte COLON = 0x3A; // ':'

}
