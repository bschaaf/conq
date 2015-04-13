package com.atex.plugins.personalization.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.ContentWrite;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.onecms.content.aspects.Aspect;
import com.atex.onecms.content.repository.ContentModifiedException;
import com.atex.onecms.content.repository.StorageException;
import com.atex.plugins.personalization.ConfigPolicy;
import com.atex.plugins.personalization.Personalization;
import com.atex.plugins.personalization.PersonalizationDomainObject;
import com.atex.plugins.users.AccessToken;
import com.atex.plugins.users.AuthenticationManager;
import com.atex.plugins.users.User;
import com.atex.plugins.users.WebClientUtil;
import com.google.gson.Gson;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.application.servlet.ApplicationServletUtil;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.metadata.Dimension;
import com.atex.onecms.content.metadata.MetadataInfo;

@WebServlet
public class Servlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(Servlet.class.getName());
    private static final Subject SYSTEM_SUBJECT = new Subject("98", null);
    private static final String PERSONALIZATION = "personalization";
    private static final Gson GSON = new Gson();

    private CmClient cmClient;
    private AuthenticationManager authManager;

    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        try {
            cmClient = ApplicationServletUtil.getApplication(config.getServletContext())
                .getPreferredApplicationComponent(CmClient.class);
        } catch (IllegalApplicationStateException e) {
            throw new ServletException("Failed to get CmClient", e);
        }
        authManager = new AuthenticationManager(cmClient);

        LOG.info("Started personalization servlet");
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {

        ConfigPolicy config = getConfig();
        if (!config.isEnabled()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        ContentManager cm = cmClient.getContentManager();

        ContentId userId = getUserId(request);
        if (userId == null || !userId.equals(IdUtil.fromString(request.getParameter("userId")))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        ContentVersionId userVerId = cm.resolve(userId, SYSTEM_SUBJECT);
        if (userVerId == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        ContentId persId = null;
        try {
            ContentResult<User> userRead = cm.get(userVerId, null, User.class, null, SYSTEM_SUBJECT);

            User userData = userRead.getContent().getContentData();
            persId = userData.getEngagements().get(PERSONALIZATION);
        } catch (Exception e) {
            // This might happen if the user id from cookies isn't in this system
            LOG.log(Level.WARNING, "Encountered an error while fetching personalized data, got exception message: "
                                    + e.getMessage());
        }

        if (persId == null) {
            PrintWriter writer = response.getWriter();
            writer.print("{}");
            writer.flush();
            return;
        }

        ContentVersionId persVerId = cm.resolve(persId, SYSTEM_SUBJECT);
        ContentResult<Personalization> persRead = cm.get(persVerId, null, Personalization.class, null, SYSTEM_SUBJECT);
        PersonalizationDomainObject personalization =
                new PersonalizationDomainObject(persRead.getContent().getContentData());

        Map<String, Integer> tags = personalization.getTopLevelEntitiesAndCounts();

        response.setHeader("Cache-Control", "no-cache, max-age=0");
        response.setHeader("Pragma", "No-cache");
        response.setDateHeader("Expires", 0);

        PrintWriter writer = response.getWriter();
        writer.print(GSON.toJson(tags));
        writer.flush();
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {
        ConfigPolicy config = getConfig();
        if (!config.isEnabled()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        ContentId articleId;
        try {
            articleId = IdUtil.fromString(request.getParameter("articleId"));
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        ContentManager cm = cmClient.getContentManager();

        ContentId userId = getUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        ContentVersionId userVerId = cm.resolve(userId, SYSTEM_SUBJECT);
        if (userVerId == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            ContentResult<User> userRead = cm.get(userVerId, null, User.class, null, SYSTEM_SUBJECT);

            User userData = userRead.getContent().getContentData();
            ContentId persId = userData.getEngagements().get(PERSONALIZATION);
            if (persId == null) {
                createPersonalization(response, articleId, userRead, userData, config);
            } else {
                updatePersonalization(response, articleId, persId, config);
            }
        } catch (Exception e) {
            // This might happen if the user id from cookies isn't in this system
            LOG.log(Level.WARNING, "Encountered an error while fetching personalized data, got exception message: "
                                    + e.getMessage());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
    }

    private void updatePersonalization(final HttpServletResponse response,
                                       final ContentId articleId,
                                       final ContentId persId,
                                       final ConfigPolicy config) {

        ContentManager cm = cmClient.getContentManager();
        ContentVersionId persVerId = cm.resolve(persId, SYSTEM_SUBJECT);

        ContentResult<Personalization> persRead = cm.get(persVerId, null, Personalization.class, null, SYSTEM_SUBJECT);

        List<Personalization.View> views = persRead.getContent().getContentData().getViews();

        for (Personalization.View view : views) {
            if (view.getArticleId().equals(articleId)) {
                views.remove(view);
                break;
            }
        }

        Personalization.View view = getMetadata(articleId, config);
        if (view == null) {
            return;
        }

        views.add(view);
        while (views.size() > config.getMaxHistory()) {
            views.remove(0);
        }

        ContentWrite<Personalization> persUpdate = new ContentWrite<>(persRead.getContent());
        persUpdate.setContentData(persRead.getContent().getContentData());

        try {
            cm.update(persId, persUpdate, SYSTEM_SUBJECT);
        } catch (ContentModifiedException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        } catch (StorageException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            LOG.log(Level.WARNING, "Failed to update personalization engagement " + persId, e);
        }
    }

    private void createPersonalization(final HttpServletResponse response,
                                       final ContentId articleId,
                                       final ContentResult<User> userRead,
                                       final User userData,
                                       final ConfigPolicy config) {

        Personalization.View view = getMetadata(articleId, config);
        if (view == null) {
            return;
        }

        List<Personalization.View> views = new ArrayList<>();
        views.add(view);

        Personalization personalization = new Personalization();
        personalization.setViews(views);

        ContentManager cm = cmClient.getContentManager();

        try {
            ContentWrite<Personalization> persCreate =
                new ContentWrite<>(Personalization.class.getName(), personalization);
            ContentResult<Personalization> createResult = cm.create(persCreate, SYSTEM_SUBJECT);

            userData.getEngagements().put(PERSONALIZATION, createResult.getContentId().getContentId());
            ContentWrite<User> userUpdate = new ContentWrite<>(userRead.getContent());
            userUpdate.setContentData(userData);

            cm.update(userUpdate.getId().getContentId(), userUpdate, SYSTEM_SUBJECT);
        } catch (ContentModifiedException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        } catch (StorageException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            LOG.log(Level.WARNING, "Failed to create personalization engagement for user "
                + userRead.getContentId(), e);
        }
    }

    private Personalization.View getMetadata(final ContentId articleId, final ConfigPolicy config) {
        ContentManager cm = cmClient.getContentManager();
        ContentResult<Object> result;
        try {
            ContentVersionId versionedId = cm.resolve(articleId, SYSTEM_SUBJECT);
            result = cm.get(versionedId, null, Object.class, Collections.<String, Object>emptyMap(), SYSTEM_SUBJECT);
        } catch (StorageException | ClassCastException e) {
            LOG.log(Level.WARNING, "Failed to read content " + articleId, e);
            return null;
        }

        @SuppressWarnings("unchecked")
        Aspect<MetadataInfo> metadataAspect = (Aspect<MetadataInfo>) result.getContent().getAspect("atex.Metadata");
        if (metadataAspect == null || metadataAspect.getData() == null) {
            return null;
        }

        MetadataInfo metadata = metadataAspect.getData();

        List<Dimension> dimensions = new ArrayList<>();

        for (String dimensionId : config.getDimensionIds()) {
            Dimension dimension = metadata.getMetadata().getDimensionById(dimensionId);
            if (dimension != null && !dimensionId.isEmpty()) {
                dimensions.add(dimension);
            }
        }

        if (dimensions.isEmpty()) {
            return null;
        }

        Personalization.View view = new Personalization.View();
        view.setDimensions(dimensions);
        view.setArticleId(articleId);
        return view;
    }

    private ContentId getUserId(final HttpServletRequest request) {
        AccessToken token = WebClientUtil.getToken(request);
        return authManager.getUserId(token);
    }

    private ConfigPolicy getConfig() {
        PolicyCMServer policyCMServer = cmClient.getPolicyCMServer();
        try {
            return (ConfigPolicy) policyCMServer.getPolicy(new ExternalContentId(ConfigPolicy.EXTERNAL_ID));
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Failed to read Personalization configuration.", e);
            throw new RuntimeException("Failed to read config.");
        }
    }
}
