package com.evolvedbinary.tulip;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * A lexer for tokenizing XPath 1.0 expressions.
 *
 * It reads characters from a {@link Source} and produces a stream of {@link Token} objects.
 * It utilizes a Trie for efficient recognition of keywords (Axis Names and Function Names).
 */
public class XPath10Lexer extends AbstractLexer {

    // Trie for efficient keyword lookup (Axis Names and Function Names)
    private static final Trie keywordTrie = buildKeywordTrie();

    /**
     * Builds and populates the Trie with XPath 1.0 keywords.
     *
     * @return The populated Trie.
     */
    private static Trie buildKeywordTrie() {
        Trie trie = new Trie();

        // --- Axis Names ---
        List<String> axisNames = Arrays.asList(
                "ancestor", "ancestor-or-self", "attribute", "child",
                "descendant", "descendant-or-self", "following",
                "following-sibling", "namespace", "parent",
                "preceding", "preceding-sibling", "self"
        );
        for (String axis : axisNames) {
            trie.insert(axis, false, true);
        }

        // --- Function Names ---
        List<String> functionNames = Arrays.asList(
                // Node set functions
                "last", "position", "count", "id", "local-name",
                "namespace-uri", "name",
                // String functions
                "string", "concat", "starts-with", "contains",
                "substring-before", "substring-after", "substring",
                "string-length", "normalize-space", "translate",
                // Boolean functions
                "boolean", "not", "true", "false", "lang",
                // Number functions
                "number", "sum", "floor", "ceiling", "round"
        );
        for (String function : functionNames) {
            trie.insert(function, true, false);
        }

        // --- Arithmetic Operator Names ---
        List<String> arithmeticOperatorNames = Arrays.asList(
                "and", "or", "div", "mod"
        );
        for (String op : arithmeticOperatorNames) {
            trie.insert(op, false, false);
        }

        // System.out.println("Trie has been created and populated"); // Keep commented out or remove for production
        return trie;
    }

    /**
     * Constructs an XPath10Lexer.
     *
     * @param source           The source of characters.
     * @param bufferSize       The size of the internal buffer.
     * @param xmlSpecification The XML specification rules (e.g., for whitespace).
     * @throws IOException If an I/O error occurs during initial read.
     */
    protected XPath10Lexer(final Source source, final int bufferSize, final XmlSpecification xmlSpecification) throws IOException {
        super(source, bufferSize, xmlSpecification);
    }

    /**
     * Retrieves the next token from the input source.
     * Skips whitespace before identifying the token.
     *
     * @return The next {@link Token}, or a Token with {@link TokenType#EOF} if the end of the source is reached.
     * @throws IOException If an I/O error occurs during reading or if an invalid sequence is encountered.
     */
    @Override
    public Token next() throws IOException {
        resetLexemeBegin(); // Begin to be set ahead of forward
        skipWhitespaceAndResetBegin();

        final byte firstByte = forwardBuffer[forward];
        TokenType tokenType;

        if (firstByte == -1) {
            // System.out.println("Reached end of file"); // Keep commented out or remove
            tokenType = TokenType.EOF;
        } else if (isLetter(firstByte)) {
            tokenType = handleIdentifierOrKeyword();
        } else if (firstByte == UNDERSCORE) {
            tokenType = handleIdentifierStartingWithUnderscore();
        } else if (isDigit(firstByte)) {
            tokenType = handleNumberStartingWithDigit();
        } else if (firstByte == FULL_STOP) {
            tokenType = handleDotOrNumberStartingWithDot();
        } else if(firstByte == MINUS) {
            tokenType = handleNegativeNumbers();
        }else if (firstByte == QUOTATION_MARK || firstByte == APOSTROPHE) {
            tokenType = handleLiteral(firstByte);
        } else {
            // Handle operators and punctuation
            tokenType = handleOperatorOrPunctuation(firstByte);
        }

        // Use try-with-resources assuming Token implements AutoCloseable for pooling/cleanup
        try (Token token = getFreeToken()) {
            token.tokenType = tokenType;
            token.forwardBuffer = forwardBuffer;
            token.beginBuffer = beginBuffer;
            token.forward = forward;
            token.lexemeBegin = lexemeBegin;
            token.beginOffset = beginOffset;
            token.forwardOffset = forwardOffset;
            // Debugging print statements (remove or comment out for production):
            // System.out.println("Begin buffer: " + new String(beginBuffer));
            // System.out.println("Forward buffer: "+ new String(forwardBuffer));
            // System.out.println("Begin pointer: " + lexemeBegin + " Forward Pointer: "+ forward);
            // System.out.println("Token Type: " + tokenType);
            return token;
        } catch (Exception e) {
            // This broad catch might hide issues; consider more specific exception handling if possible.
            // Re-throwing or wrapping might be better depending on application needs.
            System.err.println("Error creating or finalizing token: " + e.getMessage());
            // Re-throw as an IOException or a custom LexerException if appropriate
            throw new IOException("Failed to process token", e);
            // Or return null / a special error token if the API contract allows
            // return null;
        }
    }

