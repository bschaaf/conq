/*
 *  (c) Polopoly AB (publ).
 *  This software is protected by copyright law and international copyright
 *  treaties as well as other intellectual property laws and treaties.
 *  All title and rights in and to this software and any copies thereof
 *  are the sole property of Polopoly AB (publ).
 *  Polopoly is a registered trademark of Polopoly AB (publ).
 */
package it.conquiste.cm.widget;

import com.polopoly.cm.app.widget.ContentListEntryContainer;
import com.polopoly.cm.app.widget.ContentListEntryWidget;
import com.polopoly.cm.app.widget.OComplexFieldPolicyWidget;
import it.conquiste.cm.teaser.TeaserPolicy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.ContentReference;
import com.polopoly.cm.app.Editor;
import com.polopoly.cm.app.PolicyWidget;
import com.polopoly.cm.app.Viewer;
import com.polopoly.cm.app.orchid.widget.ContentListEntryButtons;
import com.polopoly.cm.app.policy.ConfigurableContentListWrapper;
import com.polopoly.cm.app.policy.ContentListInsertionHook;
import com.polopoly.cm.app.policy.CyclicReferenceUtil;
import com.polopoly.cm.app.util.ContentItemFactory;
import com.polopoly.cm.app.util.LockUtil;
import com.polopoly.cm.app.util.PolicyCMServerVersionUtil;
import com.polopoly.cm.app.util.PolicyWidgetUtil;
import com.polopoly.cm.app.util.impl.GuiLocalizationUtil;
import com.polopoly.cm.app.widget.OContentListAwareEditorPolicyWidget.ListEvent;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.client.ContentListAware;
import com.polopoly.cm.client.ContentRead;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.client.OperationNotAllowedException;
import com.polopoly.cm.client.ReferenceMetaData;
import com.polopoly.cm.collections.ContentList;
import com.polopoly.cm.collections.ContentListRead;
import com.polopoly.cm.i18n.LocalizationUtil;
import com.polopoly.cm.i18n.LocalizationVariant;
import com.polopoly.cm.policy.ContentListWrapper;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.common.collections.Pair;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.ajax.AjaxEvent;
import com.polopoly.orchid.ajax.JSCallback;
import com.polopoly.orchid.ajax.event.ClickEvent;
import com.polopoly.orchid.ajax.lifecyclehook.OAjaxBusyIndicator;
import com.polopoly.orchid.ajax.listener.StandardAjaxEventListener;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.event.OrchidEvent;
import com.polopoly.orchid.event.WidgetEventListener;
import com.polopoly.orchid.js.JSExtendedClipboardClient;
import com.polopoly.orchid.js.JSWidget;
import com.polopoly.orchid.js.JSWidgetUtil;
import com.polopoly.orchid.util.MessageUtil;
import com.polopoly.orchid.util.WidgetUtil;
import com.polopoly.orchid.widget.OCheckbox;
import com.polopoly.orchid.widget.OHidden;
import com.polopoly.orchid.widget.OLabel;
import com.polopoly.orchid.widget.OSelect;
import com.polopoly.orchid.widget.OWidget;
import com.polopoly.orchid.widget.impl.OAjaxButton;
import com.polopoly.tools.publicapi.annotations.PublicApi;
import com.polopoly.util.LocaleUtil;
import com.polopoly.util.StringUtil;

/**
 * Policy widget for content lists that implements
 * {@link com.polopoly.cm.app.widget.ContentListEntryContainer}. It works similarly to
 * {@link com.polopoly.cm.app.widget.OContentListAwareEditorPolicyWidget} and
 * {@link com.polopoly.cm.app.widget.OContentListAwareViewerPolicyWidget}, but contains other types
 * of widgets. <code>ContentListEntryContainer</code> works together with
 * {@link com.polopoly.cm.app.widget.ContentListEntryWidget}s that are instantiated with
 * {@link ContentItemFactory}. These types of widgets are easier to customize.
 * Also, this policy widget works with {@link ContentListWrapper}s that allow
 * you to override the methods on a content list.
 * @since 9.7
 */
