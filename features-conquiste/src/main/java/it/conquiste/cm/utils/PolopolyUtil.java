package it.conquiste.cm.utils;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.search.categorization.dimension.tree.CategoryTreePolicy;
import com.polopoly.cm.app.search.categorization.dimension.tree.TreeCategory;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.metadata.Entity;
import com.polopoly.model.Model;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.structure.SitePolicy;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PolopolyUtil {

    private static Logger LOG = Logger.getLogger(PolopolyUtil.class.getName());

    public static SitePolicy getSite(PolicyCMServer CMServer, ContentId securityParentId) {
        ContentPolicy content = null;

        try {
            content = (ContentPolicy) CMServer.getPolicy(securityParentId);
            int i = 0; //Used to avoid potential infinite loops
            while (!(content instanceof SitePolicy) && i < 30) {
                i++;
                if (content.getSecurityParentId() == null)
                    break;
                content = (ContentPolicy) CMServer.getPolicy(content.getSecurityParentId());
            }
        } catch (CMException e) {
            LOG.log(Level.WARNING, e.getLocalizedMessage(), e);
        }

        if (content instanceof SitePolicy) {
            return (SitePolicy) content;
        } else {
            return null;
        }
    }
    
    public static List<ContentRead> getPageStructure(ContentId startContentId,PolicyCMServer cmServer) throws CMException{
      List<ContentRead> list = new ArrayList<ContentRead>();
      getPageStructure(startContentId, cmServer,list);
      return list;
    }

    private static void getPageStructure(ContentId startContentId,PolicyCMServer cmServer, List<ContentRead> list) throws CMException{
      ContentRead pageOrSite = cmServer.getContent(startContentId);
      list.add(pageOrSite);
      ContentList pages = pageOrSite.getContentList("polopoly.Department");
      ListIterator<ContentReference> iterator = pages.getListIterator();
      while (iterator.hasNext()) {
        ContentId contentId = iterator.next().getReferredContentId();
        getPageStructure(contentId,cmServer,list);
      }
    }
 
    private static Entity getCategoryEntityTreeByName(List<TreeCategory> treeCategories, String name, PolicyCMServer cmServer, int level) throws CMException{
      if (level>5){
          throw new CMException("reach max");
      }
      for (TreeCategory node: treeCategories ){
          if (node.getName().equalsIgnoreCase(name)){
              return new Entity(node.getExternalId().getExternalId(),node.getName());
          }else{
              List<TreeCategory> list = node.getChildCategories();
              if (list!=null && list.size()>0){
                  Entity result = getCategoryEntityTreeByName(list, name, cmServer,level);
                  if (result !=null ){
                      return new Entity(node.getExternalId().getExternalId(),node.getName(),result);
                  }
              }
          }
      }
      return null;
    }

    public static Entity getCategoryEntityTreeByName(String categoryTypeExternalId, String name, PolicyCMServer cmServer) throws CMException{
      CategoryTreePolicy parent = (CategoryTreePolicy)cmServer.getPolicy(new ExternalContentId(categoryTypeExternalId));
      List<TreeCategory> list = parent.getChildCategories();
      return getCategoryEntityTreeByName(list, name, cmServer,0);
    }

    public static List<Map<String, Object>> getModelMap(ContentList contentList, ControllerContext context) throws CMException {
        List<Map<String, Object>> models = new ArrayList<>();

        ListIterator<ContentReference> iterator =
                contentList.getListIterator();

        while (iterator.hasNext()) {
            ContentReference ref = iterator.next();
            Model model;

            try {
                model = context.getModelProvider().getModel(ref.getReferredContentId());
            } catch (Exception e) {
                continue;
            }

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("content", model);
            models.add(map);
        }
        return models;
    }
}
