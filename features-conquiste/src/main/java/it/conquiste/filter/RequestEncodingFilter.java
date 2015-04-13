package it.conquiste.filter;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 */

public class RequestEncodingFilter implements Filter{
    private static final Logger logger = Logger.getLogger(RequestEncodingFilter.class.getName());

    private static final String ENCODING_PARAM = "encoding";

    private static String m_encoding = "";

    @Override
    public void init(FilterConfig config) throws ServletException{
        m_encoding = config.getInitParameter(ENCODING_PARAM);
    }

    @Override
    public void destroy(){
        // nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException{
//        logger.info("enter encoding");
    	System.out.println("-----------------> config encoding: " + this.m_encoding);
    	System.out.println("-----------------> req encoding: " + ((HttpServletRequest) request).getCharacterEncoding());
        request.setCharacterEncoding(m_encoding);

        chain.doFilter(request, response);
        System.out.println("-----------------> resp encoding: " + response.getCharacterEncoding());
    }

}