@PublicApi
@SuppressWarnings({ "serial", "deprecation" })
public class OContentListPolicyWidget extends OComplexFieldPolicyWidget
        implements JSExtendedClipboardClient,
        ContentListEntryContainer,
        Editor,
        Viewer,
        JSWidget
{
    protected static final String CLASS = OContentListPolicyWidget.class.getName();
    protected static final Logger logger = Logger.getLogger(CLASS);

    protected boolean showCopyButton;
    protected boolean showPasteButton;
    protected boolean showCutButton;
    protected boolean showCheckboxes;
    protected boolean showActionMenu;
    protected boolean showMoveButtons;
    protected boolean showActionButtons;
    protected boolean showActionButtonsInViewMode;
    protected boolean showLanguageFilter;
    private   boolean showDraghandleInViewMode;

    protected boolean sendFormOnPaste;
    private boolean forceReload;

    private boolean isSortable = true;

    protected boolean alwaysSyncWithContentList = false;

    /** The list of entry widgets */
    protected ArrayList<ContentListEntryWidget> entryWidgets;
    protected String friendlyName;
    private String[] contextNames;
    protected String contextName; // Name to get entry widgets with

    protected OHidden itemOrder;
    protected OHidden pastedItems;
    protected OHidden itemCount;
    /**
     * @deprecated No longer used
     */
    protected OHidden cutItems;
    private OHidden listItems;

    private int listMaxSize = -1;
    private boolean allowAddToFullList = false;

    private boolean pasteToEnd = false;

    private OAjaxButton showAllButton;

    private OCheckbox selectAll;
    private OLabel selectAllLabel;

    private OSelect languageFilterSelect;
    private OCheckbox languageFilterCheckbox;

    private ContentId[] allowedInputTemplateIds;
    private ContentId[] disallowedInputTemplateIds;
    private Set<String> acceptedResourceTypes;

    private static final String OPTION_NO_FILTER = "nofilter";

    private static final String OPTION_ONLY_ACTIVE_VARIANTS =
            "onlyactivevariants";
    private static final String OPTION_NOT_ACTIVE_VARIANTS =
            "notactivevariants";
    private boolean renderChildPolicyWidgets;

    private List<WidgetEventListener> listEventListeners;

    // Variables to "FIX" initialization problems causing
    // lots of unneeded work in some situations

    private boolean isReadOnlyContentList = true;
    private ContentList contentListUnderInit = null;

    private int maxEntriesToRender;
    private boolean renderAll = false;
    private boolean callInsertionHooks;
    private OAjaxBusyIndicator busyIndicator;
    private String tocClassName;
    private boolean disableReorder;
    private boolean showEmptyTocMessage = true;
    private boolean showSelectAllDivWhenEmpty = true;
    private List<ContentId> addedEntries = new ArrayList<ContentId>();

    public void initSelf(OrchidContext oc)
            throws OrchidException
    {
        try {
            contentListUnderInit = getContentList();
            isReadOnlyContentList = contentListUnderInit.isReadOnly();

            initParameters();

            pastedItems = new OHidden();
            addAndInitChild(oc, pastedItems);

            itemOrder = new OHidden();
            addAndInitChild(oc, itemOrder);

            listItems = new OHidden();
            addAndInitChild(oc, listItems);

            itemCount = new OHidden();
            addAndInitChild(oc, itemCount);

            showAllButton = new OAjaxButton() {
                @Override
                public void onClick(OrchidContext oc, ClickEvent clickEvent)
                        throws OrchidException
                {
                    renderAll = !renderAll;
                    refreshEntryWidgetsOnRenderAllChange(oc);
                }
            };
            showAllButton.updateMessageAreaOnClick(true);
            showAllButton.setUpdateWidgetOnClick(this);

            showAllButton.setLabel(LocaleUtil.format("cm.label.ShowAll", oc.getMessageBundle()));
            addAndInitChild(oc, showAllButton);

            setupAddToListListener(oc);

            entryWidgets = new ArrayList<ContentListEntryWidget>();

            if (showLanguageFilter) {
                languageFilterSelect = new OSelect();
                addAndInitChild(oc, languageFilterSelect);

                languageFilterSelect.addOption(LocaleUtil.format(
                        "cm.label.localization.language.noFilter", oc.getMessageBundle()), OPTION_NO_FILTER);

                languageFilterSelect.addOption(LocaleUtil.format(
                        "cm.label.localization.language.showCurrent", oc.getMessageBundle()), OPTION_ONLY_ACTIVE_VARIANTS);

                languageFilterSelect.addOption(LocaleUtil.format(
                        "cm.label.localization.language.hideCurrent", oc.getMessageBundle()),OPTION_NOT_ACTIVE_VARIANTS);

                languageFilterSelect.setSelectedValue(OPTION_NO_FILTER);
                languageFilterSelect.setOnChange("sendForm();");
                languageFilterCheckbox = new OCheckbox();

                addAndInitChild(oc, languageFilterCheckbox);
                languageFilterCheckbox.setOnChange("sendForm();");
            }

            if (showCheckboxes) {
                selectAll = new OCheckbox();
                addAndInitChild(oc, selectAll);

                selectAllLabel = new OLabel();
                addAndInitChild(oc, selectAllLabel);

                selectAllLabel.setText(LocaleUtil.format("cm.label.SelectAll", oc.getMessageBundle()));

                selectAll.setOnClick("Jasmine.findWidgetById(\""
                        + getCompoundId() + "\", \""
                        + WidgetUtil.getFrameName(this)
                        + "\").setAllCheckboxes(this.checked);");

                selectAllLabel.setTargetId(selectAll.getCompoundId());
            }
            if (disableReorder) {
                setSortable(false);
            }
            buildListWidgets(oc);
        } finally {
            contentListUnderInit = null;
        }
    }

    private void setupAddToListListener(OrchidContext oc)
            throws OrchidException {
        StandardAjaxEventListener addToListListener = new StandardAjaxEventListener() {

            public boolean triggeredBy(OrchidContext oc, AjaxEvent e) {
                return true;
            }

            public JSCallback processEvent(OrchidContext oc, AjaxEvent e)
                    throws OrchidException {
                return null;
            }
        };
        addToListListener.addRenderWidget(this);
        addToListListener.addDecodeWidget(this);
        getTree().registerAjaxEventListener(this, addToListListener);

        busyIndicator = new OAjaxBusyIndicator();
        addAndInitChild(oc, busyIndicator);
    }

    protected void initParameters() {
        contextNames = getParameterAsContextArray("entryContextNames");

        contextName = PolicyUtil.getParameter("entryContextName", "TOCENTRY", getPolicy());
        contextName = getContentSession().getContextName() + "_" + contextName;

        friendlyName = PolicyUtil.getParameter("friendlyName", getPolicy());

        showPasteButton =  PolicyUtil.getParameterAsBoolean("showPasteButton",
                true, getPolicy());

        showCopyButton = PolicyUtil.getParameterAsBoolean("showCopyButton",
                true, getPolicy());

        showCutButton = PolicyUtil.getParameterAsBoolean("showCutButton",
                true, getPolicy());

        showCheckboxes = PolicyUtil.getParameterAsBoolean("showCheckboxes",
                false, getPolicy());

        showActionButtons = PolicyUtil.getParameterAsBoolean("showActionButtons",
                true, getPolicy());

        showActionButtonsInViewMode = PolicyUtil.getParameterAsBoolean("showActionButtonsInViewMode",
                true, getPolicy());

        showMoveButtons = PolicyUtil.getParameterAsBoolean("showMoveButtons",
                false, getPolicy());

        showActionMenu = PolicyUtil.getParameterAsBoolean("showActionMenu",
                true, getPolicy());

        sendFormOnPaste = PolicyUtil.getParameterAsBoolean("sendFormOnPaste",
                true, getPolicy());

        showDraghandleInViewMode = PolicyUtil.getParameterAsBoolean("showDraghandleInViewMode",
                false, getPolicy());

        forceReload = PolicyUtil.getParameterAsBoolean("forceReload", forceReload, getPolicy());

        showLanguageFilter =  PolicyUtil.getParameterAsBoolean("showLanguageFilter",
                false, getPolicy());

        alwaysSyncWithContentList = PolicyUtil.getParameterAsBoolean("alwaysSyncWithContentList",
                false, getPolicy());

        renderChildPolicyWidgets = PolicyUtil.getParameterAsBoolean("renderChildPolicyWidgets",
                false, getPolicy());

        maxEntriesToRender = PolicyUtil.getParameterAsInt("maxEntriesToRender",
                30, getPolicy());
        callInsertionHooks = PolicyUtil.getParameterAsBoolean("callInsertionHooks",
                false, getPolicy());

        tocClassName = PolicyUtil.getParameter("tocClassName", "toc", getPolicy());
        disableReorder = PolicyUtil.getParameterAsBoolean("disableSortableReorder", false, getPolicy());

        showEmptyTocMessage = PolicyUtil.getParameterAsBoolean("showEmptyTocMessage", true, getPolicy()); // Undocumented
        showSelectAllDivWhenEmpty = PolicyUtil.getParameterAsBoolean("showSelectAllDivWhenEmpty", true, getPolicy()); // Undocumented

        pasteToEnd = "last".equals(PolicyUtil.getParameter("pastePosition", "first", getPolicy()));

        try {
            acceptedResourceTypes = new HashSet<String>();

            String types = PolicyUtil.getParameter("acceptResourceTypes", "", getPolicy());

            StringTokenizer st = new StringTokenizer(types, ", ");
            while (st.hasMoreTokens()) {
                String type = st.nextToken();
                acceptedResourceTypes.add(type);
            }

            if (getContentList() instanceof ConfigurableContentListWrapper) {
                allowedInputTemplateIds =
                        ((ConfigurableContentListWrapper) getContentList()).
                                getAllowedInputTemplatesFromTemplateStructure();

                ContentListRead list = null;

                if (allowedInputTemplateIds == null ||
                        allowedInputTemplateIds.length == 0) {
                    list = ((ConfigurableContentListWrapper) getContentList()).getAllowedInputTemplates();

                    if (list != null) {
                        allowedInputTemplateIds = new ContentId[list.size()];

                        for (int i = 0; i < list.size(); i++) {
                            ContentReference entry = list.getEntry(i);
                            allowedInputTemplateIds[i] = entry.getReferredContentId();
                        }
                    }
                }

                list = ((ConfigurableContentListWrapper) getContentList()).getDisallowedInputTemplates();

                if (list != null) {
                    disallowedInputTemplateIds = new ContentId[list.size()];

                    for (int i = 0; i < list.size(); i++) {
                        ContentReference entry = list.getEntry(i);
                        disallowedInputTemplateIds[i] = entry.getReferredContentId();
                    }
                }
            }

            listMaxSize = getContentList().getMaxSize();
            allowAddToFullList = getContentList().allowAddToFullList();

        } catch (Exception e) {
            logger.logp(Level.WARNING, CLASS, "initSelf",
                    "Could not initialize requiredInputTemplateIds", e);
        }
    }

    private String[] getParameterAsContextArray(String parameterName)
    {
        String contexts = PolicyUtil.getParameter(parameterName, getPolicy());
        if (contexts == null) {
            return null;
        }
        String ctx = getContentSession().getContextName();
        List<String> result = new ArrayList<String>();
        for (String context : contexts.split(",")) {
            if (context.trim().length() > 0) {
                result.add(ctx + "_" + context);
            }
        }
        if (result.size() == 0) {
            return null;
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Builds the list of entry widgets by looping over the underlying content
     * list
     */
    public void buildListWidgets(OrchidContext oc)
    {
        try {
            ContentList contentList = getContentList();

            if (contentList == null) {
                logger.logp(Level.WARNING,
                        CLASS, "buildListWidgets",
                        "Could not retrieve content list");
                return;
            }

            int size = getSizeToRender(contentList);

            for (int i = 0; i < size; i++) {
                ContentReference contentRef = contentList.getEntry(i);
                initEntry(oc, contentRef, -1, entryWidgets, false);
            }

            itemCount.setValue(String.valueOf(size));
        } catch (OperationNotAllowedException e) {
            logger.logp(Level.FINER,
                    CLASS, "initContentListEntryWidgets",
                    "Do not have permission to access content list for " +
                            getPolicy().getContentId(), e);
        } catch (CMException cme) {
            handleError(cme, oc);
        }
    }

    private int getSizeToRender(ContentList contentList)
    {
        int listSize = contentList.size();

        if (renderAll || maxEntriesToRender < 0) {
            return listSize;
        } else {
            return Math.min(listSize, maxEntriesToRender);
        }
    }

    /**
     * Rebuilds the list widgets from the underlying list widgets
     *
     * @param oc
     */
    public void rebuildListWidgets(OrchidContext oc)
    {
        if (isInitialized()) {
            try {
                ContentList contentList = getContentList();

                if (contentList == null) {
                    logger.logp(Level.WARNING,
                            CLASS, "buildListWidgets",
                            "Could not retrieve content list");
                    return;
                }

                int entriesToInit = getSizeToRender(contentList);

                ArrayList<ContentListEntryWidget> newEntryWidgets = new ArrayList<ContentListEntryWidget>(entriesToInit);
                // Initialize empty array list
                for (int i = 0; i < entriesToInit; i++) {
                    newEntryWidgets.add(null);
                }

                contentListUnderInit = contentList; // Optimization while rebuilding

                // Try to sync with the existing entry widgets with the
                // underlying content list.
                // First check all existing entries, if they have been moved
                // or removed. Then add new entries.

                // Check existing entries
                int listIndex = 0;
                Set<Integer> skipIndices = new HashSet<Integer>();

                for (int i = 0; i < entryWidgets.size(); i++) {
                    ContentListEntryWidget entryWidget = entryWidgets.get(i);
                    Policy entryPolicy = entryWidget.getPolicy();

                    if (entryPolicy == null) {
                        continue;
                    }

                    int newEntryPos = findIdInList(entryPolicy.getContentId(), contentList,
                            listIndex, skipIndices);
                    if (newEntryPos == -1) {
                        // Entry has been removed from content list, remove from
                        //entries too.
                        discardChild(entryWidget);
                        continue;
                    }
                    if (newEntryPos == listIndex) {
                        // increase counter for the start position for findInList
                        // just a small optimisation
                        listIndex++;
                    }
                    entryWidget.setIndex(newEntryPos);
                    newEntryWidgets.set(newEntryPos, entryWidget);
                }

                // Add new entries

                for (int i = 0; i < entriesToInit; i++) {
                    if (newEntryWidgets.get(i) == null) {
                        ContentReference newRef = contentList.getEntry(i);
                        initEntry(oc, newRef, i, newEntryWidgets, true);
                    }
                }

                entryWidgets = newEntryWidgets;
                itemCount.setValue(String.valueOf(entryWidgets.size()));
            } catch (CMException cme) {
                handleError(cme, oc);
            } finally {
                contentListUnderInit = null;
            }
        }
    }

    private void refreshEntryWidgetsOnRenderAllChange(OrchidContext oc)
    {
        try {
            decode(oc);
            ContentList contentList = getContentList();

            if (contentList == null) {
                logger.logp(Level.WARNING,
                        CLASS, "buildListWidgets",
                        "Could not retrieve content list");
                return;
            }

            int entriesToInit = getSizeToRender(contentList);
            ArrayList<ContentListEntryWidget> newEntryWidgets = new ArrayList<ContentListEntryWidget>(entriesToInit);

            for (int i = 0; i < entriesToInit; i++) {
                if (entryWidgets.size() > i) {
                    // Copy existing widgets
                    newEntryWidgets.add(entryWidgets.get(i));
                }
                else {
                    // Create new widgets displayed
                    newEntryWidgets.add(null);
                    ContentReference newRef = contentList.getEntry(i);
                    initEntry(oc, newRef, i, newEntryWidgets, true);
                }
            }
            // Clean up old entries no longer rendered.
            for (int i = entriesToInit ; i < entryWidgets.size() ; i++) {
                discardChild(entryWidgets.get(i));
            }

            entryWidgets = newEntryWidgets;
            itemCount.setValue(String.valueOf(entryWidgets.size()));
        } catch (CMException cme) {
            handleError(cme, oc);
        } catch (OrchidException e) {
            handleError(e, oc);
        }
    }

    /**
     * Finds the position of the id in the content list, starting from
     * startIndex and skipping indices in skipIndices. Returns the position
     * or -1 if not found.
     * @since 9.8
     * @throws CMException
     */
    private int findIdInList(ContentId id,
                             ContentList contentList,
                             int startIndex,
                             Set<Integer> skipIndices)
            throws CMException
    {
        int size = Math.min(contentList.size(), maxEntriesToRender);

        for (int i = startIndex; i < size; i++) {
            if (skipIndices.contains(new Integer(i))) {
                continue;
            }

            ContentReference ref = contentList.getEntry(i);
            ContentId refId = ref.getReferredContentId();
            if (ref.getReferenceMetaDataId() != null) {
                refId = ref.getReferenceMetaDataId();
            }

            if (id.equalsIgnoreVersion(refId)) {
                skipIndices.add(new Integer(i));
                return i;
            }
        }

        return -1;
    }

    private void initEntry(OrchidContext oc,
                           ContentReference contentReference,
                           int index,
                           List<ContentListEntryWidget> widgets,
                           boolean replace)
            throws CMException
    {
        try {
            ContentId contentListEntryId = contentReference.getReferenceMetaDataId();
            ContentId referredId = contentReference.getReferredContentId();

            Policy entryPolicy = null;
            PolicyCMServer cmServer = getContentSession().getPolicyCMServer();

            // Check if RMD exists
            if (contentListEntryId != null
                    && PolicyCMServerVersionUtil.contentExists(cmServer, contentListEntryId)) {

                entryPolicy = PolicyCMServerVersionUtil.getPolicy(cmServer, contentListEntryId);
                logger.finer("entryPolicy: " + entryPolicy + " id: " +
                        referredId);
            }

            // If it didn't, check if referred content exists
            if (entryPolicy == null
                    && PolicyCMServerVersionUtil.contentExists(cmServer, referredId)) {

                entryPolicy = PolicyCMServerVersionUtil.getPolicy(cmServer, referredId);
                logger.finer("entryPolicy: " + entryPolicy + " id: " +
                        referredId);
            }

            ContentListEntryWidget entryWidget;

            if (entryPolicy != null) {
                if (contextNames != null) {
                    entryWidget = ContentItemFactory.getContentListEntryWidget(entryPolicy, contextNames, cmServer);
                } else {
                    entryWidget = ContentItemFactory.getContentListEntryWidget(entryPolicy, contextName, cmServer);
                }
            } else {
                //Can happen if content is removed. ODefaultTocItemPolicyWidget
                //handles policy == null
                entryWidget = ContentItemFactory.
                        getDefaultContentListEntryWidget(contextName);
            }

            // TODO: register widget
            if (entryWidget instanceof PolicyWidget) {
                ((PolicyWidget)entryWidget).setContentSession(getContentSession());
            }

            entryWidget.setContentListEntryContainer(this);

            if (index >= 0 && index <= widgets.size()) {
                if (replace) {
                    widgets.set(index, entryWidget);
                } else {
                    widgets.add(index, entryWidget);
                }
            } else {
                widgets.add(entryWidget);
                index = widgets.size() - 1;
            }

            entryWidget.setIndex(index);
            addAndInitChild(oc, entryWidget);
        } catch (OperationNotAllowedException e) {
            logger.logp(Level.FINER,
                    CLASS, "initContentListEntryWidgets",
                    "Do not have permission to access " +
                            contentReference, e);
        } catch (OrchidException e) {
            logger.logp(Level.WARNING,
                    CLASS, "initContentListEntryWidgets",
                    "Could not add widget for " +
                            contentReference, e);
        }
    }

    private boolean listContentHasChanged()
    {
        return !StringUtil.isEmpty(pastedItems.getValue())
                || !StringUtil.isEmpty(listItems.getValue());
    }

    private static class ChangeResult
    {
        public final List<Pair<ContentListEntryWidget, ContentId>> resultList;
        public final List<Pair<ContentListEntryWidget, ContentId>> listOfRemovedEntries;

        public ChangeResult(final List<Pair<ContentListEntryWidget,ContentId>> resultList,
                            final List<Pair<ContentListEntryWidget, ContentId>> listOfRemovedEntries)
        {
            super();

            this.resultList = resultList;
            this.listOfRemovedEntries = listOfRemovedEntries;
        }
    }

    private ChangeResult getListChange()
            throws OrchidException
    {
        List<Pair<String, String>> newList = getListItemEntries();
        List<Pair<String, String>> oldList = getCurrentListItemEntries();
        List<Pair<String, String>> removed = new ArrayList<Pair<String,String>>();

        for (Pair<String, String> newItem : newList) {
            int index = oldList.indexOf(newItem);

            if (index != -1) {
                oldList.remove(index);
            }
        }

        removed = oldList;
        return new ChangeResult(convertListResult(newList), convertListResult(removed));
    }

    private List<Pair<ContentListEntryWidget, ContentId>> convertListResult(final List<Pair<String, String>> list)
            throws OrchidException
    {
        List<Pair<ContentListEntryWidget, ContentId>> result = new ArrayList<Pair<ContentListEntryWidget,ContentId>>();

        for (Pair<String, String> item : list) {
            ContentListEntryWidget widget = null;

            for (Object entry : entryWidgets) {
                ContentListEntryWidget entryWidget = (ContentListEntryWidget) entry;

                if (entryWidget.getCompoundId().equals(item.car())) {
                    widget = entryWidget;
                    break;
                }
            }

            result.add(new Pair<ContentListEntryWidget, ContentId>(widget, ContentIdFactory.createContentId(item.cdr())));
        }

        return result;
    }

    private List<Pair<String, String>> getListItemEntries()
            throws OrchidException
    {
        List<Pair<String, String>> list;

        if (!StringUtil.isEmpty(listItems.getValue())) {
            list = parseListItemEntries();
        } else {
            list = getCurrentListItemEntries();
        }

        addPastedItemsToList(list);

        return list;
    }

    private void addPastedItemsToList(List<Pair<String, String>> list) {
        if (!StringUtil.isEmpty(pastedItems.getValue())) {
            List<Pair<Integer, String>> pastedItems = parsePastedItems();
            Collections.sort(pastedItems, new Comparator<Pair<Integer, String>>() {
                public int compare(Pair<Integer, String> o1,
                                   Pair<Integer, String> o2) {
                    return o1.car().compareTo(o2.car());
                }
            });
            for (Pair<Integer, String> pair : pastedItems) {
                int index = pair.car();
                if (index < 0) {
                    list.add(new Pair<String, String>(null, pair.cdr()));
                } else {
                    list.add(pair.car(), new Pair<String, String>(null, pair.cdr()));
                }
            }
        }
    }

    private List<Pair<Integer, String>> parsePastedItems()
    {
        List<Pair<Integer, String>> items = new ArrayList<Pair<Integer,String>>();

        String listItemsString = pastedItems.getValue();

        StringTokenizer st = new StringTokenizer(listItemsString, ",");

        while (st.hasMoreTokens()) {
            String listItem = st.nextToken().trim();
            String[] listItemParts = listItem.split(":");

            int position = Integer.parseInt(listItemParts[0]);
            String contentIdString = listItemParts[1];
            Pair<Integer, String> item = new Pair<Integer, String>(position, contentIdString);
            items.add(item);
        }

        return items;
    }

    private List<Pair<String, String>> getCurrentListItemEntries()
            throws OrchidException
    {
        List<Pair<String, String>> items = new ArrayList<Pair<String,String>>();

        for (Object entryObject : entryWidgets) {
            ContentListEntryWidget widget = (ContentListEntryWidget) entryObject;

            Policy policy = widget.getPolicy();
            if (policy != null) {
                items.add(new Pair<String, String>(widget.getCompoundId(),
                        policy.getContentId().getContentId().getContentIdString()));
            }
        }

        return items;
    }

    private List<Pair<String, String>> parseListItemEntries()
    {
        List<Pair<String, String>> items = new ArrayList<Pair<String,String>>();

        String listItemsString = listItems.getValue();

        StringTokenizer st = new StringTokenizer(listItemsString, ",");

        while (st.hasMoreTokens()) {
            String listItem = st.nextToken().trim();
            String[] listItemParts = listItem.split(":");

            String compoundId = listItemParts[0];
            String contentId = listItemParts[1];

            Pair<String, String> item = new Pair<String, String>(compoundId, contentId);
            items.add(item);
        }

        return items;
    }

    private void handleListChanges(OrchidContext oc)
    {
        try {
            if (listContentHasChanged()) {
                ContentList contentList = getContentList();

                ChangeResult listChange = getListChange();

                if (wasItemInsertedAtEndOfFullList(contentList, listChange) ||
                        wasItemDraggedToEndOfFullList(contentList))
                {
                    handleError(oc, LocaleUtil.format("cm.msg.CouldNotAddToTheEndOfAFullList",
                            oc.getMessageBundle()));

                    return;
                }

                if (wasItemPastedAtEndOfUnexpandedList(contentList))
                {
                    handleError(oc, LocaleUtil.format("cm.msg.CouldNotPasteToTheEndOfAnUnexpandedList",
                            oc.getMessageBundle()));
                    return;
                }

                performRemovesOnContentList(listChange.resultList, listChange.listOfRemovedEntries, contentList);
                performRearrangesOnContentList(listChange.resultList, contentList);
                performAddsOnContentList(listChange.resultList, contentList, new AddToContentListAndWidgetList());

                for (Pair<ContentListEntryWidget, ContentId> removed : listChange.listOfRemovedEntries) {
                    entryWidgets.remove(removed.car());
                    discardChild(removed.car());
                    triggerListEvent(oc, new ListEvent(removed.car(), ListEvent.REMOVE_EVENT));
                }

                itemCount.setValue(String.valueOf(entryWidgets.size()));
            }
        } catch (Exception cme) {
            String error = LocaleUtil.format("cm.msg.CouldNotCutOrReorderContentList",
                    oc.getMessageBundle());
            if (cme instanceof CMException) {
                String loc_err = ((CMException)cme).getLocalizedMessage();
                if (!StringUtil.isEmpty(loc_err)) {
                    error = error.concat("\n "+ loc_err);
                }
            }
            handleError(error, cme, oc);

            return;
        } finally {
            resetClientState();
            /* Sort entry widgets based on their idea of their placement in the content list.
             * If we don't do this then the following updateIndices() will screw up the indexing
             * so it no longer matches the order in the content list (happens on autosave). */
            sortWidgets();
            updateIndices();
        }
    }

    /**
     * Make the oldList partially match newList by removing all the entries that were removed in the GUI.
     * Each time an entry is removed all widgets after it is shifted down (index is updated with - 1) to match the backing contentlist.
     */
    void performRemovesOnContentList(final List<Pair<ContentListEntryWidget, ContentId>> newList,
                                     final List<Pair<ContentListEntryWidget, ContentId>> removedList,
                                     final ContentList oldList)
    {
        for (int i = removedList.size() - 1; i >= 0; i--) {
            Pair<ContentListEntryWidget, ContentId> removed = removedList.get(i);

            for (int j = 0; j < newList.size() ; j++) {
                ContentListEntryWidget moved = newList.get(j).car();

                if (moved != null) {
                    if (moved.getIndex() > removed.car().getIndex()) {
                        moved.setIndex(moved.getIndex() - 1);
                    }
                }
            }

            oldList.remove(removed.car().getIndex());
        }
    }

    /**
     * Make the oldList partially match newList by rearraning all existing entries to match the final order.
     * Each time an entry is moved back in the oldList all existing widgets between that position and the original is shifted back (index is updated with + 1)
     */
    void performRearrangesOnContentList(final List<Pair<ContentListEntryWidget, ContentId>> newList,
                                        final ContentList oldList)
            throws CMException
    {
        int index = 0;

        for (int i = 0 ; i < newList.size() ; i++) {
            ContentListEntryWidget entry = newList.get(i).car();

            if (entry != null) {
                oldList.move(entry.getIndex(), index);

                for (int j = i + 1; j < newList.size() ; j++) {
                    ContentListEntryWidget other = newList.get(j).car();

                    if (other != null) {
                        if (other.getIndex() < entry.getIndex()) {
                            other.setIndex(other.getIndex() + 1);
                        }
                    }
                }

                entry.setIndex(index);
                index++;
            }
        }
    }

    /**
     * This is used to avoid having to mock the widget tree when testing the reorder code.
     */
    static interface PerformAdd
    {
        void add(ContentList contentList, int index, ContentId contentIdToInsert) throws CMException;
    }

    class AddToContentListAndWidgetList
            implements PerformAdd
    {
        public void add(ContentList contentList, int index, ContentId contentIdToInsert)
                throws CMException
        {
            PolicyCMServer cmServer = getPolicy().getCMServer();
            Policy policyToInsert = PolicyCMServerVersionUtil.getPolicy(cmServer, contentIdToInsert);

            if (callInsertionHooks && policyToInsert instanceof ContentListInsertionHook) {
                contentIdToInsert = ((ContentListInsertionHook) policyToInsert).onInsert(getPolicy().getContentId(),
                        contentList.getContentListStorageGroup(), index);

                // Note that the ID gets unversioned to prevent bad hook impls from getting versioned refs in content lists
                contentIdToInsert = contentIdToInsert.getContentId();
            }

            addEntry(createContentRefMeta(contentIdToInsert), index);
        }
    }

    /**
     * Make the oldList fully match newList by adding new entries to the list.
     * Each time a new entry is added all existing widgets after it is shifted right.
     */
    void performAddsOnContentList(final List<Pair<ContentListEntryWidget, ContentId>> newList,
                                  final ContentList contentList,
                                  final PerformAdd performer)
            throws CMException
    {
        for (int i = 0 ; i < newList.size(); i++) {
            if (newList.get(i).car() == null) {
                performer.add(contentList, i, newList.get(i).cdr());

                for (int j = i + 1 ; j < newList.size() ; j++) {
                    ContentListEntryWidget other = newList.get(j).car();

                    if (other != null) {
                        other.setIndex(other.getIndex() + 1);
                    }
                }
            }
        }
    }

    private void resetClientState() {
        this.pastedItems.setValue("");
        this.listItems.setValue("");
    }

    /**
     * This is concerned with paste and cross-frame drag-n-drop
     */
    private boolean wasItemInsertedAtEndOfFullList(ContentList contentList, ChangeResult listChange)
            throws CMException
    {
        List<Pair<Integer, String>> parsePastedItems = parsePastedItems();
        if (contentList.getMaxSize() != -1 && parsePastedItems.size() > 0) {
            List<Pair<ContentListEntryWidget, ContentId>> result = listChange.resultList;

            for (int i = contentList.getMaxSize(); i < result.size(); i++) {
                /* No widget means new item (as opposed to having been pushed beyond
                   the limit by a new item further up). */
                if (result.get(i).car() == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This is concerned with in-frame drag-n-drop, which retains the item widget
     */
    private boolean wasItemDraggedToEndOfFullList(ContentList contentList)
            throws OrchidException, CMException
    {
        List<Pair<String, String>> newList = parseListItemEntries();
        List<Pair<String, String>> oldList = getCurrentListItemEntries();
        return contentList.getMaxSize() != -1
                && newList.size() > contentList.getMaxSize()
                && !(newList.get(newList.size() - 1).equals(oldList.get(oldList.size() - 1)));
    }

    private boolean wasItemPastedAtEndOfUnexpandedList(ContentList contentList)
            throws OrchidException, CMException
    {
        List<Pair<Integer, String>> parsedPastedItems = parsePastedItems();
        boolean fullyExpanded = contentList.size() <= maxEntriesToRender || renderAll || maxEntriesToRender < 0;
        if (!fullyExpanded) {
            for (Pair<Integer, String> item : parsedPastedItems) {
                // Paste when pastePosition = 'last' have negative index
                if (item.car() < 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private ContentReference createContentRefMeta(ContentId contentId) throws CMException
    {
        Policy policy = PolicyCMServerVersionUtil.getPolicy(getPolicy().getCMServer(), contentId);
        if (policy instanceof ReferenceMetaData) {
            return new ContentReference(((ReferenceMetaData) policy).getReferredContentId(), contentId);
        }
        return new ContentReference(contentId, null);
    }

    protected void updateIndices()
    {
        for (int i = 0; i < entryWidgets.size(); i++) {
            entryWidgets.get(i).setIndex(i);
        }
    }

    private void sortWidgets()
    {
        Collections.sort(entryWidgets, new Comparator<ContentListEntryWidget>()
        {
            @Override
            public int compare(final ContentListEntryWidget contentListEntryWidget, final ContentListEntryWidget contentListEntryWidget2)
            {
                return contentListEntryWidget.getIndex() - contentListEntryWidget2.getIndex();
            }
        });
    }

    /**
     * Decodes the request to reflect pasted items and reordering of items
     */
    public void postDecode(OrchidContext oc)
            throws OrchidException
    {
        handleListChanges(oc);
    }

    public void updateSelf(OrchidContext oc)
            throws OrchidException
    {
        if (PolicyWidgetUtil.isEditMode(this) || alwaysSyncWithContentList) {
            rebuildListWidgets(oc);
        }

        if (languageFilterSelect != null) {
            PolicyCMServer cmServer = getContentSession().getPolicyCMServer();
            LocalizationVariant currentlyActiveVariant = GuiLocalizationUtil.getCurrentUserLanguageAsVariant(cmServer);
            String currentLanguage = (currentlyActiveVariant == null ? null :currentlyActiveVariant.getVariantName());

            if (currentLanguage != null) {
                languageFilterSelect.setEnabled(true);

                languageFilterSelect.relabelOption(LocaleUtil.
                        format("cm.label.localization.language.showCurrent",
                                new String[] {currentLanguage},
                                oc.getLocale(),
                                oc.getMessageBundle()),
                        OPTION_ONLY_ACTIVE_VARIANTS);

                languageFilterSelect.relabelOption(LocaleUtil.
                        format("cm.label.localization.language.hideCurrent",
                                new String[] {currentLanguage},
                                oc.getLocale(),
                                oc.getMessageBundle()),
                        OPTION_NOT_ACTIVE_VARIANTS);
            } else {
                languageFilterSelect.setEnabled(false);
                languageFilterSelect.setSelectedValue(OPTION_NO_FILTER);
            }

            languageFilterCheckbox.setEnabled(!OPTION_NO_FILTER.
                    equals(languageFilterSelect.getSelectedValue()));
        }

        super.updateSelf(oc);
    }

    public void localRender(OrchidContext oc)
            throws IOException,
            OrchidException
    {
        Device device = oc.getDevice();

        //selectAll must be outside of tocdiv to not be included when fetching
        //checkboxes from the content list with javascript
        if (renderChildPolicyWidgets) {
            renderChildPolicyWidgets(oc);
        }

        boolean isEditMode = isEditable();

        boolean showSelectAll = selectAll != null && entryWidgets.size() > 0;
        if (showSelectAll || showSelectAllDivWhenEmpty) {
            device.println("<div class='selectAll'>");
        }
        if (showSelectAll) {
            selectAllLabel.render(oc);
            device.print("&nbsp;");
            selectAll.render(oc);
        }
        if (showSelectAll || showSelectAllDivWhenEmpty) {
            device.println("</div>");
        }

        device.println("<div style='clear:both;'></div>");

        itemOrder.render(oc);
        listItems.render(oc);
        itemCount.render(oc);
        pastedItems.render(oc);

        device.println("<div id='tocdiv" + getCompoundId() + "' class='" + tocClassName + "'>");

        if (languageFilterSelect != null) {
            languageFilterSelect.render(oc);
        }

        if (languageFilterCheckbox != null) {
            device.printLocalized("cm.label.localization.language.hideUntranslatable");
            languageFilterCheckbox.render(oc);
        }

        boolean entryWidgetHasContent = entryWidgets.size() > 0;

        if (entryWidgetHasContent) {
            PolicyCMServer cmServer = getContentSession().getPolicyCMServer();
            LocalizationVariant currentlyActiveVariant = GuiLocalizationUtil.getCurrentUserLanguageAsVariant(cmServer);
            int countVisible = 0;
            int tempCount = -1;
            for (int i = 0; i < entryWidgets.size(); i++) {
                OWidget widget = entryWidgets.get(i);

                String rowClass;
                if (i % 2 == 0) {
                    rowClass = "even";
                } else {
                    rowClass = "odd";
                }
                if (isEditMode) {
                    rowClass += "edit";
                }

                String entryWidgetInputTemplateIdString = null;
                String entryWidgetPolicyContentIdString = null;
                String contentIdAttr = "";
                String templateIdAttr = "";

                if (widget instanceof ContentListEntryWidget && ((ContentListEntryWidget)widget).getPolicy() != null) {
                    ContentListEntryWidget entryWidget = (ContentListEntryWidget) widget;

                    entryWidgetInputTemplateIdString = getInputTemplateFromEntryWidget(entryWidget).getContentIdString();
                    entryWidgetPolicyContentIdString = entryWidget.getPolicy().getContentId().getContentId().getContentIdString();
                    if (entryWidget.getPolicy() instanceof TeaserPolicy){
                        TeaserPolicy teaserPolicy = (TeaserPolicy)entryWidget.getPolicy();

                        if (teaserPolicy.hasVisible()){
                            countVisible++;
                            tempCount = countVisible;
                        }else{
                            tempCount = -1;
                        }
                    }

                    contentIdAttr = "' polopoly:contentid='" + entryWidgetPolicyContentIdString;
                    templateIdAttr = "' polopoly:inputtemplateid='"+ entryWidgetInputTemplateIdString;
                }

                device.println("<div id=\"listentry_" + widget.getCompoundId()
                        + "\" class='p_listentry " + rowClass
                        + "' polopoly:listindex='" + i
                        + contentIdAttr
                        + templateIdAttr
                        + "' polopoly:widgetid='" + widget.getCompoundId() + "'>");

                if (isEditMode || showDraghandleInViewMode) {
                    boolean entryInEditMode = false;
                    for (Iterator<?> it = widget.getChildren() ; it.hasNext() ; ) {
                        Object that = it.next();
                        if (that instanceof PolicyWidget && PolicyWidgetUtil.isEditMode((PolicyWidget) that)) {
                            entryInEditMode = true;
                        }
                    }

                    if (!entryInEditMode && isSortable) {
                        device.print("<table class='listentrycontainer'><tr class='draghandle'><td class='draghandle' valign='top' rowspan='2'><img src='images/handle.gif' />"+getCustomElement(tempCount)+"</td><td class='listentry'>");
                    } else {
                        device.print("<table class='listentrycontainer'><tr><td valign='top'>&nbsp;</td>"+getCustomElement(tempCount)+"<td class='listentry'>");
                    }
                }else{
                    device.print(getCustomViewModeCoverBegin(tempCount));
                }

                widget.render(oc);

                if (shadeWidget(widget, cmServer, currentlyActiveVariant)) {
                    device.println("<script type='text/javascript' > <!-- \n setOpacity('"+
                            widget.getCompoundId()+"', 0.15); \n //--> </script>");
                }

                if (isEditMode || showDraghandleInViewMode) {
                    device.println("</td></tr></table>");
                }else{
                    device.println(getCustomViewModeCoverEnd());
                }

                device.println("</div>");
            }
            if (!(maxEntriesToRender < 0) && getContentList().size() > maxEntriesToRender && !renderAll) {
                String cssClass = maxEntriesToRender % 2 == 0 ? "even" : "odd";
                device.print("<div class=\"showAllButton " + cssClass + "\">");
                showAllButton.render(oc);
                device.print("</div>");
            }
        } else {
            printEmptyMessage(device);
        }

        device.println("</div>"); // end div class='tocdiv'

        // Busy indicator must come directly after toc div
        busyIndicator.render(oc);

        device.println(String.format("<div id='%s-%s' style='display: none' class='emptySink'>", "emptySink", getCompoundId()));
        if (entryWidgetHasContent) {
            printEmptyMessage(device);
        }
        device.println("</div>");
    }


    private void printEmptyMessage(Device device) throws IOException,
            OrchidException
    {
        device.print(String.format("<div id='%s-%s' class='emptyToc'>", "emptyToc", getCompoundId()));
        if (showEmptyTocMessage) {
            device.printLocalized("cm.msg.TocEmpty");
        }
        device.print("</div>");
    }

    private ContentId getInputTemplateFromEntryWidget(final ContentListEntryWidget entryWidget)
    {
        try {
            InputTemplate inputTemplate = entryWidget.getPolicy().getInputTemplate();
            return inputTemplate.getContentId().getContentId();
        } catch (CMException cme) {
            logger.log(Level.WARNING, "Could not access input template for entry widget!", cme);
        }

        return null;
    }

    private void renderChildPolicyWidgets(OrchidContext oc)
            throws IOException,
            OrchidException
    {
        for (Iterator<?> iter = getChildPolicyWidgets(); iter.hasNext();) {
            OWidget widget = (OWidget) iter.next();
            widget.render(oc);
        }
    }

    private boolean showOnlyActiveVariants()
    {
        return languageFilterSelect != null
                && OPTION_ONLY_ACTIVE_VARIANTS.equals(languageFilterSelect.getSelectedValue());
    }

    private boolean hideActiveVariants()
    {
        return languageFilterSelect != null
                && OPTION_NOT_ACTIVE_VARIANTS.equals(languageFilterSelect.getSelectedValue());
    }

    private boolean shadeWidget(OWidget widget,
                                PolicyCMServer cmServer,
                                LocalizationVariant currentlyActiveVariant)
    {
        try {
            if (currentlyActiveVariant == null) {
                return false;
            }

            if (languageFilterSelect == null ||
                    OPTION_NO_FILTER.equals(languageFilterSelect.getSelectedValue())) {
                return false;
            }

            if (widget instanceof ContentListEntryWidget) {
                Policy policy = ((ContentListEntryWidget) widget).getPolicy();
                ContentRead content = policy.getContent();

                boolean hasCurrentlyActiveVariant = false;

                if (LocalizationUtil.isMainContent(content)) {
                    hasCurrentlyActiveVariant = LocalizationUtil.
                            hasLocalizedInstance(content, currentlyActiveVariant);
                } else if (LocalizationUtil.isLocalizedInstance(content)) {
                    LocalizationVariant widgetVariant = LocalizationUtil.
                            getLocalizationVariantFromInstance(content, cmServer);
                    hasCurrentlyActiveVariant =
                            currentlyActiveVariant.equals(widgetVariant);
                } else {
                    //Always show non-localized content
                    if (languageFilterCheckbox != null) {
                        return languageFilterCheckbox.isChecked();
                    } else {
                        return false;
                    }
                }

                if (showOnlyActiveVariants()) {
                    return ! hasCurrentlyActiveVariant;
                } else if (hideActiveVariants()){
                    return hasCurrentlyActiveVariant;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            logger.logp(Level.WARNING, CLASS, "allowShowWidget",
                    "Exception when inspecting widget", e);
        }

        return false;
    }

    /**
     * Sets the context name used to get the entry widgets
     */
    public void setContextName(String contextName)
    {
        this.contextName = contextName;
    }

    /**
     * Returns an iterator over the entry widgets.
     */
    public Iterator<?> getEntryWidgets()
    {
        return entryWidgets.iterator();
    }

    /**
     * Returns the number of entry widgets.
     */
    public int getNumberOfEntries()
    {
        return entryWidgets.size();
    }

    /**
     * Add content reference to the widget as well as to the underlying list.
     * If the list has reached max size, and if it is allowed to add to the full
     * content list, the last entry will be removed from the underlying list.
     * This only works in edit mode.
     */
    public void addEntry(ContentReference contentRef, int index)
    {
        OrchidContext oc = WidgetUtil.getOrchidContext();

        try {
            if (addEntryToContentList(contentRef, index)) {
                ContentList contentList = getContentList();

                int maxSize = contentList.getMaxSize();
                boolean allowAddToFullList = contentList.allowAddToFullList();

                initEntry(oc, contentRef, index, entryWidgets, false);

                // Remove the last entry from the list iff maxsize is set
                if (maxSize >= 0 && allowAddToFullList && maxSize < entryWidgets.size()) {
                    OWidget entry = entryWidgets.remove(entryWidgets.size() - 1);
                    discardChild(entry);
                    entry.release();
                }
            }
        } catch (CMException cme) {
            handleError(LocaleUtil.format("cm.msg.CouldNotAddEntry", oc.getMessageBundle()), cme, oc);
        } finally {
            updateIndices();
        }
    }

    private boolean addEntryToContentList(ContentReference contentRef, int index)
    {
        OrchidContext oc = WidgetUtil.getOrchidContext();

        try {
            ContentList contentList = getContentList();

            if (contentList == null) {
                logger.logp(Level.WARNING,
                        CLASS, "addEntry",
                        "Could not retrieve content list");
                return false;
            }

            int maxSize = contentList.getMaxSize();
            boolean allowAddToFullList = contentList.allowAddToFullList();

            if (maxSize < 0 || (allowAddToFullList || contentList.size() < maxSize))
            {
                if (isForbiddenCyclicalReference(oc, contentRef, contentList)) {
                    return false;
                }
                contentList.add(index, contentRef);

                addedEntries.add(contentRef.getReferredContentId());

                return true;
            }
        } catch (CMException cme) {
            handleError(LocaleUtil.format("cm.msg.CouldNotAddEntry", oc.getMessageBundle()), cme, oc);
        } catch (CMRuntimeException cmre) {
            handleError(LocaleUtil.format("cm.msg.CouldNotAddEntry", oc.getMessageBundle()), cmre, oc);
        }

        return false;
    }

    private boolean isForbiddenCyclicalReference(OrchidContext oc,
                                                 ContentReference contentRef,
                                                 ContentList contentList)
            throws CMException
    {
        if (contentList instanceof ConfigurableContentListWrapper) {
            ConfigurableContentListWrapper wrapper = (ConfigurableContentListWrapper) contentList;
            Set<ContentId> detectedCycle = wrapper.detectCycle(getPolicy(), contentRef.getReferredContentId());
            if (detectedCycle != null) {
                MessageUtil.addErrorMessage(oc, LocaleUtil.format("cm.msg.CouldNotAddEntry", oc.getMessageBundle()));
                PolicyCMServer cmServer = getPolicy().getCMServer();
                StringBuilder sb = CyclicReferenceUtil.formatCycle(detectedCycle, cmServer);
                String refName = CyclicReferenceUtil.getContentNameString(contentRef.getReferredContentId(), cmServer);
                handleError(oc, LocaleUtil.format("cm.msg.CouldNotAddCircleEntry",
                        new Object[] { refName, sb.toString() },
                        oc.getLocale(), oc.getMessageBundle()));
                return true;
            }
        }
        return false;
    }

    /**
     * Changes the entry at the given position. This will reload the
     * entry widget. This only works in edit mode.
     */
    public void setEntry(ContentReference contentref, int index)
    {
        OrchidContext oc = WidgetUtil.getOrchidContext();

        try {
            ContentList contentList = getContentList();

            if (contentList == null) {
                logger.logp(Level.WARNING,
                        CLASS, "setEntry",
                        "Could not retrieve content list");
                return;
            }

            contentList.setEntry(index, contentref);

            // Remove the old widget at the index
            OWidget widget = entryWidgets.remove(index);
            discardChild(widget);
            triggerListEvent(oc, new ListEvent(widget, ListEvent.REMOVE_EVENT));

            // Add a new widget, using the new content ref at that position
            initEntry(oc, contentref, index, entryWidgets, false);
        } catch (CMException cme) {
            handleError(LocaleUtil.format("cm.msg.CouldNotAlterEntry", oc.getMessageBundle()), cme, oc);
        } finally {
            updateIndices();
        }
    }

    /**
     * Updates the forceReload attribute of this list,
     * this attribute is used to force the old submit-behavior of lists when they are updated.
     */
    public void setForceReload(boolean forceReload) {
        this.forceReload = forceReload;
    }

    /**
     * Gets the forceReload attribute of this list.
     */
    public boolean getForceReload() {
        return forceReload;
    }

    /**
     * Moves the entry from oldIndex to newIndex. This only works in edit mode.
     */
    public void moveEntry(int oldIndex, int newIndex)
    {
        OrchidContext oc = WidgetUtil.getOrchidContext();

        try {
            ContentList contentList = getContentList();

            if (contentList == null) {
                logger.logp(Level.WARNING,
                        CLASS, "moveEntry",
                        "Could not retrieve content list");
                return;
            }

            contentList.move(oldIndex, newIndex);

            //This is the same algorithm as used above
            ContentListEntryWidget o = entryWidgets.remove(oldIndex);
            if (newIndex >= entryWidgets.size()) {
                entryWidgets.add(o);
            } else {
                entryWidgets.add(newIndex, o);
            }
        } catch (CMException e) {
            handleError(LocaleUtil.format("cm.msg.CouldNotMoveEntry", oc.getMessageBundle()), e, oc);
        } finally {
            updateIndices();
        }
    }

    /**
     * Removes the entry at the specifed index. This only works in edit mode.
     */
    public void removeEntry(int index)
    {
        OrchidContext oc = WidgetUtil.getOrchidContext();

        try {
            ContentList contentList = getContentList();

            if (contentList == null) {
                logger.logp(Level.WARNING,
                        CLASS, "removeEntry",
                        "Could not retrieve content list");
                return;
            }

            contentList.remove(index);
            OWidget widget = entryWidgets.remove(index);
            itemCount.setValue(String.valueOf(StringUtil.
                    toInt(itemCount.getValue(), 1) -1));

            discardChild(widget);
            triggerListEvent(oc, new ListEvent(widget, ListEvent.REMOVE_EVENT));
        } catch (UnsupportedOperationException e) {
            handleError(LocaleUtil.format("cm.msg.CouldNotRemoveEntry", oc.getMessageBundle()), e, oc);
        } finally {
            updateIndices();
        }
    }

    /**
     * Returns the underlying policy as a <code>ContentListAware</code>.
     * If wrong policy class is
     * encountered an error is logged and null is returned.
     */
    public ContentListAware getContentListAwarePolicy()
    {
        try {
            return (ContentListAware)getPolicy();
        } catch (ClassCastException cce) {
            logger.logp(Level.WARNING, CLASS, "getContentListAwarePolicy",
                    "Wrong policy class, got " +
                            getPolicy().getClass().getName() + " expected " +
                            ContentListAware.class.getName(), cce);

            return null;
        }
    }

    // ContentListEntryContainer implementation

    public ContentList getContentList()
    {
        // Return the content list cached during init phase if existing

        if (contentListUnderInit != null) {
            return contentListUnderInit;
        }

        try {
            return getContentListAwarePolicy().getContentList();
        } catch (CMException cme) {
            handleError(cme, WidgetUtil.getOrchidContext());
        }

        return null;
    }

    public ContentListEntryButtons getContentListEntryButtons(ContentListEntryWidget entryWidget)
    {
        return new ContentListEntryButtons(entryWidget, this,
                isEditable());
    }

    public boolean showActionButtons()
    {
        return showActionButtons;
    }

    public boolean showActionButtonsInViewMode()
    {
        return showActionButtonsInViewMode;
    }

    public boolean showCheckbox()
    {
        return showCheckboxes;
    }

    public boolean showCopyButton()
    {
        return showCopyButton;
    }

    public boolean showMoveButtons()
    {
        return showMoveButtons && isEditable() && isSortable;
    }

    public boolean showPopupMenu()
    {
        return showActionMenu;
    }

//  Implementation of com.polopoly.orchid.js.JSWidget

    /**
     * Describe <code>getJSWidgetClassName</code> method here.
     *
     * @return a <code>String</code> value
     * @exception OrchidException if an error occurs
     */
    public String getJSWidgetClassName()
            throws OrchidException
    {
        return "JSContentListJQWidget";
    }

    /**
     * Describe <code>getFriendlyName</code> method here.
     *
     * @return a <code>String</code> value
     * @exception OrchidException if an error occurs
     */
    public String getFriendlyName()
            throws OrchidException
    {
        return friendlyName;
    }

    /**
     * Describe <code>getJSScriptDependencies</code> method here.
     *
     * @return a <code>String[]</code> value
     */
    public String[] getJSScriptDependencies()
    {
        return new String[] { "script/polopoly-ui.js" };
    }

    public String getInitScript()
            throws OrchidException
    {
        return JSWidgetUtil.genInitScript(this);
    }

    public String[] getInitParams()
            throws OrchidException
    {
        StringBuffer resourceTypes = new StringBuffer("[");

        if (acceptedResourceTypes != null) {
            Iterator<String> iter = acceptedResourceTypes.iterator();

            while (iter.hasNext()) {
                String resourceType = iter.next();
                resourceTypes.append(JSWidgetUtil.toJSString(resourceType));

                if (iter.hasNext()) {
                    resourceTypes.append(", ");
                }
            }
        }

        resourceTypes.append("]");

        if (isEditable()) {
            return new String[] {
                    JSWidgetUtil.toJSString(pastedItems.getCompoundId()),
                    JSWidgetUtil.toJSString(itemCount.getCompoundId()),
                    JSWidgetUtil.toJSString("tocdiv" + getCompoundId()),
                    String.valueOf(false),
                    String.valueOf(showCopyButton),
                    String.valueOf(showCheckboxes),
                    String.valueOf(sendFormOnPaste),
                    String.valueOf(isPastePossible()),
                    JSWidgetUtil.toJSArray(allowedInputTemplateIds),
                    JSWidgetUtil.toJSArray(disallowedInputTemplateIds),
                    resourceTypes.toString(),
                    JSWidgetUtil.toJSString(listItems.getCompoundId()),
                    String.valueOf(listMaxSize),
                    String.valueOf(allowAddToFullList),
                    String.valueOf(isSortable), // shouldBeSortable
                    String.valueOf(forceReload),
                    showCheckbox() ? String.valueOf(WidgetUtil.getJSElementId(selectAll, null)) : null,
                    JSWidgetUtil.toJSString(tocClassName),
                    String.valueOf(pasteToEnd),
                    String.valueOf(isInsertPossible()),
                    JSWidgetUtil.toJSString(showAllButton.getCompoundId())
            };
        } else {
            return new String[] {
                    JSWidgetUtil.toJSString(pastedItems.getCompoundId()),
                    JSWidgetUtil.toJSString(itemCount.getCompoundId()),
                    JSWidgetUtil.toJSString("tocdiv" + getCompoundId()),
                    null,
                    String.valueOf(showCopyButton),
                    String.valueOf(showCheckboxes),
                    String.valueOf(sendFormOnPaste),
                    String.valueOf(false),
                    JSWidgetUtil.toJSArray(allowedInputTemplateIds),
                    JSWidgetUtil.toJSArray(disallowedInputTemplateIds),
                    resourceTypes.toString(),
                    JSWidgetUtil.toJSString(listItems.getCompoundId()),
                    String.valueOf(listMaxSize),
                    String.valueOf(allowAddToFullList),
                    String.valueOf(false), // shouldBeSortable
                    String.valueOf(forceReload),
                    null,
                    JSWidgetUtil.toJSString(tocClassName),
                    null,
                    String.valueOf(false),
                    JSWidgetUtil.toJSString(showAllButton.getCompoundId())
            };
        }
    }

    /**
     * Enable/disable sorting of the content list.
     * <p>
     * Sorting is enabled by default.
     * </p>
     *
     * @param isSortable set to <code>false</code> to disable sorting
     */
    protected void setSortable(boolean isSortable) {
        this.isSortable = isSortable;
    }

    /**
     * Returns true if paste is possible, false otherwise. Paste is not possible
     * if {@link #isInsertPossible()} returns false, or if paste position is the end
     * of list and the list has reached its max size (because the pasted item would
     * immediately be evicted) or is not fully expanded.
     * @return true if paste is possible, false otherwise.
     */
    private boolean isPastePossible()
    {
        if (!isInsertPossible()) {
            return false;
        }

        try {
            ContentList contentList = getContentList();

            if (contentList == null) {
                logger.logp(Level.WARNING,
                        CLASS, "isPastePossible",
                        "Could not retrieve content list");
                return false;
            }

            boolean listIsFull = contentList.getMaxSize() >= 0 && contentList.size() >= contentList.getMaxSize();
            boolean fullyExpanded = contentList.size() <= maxEntriesToRender || renderAll || maxEntriesToRender < 0;

            return !(pasteToEnd && (listIsFull || !fullyExpanded));
        } catch (Exception e) {
            logger.logp(Level.WARNING,
                    CLASS, "isPastePossible",
                    "Could not access content list ", e);
            return false;
        }
    }

    /**
     * Inserting into the list is not possible if the content list has reached its max
     * size and it is not allowed to add to a full list. It is also not possible to insert in view mode.
     * See also {@link #isPastePossible()}
     * @return true if insert (paste (with additional conditions), drag-n-drop) is possible, false otherwise.
     */
    private boolean isInsertPossible()
    {
        if (!isEditable()) {
            return false;
        }

        try {
            ContentList contentList = getContentList();

            if (contentList == null) {
                logger.logp(Level.WARNING,
                        CLASS, "isInsertPossible",
                        "Could not retrieve content list");
                return false;
            }

            boolean hasMaxSize = contentList.getMaxSize() >= 0;
            boolean canAddToFullList = contentList.allowAddToFullList();
            boolean listIsFull = contentList.size() >= contentList.getMaxSize();

            return (!hasMaxSize || !listIsFull || canAddToFullList);
        } catch (Exception e) {
            logger.logp(Level.WARNING,
                    CLASS, "isPastePossible",
                    "Could not access content list ", e);
            return false;
        }
    }

//  Implementation of com.polopoly.orchid.js.JSExtendedClipboardClient

    /**
     * Describe <code>hasJSCopy</code> method here.
     *
     * @return a <code>boolean</code> value
     * @exception OrchidException if an error occurs
     */
    public boolean hasJSCopy()
            throws OrchidException
    {
        return showCopyButton && showCheckboxes;
    }

    public String getCopyButtonTooltip()
    {
        return "cm.label.CopyCheckedItems";
    }

    /**
     * Describe <code>hasJSPaste</code> method here.
     *
     * @return a <code>boolean</code> value
     * @exception OrchidException if an error occurs
     */
    public boolean hasJSPaste()
            throws OrchidException
    {
        return showPasteButton && isEditable();
    }

    public String getPasteButtonTooltip()
    {
        return "cm.label.Paste";
    }

    /**
     * Return true if the client-side widget has an implementation of the
     * copy() function. The widget framework will auto-generate the cut button.
     *
     * @return a <code>boolean</code> value
     * @exception OrchidException if an error occurs
     */
    public boolean hasJSCut()
            throws OrchidException
    {
        return showCutButton && isEditable();
    }

    /**
     * Return the desired tooltip resource key for the cut button.
     * If null is returned, a standard tooltip will be used.
     *
     * @return a <code>String</code> value
     */
    public String getCutButtonTooltip()
    {
        return "cm.label.CutCheckedItems";
    }

    //Quick fix for problems at Expressen. #8081
    public boolean isEditable()
    {
        if (!isReadOnlyContentList) {
            try {
                PolicyCMServer cmServer = getContentSession().getPolicyCMServer();

                return PolicyWidgetUtil.isEditMode(this) &&
                        LockUtil.isWritableForCaller(getPolicy().getContent(),
                                cmServer.getCurrentCaller());
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception when checking editable status", e);

                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Add a listener to list events.
     *
     * @param listener
     * @since 9.8
     */
    public void addListEventListener(WidgetEventListener listener)
    {
        if (listEventListeners == null) {
            listEventListeners = new ArrayList<WidgetEventListener>();
        }

        listEventListeners.add(listener);
    }

    @Override
    public boolean isAjaxTopWidget()
    {
        return true;
    }

    /**
     * Trigger a list event.
     *
     * @param oc
     * @param listEvent
     * @since 9.8
     */
    private void triggerListEvent(OrchidContext oc,OrchidEvent listEvent)
    {
        if (listEventListeners != null) {
            for (Iterator<WidgetEventListener> iter = listEventListeners.iterator(); iter.hasNext();) {
                WidgetEventListener listEventListener = iter.next();

                try {
                    listEventListener.processEvent(oc, listEvent);
                } catch (OrchidException e) {
                    logger.logp(Level.WARNING, CLASS, "triggerListEvent", "Error processing list event", e);
                }
            }
        }
    }
    protected String getCustomElement(int i){
        return "";
    }
    protected String getCustomViewModeCoverBegin(int i) {
        return "";
    }
    protected String getCustomViewModeCoverEnd() {
        return "";
    }
    protected void setMaxEntriesToRender(int maxEntriesToRender) {
        this.maxEntriesToRender = maxEntriesToRender;
    }
}
