/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.widget;

import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.event.FlowEventListener;
import com.polopoly.orchid.event.FlowEventTrigger;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.event.OrchidEventListener;
import com.polopoly.orchid.widget.OInput;
import com.polopoly.orchid.widget.OJavaScript;
import org.apache.commons.fileupload.FileItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A file input that allows multiple file upload, implemented via html.
 *
 * <pre>
 * {@code
 *   <input type="file" multiple="multiple" />
 * }
 * </pre>
 */
public class OMultipleFileInput extends OInput implements FlowEventTrigger {

    private static final long serialVersionUID = 5929575322259377460L;

    private List<Object> submitListeners = new ArrayList<Object>();
    private OJavaScript useMultipartScript;
    private boolean hasFlowEventListeners;
    private List<FileItem> fileItems = new ArrayList<FileItem>();
    // default is allow multiple uploads
    private boolean multiple = true;

    /**
     * Check whether this input support multiple file upload.
     *
     * @return whether the file input support multiple upload
     */
    public boolean isMultiple() {
        return multiple;
    }

    /**
     * Set the input is using multiple or not.
     *
     * @param multiple
     *            the multiple to set
     */
    public void setMultiple(final boolean multiple) {
        this.multiple = multiple;
    }

    @Override
    public void initSelf(final OrchidContext oc) throws OrchidException {
        useMultipartScript = new OJavaScript();
        useMultipartScript.setScript("useMultipartFormDataSubmit();");
        addAndInitChild(oc, this.useMultipartScript);
        setExtraAttribute("accept", "application/zip,application/x-zip-compressed,image/*");
    }

    // for unit test
    void setFileItems(final List<FileItem> fileItems) {
        this.fileItems = fileItems;
    }

    void setMultiPartScript(final OJavaScript script) {
        this.useMultipartScript = script;
    }

    /**
     * Get the list of file items. Use {@link #clearFiles} to clear underlying
     * file items
     *
     * @return unmodifiable list that contains the file items
     */
    public List<FileItem> getFileItems() {
        return Collections.unmodifiableList(fileItems);
    }

    /**
     * Clear the underlying file item collection.
     */
    public void clearFiles() {
        fileItems.clear();
    }

    @Override
    public void decodeSelf(final OrchidContext oc) throws OrchidException {
        if (isEnabled() && hasFlowEventListeners()) {
            Device device = oc.getDevice();
            String name = getName();
            Object[] values = (Object[]) device.getParameterValueObjects(name);
            clearFiles();
            for (Object value : values) {
                FileItem item = (FileItem) value;
                if ((!item.isFormField()) && (item.getName() != null) && (item.getName().length() > 0)) {
                    fileItems.add(item);
                    oc.addFlowEvent(new OrchidEvent(this));
                }
            }
        }
    }

    boolean hasFlowEventListeners() {
        return hasFlowEventListeners;
    }

    public void addNewFileListener(final OrchidEventListener listener) {
        submitListeners.add(listener);
        if ((listener instanceof FlowEventListener)) {
            hasFlowEventListeners = true;
        }
    }

    @SuppressWarnings("rawtypes")
    public List getEventListeners(final Class listenerClass) {
        List<Object> list = new ArrayList<Object>();
        Iterator<Object> iter = this.submitListeners.iterator();
        while (iter.hasNext()) {
            Object object = iter.next();
            if (listenerClass.isInstance(object)) {
                list.add(object);
            }
        }
        return list;
    }

    @Override
    public void localRender(final OrchidContext oc) throws OrchidException, IOException {
        Device device = oc.getDevice();
        useMultipartScript.render(oc);
        device.print("<input type='file' name='" + getName() + "'");
        device.print(" id='" + getName() + "'");
        device.print(getAttributesString());
        if (isMultiple()) {
            device.print(" multiple='multiple' ");
        }
        device.print(" />");
    }

    @Override
    public List getFlowEventListeners() {
        return getEventListeners(FlowEventListener.class);
    }

    @Override
    public void addFlowEventListener(final FlowEventListener flowEventListener) {
        addNewFileListener(flowEventListener);
    }
}
