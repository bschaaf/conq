package it.conquiste.plugins.sitemap.filters;

import it.conquiste.plugins.sitemap.filters.util.FileFilterUtils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class SitemapFilter implements Filter {
    private static final Logger LOG = Logger.getLogger(SitemapFilter.class.getName());
    private static final String SITEMAP_NEWS_XML="news-sitemap.xml";
    private final static String sitemapOutputTemplate = "?ot=it.conquiste.plugins.sitemap.Sitemap.ot";
    private final static Pattern sitemapPattern = Pattern.compile("(.*)/[^/]*sitemap\\.xml", Pattern.CASE_INSENSITIVE);

    public ServletContext servletContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        //Nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String url = FileFilterUtils.decodeUrl(httpRequest.getRequestURI());
        LOG.log(Level.FINE, "SitemapFilter will check for sitemap present in URL: " + url);
        Matcher matcher = sitemapPattern.matcher(url);
        if (matcher.matches()) {
            //Check for news-sitemap.xml first
            if(url.toLowerCase().endsWith(SITEMAP_NEWS_XML)) {
                httpRequest.setAttribute("mode","news_sitemap");
            }
            else {
                //URL ends with sitemap.xml
                httpRequest.setAttribute("mode","sitemap");
            }
            String newURI = FileFilterUtils.getUriToFileTemplate(sitemapPattern, url, sitemapOutputTemplate); 
            LOG.log(Level.FINE, "SitemapFilter is redirecting to: " + newURI);
            servletContext.getRequestDispatcher(newURI).forward(httpRequest, httpResponse);
            return;
        }
        chain.doFilter(request, response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        servletContext = config.getServletContext();
    }
}

