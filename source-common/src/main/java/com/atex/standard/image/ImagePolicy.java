package com.atex.standard.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.atex.onecms.content.AspectedPolicy;
import com.atex.onecms.content.ContentFileInfo;
import com.atex.onecms.content.FilesAspectBean;
import com.atex.onecms.image.ImageEditInfoAspectBean;
import com.atex.onecms.image.ImageInfoAspectBean;
import com.atex.standard.image.exif.MetadataTags;
import com.drew.imaging.ImageProcessingException;
import com.polopoly.application.Application;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.cm.policy.PrepareResult;
import com.polopoly.util.StringUtil;

public class ImagePolicy extends AspectedPolicy<ImageContentDataBean> {
    private static final Logger LOGGER = Logger.getLogger(ImagePolicy.class.getName());

    public ImagePolicy(final CmClient cmClient, final Application application) throws IllegalApplicationStateException {
        super(cmClient, application);
    }

    @Override
    public String getName() throws CMException {
        String name = super.getName();

        // Fall back on metadata tags if no name is supplied
        if (StringUtil.isEmpty(name)) {
            MetadataTags metadata = getMetadataTags();
            if (metadata != null) {
                name = metadata.getTitle();
            }
        }

        if (StringUtil.isEmpty(name) && getImageInfo() != null) {
            name = getImageInfo().getFilePath();
            if (name != null) {
                int i = name.lastIndexOf("/");
                if (i >= 0 && i < name.length() + 1) {
                    name = name.substring(i);
                }
            }
        }

        return name;
    }

    public ImageInfoAspectBean getImageInfo() throws CMException {
        return (ImageInfoAspectBean) getAspect(ImageInfoAspectBean.ASPECT_NAME);
    }

    public FilesAspectBean getFiles() throws CMException {
        return (FilesAspectBean) getAspect(FilesAspectBean.ASPECT_NAME);
    }

    public void setImageInfo(final ImageInfoAspectBean info) throws CMException {
        setAspect(ImageInfoAspectBean.ASPECT_NAME, info);
    }

    public ImageEditInfoAspectBean getImageEditInfo() throws CMException {
        return (ImageEditInfoAspectBean) getAspect(ImageEditInfoAspectBean.ASPECT_NAME);
    }

    public void setImageEditInfo(final ImageEditInfoAspectBean imageEditAspect) throws CMException {
        setAspect(ImageEditInfoAspectBean.ASPECT_NAME, imageEditAspect);
    }

    @Override
    public void importFile(final String path, final InputStream data) throws CMException, IOException {
        super.importFile(path, data);
        if (getImageInfo() == null) {
            ImageInfoAspectBean info = new ImageInfoAspectBean(cleanPath(path),
                    0, 0);
            initMetadataFromImage(info);
        }
    }


    public MetadataTags getMetadataTags() throws CMException {
        return (MetadataTags) getAspect(MetadataTags.ASPECT_NAME);
    }

    public void setMetaDataTags(final MetadataTags metadata) throws CMException {
        setAspect(MetadataTags.ASPECT_NAME, metadata);
    }


    private void initMetadataFromImage(final ImageInfoAspectBean info)
            throws CMException {
        if (info != null) {
            com.atex.onecms.content.ContentFileInfo contentFileInfo = getFileUriForImage(info);
            MetadataTags metadata = getMetadataTags();
            if (metadata == null || !StringUtil.equals(contentFileInfo.getFileUri(), metadata.getFileUri())) {
                try {
                    extractAndStoreImageMetadata(info);
                } catch (IOException e) {
                    throw new CMException("Couldn't read image metadata", e);
                }
                setImageInfo(info);
            }
        }
    }

    private ContentFileInfo getFileUriForImage(final ImageInfoAspectBean info)
            throws CMException {
        ContentFileInfo contentFileInfo = getFiles().getFiles().get(info.getFilePath());
        if (contentFileInfo == null) {
            throw new CMException(String.format("Couldn't find image file for '%s'",
                    info.getFilePath()));
        }
        return contentFileInfo;
    }

    private void extractAndStoreImageMetadata(final ImageInfoAspectBean info)
            throws IOException, CMException {

        String path = info.getFilePath();
        try {
            MetadataTags metadataTags = MetadataTags.extract(getFileStream(path));
            ContentFileInfo contentFileInfo = getFileUriForImage(info);
            metadataTags.setFileUri(contentFileInfo.getFileUri());
            setMetaDataTags(metadataTags);
            Integer imageWidth = metadataTags.getImageWidth();
            Integer imageHeight = metadataTags.getImageHeight();
            if (imageWidth != null && imageHeight != null) {
                info.setWidth(imageWidth);
                info.setHeight(imageHeight);
            } else {
                populateFromImageIO(info);
            }
        } catch (ImageProcessingException e) {
            populateFromImageIO(info);
        }
    }

    private void populateFromImageIO(final ImageInfoAspectBean info) throws CMException, IOException {
        String filePath = info.getFilePath();
        // Nothing to do if there is no actual file
        if (getFiles() == null || filePath == null) {
            return;
        }

        com.atex.onecms.content.ContentFileInfo fileInfo =
                getFiles().getFiles().get(filePath);
        if (fileInfo != null) {
            InputStream data = getFileStream(filePath);
            BufferedImage image = ImageIO.read(data);
            if (image != null) {
                info.setWidth(image.getWidth());
                info.setHeight(image.getHeight());
            } else {
                throw new CMException("Not a supported image: " + filePath);
            }
        }
    }

    @Override
    public void deleteFile(final String path) throws CMException, IOException {
        super.deleteFile(path);
        ImageInfoAspectBean imageInfo = getImageInfo();
        if (imageInfo != null && imageInfo.getFilePath().equals(cleanPath(path))) {
            setImageInfo(null);
        }
    }

    @Override
    public void preCommitSelf() throws CMException {
        super.preCommitSelf();
        initMetadataFromImage(getImageInfo());
    }

    @Override
    public PrepareResult prepareSelf() throws CMException {
        PrepareResult prepareResult = new PrepareResult(PolicyUtil.getLabelOrName(this));
        ImageInfoAspectBean imageInfo = getImageInfo();
        if (imageInfo == null || StringUtil.isEmpty(imageInfo.getFilePath())) {
            prepareResult.setError(true);
            prepareResult.setLocalizeMessage("standard.image.msg.ImageRequired");
        }
        return prepareResult;
    }
}
