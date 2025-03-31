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

import com.evolvedbinary.tulip.lexer.Token;
import com.evolvedbinary.tulip.lexer.TokenType;
import com.evolvedbinary.tulip.lexer.XPath10Lexer;
import com.evolvedbinary.tulip.source.FileSource;
import com.evolvedbinary.tulip.source.Source;
import com.evolvedbinary.tulip.spec.XmlSpecification;
import com.evolvedbinary.tulip.spec.XmlSpecification_1_0;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static com.evolvedbinary.tulip.constants.LexerConstants.BUFFER_SIZE;
import static org.junit.jupiter.api.Assertions.*;

public class XPath10LexerTest {
    private final XmlSpecification xmlSpecification = new XmlSpecification_1_0();

    // --- Helper Record ---
    record TokenInfo(TokenType type, String lexeme) {
        @Override
        public String toString() {
            // Nicer formatting for assertion failures
            return String.format("(%s, \"%s\")", type, lexeme);
        }
    }

    // --- Helper Method to Lex Input ---
    private List<TokenInfo> lex(String input) throws IOException {
        Path path = Paths.get("src/test/java/com/evolvedbinary/tulip/file.txt");
        try {
            // Overwrite the file content
            Files.write(path, input.getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            System.out.println("File successfully overwritten.");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

        List<TokenInfo> tokens = new ArrayList<>();
        try (Source source = FileSource.open(path); // Use try-with-resources for source
             XPath10Lexer lexer = new XPath10Lexer(source, BUFFER_SIZE, xmlSpecification)) {

            while (true) {
                Token t = lexer.next();
                String lexeme = (t.getTokenType() == TokenType.EOF) ? "" : t.getLexeme();
                tokens.add(new TokenInfo(t.getTokenType(), lexeme));
                if (t.getTokenType() == TokenType.EOF) {
                    break;
                }
            }
        } // Source and Lexer are closed here
        System.out.println(tokens);
        return tokens;
    }

    // ========================================================================
    // Test Methods
    // ========================================================================

    @Test
    void testOnlyWhitespace() throws IOException {
        assertEquals(List.of(new TokenInfo(TokenType.EOF, "")), lex(" \t\n\r  \n"));
    }

    // --- Literals ---
    @Test
    void testLiterals() throws IOException {
        String input = " \"double\" 'single' \"\" '' \"it's\" '\"quote\"' ";
        List<TokenInfo> expected = List.of(
                new TokenInfo(TokenType.LITERAL, "\"double\""),
                new TokenInfo(TokenType.LITERAL, "'single'"),
                new TokenInfo(TokenType.LITERAL, "\"\""),
                new TokenInfo(TokenType.LITERAL, "''"),
                new TokenInfo(TokenType.LITERAL, "\"it's\""),
                new TokenInfo(TokenType.LITERAL, "'\"quote\"'"),
                new TokenInfo(TokenType.EOF, "")
        );
        assertEquals(expected, lex(input));
    }

    // --- Numbers ---
    @Test
    void testNumbers() throws IOException {
        String input = " 123 0 45.67 0.5 .5 ";
        List<TokenInfo> expected = List.of(
                new TokenInfo(TokenType.DIGITS, "123"),
                new TokenInfo(TokenType.DIGITS, "0"),
                new TokenInfo(TokenType.NUMBER, "45.67"),
                new TokenInfo(TokenType.NUMBER, "0.5"),
                new TokenInfo(TokenType.NUMBER, ".5"),
                new TokenInfo(TokenType.EOF, "")
        );
        assertEquals(expected, lex(input));
    }

    // --- Operators ---
    @Test
    void testSimpleOperators() throws IOException {
        String input = "+ - * = | / @";
        List<TokenInfo> expected = List.of(
                new TokenInfo(TokenType.PLUS, "+"),
                new TokenInfo(TokenType.MINUS, "-"),
                new TokenInfo(TokenType.MULTIPLY_OPERATOR, "*"),
                new TokenInfo(TokenType.EQUAL_TO, "="),
                new TokenInfo(TokenType.UNION_OPERATOR, "|"),
                new TokenInfo(TokenType.SLASH, "/"),
                new TokenInfo(TokenType.AT_OPERATOR, "@"),
                new TokenInfo(TokenType.EOF, "")
        );
        assertEquals(expected, lex(input));
    }

    @Test
    void testComparisonOperators() throws IOException {
        String input = "< > <= >= = !=";
        List<TokenInfo> expected = List.of(
                new TokenInfo(TokenType.LESS_THAN, "<"),
                new TokenInfo(TokenType.GREATER_THAN, ">"),
                new TokenInfo(TokenType.LESS_THAN_EQUAL_TO, "<="),
                new TokenInfo(TokenType.GREATER_THAN_EQUAL_TO, ">="),
                new TokenInfo(TokenType.EQUAL_TO, "="),
                new TokenInfo(TokenType.NOT_EQUAL_TO, "!="),
                new TokenInfo(TokenType.EOF, "")
        );
        assertEquals(expected, lex(input));
    }

    @Test
    void testPathOperators() throws IOException {
        String input = "/ // . .. ::";
        List<TokenInfo> expected = List.of(
                new TokenInfo(TokenType.SLASH, "/"),
                new TokenInfo(TokenType.DOUBLE_SLASH, "//"),
                new TokenInfo(TokenType.CURRENT_AXIS, "."),
                new TokenInfo(TokenType.PARENT_AXIS, ".."),
                new TokenInfo(TokenType.AXIS_SEPARATOR, "::"),
                new TokenInfo(TokenType.EOF, "")
        );
        assertEquals(expected, lex(input));
    }

    @Test
    void testPunctuation() throws IOException {
        String input = "( ) [ ] ,";
        List<TokenInfo> expected = List.of(
                new TokenInfo(TokenType.LPAREN, "("),
                new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.LBRACKET, "["),
                new TokenInfo(TokenType.RBRACKET, "]"),
                new TokenInfo(TokenType.COMMA, ","),
                new TokenInfo(TokenType.EOF, "")
        );
        assertEquals(expected, lex(input));
    }

    // --- Keywords (Axis Names) ---
    @Test
    void testAxisNames() throws IOException {
        String input = "child:: attribute:: ancestor-or-self:: descendant:: descendant-or-self:: following:: following-sibling:: namespace:: parent:: preceding:: preceding-sibling:: self::";
        List<TokenInfo> expected = List.of(
                new TokenInfo(TokenType.AXIS_NAME, "child"), new TokenInfo(TokenType.AXIS_SEPARATOR, "::"),
                new TokenInfo(TokenType.AXIS_NAME, "attribute"), new TokenInfo(TokenType.AXIS_SEPARATOR, "::"),
                new TokenInfo(TokenType.AXIS_NAME, "ancestor-or-self"), new TokenInfo(TokenType.AXIS_SEPARATOR, "::"),
                new TokenInfo(TokenType.AXIS_NAME, "descendant"), new TokenInfo(TokenType.AXIS_SEPARATOR, "::"),
                new TokenInfo(TokenType.AXIS_NAME, "descendant-or-self"), new TokenInfo(TokenType.AXIS_SEPARATOR, "::"),
                new TokenInfo(TokenType.AXIS_NAME, "following"), new TokenInfo(TokenType.AXIS_SEPARATOR, "::"),
                new TokenInfo(TokenType.AXIS_NAME, "following-sibling"), new TokenInfo(TokenType.AXIS_SEPARATOR, "::"),
                new TokenInfo(TokenType.AXIS_NAME, "namespace"), new TokenInfo(TokenType.AXIS_SEPARATOR, "::"),
                new TokenInfo(TokenType.AXIS_NAME, "parent"), new TokenInfo(TokenType.AXIS_SEPARATOR, "::"),
                new TokenInfo(TokenType.AXIS_NAME, "preceding"), new TokenInfo(TokenType.AXIS_SEPARATOR, "::"),
                new TokenInfo(TokenType.AXIS_NAME, "preceding-sibling"), new TokenInfo(TokenType.AXIS_SEPARATOR, "::"),
                new TokenInfo(TokenType.AXIS_NAME, "self"), new TokenInfo(TokenType.AXIS_SEPARATOR, "::"),
                new TokenInfo(TokenType.EOF, "")
        );
        assertEquals(expected, lex(input));
    }

    // --- Keywords (Function Names) ---
    @Test
    void testFunctionNames() throws IOException {
        String input = " count( string-length( starts-with( last() position() id() local-name() namespace-uri() name() string() concat() contains() substring-before() substring-after() substring() normalize-space() translate() boolean() not() true() false() lang() number() sum() floor() ceiling() round() ";
        // Test only first few and representative ones
        List<TokenInfo> expected = List.of(
                new TokenInfo(TokenType.FUNCTION, "count"), new TokenInfo(TokenType.LPAREN, "("),
                new TokenInfo(TokenType.FUNCTION, "string-length"), new TokenInfo(TokenType.LPAREN, "("),
                new TokenInfo(TokenType.FUNCTION, "starts-with"), new TokenInfo(TokenType.LPAREN, "("),
                new TokenInfo(TokenType.FUNCTION, "last"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "position"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "id"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "local-name"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "namespace-uri"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "name"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "string"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "concat"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "contains"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "substring-before"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "substring-after"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "substring"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "normalize-space"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "translate"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "boolean"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "not"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "true"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "false"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "lang"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "number"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "sum"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "floor"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "ceiling"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.FUNCTION, "round"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.EOF, "")
        );
        assertEquals(expected, lex(input));
    }


    // --- Combinations ---
    @Test
    void testComplexExpression1() throws IOException {
        String input = "//book[@price > 10.5 and starts-with(title, 'XPath')]";
        List<TokenInfo> expected = List.of(
                new TokenInfo(TokenType.DOUBLE_SLASH, "//"),
                new TokenInfo(TokenType.IDENTIFIER, "book"),
                new TokenInfo(TokenType.LBRACKET, "["),
                new TokenInfo(TokenType.AT_OPERATOR, "@"),
                new TokenInfo(TokenType.IDENTIFIER, "price"),
                new TokenInfo(TokenType.GREATER_THAN, ">"),
                new TokenInfo(TokenType.NUMBER, "10.5"),
                new TokenInfo(TokenType.AND, "and"), // Operator keyword -> IDENTIFIER
                new TokenInfo(TokenType.FUNCTION, "starts-with"),
                new TokenInfo(TokenType.LPAREN, "("),
                new TokenInfo(TokenType.IDENTIFIER, "title"),
                new TokenInfo(TokenType.COMMA, ","),
                new TokenInfo(TokenType.LITERAL, "'XPath'"),
                new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.RBRACKET, "]"),
                new TokenInfo(TokenType.EOF, "")
        );
        assertEquals(expected, lex(input));
    }

