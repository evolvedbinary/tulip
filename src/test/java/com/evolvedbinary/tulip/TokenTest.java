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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenTest {

    @Test
    public void closeIsIdempotent() {
        final List<Token> releasedTokens = new ArrayList<>();

        final Token token = new Token(null) {
            @Override
            protected void releaseToken() {
                releasedTokens.add(this);
            }
        };

        assertTrue(releasedTokens.isEmpty());

        token.close();

        assertEquals(1, releasedTokens.size());
        assertEquals(token, releasedTokens.get(0));

        // close again, should not change released tokens
        token.close();

        assertEquals(1, releasedTokens.size());
        assertEquals(token, releasedTokens.get(0));
    }
}
