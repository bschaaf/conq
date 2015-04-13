/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.widget;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.polopoly.orchid.event.FlowEventListener;
import org.apache.commons.fileupload.FileItem;

import com.atex.plugins.imagegallery.ImageGalleryPolicy;
import com.atex.plugins.imagegallery.util.ImageUploadUtil;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.PolicyWidget;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.imagemanager.ImageTooBigException;
import com.polopoly.cm.app.util.PolicyWidgetUtil;
import com.polopoly.cm.app.widget.ComplexPolicyWidget;
import com.polopoly.cm.app.widget.ContentListEntryContainer;
import com.polopoly.cm.app.widget.OFieldPolicyWidget;
import com.polopoly.cm.app.widget.OPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policy.PrepareResult;
import com.polopoly.gui.orchid.util.FileFilter;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.util.WidgetUtil;
import com.polopoly.orchid.widget.OSubmitButton;
import com.polopoly.util.LocaleUtil;

public class OFileUploadWidget extends OFieldPolicyWidget implements Viewer, Editor {

    private static final long serialVersionUID = -5562648263878885634L;

    private OMultipleFileInput multipleFileInput;
    private OSubmitButton uploadButton;
    private transient PolicyCMServer cmServer;
    private transient ContentListEntryContainer imagesContainer;
    private transient FileFilter fileFilter;
    private ImageUploadUtil imageUtil;

    @Override
    public void initSelf(final OrchidContext oc) throws OrchidException {

        multipleFileInput = getMultipleFileInput();
        multipleFileInput.addNewFileListener(new FileUploadListener());
        addAndInitChild(oc, multipleFileInput);

        uploadButton = getSubmitButton();
        uploadButton.setValue(LocaleUtil.format("com.atex.plugins.image-gallery.action.upload", oc.getMessageBundle()));
        addAndInitChild(oc, uploadButton);
        fileFilter = ((ImageGalleryPolicy) getContentSession().getTopPolicy()).getFileFilter();
        try {
            cmServer = getPolicy().getCMServer();
        } catch (CMException e) {
            throw new OrchidException(e.getMessage(), e);
        }
    }

    @Override
    public void preRender(final OrchidContext oc) throws OrchidException {
        super.preRender(oc);
        // only during pre render phase can get imagesContainer, y?
        imagesContainer = (ContentListEntryContainer) getSiblingWidget(ImageGalleryPolicy.CONTENT_LIST_NAME);
        imageUtil = getImageUploadUtil();
    }

    @Override
    public PrepareResult validateSelf() throws CMException {
        PrepareResult result = getPrepareResult();
        if (!isValidInputTemplate()) {
            result.setError(true);
            OrchidContext oc = getOrchidContext();
            String message = LocaleUtil
                    .format("com.atex.plugins.image-gallery.error.invalid.inputtemplate", oc.getMessageBundle());
            result.setMessage(message);
            setError(true);
            handleError(oc, message);
        }
        return result;
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

    @Override
    public void localRender(final OrchidContext oc) throws OrchidException, IOException {

        boolean isEnabled = isEditMode();
        multipleFileInput.setEnabled(isEnabled);
        multipleFileInput.render(oc);
        uploadButton.setEnabled(isEnabled);
        uploadButton.render(oc);
    }

    class FileUploadListener
        implements FlowEventListener {

        public boolean processEvent(final OrchidContext oc, final OrchidEvent oe) throws OrchidException {
            try {
                doUploadFiles(oc);
            } catch (CMException e) {
                if (e.getCause().getClass() == ImageTooBigException.class) {
                    handleErrors(e.getMessage(), e, oc);
                }
                throw new OrchidException(e.getMessage(), e);
            } catch (IOException e) {
                throw new OrchidException(e.getMessage(), e);
            }

            return false;
        }
    }

    void doUploadFiles(final OrchidContext oc) throws CMException, IOException {
        try {
            imagesContainer.getContentList();
        } catch (NullPointerException e) {
            // workaround for NPE when add file but clicked cancel button
            return;
        }
        Collection<FileItem> files = multipleFileInput.getFileItems();
        // do validation on multiple files
        for (FileItem file : files) {
            if (!fileFilter.accept(file.getName(), "/", false)) {
                String error = LocaleUtil.format("cm.field.FileManager.FileNotAllowed", new Object[] {file.getName()},
                        oc.getLocale(), oc.getMessageBundle());
                handleError(oc, error + " " + fileFilter.getAcceptDescription(oc.getMessageBundle(), oc.getLocale()));
                return;
            }
        }

        for (FileItem fileItem : files) {
            if (imageUtil.isZipFile(fileItem.getName())) {
                readZipFile(fileItem, oc);
            } else {
                imageUtil.readImageFiles(fileItem, oc);
            }
        }
        multipleFileInput.clearFiles();
    }

    public void readZipFile(final FileItem fileItem, final OrchidContext oc) throws CMException, IOException {
        imageUtil.readZipFile(fileItem.getInputStream(), oc);
    }

    protected ContentId getInputTemplate() throws CMException {
        return imageUtil.getInputTemplate();
    }

    protected boolean isValidInputTemplate() {
        try {
            getInputTemplate();
            return true;
        } catch (CMException e) {
            return false;
        }
    }

    // for unit testing
    void setMultipleFileInput(final OMultipleFileInput multipleFileInput) {
        this.multipleFileInput = multipleFileInput;
    }

    // ugly hack so can stub parent method
    PrepareResult getPrepareResult() throws CMException {
        return super.validateSelf();
    }

    OrchidContext getOrchidContext() {
        return WidgetUtil.getOrchidContext();
    }

    void handleErrors(final String msg, final Exception e, final OrchidContext oc) {
        handleError(msg, e, oc);
    }

    protected OMultipleFileInput getMultipleFileInput() {
        return new OMultipleFileInput();
    }

    protected ImageUploadUtil getImageUploadUtil() {
        return new ImageUploadUtil(cmServer, imagesContainer, fileFilter);
    }

    protected OSubmitButton getSubmitButton() {
        return new OSubmitButton();
    }

    protected boolean isEditMode() {
        return PolicyWidgetUtil.isEditMode(this);
    }
}
