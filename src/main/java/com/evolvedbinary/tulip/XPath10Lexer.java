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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class XPath10Lexer extends AbstractLexer {

    public static Trie trie = new Trie();

    static {
        // Insert all axis names
        for (String axis : new String[]{
                "ancestor", "ancestor-or-self", "attribute", "child",
                "descendant", "descendant-or-self", "following",
                "following-sibling", "namespace", "parent",
                "preceding", "preceding-sibling", "self"
        }) {
            trie.insert(axis, false);
        }

        // Insert all function names
        for (String function : new String[]{
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
        }) {
            trie.insert(function, true);
        }
        System.out.println("Trie has been created and populated");
    }

    protected XPath10Lexer(final Source source, final int bufferSize, final XmlSpecification xmlSpecification) throws IOException {
        super(source, bufferSize, xmlSpecification);
    }

    @Override
    public Token next() throws IOException {

        readNextChar();
        byte b = forwardBuffer[forward];
        TokenType tokenType = null;

        while (xmlSpecification.isWhiteSpace(b)) {
            // XML white-space
            readNextChar();
            b = forwardBuffer[forward];
            resetLexemeBegin();
            decrementBegin(); //todo -> see if a better code design can replace this
        }

        if (b == 117) {
            System.out.println("Reached end of file");
            tokenType = TokenType.EOF;
        } else if(isLetter(b)) {
            TrieNode node = trie.getRoot();
            node = trie.traverse(b, node);
            while(true) {
                readNextChar();
                if (isLetter(forwardBuffer[forward])) {
                    if(node != null) {
                        node = trie.traverse(forwardBuffer[forward], node);
                    }
                } else if(forwardBuffer[forward] == MINUS) {
                    node = trie.traverse(forwardBuffer[forward], node);
                    readNextChar();
                    if(isLetter(forwardBuffer[forward])) {
                        node = trie.traverse(forwardBuffer[forward], node);
                    } else {
                        decrementForward();
                        break;
                    }
                }else {
                    break;
                }
            }
            decrementForward();
            if(node!=null && node.isAxis) {
                tokenType = TokenType.AXIS_NAME;
            } else if(node!=null && node.isFunction) {
                tokenType = TokenType.FUNCTION;
            } else {
                tokenType = TokenType.IDENTIFIER;
            }
        } else if (isDigit(b)) {
            // IntegerLiteral or (DecimalLiteral or Double Literal) starting with a digit
            tokenType = TokenType.DIGITS;
            do {
                readNextChar();
            } while (isDigit(forwardBuffer[forward]));
            if(forwardBuffer[forward]==FULL_STOP) {
                do {
                    readNextChar();
                } while (isDigit(forwardBuffer[forward]));
            }
            decrementForward(); //todo -> see if a better code design can replace this
        } else if (b == FULL_STOP) {
            readNextChar();
            if(!isDigit(forwardBuffer[forward])) {
                if(forwardBuffer[forward]==FULL_STOP) {
                    //Parent Node
                    tokenType = TokenType.PARENT_AXIS;
                }
                else {
                    //Current Node
                    tokenType = TokenType.CURRENT_AXIS;
                    decrementForward();
                }
            } else { //Decimal literal
                tokenType = TokenType.NUMBER;
                do {
                    readNextChar();
                } while (isDigit(forwardBuffer[forward]));
                decrementForward(); //todo -> see if a better code design can replace this
            }
        } else if (b == QUOTATION_MARK || b == APOSTROPHE) {
            // Literal
            do {
                readNextChar();
            } while (forwardBuffer[forward] != b);
            tokenType = TokenType.LITERAL;

        } else if(b == SLASH) {
            readNextChar();
            if(forwardBuffer[forward] != SLASH) {
                decrementForward();
                tokenType = TokenType.SLASH;
            }
            else {
                tokenType = TokenType.DOUBLE_SLASH;
            }
        } else if(b == PLUS) {
            tokenType = TokenType.PLUS;
        } else if(b == MINUS) {
            tokenType = TokenType.MINUS;
        } else if(b == MULTIPLY_OPERATOR) {
            tokenType = TokenType.MULTIPLY_OPERATOR;
        } else if(b == EQUALS) {
            tokenType = TokenType.EQUAL_TO;
        } else if (b == LPAREN) {
            tokenType = TokenType.LPAREN;
        } else if (b == RPAREN) {
            tokenType = TokenType.RPAREN;
        } else if (b == LBRACKET) {
            tokenType = TokenType.LBRACKET;
        } else if (b == RBRACKET) {
            tokenType = TokenType.RBRACKET;
        } else if (b == AT_OPERATOR) {
            tokenType = TokenType.AT_OPERATOR;
        } else if (b == COMMA) {
            tokenType = TokenType.COMMA;
        } else if (b == UNION_OPERATOR) {
            tokenType = TokenType.UNION_OPERATOR;
        } else if(b == NOT) {
            readNextChar();
            if(forwardBuffer[forward] == EQUALS) {
                tokenType = TokenType.NOT_EQUAL_TO;
            }
            else {
                throw new IOException("NOT operator not followed by EQUAL_TO not supported in XPath");
            }
        } else if(b == GREATER_THAN) {
            readNextChar();
            if(forwardBuffer[forward] != EQUALS) {
                decrementForward();
                tokenType = TokenType.GREATER_THAN;
            }
            else {
                tokenType = TokenType.GREATER_THAN_EQUAL_TO;
            }
        } else if(b == LESS_THAN) {
            readNextChar();
            if(forwardBuffer[forward] != EQUALS) {
                decrementForward();
                tokenType = TokenType.LESS_THAN;
            }
            else {
                tokenType = TokenType.LESS_THAN_EQUAL_TO;
            }
        } else if(b == COLON) {
            readNextChar();
            if(forwardBuffer[forward] == COLON) {
                tokenType = TokenType.AXIS_SEPARATOR;
            }
            else {
                throw new IOException("COLON operator not followed by COLON not supported in XPath");
            }
        }
        // TODO(AR) set line number, column number
        try (Token token = getFreeToken()) { // doing this for not letting many token objects get created in runtime
            token.tokenType = tokenType;
            int tokenLength = ((forward-lexemeBegin+1>0)?(forward-lexemeBegin+1):(forward-lexemeBegin+1+getBufferSize()));
            byte[] currentToken = new byte[tokenLength];
            populateLexeme(currentToken);
            token.lexeme = currentToken;
            resetLexemeBegin();
            return token;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Determines if the character is a digit.
     * {@see https://www.w3.org/TR/xpath-10/#NT-Digits}
     *
     * @param b the character to test.
     * @return true if the character is a digit, false otherwise.
     */
    boolean isDigit(final byte b) {
        return b >= ZERO && b <= NINE;
    }

    boolean isLetter(final byte b) {
        return (b >= LOWERCASE_A && b <= LOWERCASE_Z) || (b >= UPPERCASE_A && b <= UPPERCASE_Z);
    }
}
