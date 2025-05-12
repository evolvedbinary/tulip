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
public class FileSource implements Source {

    private final Path file;
    private final InputStream is;

    private FileSource(final Path file, final InputStream is) {
        this.file = file;
        this.is = is;
    }

    /**
     * Open the file.
     *
     * @param file the file to open
     *
     * @return a source for the file.
     */
    public static FileSource open(final Path file) throws IOException {
        final InputStream is = Files.newInputStream(file, StandardOpenOption.READ);
        return new FileSource(file, is);
    }

    @Override
    public String getIdentifier() {
        return file.normalize().toAbsolutePath().toString();
    }

    @Override
    public int read(final byte[] buffer) throws IOException {
        return is.read(buffer);
    }

    @Override
    public void close() throws IOException {
        is.close();
    }
}
