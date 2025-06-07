/*
 * XSLTFilter.java
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

/**
 * An XSLT filter.
 * This can be used to transform the XML output of one servlet (or file) in the
 * web application to another XML/XHTML representation.
 * Multiple filters can be applied to different paths (routes) in the web
 * application by declaring separate instances of this filter with their
 * associated XSLT stylesheets.
 *
 * @author Chris Burdess
 */
public class XSLTFilter implements Filter {

    private static final Map<String,String> MEDIA_TYPES = new TreeMap<>();
    static {
        MEDIA_TYPES.put("xml", "text/xml");
        MEDIA_TYPES.put("html", "text/html");
        MEDIA_TYPES.put("text", "text/plain");
    }

    /**
     * The servlet container. This can be used to report errors.
     */
    private ServletContext context;

    /**
     * Compiled representation of the XSLT stylesheet.
     * This can be used to create an individual Transformer object per
     * request, so we don't have to synchronize on the transformer.
     */
    private Templates templates;

    /**
     * URI resolver used to resolve contents of xsl:imports etc.
     */
    private URIResolver resolver;

    public void init(FilterConfig config) throws ServletException {
        // Load the XSLT for this filter as a compiled Transformer object
        String path = config.getInitParameter("xslt-path");
        if (path == null) {
            throw new UnavailableException("xslt-path is a required parameter for XSLTFilter");
        }
        context = config.getServletContext();
        resolver = new ServletContextURIResolver(context);
        InputStream in = context.getResourceAsStream(path);
        if (path == null) {
            throw new UnavailableException("xslt-path must refer to a valid resource in the web application: "+path);
        }
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setURIResolver(resolver); // required to resolve imported stylesheets
            templates = factory.newTemplates(new StreamSource(in, path));
        } catch (TransformerConfigurationException e) {
            throw new ServletException("Cannot compile XSLT stylesheet "+path, e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new ServletException("Cannot instantiate TransformerFactory", e);
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("Invoking filter for path: "+((HttpServletRequest)request).getServletPath()+" "+((HttpServletRequest)request).getPathInfo());
        // Wrap the response
        if (!(response instanceof HttpServletResponse)) {
            throw new ServletException("non-HTTP servlet response");
        }
        BufferedHttpServletResponse proxyResponse = new BufferedHttpServletResponse((HttpServletResponse) response);
        // Invoke the next filter in the chain
        chain.doFilter(request, proxyResponse);
        // NB sometimes containers cache the response, and we never see it here.
        // In that case allow the container to serve the cached response.
        if (proxyResponse.getBufferSize() == 0) {
            chain.doFilter(request, response);
            return;
        }
        // Perform the transformation
        try {
            Transformer transformer = templates.newTransformer();
            Source source = new StreamSource(proxyResponse.getInputStream());
            ByteArrayOutputStream sink = new ByteArrayOutputStream();
            transformer.transform(source, new StreamResult(sink));
            // Set Content-Type and Content-Length
            Properties properties = templates.getOutputProperties();
            String mediaType = properties.getProperty("media-type");
            if (mediaType == null) {
                mediaType = MEDIA_TYPES.get(properties.get("method"));
                if (mediaType == null) {
                    mediaType = "text/xml";
                }
            }
            response.setContentType(mediaType);
            response.setContentLength(sink.size());
            // Write output
            response.getOutputStream().write(sink.toByteArray());
            response.flushBuffer();
        } catch (TransformerException e) {
            if (request instanceof HttpServletRequest) {
                // Include the path of the request
                HttpServletRequest hrequest = (HttpServletRequest) request;
                String pathInfo = hrequest.getPathInfo();
                String path = hrequest.getServletPath();
                if (pathInfo != null) {
                    path = path + pathInfo;
                } 
                throw new ServletException("Error transforming "+path, e);
            } else {
                throw new ServletException(e);
            }
        }
    }

    public void destroy() {
        templates = null;
        context = null;
    }

}

