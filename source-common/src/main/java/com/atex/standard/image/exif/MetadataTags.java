package com.atex.standard.image.exif;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.icc.IccDirectory;
import com.drew.metadata.iptc.IptcDirectory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MetadataTags {
    public static final String ASPECT_NAME = "standard.Image.MetadataTags";
    private Map<String, Map<String, ?>> tags;
    private String fileUri;
    private Integer imageWidth;
    private Integer imageHeight;
    private String title;
    private String byline;
    private String description;

    public MetadataTags() {
        tags = new HashMap<>();
    }

    public MetadataTags(final Map<String, Map<String, ?>> tags) {
        this.tags = tags;
    }

    public Map<String, Map<String, ?>> getTags() {
        return new HashMap<>(tags);
    }

    public void setTags(final Map<String, Map<String, ?>> tags) {
        this.tags = tags;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(final String fileUri) {
        this.fileUri = fileUri;
    }

    public Integer getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(final Integer imageWidth) {
        this.imageWidth = imageWidth;
    }

    public Integer getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(final Integer imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getByline() {
        return byline;
    }

    public void setByline(final String byline) {
        this.byline = byline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public static MetadataTags extract(final InputStream inputStream) throws ImageProcessingException, IOException {
        Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
        Map<String, Map<String, ?>> tags = new HashMap<>();
        MetadataTags metadataTags = new MetadataTags();
        for (Directory directory : metadata.getDirectories()) {
            if (!(directory instanceof IccDirectory)) {
                Map<String, Object> tagsInDirectory = new HashMap<>();
                for (Tag tag : directory.getTags()) {
                    tagsInDirectory.put(tag.getTagName(), getTagValue(directory, tag));
                    if (directory instanceof ExifSubIFDDirectory) {
                        readExifSubIFDDirectoryTag(directory, tag, metadataTags);
                    } else if (directory instanceof IptcDirectory) {
                        readIptcDirectoryTag(directory, tag, metadataTags);
                    }
                    if (metadataTags.getImageWidth() == null && "Image Width".equals(tag.getTagName())) {
                        metadataTags.setImageWidth(directory.getInteger(tag.getTagType()));
                    }
                    if (metadataTags.getImageHeight() == null && "Image Height".equals(tag.getTagName())) {
                        metadataTags.setImageHeight(directory.getInteger(tag.getTagType()));
                    }
                }
                tags.put(directory.getName(), tagsInDirectory);
            }
        }
        metadataTags.setTags(tags);
        return metadataTags;
    }

    private static Object getTagValue(final Directory directory, final Tag tag) {
        Object tagValue = directory.getObject(tag.getTagType());
        if (!(tagValue instanceof Rational) && tagValue instanceof Number) {
            return tagValue;
        }
        return tag.getDescription();
    }

    private static void readExifSubIFDDirectoryTag(final Directory directory,
                                                   final Tag tag,
                                                   final MetadataTags metadataTags) {
        if (tag.getTagType() == ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH) {
            Integer width = directory.getInteger(tag.getTagType());
            if (width != null) {
                metadataTags.setImageWidth(width);
            }
        } else if (tag.getTagType() == ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT) {
            Integer height = directory.getInteger(tag.getTagType());
            if (height != null) {
                metadataTags.setImageHeight(height);
            }
        }
    }

    private static void readIptcDirectoryTag(final Directory directory,
                                             final Tag tag,
                                             final MetadataTags metadataTags) {
        int tagType = tag.getTagType();
        switch (tagType) {
            case IptcDirectory.TAG_OBJECT_NAME:
                metadataTags.setTitle(directory.getDescription(tagType));
                break;
            case IptcDirectory.TAG_BY_LINE:
                metadataTags.setByline(directory.getDescription(tagType));
                break;
            case IptcDirectory.TAG_CAPTION:
                metadataTags.setDescription(directory.getDescription(tagType));
                break;
            case IptcDirectory.TAG_HEADLINE:
                if (metadataTags.getDescription() == null) {
                    metadataTags.setDescription(directory.getDescription(tagType));
                }
                break;
            default:
                break;
        }
    }
}
