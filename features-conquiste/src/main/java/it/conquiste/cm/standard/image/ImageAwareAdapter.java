package it.conquiste.cm.standard.image;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import com.atex.onecms.content.Content;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.SubjectUtil;
import com.atex.onecms.image.ImageInfoAspectBean;
import com.atex.onecms.ws.image.ImageServiceConfigurationProvider;
import com.atex.onecms.ws.image.ImageServiceUrlBuilder;
import com.atex.standard.image.ImageContentDataBean;
import com.google.gson.Gson;
import com.polopoly.cm.app.search.categorization.Categorization;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.unified.content.ImageAware_v1;
import com.polopoly.unified.content.Image_v1;
import com.polopoly.unified.content.InputStreamWithMimeType;
import com.polopoly.unified.content.Media_v1;
import com.polopoly.unified.content.RepresentationApplicationFailedException;
import com.polopoly.unified.content.RepresentationFactory;

/**
 * ImageAwareAdapter.
 *
 * @author mnova
 */
public class ImageAwareAdapter implements ImageAware_v1 {

    private static final String CUSTOM_ATTRIBUTES = "customAttributes";
    private static final String CATEGORIZATION = "categorization";

    private final ImagePolicy policy;
    private final CmClient cmClient;

    public ImageAwareAdapter(final ImagePolicy policy, final CmClient cmClient) {
        this.policy = policy;
        this.cmClient = cmClient;
    }

    @Override
    public void applyRepresentation(final Image_v1 image)
            throws RepresentationApplicationFailedException, RuntimeException {

        try {
            ImageContentDataBean contentData = (ImageContentDataBean) policy.getContentData();
            contentData.setTitle(image.getName());
            contentData.setDescription(image.getDescription());
            contentData.setByline(image.getByline());
            policy.setContentData(contentData);

            policy.setComponent("caption", "value", image.getCaption());


            if (Media_v1.THIS_URI.equals(image.getImageUri())) {
                InputStreamWithMimeType inputStreamWithMimeType = image.getImageAsStream();
                if (inputStreamWithMimeType != null) {
                    policy.importFile("image.jpg", inputStreamWithMimeType.getInputStream());
                }
            }

            Map<String, String> newAttributes = image.getCustomAttributes();
            if (newAttributes != null) {
                setCustomAttributes(newAttributes);
            }

            setCategorization(image.getCategorization());
        } catch (CMException e) {
            throw new CMRuntimeException(e);
        } catch (IOException e) {
            throw new CMRuntimeException(e);
        }

    }

    @Override
    public Image_v1 getRepresentation(final Image_v1 image, final RepresentationFactory factory)
            throws RepresentationApplicationFailedException, RuntimeException {

        try {
            ImageContentDataBean contentData = (ImageContentDataBean) policy.getContentData();
            image.setName(contentData.getTitle());
            image.setDescription(contentData.getDescription());
            image.setByline(contentData.getByline());
            image.setCaption(null);

            final ImageInfoAspectBean imageBean = policy.getImageInfo();
            if (imageBean != null) {
                image.setImageUri(getUrlBuilder().buildUrl());
                image.setHeight(imageBean.getHeight());
                image.setWidth(imageBean.getWidth());
                String filePath = imageBean.getFilePath();
                final ByteArrayOutputStream os = new ByteArrayOutputStream();
                policy.exportFile(filePath, os);
                image.setImageFromStream(new InputStreamWithMimeType(new BufferedInputStream(new ByteArrayInputStream(os
                        .toByteArray())), null, filePath));
            }

            image.setCaption(policy.getComponent("caption", "value"));
            image.setCustomAttributes(getCustomAttributes());
            image.setCategorization(getCategorization());

        } catch (CMException e) {
            throw new CMRuntimeException(e);
        } catch (IOException e) {
            throw new CMRuntimeException(e);
        }

        return image;
    }

    public void setCustomAttributes(final Map<String, String> attributes) throws CMException {
        if (attributes == null) {
            policy.setComponent(CUSTOM_ATTRIBUTES, "value", null);
        } else {
            policy.setComponent(CUSTOM_ATTRIBUTES, "value", new Gson().toJson(attributes));
        }
    }

    public Map<String, String> getCustomAttributes() throws CMException {
        String json = policy.getComponent(CUSTOM_ATTRIBUTES, "value");
        if (json == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, String> map = new Gson().fromJson(json, Map.class);
        return map;
    }

    private Categorization getCategorization() throws CMException {
        String json = policy.getComponent(CATEGORIZATION, "value");
        if (json == null) {
            return null;
        }
        return new Gson().fromJson(json, Categorization.class);
    }

    private void setCategorization(final Categorization categorization) throws CMException {
        if (categorization == null) {
            policy.setComponent(CATEGORIZATION, "value", null);
        } else {
            policy.setComponent(CATEGORIZATION, "value", new Gson().toJson(categorization));
        }
    }

    private ImageServiceUrlBuilder getUrlBuilder() {

        String secret;
        try {
            secret = new ImageServiceConfigurationProvider(cmClient.getPolicyCMServer())
                    .getImageServiceConfiguration().getSecret();
        } catch (CMException e) {
            throw new RuntimeException("Secret not found", e);
        }

        Subject subject = SubjectUtil.fromCaller(cmClient.getCMServer().getCurrentCaller());
        Content<ImageContentDataBean> content =
                cmClient.getContentManager().get(cmClient.getContentManager().resolve(IdUtil.fromPolicyContentId(policy.getContentId()), Subject.NOBODY_CALLER),
                        null,
                        ImageContentDataBean.class,
                        null,
                        subject).getContent();
        return new ImageServiceUrlBuilder(content, secret); //.format(format);
    }

}
