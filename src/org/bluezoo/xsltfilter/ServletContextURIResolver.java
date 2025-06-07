/*
 * ServletContextURIResolver.java
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
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.ServletContext;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

/**
 * A URI resolver that can be used to resolve any XSL import, include, or
 * document functions when loading within a servlet context.
 * This will use the servlet context getResource method.
 *
 * @author Chris Burdess
 */
class ServletContextURIResolver implements URIResolver {

    private ServletContext ctx;

    ServletContextURIResolver(ServletContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        // Clean up base. Tomcat will try to turn it into a file URL.
        if (base.startsWith("file://")) {
            base = base.substring(7);
        }
        try {
            String path = new URI(base).resolve(new URI(href)).toString();
            InputStream in = ctx.getResourceAsStream(path);
            return (in == null) ? null : new StreamSource(in);
        } catch (URISyntaxException e) {
            throw new TransformerException("Invalid URL resolving "+href+" against "+base, e);
        }
    }

}
