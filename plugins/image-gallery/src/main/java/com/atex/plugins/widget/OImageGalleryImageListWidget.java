package com.atex.plugins.widget;

import com.atex.plugins.imagegallery.MetadataWidgetRenderer;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.app.util.PolicyWidgetUtil;
import com.polopoly.cm.app.widget.ContentListEntryWidget;
import com.polopoly.cm.app.widget.OContentListPolicyWidget;
import com.polopoly.cm.app.widget.OPolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.OrchidContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OImageGalleryImageListWidget extends OContentListPolicyWidget {

    private Map<ContentId, OPolicyWidget> metaWidgets = new HashMap<>();

    private static final Logger LOG = Logger.getLogger(OImageGalleryImageListWidget.class.getName());

    @Override
    public void store() throws CMException {
        super.store();

        for (String name : getPolicy().getComponentNames()) {
            getPolicy().setComponent(name, null);
        }

        Iterator<?> entryWidgets = getEntryWidgets();
        while (entryWidgets.hasNext()) {
            ContentListEntryWidget entry = (ContentListEntryWidget) entryWidgets.next();
            OPolicyWidget metaWidget = getMetaWidget(entry);

            if (metaWidget != null) {
                Policy metaPolicy = metaWidget.getPolicy();
                List childPolicyNames = metaPolicy.getChildPolicyNames();
                for (Object name : childPolicyNames) {
                    Policy childPolicy = metaPolicy.getChildPolicy((String) name);
                    if (childPolicy instanceof SingleValued) {
                        getPolicy().setComponent(entry.getIndex() + ":" + childPolicy.getPolicyName(),
                                                 ((SingleValued) childPolicy).getValue());
                    }
                }
            }
        }
    }

    private void addMetaWidgetIfNecessary(final OrchidContext oc, final boolean initData) throws OrchidException {
        Iterator<?> entryWidgets = getEntryWidgets();
        while (entryWidgets.hasNext()) {
            ContentListEntryWidget entry = (ContentListEntryWidget) entryWidgets.next();
            if (entry instanceof MetadataWidgetRenderer) {
                OPolicyWidget metaWidget = getMetaWidget(entry);
                if (metaWidget == null) {
                    try {
                        metaWidget = createMetaWidget(oc, entry, initData);
                        addAndInitChildPolicyWidget(oc, metaWidget);
                    } catch (CMException e) {
                        LOG.log(Level.WARNING, "Error creating metadata widget", e);
                    }
                }
                ((MetadataWidgetRenderer) entry).setMetadataWidget(metaWidget);
            }
        }
    }

    @Override
    public void initSelf(final OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        addMetaWidgetIfNecessary(oc, true);
    }

    @Override
    public void updateSelf(final OrchidContext oc) throws OrchidException {
        super.updateSelf(oc);
        addMetaWidgetIfNecessary(oc, false);
    }

    private OPolicyWidget createMetaWidget(final OrchidContext oc, final ContentListEntryWidget entry,
                                           final boolean initData)
        throws CMException {
        OPolicyWidget metaWidget = (OPolicyWidget)
            PolicyWidgetUtil.createMemoryBackedPolicyWidget(oc,
                                                            getContentSession(),
                                                            getPolicy().getContentId().getContentId(),
                                                            "com.atex.plugins.image-gallery.Caption", false);
        putMetaWidget(entry, metaWidget);

        if (initData) {
            Policy metaPolicy = metaWidget.getPolicy();
            List childPolicyNames = metaPolicy.getChildPolicyNames();
            for (Object name : childPolicyNames) {
                Policy childPolicy = metaPolicy.getChildPolicy((String) name);
                if (childPolicy instanceof SingleValued) {
                    String value = getPolicy().getComponent(entry.getIndex() + ":" + childPolicy.getPolicyName());
                    ((SingleValued) childPolicy).setValue(value);
                }
            }
        }
        return metaWidget;
    }

    private void putMetaWidget(final ContentListEntryWidget entry, final OPolicyWidget widget) {
        metaWidgets.put(entry.getPolicy().getContentId().getContentId(), widget);
    }

    private OPolicyWidget getMetaWidget(final ContentListEntryWidget entry) {
        return metaWidgets.get(entry.getPolicy().getContentId().getContentId());
    }
}
