package com.atex.gong.plugins.pagelayout;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.policy.Policy;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.model.request.ContentPath;
import com.polopoly.siteengine.mvc.RenderControllerBase;

public class PageLayoutRenderController extends RenderControllerBase {

    private static final Logger LOGGER = Logger.getLogger(PageLayoutRenderController.class.getName());

    @Override
    public void populateModelBeforeCacheKey(final RenderRequest request,
                                            final TopModel topModel,
                                            final ControllerContext context) {
    	
    	String applixUser = (String)((HttpServletRequest) request).getSession().getAttribute("applixLogin");
    	topModel.getGlobal().setChild("applixUser", applixUser);
        List<Model> pagePath = new ArrayList<Model>();
        for (ContentId pathId : topModel.getContext().getPage().getContentPath()) {
            try {
                pagePath.add(context.getModelProvider().getModel(pathId));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error getting model", e);
            }
        }
        topModel.getLocal().setChild("pagePath", pagePath);
        List<Model> topPages = new ArrayList<Model>();
        ListIterator<ContentReference> iterator =
                topModel.getContext().getSite().getBean().getSubPages().getListIterator();
        while (iterator.hasNext()) {
            ContentReference ref = iterator.next();
            try {
                topPages.add(context.getModelProvider().getModel(ref.getReferredContentId()));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error getting model", e);
            }
        }
        topModel.getLocal().setChild("topPages", topPages);

        checkGallery(request, topModel, context);
    }
   
    protected void checkGallery(final RenderRequest request,
            final TopModel topModel,
            final ControllerContext context) {
        try {
            ContentPath pathAfterPage = topModel.getContext().getPage().getPathAfterPage();
            if (!pathAfterPage.isEmpty()) {
                ContentId contentId = pathAfterPage.get(0);
                Model contentModel = context.getContentModel();
                Policy policy = (Policy) contentModel.getAttribute("_data");
                ContentRead content = policy.getCMServer().getContent(contentId);
                ContentId inputTemplateId = content.getInputTemplateId();
                ContentRead itContent = policy.getCMServer().getContent(inputTemplateId);
                if ("com.atex.plugins.image-gallery.MainElement".equals(itContent.getName())) {
                    ModelWrite model = topModel.getLocal();
                     ModelPathUtil.set(model, "isGalleryDetail", true);
                }
            }
        } catch (CMException e) {
            LOGGER.log(Level.WARNING, "Error checking gallery", e);
        }
    }

}
