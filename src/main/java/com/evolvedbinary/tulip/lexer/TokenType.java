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

public enum TokenType {
    LITERAL,
    AXIS_NAME,
    FUNCTION,
    DIGITS,
    NUMBER,
    SLASH,
    DOUBLE_SLASH,
    UNION_OPERATOR,
    PLUS,
    MINUS,
    EQUAL_TO,
    NOT_EQUAL_TO,
    LESS_THAN,
    LESS_THAN_EQUAL_TO,
    GREATER_THAN,
    GREATER_THAN_EQUAL_TO,
    MULTIPLY_OPERATOR,
    LPAREN,
    RPAREN,
    LBRACKET,
    RBRACKET,
    AT_OPERATOR,
    COMMA,
    CURRENT_AXIS,
    PARENT_AXIS,
    AXIS_SEPARATOR,
    IDENTIFIER,
    AND,
    OR,
    DIV,
    MOD,
    NODE_TYPE,
    TEXT_NODE,
    COMMENT_NODE,
    PROCESSING_INSTRUCTION,
    COLON,
    EOF
}
