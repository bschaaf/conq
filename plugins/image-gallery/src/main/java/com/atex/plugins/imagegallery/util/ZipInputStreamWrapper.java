/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.imagegallery.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

public class ZipInputStreamWrapper extends ZipInputStream {

    public ZipInputStreamWrapper(final InputStream is) {
        super(is);
    }

    @Override
    public void close() throws IOException {
//        override the close method without close process.
//        to avoid ZipInputStream to be closed by other input stream.
    }

    public void closeInput() throws IOException {
        super.close();
    }
}