    @Test
    void testComplexExpression2() throws IOException {
        String input = "sum(//@value | /data/item/@val) + count(//*)";
        List<TokenInfo> expected = List.of(
                new TokenInfo(TokenType.FUNCTION, "sum"),
                new TokenInfo(TokenType.LPAREN, "("),
                new TokenInfo(TokenType.DOUBLE_SLASH, "//"),
                new TokenInfo(TokenType.AT_OPERATOR, "@"),
                new TokenInfo(TokenType.IDENTIFIER, "value"),
                new TokenInfo(TokenType.UNION_OPERATOR, "|"),
                new TokenInfo(TokenType.SLASH, "/"),
                new TokenInfo(TokenType.IDENTIFIER, "data"),
                new TokenInfo(TokenType.SLASH, "/"),
                new TokenInfo(TokenType.IDENTIFIER, "item"),
                new TokenInfo(TokenType.SLASH, "/"),
                new TokenInfo(TokenType.AT_OPERATOR, "@"),
                new TokenInfo(TokenType.IDENTIFIER, "val"),
                new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.PLUS, "+"),
                new TokenInfo(TokenType.FUNCTION, "count"),
                new TokenInfo(TokenType.LPAREN, "("),
                new TokenInfo(TokenType.DOUBLE_SLASH, "//"),
                new TokenInfo(TokenType.MULTIPLY_OPERATOR, "*"), // '*' likely MULTIPLY_OPERATOR
                new TokenInfo(TokenType.RPAREN, ")"),
                new TokenInfo(TokenType.EOF, "")
        );
        assertEquals(expected, lex(input));
    }


    // --- Edge Cases ---
    @Test
    void testDotVariants() throws IOException {
        assertEquals(List.of(new TokenInfo(TokenType.CURRENT_AXIS, "."), new TokenInfo(TokenType.EOF, "")), lex("."));
        assertEquals(List.of(new TokenInfo(TokenType.PARENT_AXIS, ".."), new TokenInfo(TokenType.EOF, "")), lex(".."));
        assertEquals(List.of(new TokenInfo(TokenType.NUMBER, ".5"), new TokenInfo(TokenType.EOF, "")), lex(".5"));
        assertEquals(List.of(new TokenInfo(TokenType.PARENT_AXIS, ".."), new TokenInfo(TokenType.DIGITS, "5"), new TokenInfo(TokenType.EOF, "")), lex("..5"));
        assertEquals(List.of(new TokenInfo(TokenType.PARENT_AXIS, ".."), new TokenInfo(TokenType.CURRENT_AXIS, "."), new TokenInfo(TokenType.EOF, "")), lex("...")); // . followed by ..
    }

    @Test
    void testNumberAndDot() throws IOException {
        // Ensure dot after number is treated as CURRENT_AXIS
        assertEquals(
                List.of(new TokenInfo(TokenType.NUMBER, "1"), new TokenInfo(TokenType.CURRENT_AXIS, "."), new TokenInfo(TokenType.EOF, "")),
                lex("1.")
        );
        // Ensure dot before number is part of the number
        assertEquals(
                List.of(new TokenInfo(TokenType.NUMBER, ".5"), new TokenInfo(TokenType.EOF, "")),
                lex(".5")
        );
    }

    @Test
    void testUnaryMinus() throws IOException {
        assertEquals(
                List.of(new TokenInfo(TokenType.MINUS, "-"), new TokenInfo(TokenType.DIGITS, "1"), new TokenInfo(TokenType.EOF, "")),
                lex("-1")
        );
        assertEquals(
                List.of(new TokenInfo(TokenType.MINUS, "-"), new TokenInfo(TokenType.NUMBER, ".5"), new TokenInfo(TokenType.EOF, "")),
                lex("-.5")
        );
        assertEquals( // Check space doesn't break it
                List.of(new TokenInfo(TokenType.MINUS, "-"), new TokenInfo(TokenType.DIGITS, "5"), new TokenInfo(TokenType.EOF, "")),
                lex("- 5")
        );
    }

    // --- Operator Keywords ---
    @Test
    void testOperatorKeywords() throws IOException {
        String input = "and or mod div";
        List<TokenInfo> expected = List.of(
                new TokenInfo(TokenType.AND, "and"), // Assuming lexer returns IDENTIFIER
                new TokenInfo(TokenType.OR, "or"),  // Assuming lexer returns IDENTIFIER
                new TokenInfo(TokenType.MOD, "mod"), // Assuming lexer returns IDENTIFIER
                new TokenInfo(TokenType.DIV, "div"), // Assuming lexer returns IDENTIFIER
                new TokenInfo(TokenType.EOF, "")
        );
        assertEquals(expected, lex(input));
    }

    // --- Identifiers ---
    @Test
    void testIdentifiers() throws IOException {
        String input = " simple _underscore with-hyphen with.dot a123";
        List<TokenInfo> expected = List.of(
                new TokenInfo(TokenType.IDENTIFIER, "simple"),
                new TokenInfo(TokenType.IDENTIFIER, "_underscore"),
                new TokenInfo(TokenType.IDENTIFIER, "with-hyphen"),
                new TokenInfo(TokenType.IDENTIFIER, "with.dot"),
                new TokenInfo(TokenType.IDENTIFIER, "a123"),
                new TokenInfo(TokenType.EOF, "")
        );
        assertEquals(expected, lex(input));
    }

    // ========================================================================
    // Not programmed yet or edge cases which don't work
    // ========================================================================


    // --- Node Types ---
    @Test
    void testNodeTypes() throws IOException {
        String input = "node() text() comment() processing-instruction()";
        List<TokenInfo> expected = List.of(
                new TokenInfo(TokenType.NODE_TYPE, "node"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"), // Assuming IDENTIFIER
                new TokenInfo(TokenType.TEXT_NODE, "text"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"), // Assuming IDENTIFIER
                new TokenInfo(TokenType.COMMENT_NODE, "comment"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"), // Assuming IDENTIFIER
                new TokenInfo(TokenType.PROCESSING_INSTRUCTION, "processing-instruction"), new TokenInfo(TokenType.LPAREN, "("), new TokenInfo(TokenType.RPAREN, ")"), // Assuming IDENTIFIER
                new TokenInfo(TokenType.EOF, "")
        );
        assertEquals(expected, lex(input));
    }

}
