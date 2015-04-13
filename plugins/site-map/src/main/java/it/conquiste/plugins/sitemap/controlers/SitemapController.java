package it.conquiste.plugins.sitemap.controlers;

import it.conquiste.plugins.sitemap.policies.SitemapConfigFieldPolicy;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.baseline.collection.PublishingQueuePolicyBase;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

public class SitemapController extends RenderControllerBase {

	private static final Logger LOGGER = Logger.getLogger(SitemapController.class.getName());

	private static final String newsSiteMapMode = "news_sitemap";
	public static final String CONFIG_EXT_ID = "plugins.it.conquiste.site-map.Config";

	/**
	 * {@inheritDoc}
	 */
	public void populateModelAfterCacheKey(RenderRequest request, TopModel m, CacheInfo cacheInfo, ControllerContext context) {
		super.populateModelAfterCacheKey(request, m, cacheInfo, context);
		ModelWrite localModel = m.getLocal();

		String siteMapMode = (String) request.getAttribute("mode");
		if (newsSiteMapMode.equals(siteMapMode)) {
			localModel.setAttribute("sitemapMode", newsSiteMapMode);
		}
	}

	protected void addSitemapConfigToModel(TopModel m, ModelWrite model, ControllerContext context) {
		PolicyCMServer policyCMServer = getCmClient(context).getPolicyCMServer();
		try {

			SitemapConfigFieldPolicy policy = (SitemapConfigFieldPolicy) policyCMServer.getPolicy(new ExternalContentId(CONFIG_EXT_ID));
			model.setAttribute("manual", policy.isManual());
			model.setAttribute("manualXML", policy.getManualXML());
			model.setAttribute("publishingQueues", policy.isPublishingQueues());
			model.setAttribute("googleSiteMap", policy.isGoogleSiteMap());

			String siteMapMode = (String) model.getAttribute("sitemapMode");
			if (newsSiteMapMode.equals(siteMapMode)) {
				model.setAttribute("googleNewsSitemapPublishingQueue",
						getContentList(policy, "googleNewsSitemapPublishingQueue", m, policyCMServer));
			} else {
				model.setAttribute("sitemapPublishingQueue", getContentList(policy, "sitemapPublishingQueue", m, policyCMServer));
			}
		} catch (CMException e) {
			LOGGER.log(Level.WARNING, "Failed to read Site map configuration.", e);
		}

	}

	private ContentList getContentList(SitemapConfigFieldPolicy policy, String childName, TopModel m, PolicyCMServer policyCMServer)
			throws CMException {
		ContentList queue = policy.getContentList(childName);
		for (int i = 0; i < queue.size(); i++) {
			ContentId queueId = queue.getEntry(i).getReferredContentId();
			PublishingQueuePolicyBase queuePolicy = (PublishingQueuePolicyBase) policyCMServer.getPolicy(queueId);
			ContentId securityParentId = queuePolicy.getSecurityParentId();
			if (securityParentId.equalsIgnoreVersion(m.getContext().getSite().getContent().getId())) {
				ContentList contentList = queuePolicy.getContentList();
				return contentList;
			}
		}
		return null;
	}

}