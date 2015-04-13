package it.conquiste.cm.rss;

import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

public class RssFeedReader {
    
    private final RssParser rssParser;

    public RssFeedReader(RssParser rssParser) {
        this.rssParser = rssParser;
    }

    public RssFeed getFeed(String url) throws RssFeedException {
        
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setSoTimeout(3000); // Five seconds timeout
        
        GetMethod getMethod = null;
        RssFeed feed = null;
        
        try {
            getMethod = new GetMethod(url);
            getMethod.addRequestHeader("Accept-Language", "it");
            httpClient.executeMethod(getMethod);
            
            InputStream responseBodyAsStream = getMethod.getResponseBodyAsStream();
            feed = rssParser.parse(responseBodyAsStream);
            
        } catch (Exception e) {
            throw new RssFeedException("Failed to established connection to: " + url,e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        
        return feed;
    }
}
