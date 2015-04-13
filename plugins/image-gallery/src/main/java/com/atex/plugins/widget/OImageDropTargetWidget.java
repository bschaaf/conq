/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.widget;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.imagegallery.ImageGalleryPolicy;
import com.atex.plugins.imagegallery.util.ImageUploadUtil;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.ContentSession;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.PolicyWidget;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.util.URLBuilder;
import com.polopoly.cm.app.widget.ComplexPolicyWidget;
import com.polopoly.cm.app.widget.ContentListEntryContainer;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.cm.app.widget.OPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.gui.orchid.util.FileFilter;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.ajax.AjaxEvent;
import com.polopoly.orchid.ajax.JSCallback;
import com.polopoly.orchid.ajax.listener.StandardAjaxEventListener;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.js.JSWidget;
import com.polopoly.orchid.js.JSWidgetUtil;
import com.polopoly.orchid.util.MessageUtil;
import com.polopoly.orchid.widget.OJavaScript;
import com.polopoly.util.Base64;
import com.polopoly.util.LocaleUtil;

public class OImageDropTargetWidget extends OFieldPolicyWidget implements Viewer, Editor, JSWidget {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(OImageDropTargetWidget.CLASS);

    public static final ExternalContentId PLUGIN_FILES_EXTERNAL_CONTENT_ID =
        new ExternalContentId("plugins.com.atex.gong.plugins.image-gallery-plugin.files");
    public static final String CSS_FILE_PATH = "css/styles.css";
    public static final String FILEDROPS_JS_FILE_PATH = "js/jquery.filedrop.js";
    public static final String WIDGET_JS_FILE_PATH = "js/widget.js";

    private String fileDropJsPath;
    private OJavaScript fileDropJsScript;

    private String widgetJsPath;
    private OJavaScript widgetJsScript;

    private String cssPath;

    private transient PolicyCMServer cmServer;
    private transient ContentListEntryContainer imagesContainer;
    private transient ImageDropTargetPolicy imageDropTargetPolicy;
    private ImageUploadUtil imageUtil;

    private boolean isEditMode;

    private int maxFile;
    private int maxFileSize;

    private String maxFileMessage;
    private String maxFileSizeMessage;
    private String maxFilesSizeMessage;
    private String invalidContentMessage;
    private String invalidContentsMessage;
    private AjaxImageUploadListener ajaxUploadListener;

    @Override
    public void initSelf(final OrchidContext oc) throws OrchidException {
        imageDropTargetPolicy = (ImageDropTargetPolicy) getPolicy();

        maxFileSize = imageDropTargetPolicy.getMaxFileSize();
        maxFile = imageDropTargetPolicy.getMaxNofFile();

        maxFileMessage = LocaleUtil.format("com.atex.plugins.image-gallery.maxfiles",
                                           new Object[] {maxFile},
                                           oc.getLocale(),
                                           oc.getMessageBundle());

        maxFileSizeMessage = LocaleUtil.format("com.atex.plugins.image-gallery.maxfilesize",
                                               new Object[] {"{0}", maxFileSize},
                                               oc.getLocale(),
                                               oc.getMessageBundle());

        maxFilesSizeMessage = LocaleUtil.format("com.atex.plugins.image-gallery.maxfilessize",
                                                new Object[] {maxFileSize},
                                                oc.getLocale(),
                                                oc.getMessageBundle());

        invalidContentMessage =
            LocaleUtil.format("com.atex.plugins.image-gallery.invalidcontenttype",
                              oc.getMessageBundle());

        invalidContentsMessage =
            LocaleUtil.format("com.atex.plugins.image-gallery.invalidcontentstype",
                              oc.getMessageBundle());

        isEditMode = getContentSession().getMode() == ContentSession.EDIT_MODE;

        fileDropJsScript = new OJavaScript();
        addAndInitChild(oc, fileDropJsScript);

        widgetJsScript = new OJavaScript();
        addAndInitChild(oc, widgetJsScript);

        fileDropJsPath = lookupContentFile(PLUGIN_FILES_EXTERNAL_CONTENT_ID, FILEDROPS_JS_FILE_PATH, oc);
        fileDropJsScript.setSrc(fileDropJsPath);

        widgetJsPath = lookupContentFile(PLUGIN_FILES_EXTERNAL_CONTENT_ID, WIDGET_JS_FILE_PATH, oc);
        widgetJsScript.setSrc(widgetJsPath);

        ajaxUploadListener = new AjaxImageUploadListener();
        ajaxUploadListener.addRenderWidget(this);

        getTree().registerAjaxEventListener(this.getCompoundId(), ajaxUploadListener);
    }

    @Override
    public void localRender(final OrchidContext oc) throws OrchidException, IOException {
        Device device = oc.getDevice();

        if (cssPath == null) {
            cssPath = lookupContentFile(PLUGIN_FILES_EXTERNAL_CONTENT_ID, CSS_FILE_PATH, oc);
        }

        device.println(String.format("<link type=\"text/css\" rel=\"stylesheet\" href=\"%s\" />", cssPath));

        if (isEditMode) {

            String indicatorMessage = "<img src=\"images/ajax/busy_indicator_big_green.gif\"/>"
                    + LocaleUtil.format("com.atex.plugins.image-gallery.label.dnd", oc.getMessageBundle());

            device.println("<div id=\"dropbox\">");
            device.println("<span class=\"message\">" + indicatorMessage + "</span>");
            device.println("</div>");
        }
    }

