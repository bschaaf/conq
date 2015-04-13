/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.imagegallery.util;

import com.atex.onecms.content.LegacyContentAdapter;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.VersionedContentId;
import com.polopoly.cm.app.widget.ContentListEntryContainer;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.gui.orchid.util.FileFilter;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.util.MessageUtil;
import com.polopoly.util.LocaleUtil;
import org.apache.commons.fileupload.FileItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import static com.atex.plugins.imagegallery.ImageGalleryConfigurationPolicy.getConfigurationPolicy;

public class ImageUploadUtil {

    protected static final Pattern ZIP_EXTENSION_REGEXP = Pattern.compile("^.*.zip$", Pattern.CASE_INSENSITIVE);
    protected static final Pattern MAC_OS_TEMP_FILES = Pattern.compile(".*(__MACOSX).+", Pattern.CASE_INSENSITIVE);
    private static final Logger LOG = Logger.getLogger(ImageUploadUtil.class.getName());

    private PolicyCMServer cmServer;
    private ContentListEntryContainer imagesContainer;
    private FileFilter fileFilter;

    public ImageUploadUtil(final PolicyCMServer cmServer,
                           final ContentListEntryContainer imagesContainer,
                           final FileFilter fileFilter) {
        this.cmServer = cmServer;
        this.imagesContainer = imagesContainer;
        this.fileFilter = fileFilter;
    }

    public ContentPolicy createContent(final String name, final InputStream fileData, final OrchidContext oc) {
        ContentPolicy policy = null;
        try {
            ContentId parentId = imagesContainer.getPolicy().getContentId().getContentId();
            LegacyContentAdapter image = (LegacyContentAdapter) cmServer.createContent(1, parentId, getInputTemplate());
            policy = (ContentPolicy) image;
            // Uploads the image and initializes the image and file aspects
            // Also sets the name from metadata
            policy.importFile("image/" + name, fileData);
            cmServer.commitContent(policy);
        } catch (CMException | IOException e) {
            LOG.log(Level.WARNING, "Error creating image content", e);
            MessageUtil.addErrorMessage(oc, LocaleUtil.format("cm.field.FileManager.FileNotAllowed",
                    new String[]{name},
                    oc.getLocale(),
                    oc.getMessageBundle()));
            return null;
        }
        return policy;
    }

    /**
     * To read from ZIP file.
     *
     * @param oc OrchidContext for error message handling
     * @param is <code>InputStream</code>
     * @throws CMException
     * @throws IOException
     */
    public void readZipFile(final InputStream is, final OrchidContext oc) throws CMException, IOException {
        ZipInputStreamWrapper zipinputstream = getZipInputStreamWrapper(is);
        try {
            ZipEntry zipentry;
            while ((zipentry = zipinputstream.getNextEntry()) != null) {
                String fileName = zipentry.getName();
                readImageFiles(fileName, zipinputstream, oc);
                zipinputstream.closeEntry();
            }
        } finally {
            zipinputstream.close();
        }
    }

    /**
     * To read images from file item, the hidden file of Mac OS zipped together will be omitted.
     *
     * @param fileItem
     * @param oc
     * @throws CMException
     * @throws IOException
     */
    public void readImageFiles(final FileItem fileItem, final OrchidContext oc) throws CMException, IOException {
        String fileName = fileItem.getName();
        readImageFiles(fileName, fileItem.getInputStream(), oc);
    }

    /**
     * To read images from input stream,  the hidden file of Mac OS zipped together will be omitted.
     *
     * @param fileName
     * @param is The Input Stream
     * @param oc
     * @throws CMException
     * @throws IOException
     */
    public void readImageFiles(final String fileName, final InputStream is, final OrchidContext oc)
            throws CMException, IOException {
        if (fileFilter.accept(fileName, "/", false) && !isMacHiddenFile(fileName)) {
            ContentPolicy contentPolicy = createContent(fileName, is, oc);
            if (contentPolicy != null) {
                addImageToList(contentPolicy);
            }
        }
    }

    /**
     * Check whether the filename is with zip extension.
     *
     * @param fileName
     * @return
     */
    public boolean isZipFile(final String fileName) {
        return ZIP_EXTENSION_REGEXP.matcher(fileName).matches();
    }

    public ContentId getInputTemplate() throws CMException {
        String templateId = getConfigurationPolicy(cmServer).getCreatorExternalIdString();
        if (templateId == null) {
            throw new CMException("No template id configured");
        }
        ContentId id = new ExternalContentId(templateId);
        if (!cmServer.contentExists(id)) {
            throw new CMException("Template with id '" + templateId + "' was not found.");
        } else {
            return id;
        }
    }

    public void addImageToList(final ContentPolicy contentPolicy) {
        imagesContainer.addEntry(new ContentReference(contentPolicy.getContentId().getContentId()), 0);
    }

    protected boolean isMacHiddenFile(final String input) {
        return MAC_OS_TEMP_FILES.matcher(input).matches();
    }

    protected ZipInputStreamWrapper getZipInputStreamWrapper(final InputStream is) {
        return new ZipInputStreamWrapper(is);
    }

    protected VersionedContentId getContentVersionedId(final ContentPolicy contentPolicy) {
        return contentPolicy.getContentId();
    }
}
