package it.conquiste.cm.util.slot;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.DefaultMajorNames;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policymvc.PolicyModelDomain;
import com.polopoly.model.Model;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

import it.conquiste.cm.teaser.TeaserPolicy;
import it.conquiste.cm.standard.article.ArticlePolicy;

public class SlotUtil {

    public static TeaserPolicy createTeaser(PolicyCMServer cmServer, ContentId securityParentContentId, ContentId articleContentId) throws CMException {
        int layoutElementMajor = cmServer.getMajorByName(DefaultMajorNames.LAYOUTELEMENT);
        TeaserPolicy teaser = (TeaserPolicy)cmServer.createContent(layoutElementMajor, securityParentContentId, TeaserPolicy.TEASER_INPUT_TEMPLATE_ID);
        teaser.setArticleId(articleContentId);
        ArticlePolicy article = (ArticlePolicy) cmServer.getPolicy(articleContentId);
        teaser.setOvertitle(article.getOvertitle());
        teaser.setName(article.getContent().getName());
        teaser.setText(article.getLead());
        ContentId teaserImage = article.getReferredImageId();
        ContentList images = teaser.getContentList("images");
        if(teaserImage != null && images.size() == 0){
    	  images.add(0, new ContentReference(teaserImage));
        }
        cmServer.commitContent(teaser);
        return teaser;
    }

    public static Integer getMaxTeasersForSlot(Policy siteOrPagePolicy, String slotName) throws CMException {
        String temp  = siteOrPagePolicy.getContent().getComponent("pageLayout/selected/maxTeasers_"+slotName,"value");
        if (temp == null || temp.trim().length() <= 0) {
            return 15;
        }
        return Integer.valueOf(temp);
    }

    public static void addArticleTeaserToSlot(PolicyCMServer cmServer, String slotName, int position, ContentId pageOrSiteContentId, ContentId articleContentId) throws CMException {
        if (pageOrSiteContentId == null) {
            return;
        }
        ContentPolicy pagePolicy = (ContentPolicy)cmServer.getPolicy(pageOrSiteContentId);
        pagePolicy = (ContentPolicy)cmServer.createContentVersion(pagePolicy.getContentId());
        addArticleTeaserToSlot(cmServer, slotName, position, pagePolicy, articleContentId, null);
        cmServer.commitContent(pagePolicy);
    }

    private static void addArticleTeaserToSlot(PolicyCMServer cmServer,String slotName, int position, ContentPolicy pageOrSiteContentPolicy, ContentId articleContentId, ContentReference teaserReference) throws CMException {
        if (articleContentId == null) {
            return;
        }
        if (slotName == null) {
            return;
        }

        ContentList contentList = pageOrSiteContentPolicy.getContentList("pageLayout/selected/"+slotName+"/slotElements");

        TreeMap<Integer, ContentReference> existingTeaserInfo = getExistingTeaserInfo(cmServer, contentList, articleContentId);

        if (existingTeaserInfo != null) {
            int currentPosition = existingTeaserInfo.firstEntry().getKey();

            if( currentPosition == position) {
                //If teaser already in same position in the slot, do nothing.
                return;
            } else {
                //Remove reference in current position and replace in new.
                ContentReference reference = existingTeaserInfo.firstEntry().getValue();
                contentList.remove(currentPosition);

                if(position != -1) { //-1 used to remove teaser but not add again (this would be 0 in the GUI)
                    contentList.add(position, reference);
                }
                return;
            }
        }

        contentList = pageOrSiteContentPolicy.getContentList("pageLayout/selected/"+slotName+"/slotElements");

        if (teaserReference == null) {
            //Create teaser
            TeaserPolicy teaser = SlotUtil.createTeaser(cmServer, pageOrSiteContentPolicy.getContentId().getContentId(), articleContentId);
            teaserReference = new ContentReference(teaser.getContentId().getContentId());
        }
        contentList.add(position, teaserReference);
    }

    private static TreeMap<Integer, ContentReference> getExistingTeaserInfo(PolicyCMServer cmServer, ContentList contentList, ContentId articleContentId) throws CMException {
        ListIterator<ContentReference> iterator = contentList.getListIterator();

        while(iterator.hasNext()) {
            ContentReference contentReference = iterator.next();
            Policy policy = cmServer.getPolicy(contentReference.getReferredContentId());

            if(policy instanceof TeaserPolicy) {
                TeaserPolicy teaserPolicy = (TeaserPolicy)policy;

                if (teaserPolicy != null && teaserPolicy.getArticleId()!= null && teaserPolicy.getArticleId().getContentId().equals(articleContentId.getContentId())) {
                    TreeMap<Integer, ContentReference> posAndRef = new TreeMap<Integer, ContentReference>();
                    posAndRef.put(iterator.nextIndex() - 1, contentReference);
                    return posAndRef;
                }
            }
        }
        return null;
    }

