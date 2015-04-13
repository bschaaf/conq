package it.conquiste.cm.widget;

import com.polopoly.cm.app.PolicyWidget;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyUtil;
import it.conquiste.cm.util.slot.SlotUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OConquisteContentListPolicyWidget extends OContentListPolicyWidget {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(OConquisteContentListPolicyWidget.class.getName());
    private boolean checkMaxItems;

    protected void initParameters() {
        super.initParameters();
        checkMaxItems = PolicyUtil.getParameterAsBoolean("checkMaxItems", false, getPolicy());
        setMaxEntriesToRender(getMaxItems());
    }

    @Override
    protected String getCustomElement(int i){

        if (lessThanMaxItems(i) && i != -1) {
            return "<td>"+i+"</td>";
        }
        return "";
    }

    @Override
    protected String getCustomViewModeCoverBegin(int i) {
        return "<table class='listentrycontainer'><tr><td valign='top'>&nbsp;</td>" + getCustomElement(i) + "<td class='listentry'>";
    }

    @Override
    protected String getCustomViewModeCoverEnd() {
        return "</td></tr></table>";
    }

    private boolean lessThanMaxItems(int i) {
        Integer max = getMaxItems();

        if (max != null){
            if ( i > max.intValue()){
                return false;
            }
        }
        return true;
    }

    private int getMaxItems() {
        PolicyWidget parentWidget = getParentPolicyWidget();

        if(checkMaxItems) {
            if (parentWidget != null) {
                String nameOfSlot = "first";//parentWidget.getRelativeName();

                if (nameOfSlot != null) {
                    try {
                        if(SlotUtil.getMaxTeasersForSlot(getPolicy(), nameOfSlot) != null)
                            return SlotUtil.getMaxTeasersForSlot(getPolicy(), nameOfSlot);
                    } catch (CMException e) {
                        LOG.log(Level.WARNING,"Could not get max teasers setting for slot.", e);
                    }
                }
            }
        }
        return 15;
    }
}