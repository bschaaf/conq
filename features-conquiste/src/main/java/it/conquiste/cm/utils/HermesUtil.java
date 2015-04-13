package it.conquiste.cm.utils;

import it.conquiste.cm.standard.article.ArticlePolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;

import com.atex.standard.image.ImagePolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.management.ServiceNotAvailableException;
import com.polopoly.metadata.Dimension;
import com.polopoly.metadata.Entity;
import com.polopoly.metadata.Metadata;
import com.polopoly.search.solr.SearchClient;



public class HermesUtil {
    public enum HermesCustomField {
        securityParentId,
        PQ, // insert into which slot
        Categoria, //subject category
        caption, // caption for image
        Edizione, // security parent by name . This field has more priority than securityParentId
        hermesPk,
        article_hermesQuote,
        UGC,
        Titolo_Url,
        article_hermesOvertitle, 
    };

    public static String KEY_PG_NONE = "None";
    public static String KEY_PK_WIRE_PREFIX = "HermesPkWire";

    private ContentRead getPageMatchedHermesKey(String hermesDepartmentKey, PolicyCMServer cmServer) throws CMException{
        if (hermesDepartmentKey==null){
            return null;
        }
        List<ContentRead> pageList = PolopolyUtil.getPageStructure(new ExternalContentId("p.siteengine.Sites.d"), cmServer);
        for (ContentRead page:pageList){
            if (hermesDepartmentKey.equalsIgnoreCase(page.getComponent("hermesDepartmentKey", "value"))){
                return page;
            }
        }
        for (ContentRead page:pageList){
            if (hermesDepartmentKey.equalsIgnoreCase(page.getName())){
                return page;
            }
        }
        return null;
    }

    public void applyCustomFields(ContentPolicy policy, Map<String, String> customAttributes, PolicyCMServer cmServer ,SearchClient solrClient ) throws CMException{

        ExternalContentId parentExternalId = null;
        // assign the right security parent
        if (customAttributes.containsKey(HermesCustomField.Edizione.toString())){
            ContentRead possibleParent = getPageMatchedHermesKey(customAttributes.get(HermesCustomField.Edizione.toString()), cmServer);
            if (possibleParent!=null){
                parentExternalId =possibleParent.getExternalId();
                policy.setSecurityParentId(parentExternalId);
            }else if (customAttributes.containsKey(HermesCustomField.securityParentId.toString())){
                parentExternalId =new ExternalContentId(customAttributes.get(HermesCustomField.securityParentId.toString()));
                policy.setSecurityParentId(parentExternalId);
            }
        }else{
            if (customAttributes.containsKey(HermesCustomField.securityParentId.toString())){
                parentExternalId =new ExternalContentId(customAttributes.get(HermesCustomField.securityParentId.toString()));
                policy.setSecurityParentId(parentExternalId);
            }
        }
        
        //Assign the "tag"
        if (customAttributes.containsKey(HermesCustomField.article_hermesQuote.toString())){
            Metadata metadata = ((ArticlePolicy)policy).getMetadata();
            String[] values =customAttributes.get(HermesCustomField.article_hermesQuote.toString()).split(",");

            Dimension dimension = metadata.getDimensionById("dimension.Tag");
            if (dimension == null ){
                dimension = new Dimension("dimension.Tag","Tag",false);
            }

            List<Entity> entityList = dimension.getEntities();

            if (entityList==null){
                entityList = new ArrayList<Entity>();
            }
            for (String value:values){
                Entity tag = new Entity(value.trim(), value.trim());
                entityList.add(tag);
            }

            dimension.setEntities(entityList);

            List<Dimension> dimensionList = metadata.getDimensions();
            dimensionList.add(dimension);
            metadata.setDimensions(dimensionList);

            ((ArticlePolicy)policy).setMetadata(CategorizationHelper.updateMetadata(metadata, ((ArticlePolicy)policy).getMetadata()));
        }
        
        
        //Set overtitle
        if (customAttributes.containsKey(HermesCustomField.article_hermesOvertitle.toString())){
        	String overtitle = customAttributes.get(HermesCustomField.article_hermesOvertitle.toString());
        	((ArticlePolicy)policy).setOvertitle(overtitle);
        }
        
/*
        //Assign the "subject"
        if (customAttributes.containsKey(HermesCustomField.Categoria.toString())){
            Metadata metadata = ((ArticlePolicy)policy).getMetadata();

            List<Entity> entityList = new ArrayList<Entity>();
            entityList.add(PolopolyUtil.getCategoryEntityTreeByName("department.categorydimension.subject", customAttributes.get(HermesCustomField.Categoria.toString()), cmServer));

            Dimension dimension = new Dimension("department.categorydimension.subject","Subject",false);
            dimension.setEntities(entityList);

            List<Dimension> dimensionList = metadata.getDimensions();
            dimensionList.add(dimension);
            metadata.setDimensions(dimensionList);

            ((ArticlePolicy)policy).setMetadata(CategorizationHelper.updateMetadata(metadata, ((ArticlePolicy)policy).getMetadata()));
        }

        //Assign the "tag"
        if (customAttributes.containsKey(HermesCustomField.article_hermesQuote.toString())){
            Metadata metadata = ((ArticlePolicy)policy).getMetadata();
            String[] values =customAttributes.get(HermesCustomField.article_hermesQuote.toString()).split(",");

            Dimension dimension = metadata.getDimensionById("department.categorydimension.tag.Tag");
            if (dimension == null ){
                dimension = new Dimension("department.categorydimension.tag.Tag","Tag",false);
            }

            List<Entity> entityList = dimension.getEntities();

            if (entityList==null){
                entityList = new ArrayList<Entity>();
            }
            for (String value:values){
                Entity tag = new Entity(value.trim(), value.trim());
                entityList.add(tag);
            }

            dimension.setEntities(entityList);

            List<Dimension> dimensionList = metadata.getDimensions();
            dimensionList.add(dimension);
            metadata.setDimensions(dimensionList);

            ((ArticlePolicy)policy).setMetadata(CategorizationHelper.updateMetadata(metadata, ((ArticlePolicy)policy).getMetadata()));
        }*/
    }



    private void applyCaptionToImage(ContentList contentList, String caption , PolicyCMServer cmServer) throws CMException{
        if (contentList==null){
            return;
        }

        ListIterator<ContentReference> iterator = contentList.getListIterator();
        while (iterator.hasNext()){
            ContentId contentId = iterator.next().getReferredContentId();
            Policy imagePolicy = cmServer.getPolicy(contentId);
            ImagePolicy newImagePolicy = (ImagePolicy)cmServer.createContentVersion(imagePolicy.getContentId());
            newImagePolicy.setName(caption);
            cmServer.commitContent(newImagePolicy);
        }

    }
}