    public static List<List<Model>> getTeasersFromSlot(PolicyCMServer CMServer, ControllerContext context,
                                                        PolicyModelDomain domain, TopModel m, String slot) throws CMException {
        List<List<Model>> teasers = new ArrayList<>();
        List<Model> openingBlock = new ArrayList<>();
        List<Model> block1 = new ArrayList<>();
        List<Model> block2 = new ArrayList<>();
        List<Model> block3 = new ArrayList<>();

        ContentPolicy pageOrSite = (ContentPolicy) CMServer.getPolicy(context.getContentId());

        ContentList list = pageOrSite.getContentList("pageLayout/selected/" + slot + "/slotElements");

        int openingTeaserBlockFinish = 
                SlotUtil.getNumberOfOpeningTeasers(SlotUtil.getOpeningTeasersLayout(pageOrSite)) - 1;
        int teaserBlock1Start; int teaserBlock1Finish;
        int teaserBlock2Start; int teaserBlock2Finish;
        int teaserBlock3Start; int teaserBlock3Finish;

        teaserBlock1Start = openingTeaserBlockFinish + 1;
        teaserBlock1Finish = teaserBlock1Start + getMaxTeasersForBlock(pageOrSite, "maxTeasers_block1") - 1;
        teaserBlock2Start = teaserBlock1Finish + 1;
        teaserBlock2Finish = teaserBlock2Start + getMaxTeasersForBlock(pageOrSite, "maxTeasers_block2") - 1;
        teaserBlock3Start = teaserBlock2Finish + 1;
        teaserBlock3Finish = teaserBlock3Start + getMaxTeasersForBlock(pageOrSite, "maxTeasers_block3") - 1;

        int maxTotalTeasers = Math.min(list.size(), SlotUtil.getMaxTeasersForSlot(pageOrSite, "first-left-left"));

        int count = 0;
        for(int i=0; i < maxTotalTeasers; i++) {
            ContentId teaserId = list.getEntry(i).getReferredContentId();
            Policy policy = CMServer.getPolicy(teaserId);

            if(policy instanceof  TeaserPolicy) {
                TeaserPolicy teaserPolicy = (TeaserPolicy) policy;

               // if(teaserPolicy.hasVisible() || m.getRequest().getPreview().isInPreviewMode()) {

                    Model teaserModel = domain.getModel(teaserId);
                    if(count >= 0 && count <= openingTeaserBlockFinish) {
                        openingBlock.add(teaserModel);

                    } else if(count >= teaserBlock1Start && count <= teaserBlock1Finish) {
                        block1.add(teaserModel);

                    } else if(count >= teaserBlock2Start && count <= teaserBlock2Finish) {
                        block2.add(teaserModel);

                    } else if(count >= teaserBlock3Start && count <= teaserBlock3Finish) {
                        block3.add(teaserModel);
                    }
                    count++;
               // }
            }
        }

        teasers.add(openingBlock);
        teasers.add(block1);
        teasers.add(block2);
        teasers.add(block3);
        return teasers;
    }

    public static String getOpeningTeasersLayout(ContentPolicy pageOrSite) throws CMException {

        SingleValuePolicy types = (SingleValuePolicy) pageOrSite.getChildPolicy("pageLayout")
                .getChildPolicy("selected").getChildPolicy("openingTeasersLayout");

        return types != null ? types.getValue() : "type1" ;
    }

    public static int getNumberOfOpeningTeasers(String type) {

        if(type != null)
            switch (type) {
                case "type1": return 3;
                case "type2": return 4;
                case "type3": return 1;
            }
        return 3;
    }

    public static int getMaxTeasersForBlock(ContentPolicy pageOrSite, String block) throws CMException {

        SingleValuePolicy max = (SingleValuePolicy) pageOrSite.getChildPolicy("pageLayout")
                .getChildPolicy("selected").getChildPolicy(block);

        if(max != null) {
            return Integer.parseInt(max.getValue());
        }
        return 5;
    }
}