    /**
     * Skips XML whitespace characters and resets the lexeme beginning pointer.
     *
     * @throws IOException If an I/O error occurs.
     */
    private void skipWhitespaceAndResetBegin() throws IOException {
        readNextChar();
        byte currentByte = forwardBuffer[forward];
        while (xmlSpecification.isWhiteSpace(currentByte)) {
            readNextChar();
            currentByte = forwardBuffer[forward];
            resetLexemeBegin(); // Reset lexeme start to the beginning of the *next* potential token
            decrementBegin(); // Move begin back to match forward before potential token start
        }
    }

    /**
     * Handles tokens starting with a letter, which could be an identifier,
     * an axis name, or a function name. Uses the Trie for keyword detection.
     *
     * @return The determined TokenType (IDENTIFIER, AXIS_NAME, or FUNCTION,or Arithmetic Keyword).
     * @throws IOException If an I/O error occurs.
     */
    private TokenType handleIdentifierOrKeyword() throws IOException {
        TrieNode node = keywordTrie.getRoot();
        node = keywordTrie.traverse(forwardBuffer[forward], node); // Traverse the first letter

        while (true) {
            readNextChar();
            byte currentByte = forwardBuffer[forward];

            if (isLetter(currentByte) || currentByte == MINUS || currentByte == FULL_STOP || isDigit(currentByte)) {
                // Continue traversing if it's a letter or could be an identifier
                if (node != null) {
                    node = keywordTrie.traverse(currentByte, node);
                }
            } else {
                // Not a letter or valid hyphen sequence, end of potential identifier/keyword
                break;
            }
        }
        decrementForward(); // Backtrack to the last token
        // Determine token type based on final Trie node state
        if (node != null && node.isFunction) {
            return TokenType.FUNCTION;
        } else if(node != null && node.isAxis) {
            return TokenType.AXIS_NAME;
        } else if(node != null && node.isKeyword) {
            if(beginBuffer[lexemeBegin] == 0x61) {
                return TokenType.AND;
            } else if(beginBuffer[lexemeBegin] == 0x6F) {
                return TokenType.OR;
            } else if(beginBuffer[lexemeBegin] == 0x64) {
                return TokenType.DIV;
            } else {
                return TokenType.MOD;
            }
        } else {
            return TokenType.IDENTIFIER; // Matched prefix or not a keyword
        }
    }

    /**
     * Handles tokens starting with a underscore, which could be an identifier,
     * calls upon handleIdentifierOrKeyword() after reading underscore
     *
     * @return The determined TokenType (IDENTIFIER, AXIS_NAME, or FUNCTION, or Arithmetic Keyword).
     * @throws IOException If an I/O error occurs.
     */
    private TokenType handleIdentifierStartingWithUnderscore() throws IOException {
        readNextChar();
        if (isLetter(forwardBuffer[forward])) {
            return handleIdentifierOrKeyword();
        } else {
            throw new IOException("Underscore should be followed by a letter for it to be identified as a proper identifier in XPath 1.0");
        }
    }

    /**
     * Handles tokens starting with a digit (Integer or Decimal).
     *
     * @return TokenType.DIGITS (representing a number literal).
     * @throws IOException If an I/O error occurs.
     */
    private TokenType handleNumberStartingWithDigit() throws IOException {
        TokenType tokenType = TokenType.DIGITS;
        // Consume initial sequence of digits
        do {
            readNextChar();
        } while (isDigit(forwardBuffer[forward]));

        // Check for a decimal part
        if (forwardBuffer[forward] == FULL_STOP) {
            tokenType = TokenType.NUMBER; // If dot is present then TokenType is NUMBER
            readNextChar(); // Consume the dot
            // Consume digits after the dot
            if(isDigit(forwardBuffer[forward])) {
                while (isDigit(forwardBuffer[forward])) {
                    readNextChar();
                }
            } else {
                decrementForward();
            }
        }

        // Backtrack to the last character belonging to the number
        decrementForward();
        return tokenType;
    }

    /**
     * Handles tokens starting with a FULL_STOP (.), which could be the
     * current node context selector (.), the parent step (..), or a
     * decimal number starting with a dot (e.g., .5).
     *
     * @return The determined TokenType (CURRENT_AXIS, PARENT_AXIS, or NUMBER).
     * @throws IOException If an I/O error occurs.
     */
    private TokenType handleDotOrNumberStartingWithDot() throws IOException {
        readNextChar(); // Consume the first dot

        if (isDigit(forwardBuffer[forward])) {
            // It's a number starting with a dot (e.g., .5)
            do {
                readNextChar();
            } while (isDigit(forwardBuffer[forward]));
            decrementForward(); // Backtrack to the last digit
            return TokenType.NUMBER;
        } else if (forwardBuffer[forward] == FULL_STOP) {
            // It's the parent axis operator '..'
            return TokenType.PARENT_AXIS;
        } else {
            // It's the current node selector '.'
            decrementForward(); // Backtrack as we only consumed the first dot
            return TokenType.CURRENT_AXIS;
        }
    }

