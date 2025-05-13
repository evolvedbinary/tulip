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
package com.evolvedbinary.tulip.source;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A file to read unparsed text from.
 */
public class FileSource extends InputStreamSource {

    private FileSource(final Path unparsedText, final InputStream is) {
        super(unparsedText.normalize().toAbsolutePath().toString(), is);
    }

    /**
     * Open the file.
     *
     * @param unparsedText the file to open
     *
     * @return a source for the file.
     */
    public static FileSource open(final Path unparsedText) throws IOException {
        if (unparsedText == null) {
            throw new IllegalArgumentException("Input unparsed text must not be null");
        }

        final InputStream is = Files.newInputStream(unparsedText, StandardOpenOption.READ);
        return new FileSource(unparsedText, is);
    }
}