    protected String lookupContentFile(final ContentId contentId, final String fileName, final OrchidContext oc) {
        try {
            return URLBuilder.getFileUrl(contentId, fileName, oc);
        } catch (OrchidException e) {
            LOG.log(Level.WARNING, e.getMessage(), e);
        }
        return null;
    }

    public String[] getJSScriptDependencies() {
        return new String[] {widgetJsPath, fileDropJsPath};
    }

    public String getFriendlyName() throws OrchidException {
        return getName();
    }

    public String[] getInitParams() throws OrchidException {
        return new String[] {"'" + this.getCompoundId() + "'",
                             "'" + maxFile + "'",
                             "'" + maxFileSize + "'",
                             "'" + maxFileMessage + "'",
                             "'" + maxFileSizeMessage + "'",
                             "'" + maxFilesSizeMessage + "'",
                             "'" + invalidContentMessage + "'",
                             "'" + invalidContentsMessage + "'"};
    }

    public String getInitScript() throws OrchidException {
        return JSWidgetUtil.genInitScript(this);
    }

    public String getJSWidgetClassName() throws OrchidException {
        return "JSImageDropTargetWidget";
    }

    protected class AjaxImageUploadListener extends StandardAjaxEventListener {
        public boolean triggeredBy(final OrchidContext oc, final AjaxEvent e) {
            return e instanceof AjaxUploadEvent;
        }

        public JSCallback processEvent(final OrchidContext oc, final AjaxEvent event) throws OrchidException {
            try {
                AjaxUploadEvent ajaxUploadEvent = (AjaxUploadEvent) event;

                String fileName = ajaxUploadEvent.getFileName();
                InputStream fileData = getInputStream(ajaxUploadEvent);
                int index = ajaxUploadEvent.getIndex();

                imageUtil = getImageUploadUtil();
                if (imageUtil.isZipFile(fileName)) {
                    imageUtil.readZipFile(fileData, oc);
                } else {
                    ContentPolicy contentPolicy = createImageContent(fileName, fileData, oc);
                    if (contentPolicy != null) {
                        imageUtil.addImageToList(contentPolicy);
                    }
                }
                index++;
                ajaxUploadListener.addRenderWidget(getSiblingWidget(ImageGalleryPolicy.CONTENT_LIST_NAME));
                return new JSCallback("JSImageDropTargetWidget.prototype.callbackEvent", String.valueOf(index));
            } catch (Exception e) {
                MessageUtil.addErrorMessage(oc, e.getMessage());
                LOG.log(Level.WARNING, e.getMessage(), e);
            }
            return null;
        }
    }

    @Override
    public boolean isAjaxTopWidget() {
        return true;
    }

    @Override
    public void initValueFromPolicy() throws CMException {
        super.initValueFromPolicy();
        cmServer = getPolicy().getCMServer();
    }

    @Override
    public void preRender(final OrchidContext oc) throws OrchidException {
        super.preRender(oc);
        imagesContainer = (ContentListEntryContainer) getSiblingWidget(ImageGalleryPolicy.CONTENT_LIST_NAME);
    }

    @SuppressWarnings("rawtypes")
    protected OPolicyWidget getSiblingWidget(final String name) {
        Iterator siblingWidgets = Collections.EMPTY_LIST.iterator();
        PolicyWidget parent = getParentPolicyWidget();

        if (parent instanceof ComplexPolicyWidget) {
            siblingWidgets = ((ComplexPolicyWidget) parent).getChildPolicyWidgets();
        }

        while (siblingWidgets.hasNext()) {
            OPolicyWidget child = (OPolicyWidget) siblingWidgets.next();
            String childName = child.getRelativeName();
            if (childName.equals(name)) {
                return child;
            }
        }
        return null;
    }

    protected ByteArrayInputStream getInputStream(final AjaxUploadEvent ajaxUploadEvent) {
        return new ByteArrayInputStream(Base64.decode(ajaxUploadEvent.getFileData()));
    }

    protected ImageUploadUtil getImageUploadUtil() {
        FileFilter fileFilter = ((ImageGalleryPolicy) getContentSession().getTopPolicy()).getFileFilter();
        return new ImageUploadUtil(cmServer, imagesContainer, fileFilter);
    }

    protected ContentId getIdFromContentPolicy(final ContentPolicy contentPolicy) {
        return contentPolicy.getContentId().getContentId();
    }

    protected ContentPolicy createImageContent(final String fileName,
                                               final InputStream fileData,
                                               final OrchidContext oc) {
        return imageUtil.createContent(fileName, fileData, oc);
    }

    protected boolean isContentExists(final ContentId cid) throws CMException {
        return cmServer.contentExists(cid);
    }

}