    /**
     * Handles tokens starting with a minus, which could be a number or digit,
     *
     * @return The determined TokenType (MINUS).
     * @throws IOException If an I/O error occurs.
     */
    private TokenType handleNegativeNumbers() throws IOException {
        return TokenType.MINUS;
    }

    /**
     * Handles literal strings enclosed in single or double quotes.
     *
     * @param quoteByte The starting quote character (APOSTROPHE or QUOTATION_MARK).
     * @return TokenType.LITERAL.
     * @throws IOException If an I/O error occurs or the closing quote is not found.
     */
    private TokenType handleLiteral(final byte quoteByte) throws IOException {
        do {
            readNextChar();
            // Check for EOF before finding the closing quote
            if (forwardBuffer[forward] == -1) {
                throw new IOException("Unterminated literal string");
            }
        } while (forwardBuffer[forward] != quoteByte);
        return TokenType.LITERAL;
    }

    /**
     * Handles single and multi-character operators and punctuation.
     *
     * @param firstByte The first byte of the potential operator/punctuation.
     * @return The corresponding TokenType.
     * @throws IOException If an I/O error occurs or an invalid sequence is found (e.g., '!' not followed by '=').
     */
    private TokenType handleOperatorOrPunctuation(final byte firstByte) throws IOException {
        switch (firstByte) {
            case SLASH:
                readNextChar();
                if (forwardBuffer[forward] == SLASH) {
                    return TokenType.DOUBLE_SLASH; // '//'
                } else {
                    decrementForward(); // Backtrack, it's just '/'
                    return TokenType.SLASH;
                }
            case PLUS:
                return TokenType.PLUS;
            case MINUS:
                return TokenType.MINUS;
            case MULTIPLY_OPERATOR: // Assuming this constant represents '*'
                return TokenType.MULTIPLY_OPERATOR;
            case EQUALS:
                return TokenType.EQUAL_TO;
            case LPAREN:
                return TokenType.LPAREN;
            case RPAREN:
                return TokenType.RPAREN;
            case LBRACKET:
                return TokenType.LBRACKET;
            case RBRACKET:
                return TokenType.RBRACKET;
            case AT_OPERATOR: // Assuming this constant represents '@'
                return TokenType.AT_OPERATOR;
            case COMMA:
                return TokenType.COMMA;
            case UNION_OPERATOR: // Assuming this constant represents '|'
                return TokenType.UNION_OPERATOR;
            case NOT: // Assuming this constant represents '!'
                readNextChar();
                if (forwardBuffer[forward] == EQUALS) {
                    return TokenType.NOT_EQUAL_TO; // '!='
                } else {
                    // According to original code, only '!=' is supported.
                    // Backtracking might be needed if '!' could be valid alone in other contexts.
                    // decrementForward(); // If '!' were valid alone.
                    throw new IOException("Invalid character sequence: '!' must be followed by '=' in XPath 1.0 for '!=' operator.");
                }
            case GREATER_THAN:
                readNextChar();
                if (forwardBuffer[forward] == EQUALS) {
                    return TokenType.GREATER_THAN_EQUAL_TO; // '>='
                } else {
                    decrementForward(); // Backtrack, it's just '>'
                    return TokenType.GREATER_THAN;
                }
            case LESS_THAN:
                readNextChar();
                if (forwardBuffer[forward] == EQUALS) {
                    return TokenType.LESS_THAN_EQUAL_TO; // '<='
                } else {
                    decrementForward(); // Backtrack, it's just '<'
                    return TokenType.LESS_THAN;
                }
            case COLON:
                readNextChar();
                if (forwardBuffer[forward] == COLON) {
                    return TokenType.AXIS_SEPARATOR; // '::'
                } else {
                    // Backtrack and return forward
                    decrementForward();
                    return TokenType.COLON;
                }
            default:
                // If none of the known characters match, it's an unexpected character.
                throw new IOException("Unexpected character encountered: " + (char) firstByte + " (byte value: " + firstByte + ")");
        }
    }


    /**
     * Checks if a byte represents an ASCII digit ('0'-'9').
     *
     * @param b The byte to check.
     * @return True if the byte is a digit, false otherwise.
     */
    private boolean isDigit(final byte b) {
        return b >= ZERO && b <= NINE; // Assuming ZERO and NINE constants exist
    }

    /**
     * Checks if a byte represents an ASCII letter ('a'-'z' or 'A'-'Z').
     * Note: XPath 1.0 NCName rules are more complex (allow '_', non-ASCII letters),
     * but this lexer seems to only handle basic ASCII letters based on the original code.
     *
     * @param b The byte to check.
     * @return True if the byte is an ASCII letter, false otherwise.
     */
    private boolean isLetter(final byte b) {
        // Assuming LOWERCASE_A, LOWERCASE_Z, UPPERCASE_A, UPPERCASE_Z constants exist
        return (b >= LOWERCASE_A && b <= LOWERCASE_Z) || (b >= UPPERCASE_A && b <= UPPERCASE_Z);
    }
}