package it.conquiste.cm.widget;

import java.io.IOException;

import com.atex.plugins.baseline.widget.OContentListEntryBasePolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import it.conquiste.cm.teaser.TeaserPolicy;


public class TeaserTocEntryViewer extends OContentListEntryBasePolicyWidget {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    protected void renderEntryHeader(Device device, OrchidContext oc) throws OrchidException, IOException {
        TeaserPolicy teaserPolicy = (TeaserPolicy)getPolicy();
        boolean emptyTitle = false;
        try {
            String teaserName = teaserPolicy.getTeaserName();
            if(teaserName == null || teaserName == "") {
                emptyTitle = true;
            }
        } catch(Exception e) {}
        try {
            String teaserTitleClass = "teaser_has_title";
            if(emptyTitle == true) {
                teaserTitleClass = "no_teaser_title";
            }
            device.println("<div class=\""+teaserTitleClass+"\">");
            if (teaserPolicy.isVisible() || teaserPolicy.hasVisible()) {
                super.renderEntryHeader(device, oc);
            } else {
                device.println("<div class=\"preview_only\">");
                super.renderEntryHeader(device, oc);
            }
            device.println("</div>");
        } catch (CMException e) {
            device.println("<div style='color:red;'> ERROR: ref content not found   ");
            device.println("<div class=\"preview_only\">");
            super.renderEntryHeader(device, oc);
            device.println("</div>");
            device.println("</div>");
            handleError("reference content not found. ("+e.getMessage()+") ",e, oc);
        }
    }
}
