package it.conquiste.cm.rss;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.application.Application;
import com.polopoly.cache.CacheKey;
import com.polopoly.cache.LRUSynchronizedUpdateCache;
import com.polopoly.cache.SynchronizedUpdateCache;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

public class RenderControllerRssElement
    extends RenderControllerBase
    
{
    private static final int SOFT_TIMEOUT = 10 * 60 * 1000;
    
    private static final Logger logger = Logger.getLogger(RenderControllerRssElement.class.getName());

       
    @Override
    public void populateModelBeforeCacheKey(RenderRequest request,
                                            TopModel m,
                                            ControllerContext context)
    {
        
        SynchronizedUpdateCache cache = getSynchronizedUpdateCache(context);
        
        Model contentModel = context.getContentModel();
        String url = (String) ModelPathUtil.get(contentModel, "url/value");
        
        RssFeed rssFeed = null;
        
        CacheKey cacheKey = new CacheKey(RenderControllerRssElement.class, url);
        try {
            rssFeed = (RssFeed) cache.get(cacheKey);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Failed to look up cache result for key: " + cacheKey, e);
        }
        
        if (rssFeed == null) {
            RssFeedReader rssFeedReader = new RssFeedReader(new RssParser());
            try {
                rssFeed = rssFeedReader.getFeed(url);
            } catch (RssFeedException e) {
                logger.log(Level.WARNING, "Failed to read RSS Feed on URL: " + url
                    + " - " + e.getMessage()
                    + e.getCause() != null ? " - " + e.getCause().getMessage() : "");
                logger.log(Level.FINEST, "Failed to read RSS Feed on URL: " + url, e);
            } finally {
                if (rssFeed != null) {
                    cache.put(cacheKey, rssFeed, SOFT_TIMEOUT);
                } else {
                    rssFeed = (RssFeed) cache.release(cacheKey, 2 * 1000);
                }
            }
        }
        
        m.getLocal().setAttribute("rssFeed", rssFeed);
        
        int maxChars = 44;
        String maxCharsValue = (String) ModelPathUtil.get(contentModel, "maxChars/value");
        if (maxCharsValue != null) {
          try {
            maxChars = Integer.parseInt(maxCharsValue);
          } catch (NumberFormatException e) {
          }
        }
        m.getLocal().setAttribute("maxChars", maxChars);
    }

    private SynchronizedUpdateCache getSynchronizedUpdateCache(
            ControllerContext context) {
        Application application = context.getApplication();
        SynchronizedUpdateCache cache = 
            (SynchronizedUpdateCache) application.getApplicationComponent(LRUSynchronizedUpdateCache.DEFAULT_COMPOUND_NAME);
        return cache;
    }
}
