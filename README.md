# xsltfilter
XSLT servlet filter for J2EE web applications

## Installation
```
git clone https://github.com/cpkb-bluezoo/xsltfilter.git
cd xsltfilter
ant dist
```
This will create a jar file in your `dist/lib` subdirectory that you can then include in the WEB-INF/lib of your web application directory or WAR file.

## Deployment
You configure the XSLTFilter in the web.xml deployment descriptor of your web application.

Here is an example:
```
    <filter>
        <filter-name>home-xslt</filter-name>
        <filter-class>org.bluezoo.xsltfilter.XSLTFilter</filter-class>
        <init-param>
            <param-name>xslt-path</param-name>
            <param-value>/xslt/home.xslt</param-value>
        </init-param>
    </filter>

    <filter-mapping>
        <filter-name>home-xslt</filter-name>
        <url-pattern>/</url-pattern>
    </filter-mapping>
```
This states that all requests matched by "/" (the root) of the web application will have the XSLT filter applied using the stylesheet `xslt/home.xslt` (contained in the web application itself).

It will thus take XML output from a servlet also mapped to this path, and transform it using the specified stylesheet.
