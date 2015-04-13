package com.atex.plugins.rssfeed;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.onecms.ws.image.ImageServiceUrlBuilder;
import com.polopoly.cm.client.CmClient;
import org.apache.commons.lang.StringUtils;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Entity;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataAware;
import com.polopoly.metadata.util.MetadataUtil;
import com.polopoly.metadata.util.MetadataUtil.Filtering;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.standard.feed.FeedProvider;

public class RssFeedRenderController extends RssImageController {

    private static final Logger LOG = Logger.getLogger(RssFeedRenderController.class.getName());

    private static final String CDATA = "<![CDATA[%s]]>";
    private static final Object LOCK = new Object();
    private static final TimeZone standardTimeZone = TimeZone.getTimeZone("UTC Universal");

    private final SimpleDateFormat rssDateFormatter = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    public RssFeedRenderController() {
        super();
        rssDateFormatter.setTimeZone(standardTimeZone);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void populateModelAfterCacheKey(RenderRequest request, TopModel m, CacheInfo cacheInfo,
            ControllerContext context) {

        super.populateModelAfterCacheKey(request, m, cacheInfo, context);

        Date lastBuildDate = null;

        Model localModel = m.getLocal();
        Model thisModel = localModel.getModel("content");

        // Retrieve the selected categories for the feed
        MetadataAware categorizationProvider = (MetadataAware) ModelPathUtil.getBean(thisModel, "categories");
        Set<String> entities = getFeedEntities(categorizationProvider.getMetadata());

        // Get the items from the publishing Queues and/or articles
        List<Model> contents = (List<Model>) ModelPathUtil.get(thisModel, "publishingQueues/list");
        contents = (List<Model>) (contents != null ? contents : Collections.emptyList());
        List<RssItem> rssItems = new ArrayList<RssItem>();

        int maxLength = (ModelPathUtil.get(thisModel, "length/value") != null) ? Integer.valueOf(
                ModelPathUtil.get(thisModel, "length/value").toString()).intValue() : 0;

        for (Model content : contents) {
            Object bean = ModelPathUtil.getBean((Model) content, "content");
            if (!(bean instanceof Policy)) {
                LOG.log(Level.FINE, "Encountered invalid content in feed", bean);
                continue;
            }

            Policy articleOrQueue = (Policy) bean;
            if (articleOrQueue instanceof Rssable) {
                lastBuildDate = prepareFeedItem((Rssable) articleOrQueue, rssItems, lastBuildDate, m, getCmClient(context));
            } else if (articleOrQueue instanceof FeedProvider) {
                try {
                    List<Rssable> feedables = ((FeedProvider) articleOrQueue).getFeedables();
                    feedables = (List<Rssable>) (feedables != null ? feedables : Collections.emptyList());
                    for (Rssable rssable : feedables) {
                        lastBuildDate = prepareFeedItem(rssable, rssItems, lastBuildDate, m, getCmClient(context));
                    }
                } catch (CMException e) {
                    LOG.log(Level.WARNING, "Unable to get data from feed provider", e);
                }

            }
        }

        // Sort the items based on pub date and make sure there are no duplicate entries
        Set<RssItem> unique = new HashSet<RssItem>();
        unique.addAll(rssItems);
        rssItems.clear();
        rssItems.addAll(unique);
        synchronized (LOCK) { // Required due to a bug in at least java 1.5
            Collections.sort(rssItems);
        }

        if (maxLength > 0 && rssItems.size() > maxLength) {
            rssItems = rssItems.subList(0, maxLength);
        }

        // Append CDATA to name
        ModelPathUtil.set(localModel, "title", wrapWithCDATA((String) ModelPathUtil.get(thisModel, "name")));

        // Append CDATA to description
        ModelPathUtil.set(localModel, "description",
                wrapWithCDATA((String) ModelPathUtil.get(thisModel, "description/value")));

        // Add computed last build date to model, fallback to todays date
        if (lastBuildDate == null) {
            lastBuildDate = new Date();
        }

        // Add last build date
        ModelPathUtil.set(localModel, "lastBuildDate", rssDateFormatter.format(lastBuildDate));

        // Add the categories
        ModelPathUtil.set(localModel, "categories", entities);

        // Add the image of the feed
        ContentId imageId = (ContentId) ModelPathUtil.get(thisModel, "imageSelect/contentId");
        populateModelWithImage(imageId, m, context, getCmClient(context));

        // Add the items to the model
        ModelPathUtil.set(localModel, "rssItems", rssItems);
    }

    protected Set<String> getFeedEntities(Metadata metadata) {
        Set<String> entities = new LinkedHashSet<String>();
        for (Dimension dimension : metadata.getDimensions()) {
            if (!dimension.isEnumerable()) {
                continue;
            }
            Iterable<List<Entity>> paths = MetadataUtil.traverseEntityPaths(dimension, Filtering.ONLY_LEAVES);
            for (List<Entity> path : paths) {
                StringBuilder category = new StringBuilder();
                String separator = "";
                for (Entity entity : path) {
                    category.append(separator);
                    category.append(entity.getName());
                    separator = "/";
                }
                entities.add(wrapWithCDATA(category.toString()));
            }
        }
        return entities;
    }

    protected Date prepareFeedItem(Rssable rssable, List<RssItem> rssItems, Date lastBuildDate, TopModel topModel, CmClient cmClient) {
        Date itemPublishedDate = rssable.getItemPublishedDate();
        ContentId contentId = rssable.getItemContentId();
        // Update last build date of the feed
        if (lastBuildDate == null || (itemPublishedDate != null && lastBuildDate.before(itemPublishedDate))) {
            lastBuildDate = itemPublishedDate;
        }

        // Make sure there are no bad characters in the title or
        String appendCDATA = wrapWithCDATA(rssable.getItemTitle());

        // description which can break the feed
        String appendCDATA2 = wrapWithCDATA(rssable.getItemDescription());

        String contentIdString = contentId.getContentId().getContentIdString();
        String pubDate = itemPublishedDate != null ? rssDateFormatter.format(itemPublishedDate) : "";
        ContentId imageId = rssable.getReferredImageId();
        ImageServiceUrlBuilder imageUrlBuilder = getImageUrlBuilder(imageId, topModel, cmClient);
        RssItem rssItem = new RssItem(appendCDATA, appendCDATA2, pubDate, contentIdString, rssable.getItemParentIds(), imageUrlBuilder);
        rssItems.add(rssItem);
        return lastBuildDate;
    }

    private String wrapWithCDATA(String str) {
        if (!StringUtils.isEmpty(str)) {
            return String.format(CDATA, str);
        }
        return "";
    }
}
