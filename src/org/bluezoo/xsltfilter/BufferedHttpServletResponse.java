/*
 * BufferedHttpServletResponse.java
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

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Wrapper for an HTTP servlet response.
 * This will use an underlying BufferedOutputStream to store and retrieve
 * the actual data written to it.
 *
 * @author Chris Burdess
 */
class BufferedHttpServletResponse extends HttpServletResponseWrapper {

    private BufferedServletOutputStream out = new BufferedServletOutputStream();

    private PrintWriter writer;
    private ServletOutputStream outputStream;

    BufferedHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            throw new IllegalStateException("Cannot call getWriter after getOutputStream");
        }
        if (writer == null) {
            writer = new PrintWriter(out);
        }
        return writer;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (writer != null) {
            throw new IllegalStateException("Cannot call getOutputStream after getWriter");
        }
        if (outputStream == null) {
            outputStream = out;
        }
        return outputStream;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (outputStream != null) {
            outputStream.flush();
        } else if (writer != null) {
            writer.flush();
        }
    }

    @Override
    public int getBufferSize() {
        return out.getBufferSize();
    }

    @Override
    public void setBufferSize(int size) {
        out.setBufferSize(size);
    }

    @Override
    public void reset() {
        out.reset();
    }

    @Override
    public void resetBuffer() {
        out.reset();
    }

    /**
     * Used by XSLTFilter to retrieve the source to transform.
     */
    InputStream getInputStream() {
        return out.getInputStream();
    }

}
