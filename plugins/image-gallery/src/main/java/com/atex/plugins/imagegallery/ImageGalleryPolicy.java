/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.imagegallery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.inbox.AssociatedUsersPolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.app.util.SuffixFileNameFilter;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.gui.orchid.util.FileFilter;
import com.polopoly.siteengine.layout.ContentRepresentative;
import com.polopoly.unified.content.MediaGalleryAware_v1;
import com.polopoly.unified.content.MediaGallery_v1;
import com.polopoly.unified.content.RepresentationApplicationFailedException;
import com.polopoly.unified.content.RepresentationFactory;

public class ImageGalleryPolicy
extends BaselinePolicy
implements ContentRepresentative, ImageGalleryModelTypeDescription,
MediaGalleryAware_v1 {
	public enum HermesCustomField {
		securityParentId,
		PQ, // insert into which slot
		Categoria, //subject category
		caption, // caption for image
		Edizione, // security parent by name . This field has more priority than securityParentId
	};

	private static final Logger LOGGER = Logger.getLogger(ImageGalleryPolicy.class.getName());
	private static final String[] ACCEPTED_EXTENSIONS = {"jpg", "jpeg", "png", "gif" , "zip"};
	private static final FileFilter FILE_FILTER = new SuffixFileNameFilter(Arrays.asList(ACCEPTED_EXTENSIONS), true);
	private static final String DESCRIPTION= "description";
	public static final String CONTENT_LIST_NAME = "images";

	private MediaGalleryAwareAdapter mediaGalleryAwareAdapter = new MediaGalleryAwareAdapter(this);

	public Collection<ContentId> getRepresentedContent() {

		Collection<ContentId> results = new ArrayList<>();
		ContentList contentList;
		try {
			contentList = getContentList(CONTENT_LIST_NAME);
			for (int contentIndex = 0; contentIndex < contentList.size(); contentIndex++) {
				ContentReference reference = contentList.getEntry(contentIndex);
				results.add(reference.getReferredContentId());
			}

		} catch (CMException e) {
			LOGGER.log(Level.WARNING, e.getMessage(), e);
		}
		return results;
	}

	@Override
	public String getName() throws CMException {
		return super.getName() != null ? super.getName() : abbreviate(getAllCaptions().trim(), 60);
	}
	
	/**
	 * @return value of lead field for article, null if not present.
	 */
	public String getDescription() {
		String lead = null;
		try {
			lead = ((SingleValuePolicy) getChildPolicy(DESCRIPTION)).getValue();
		} catch (CMException e) {
			logger.log(Level.SEVERE, "Couldn't read child policy with name '" + DESCRIPTION + "'", e);
		}
		return lead;
	}

	public void setDescription(final String lead) {
		try {
			((SingleValuePolicy) getChildPolicy(DESCRIPTION)).setValue(lead);
		} catch (CMException e) {
			logger.log(Level.SEVERE, "Couldn't read child policy with name '" + DESCRIPTION + "'", e);
		}
	}

	private String abbreviate(final String str, final int maxWidth) {
		if (str.length() <= maxWidth) {
			return str;
		} else {
			return str.substring(0, maxWidth - 3) + "...";
		}
	}

	public String getAllCaptions() {
		int nrImages = getImages().size();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nrImages; i++) {
			try {
				String val = getComponent("images", i + ":caption");
				if (val != null) {
					sb.append(val).append(" ");
				}
			} catch (CMException e) {
				LOGGER.log(Level.WARNING, "Problems retrieving component with caption text for image in gallery");
			}
		}
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<ContentId> getImages() {
		return getRepresentedContent();
	}

	/**
	 * Get the file filter that validates the file to be uploaded.
	 *
	 * @return the file filter for image gallery
	 */
	public FileFilter getFileFilter() {
		return FILE_FILTER;
	}

	@Override
	public void applyRepresentation(final MediaGallery_v1 mediaGallery)
			throws RepresentationApplicationFailedException, RuntimeException {
		mediaGalleryAwareAdapter.applyRepresentation(mediaGallery);

		Map<String, String> customAttributes = mediaGallery.getCustomAttributes();
		if (customAttributes.containsKey(HermesCustomField.securityParentId.toString())){
			ExternalContentId parentExternalId = new ExternalContentId(customAttributes.get(HermesCustomField.securityParentId.toString()));
			try {
				this.setSecurityParentId(parentExternalId);
			} catch (CMException e) {
				logger.log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}

	@Override
	public MediaGallery_v1 getRepresentation(final MediaGallery_v1 mediaGallery, final RepresentationFactory factory)
			throws RepresentationApplicationFailedException, RuntimeException {
		return mediaGalleryAwareAdapter.getRepresentation(mediaGallery, factory);
	}
}
