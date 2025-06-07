/*
 * BufferedServletOutputStream.java
 * Copyright (C) 2025 Chris Burdess <cpkb@hush.ai>
 * 
 * This file is part of xsltfilter.
 * 
 * xsltfilter is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * xsltfilter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.bluezoo.xsltfilter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import javax.servlet.ServletOutputStream;

/**
 * A servlet output stream that stores its data in a buffer.
 * It will accept a hint on how large the buffer is to be if given, and go
 * with a default otherwise.
 *
 * @author Chris Burdess
 */
public class BufferedServletOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream sink;

    /**
     * @return the number of bytes written to this stream
     */
    int getBufferSize( ) {
        return (sink == null) ? 0 : sink.size();
    }

    void setBufferSize(int size) {
        sink = new ByteArrayOutputStream(size);
    }

    void reset() {
        if (sink != null) {
            sink.reset();
        }
    }

    /**
     * Used by BufferedServletResponse to return the input stream.
     */
    InputStream getInputStream() {
        return new ByteArrayInputStream(sink.toByteArray());
    }

    @Override
    public void write(int b) throws IOException {
        if (sink == null) {
            sink = new ByteArrayOutputStream();
        }
        sink.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (sink == null) {
            sink = new ByteArrayOutputStream();
        }
        sink.write(b, off, len);
    }

}
