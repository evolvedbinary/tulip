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

import com.evolvedbinary.tulip.source.StringSource;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractLexerTest {

    @Test
    public void initialState() throws IOException {
        final int bufferSize = 4096;
        final XmlSpecification xmlSpecification = XmlSpecification_1_0.INSTANCE;

        try (final Source source = new StringSource("'literal'")) {
            final TestableAbstractLexer lexer = new TestableAbstractLexer(source, bufferSize, xmlSpecification);

            assertEquals(source, lexer.source);
            assertEquals(bufferSize, lexer.bufferSize);
            assertNull(lexer.buffer1);
            assertNull(lexer.buffer2);
            assertNull(lexer.currentBuffer);
            assertEquals(0, lexer.currentBufferLength);
            assertEquals(xmlSpecification, lexer.getXmlSpecification());
            assertEquals(0, lexer.lexemeBegin);
            assertEquals(-1, lexer.forward);
        }
    }

    @Test
    public void tokenCache() throws IOException {
        final int bufferSize = 4096;
        final XmlSpecification xmlSpecification = XmlSpecification_1_0.INSTANCE;

        try (final Source source = new StringSource("'literal'")) {
            final TestableAbstractLexer lexer = new TestableAbstractLexer(source, bufferSize, xmlSpecification);

            assertTrue(lexer.freeTokens.isEmpty());

            // take a first token
            final Token firstToken = lexer.getFreeToken();
            assertNotNull(firstToken);
            assertTrue(lexer.freeTokens.isEmpty());

            // take a second token
            final Token secondToken = lexer.getFreeToken();
            assertNotNull(secondToken);
            assertTrue(lexer.freeTokens.isEmpty());

            // return the first token
            firstToken.close();
            assertEquals(firstToken, lexer.freeTokens.peek());

            // return the second token
            secondToken.close();
            assertEquals(secondToken, lexer.freeTokens.peek());

            // take a third token - should be the second token reused
            final Token thirdToken = lexer.getFreeToken();
            assertNotNull(thirdToken);
            assertEquals(secondToken, thirdToken);

            // only first token left in the cache
            assertEquals(firstToken, lexer.freeTokens.peek());

            // take a fourth token - should be the first token reused
            final Token fourthToken = lexer.getFreeToken();
            assertNotNull(fourthToken);
            assertEquals(fourthToken, firstToken);

            //cache should now be empty
            assertNull(lexer.freeTokens.peek());
        }
    }

    private static class TestableAbstractLexer extends AbstractLexer {
        public TestableAbstractLexer(final Source source, final int bufferSize, final XmlSpecification xmlSpecification) {
            super(source, bufferSize, xmlSpecification);
        }

        @Override
        public @Nullable Token next() throws IOException {
            return null;
        }

        public XmlSpecification getXmlSpecification() {
            return xmlSpecification;
        }
    }
}
