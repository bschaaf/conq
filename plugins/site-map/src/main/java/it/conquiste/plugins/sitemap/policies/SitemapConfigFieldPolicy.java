package it.conquiste.plugins.sitemap.policies;

import java.util.logging.Level;

import it.conquiste.plugins.sitemap.policies.util.PolicyUtils;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.app.policy.ContentListWrapperPolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.cm.collections.ContentListUtil;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.PolicyImplBase;
import com.polopoly.model.ModelTypeDescription;

/**
 * Contain convenience methods for settings used by vm.
 * 
 * @author sarasprang
 */
public class SitemapConfigFieldPolicy extends ContentPolicy implements ModelTypeDescription {

	public boolean isManual() {
		return PolicyUtils.isChecked(false, this, "useManual");
	}

	public String getManualXML() {
		return PolicyUtils.getSingleValue(this, "manualxml", "");
	}

	public boolean isPublishingQueues() {
		return PolicyUtils.isChecked(false, this, "usePublishingQueues");
	}

	public boolean isGoogleSiteMap() {
		return PolicyUtils.isChecked(false, this, "useGoogleNews");
	}

}
