package it.conquiste.plugins.sitemap.controlers;

import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;

import com.polopoly.model.ModelWrite;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.model.context.PageScope;
import com.polopoly.siteengine.model.context.SiteScope;
import com.polopoly.siteengine.model.context.StructureScope;
import com.polopoly.siteengine.structure.Page;

public class GoogleNewsSitemapController extends SitemapController {

	private static final String DEFAULT_PUBLICATION_NAME = "";

	/**
	 * {@inheritDoc}
	 */
	public void populateModelAfterCacheKey(RenderRequest request, TopModel m, CacheInfo cacheInfo, ControllerContext context) {
		super.populateModelAfterCacheKey(request, m, cacheInfo, context);
		ModelWrite localModel = m.getLocal();
		StructureScope siteOrPage = m.getContext().getPage();
		siteOrPage = siteOrPage != null ? siteOrPage : m.getContext().getSite();
		Page pagePolicy = null;
		// Fetch model
		if (siteOrPage instanceof SiteScope) {
			pagePolicy = (Page) (((SiteScope) siteOrPage).getBean());
		} else if (siteOrPage instanceof PageScope) {
			pagePolicy = (Page) (((PageScope) siteOrPage).getBean());
		}
		addSitemapConfigToModel(m, localModel, context);
		if (pagePolicy != null) {
			String siteName = pagePolicy.getName();
			localModel.setAttribute("timezone", TimeZone.getTimeZone("UTC"));
			localModel.setAttribute("publicationName", StringUtils.isEmpty(siteName) ? DEFAULT_PUBLICATION_NAME : siteName);
		}
	}
}
