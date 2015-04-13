package com.atex.plugins.imagegallery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.util.SuffixFileNameFilter;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.collections.ComponentMap;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListUtil;
import com.polopoly.cm.collections.ReadOnlyComponentMap;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.gui.orchid.util.FileFilter;
import com.polopoly.unified.content.MediaGalleryAware_v1;
import com.polopoly.unified.content.MediaGallery_v1;
import com.polopoly.unified.content.MediaReference_v1;
import com.polopoly.unified.content.RepresentationApplicationFailedException;
import com.polopoly.unified.content.RepresentationFactory;
import com.polopoly.unified.content.RepresentationNotCompatibleException;
import com.polopoly.unified.content.RepresentationNotFoundException;

/**
 * MediaGalleryAwareAdapter.
 */
public class MediaGalleryAwareAdapter<T extends ImageGalleryPolicy>
        implements MediaGalleryAware_v1
{
    private static final Logger LOGGER = Logger.getLogger(MediaGalleryAwareAdapter.class.getName());
    private static final String[] ACCEPTED_EXTENSIONS = {"jpg", "jpeg", "png", "gif" , "zip"};
    private static final FileFilter FILE_FILTER = new SuffixFileNameFilter(Arrays.asList(ACCEPTED_EXTENSIONS), true);

    public static final String CONTENT_LIST_NAME = "images";
    public static final String CONTENT_LIST_NAME_PQ = "publishingQueue";

    private final T policy;

    public MediaGalleryAwareAdapter(final T policy) {
        this.policy = policy;
    }


    @Override
    public void applyRepresentation(final MediaGallery_v1 mediaGallery)
            throws RepresentationApplicationFailedException, RuntimeException {

        try {
            validateMedia(mediaGallery.getMediaReferences());

            policy.setName(mediaGallery.getName());
            //setComponent("description", "value", mediaGallery.getDescription());
            policy.setDescription(mediaGallery.getDescription());
            
            ContentList publishingQueueList = getContentList(CONTENT_LIST_NAME_PQ);
            ContentList images = getContentList(CONTENT_LIST_NAME);
            int size = images.size();
            ContentListUtil.clear(images);
            Policy publishingQueue = null;
            if (publishingQueueList.size() > 0) {
                ContentId publishingQueueId = publishingQueueList.getEntry(0).getReferredContentId();
                publishingQueue =
                        getCMServer().createContentVersion(getCMServer().getPolicy(publishingQueueId).getContentId());
                images = publishingQueue.getContent().getContentList();
                size = images.size();
                ContentListUtil.clear(images);
            }
            for (int i = 0; i < size; ++i) {
                setComponent("images", i + ":caption", null);
            }
            int i = 0;
            for (MediaReference_v1 reference : mediaGallery.getMediaReferences()) {
                ContentId referredContentId = reference.getId();
                if (referredContentId instanceof ExternalContentId) {
                    ExternalContentId externalContentId = (ExternalContentId) referredContentId;
                    referredContentId = getCMServer().findContentIdByExternalId(externalContentId);
                    if (referredContentId == null) {
                        throw new RepresentationApplicationFailedException(HttpServletResponse.SC_BAD_REQUEST,
                                "No such external ID: "
                                        + externalContentId.getExternalId());
                    }
                }
                referredContentId = referredContentId.getContentId();

                String caption = reference.getCaption();
                images.add(i, new ContentReference(referredContentId, null));
                if (caption != null) {
                    setComponent("images", i + ":caption", caption);
                }
                i++;
            }
            Map<String, String> customAttributes = new ComponentMap(policy, "custom");
            customAttributes.clear();
            customAttributes.putAll(mediaGallery.getCustomAttributes());
            if (publishingQueue != null) {
                getCMServer().commitContent(publishingQueue);
            }
        } catch (CMException e) {
            throw new CMRuntimeException(e);
        }
    }

    @Override
    public MediaGallery_v1 getRepresentation(final MediaGallery_v1 mediaGallery, final RepresentationFactory factory)
            throws RepresentationApplicationFailedException, RuntimeException {

        try {
            mediaGallery.setName(policy.getName());
            //mediaGallery.setDescription(getComponent("description", "value"));
            mediaGallery.setDescription(policy.getDescription());
            Collection<ContentId> contentList = policy.getImages();
            ArrayList<MediaReference_v1> references = new ArrayList<>();
            Iterator<ContentId> iterator = contentList.iterator();
            int idx = 0;
            while (iterator.hasNext()) {
                ContentId imageId = iterator.next();
                MediaReference_v1 reference = factory.createEmptyRepresentation(MediaReference_v1.class);
                reference.setCaption(getComponent("images", idx + ":caption"));
                reference.setId(imageId.getContentId());
                references.add(reference);
                idx++;
            }
            mediaGallery.setMediaReferences(references);
            Map<String, String> customAttributes = new ReadOnlyComponentMap(policy, "custom");
            mediaGallery.setCustomAttributes(customAttributes);
        } catch (CMException e) {
            throw new CMRuntimeException(e);
        }

        return mediaGallery;
    }

    private void validateMedia(final List<MediaReference_v1> media) {
        for (MediaReference_v1 reference : media) {
            try {
                reference.getMedia();
            } catch (RepresentationNotFoundException | RepresentationNotCompatibleException e) {
                throw new RepresentationApplicationFailedException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            }
        }
    }

    private ContentList getContentList(final String contentListName) throws CMException {
        return policy.getContentList(contentListName);
    }

    private void setComponent(final String group, final String name, final String value) throws CMException {
        policy.setComponent(group, name, value);
    }

    private String getComponent(final String group, final String name) throws CMException {
        return policy.getComponent(group, name);
    }

    private PolicyCMServer getCMServer() {
        return policy.getCMServer();
    }
}
