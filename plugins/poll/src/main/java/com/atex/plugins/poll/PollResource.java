package com.atex.plugins.poll;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.application.servlet.ApplicationServletUtil;
import com.polopoly.cm.ContentIdFactory;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.poll.FraudDetector;
import com.polopoly.poll.client.PollCouchClient;
import com.polopoly.poll.policy.QuestionPolicy;
import com.sun.jersey.api.client.ClientResponse;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;


@Path("/")
public class PollResource {

    private static final Logger LOG = Logger.getLogger(PollResource.class.getName());
    private static final long ONE_MINUTE_IN_MILLIS = 60000L;
    protected static final String YOU_HAVE_ALREADY_VOTED = "You have already voted";
    protected static final String YOU_MUST_HAVE_COOKIES_ENABLED = "You must have cookies enabled";
    protected static final String UNABLE_TO_SUBMIT_VOTE = "Unable to submit vote";

    @Context
    private ServletContext servletContext;
    @Context
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;
    @Context
    private UriInfo uriInfo;

    @POST
    @Produces("application/json")
    public Response postVote(@FormParam("pollId") final String pollId,
                             @FormParam("pollContentId") final String pollContentId,
                             @FormParam("questionId") final String questionId,
                             @FormParam("answerId") final String answerId,
                             @QueryParam("cookieSet") @DefaultValue("false") final boolean cookieSet) {

        return getResponse(pollId, pollContentId, questionId, answerId, cookieSet);
    }

    @GET
    @Produces("application/json")
    public Response getVote(@QueryParam("pollId") final String pollId,
                            @QueryParam("pollContentId") final String pollContentId,
                            @QueryParam("questionId") final String questionId,
                            @QueryParam("answerId") final String answerId,
                            @QueryParam("cookieSet") @DefaultValue("false") final boolean cookieSet) {

        return getResponse(pollId, pollContentId, questionId, answerId, cookieSet);
    }

    private Response getResponse(final String pollId,
                                 final String pollContentId,
                                 final String questionId,
                                 final String answerId,
                                 final boolean cookieSet) {
        try {
            if (FraudDetector.isLocked(request, pollId)) {
                return Response.status(ClientResponse.Status.FORBIDDEN)
                        .entity(getErrorJson(YOU_HAVE_ALREADY_VOTED)).build();
            }

            if (!cookieSet) {
                FraudDetector.setCookiesEnabledCheckCookie(request, response, (System.currentTimeMillis()));
                URI redirectUri = uriInfo.getAbsolutePathBuilder()
                        .queryParam("pollId", pollId)
                        .queryParam("pollContentId", pollContentId)
                        .queryParam("questionId", questionId)
                        .queryParam("answerId", answerId)
                        .queryParam("cookieSet", true)
                        .build();
                return Response.seeOther(redirectUri).build();
            }

            boolean isVoteSuccess = false;
            if (isCookieValid()) {
                isVoteSuccess = submitVote(pollId, questionId, answerId);
            } else {
                return Response.status(ClientResponse.Status.FORBIDDEN)
                        .entity(getErrorJson(YOU_MUST_HAVE_COOKIES_ENABLED)).build();
            }

            if (isVoteSuccess) {
                FraudDetector.setLock(request, response, pollId);
                String json = getSuccessJson(pollContentId, pollId);
                return Response.ok(json).build();
            } else {
                return Response.serverError().entity(getErrorJson(UNABLE_TO_SUBMIT_VOTE)).build();
            }

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error voting", e);
            return Response.serverError().entity(getErrorJson(UNABLE_TO_SUBMIT_VOTE)).build();
        }
    }

    private String getErrorJson(final String message) {
        JsonObject error = new JsonObject();
        error.addProperty("status", "error");
        error.addProperty("message", message);
        return new Gson().toJson(error);
    }

    private String getSuccessJson(final String pollContentId,
                                  final String pollId)
            throws IllegalApplicationStateException, CMException {

        PollElementPolicy policy = getPollElementPolicy(pollContentId);
        QuestionPolicy.Value[] results = policy.getQuestion().getResults(pollId);
        long totalVoteCount = policy.getQuestion().getTotalVoteCount(pollId);

        JsonObject pollResult = new JsonObject();
        for (QuestionPolicy.Value value : results) {
            JsonObject answerResult = new JsonObject();
            answerResult.addProperty("count", value.getValue());
            answerResult.addProperty("percent", value.getPercentageAsInt());
            pollResult.add(value.getId(), answerResult);
        }

        JsonObject pollResponse = new JsonObject();
        pollResponse.addProperty("status", "ok");
        pollResponse.add("results", pollResult);
        pollResponse.addProperty("totalVoteCount", totalVoteCount);

        String json = new Gson().toJson(pollResponse);
        return json;
    }

    /**
     * Visibility protected in order to facilitate test since CmClient is hard to mock.
     */
    protected PollElementPolicy getPollElementPolicy(final String pollContentId)
            throws CMException, IllegalApplicationStateException {
        return (PollElementPolicy) getCmClient()
                .getPolicyCMServer()
                .getPolicy(ContentIdFactory.createContentId(pollContentId));
    }

    private CmClient getCmClient() throws IllegalApplicationStateException {
        return ApplicationServletUtil.getApplication(servletContext)
                .getPreferredApplicationComponent(CmClient.class);
    }

    private boolean submitVote(final String pollId, final String questionId, final String answerId) {
        try {
            if (pollId != null && questionId != null && answerId != null
                    && !FraudDetector.isLocked(request, pollId)) {
                boolean voteSuccessfullyRegistered = registerVote(pollId, questionId, answerId);
                if (voteSuccessfullyRegistered) {
                    FraudDetector.setLock(request, response, pollId);
                }
                return voteSuccessfullyRegistered;
            }
            return false;

        } catch (Exception e) {
            LOG.warning("Unable to submit vote: " + e.getMessage());
            LOG.log(Level.FINE, "Detailed error", e);
            return false;
        }
    }

    /**
     * Visibility protected in order to facilitate test since PollCouchClient is final and therefore is hard to mock.
     */
    protected boolean registerVote(final String pollId, final String questionId, final String answerId)
            throws IllegalApplicationStateException {
        PollCouchClient pollClient = getPollClient();
        boolean success = false;
        if (pollClient.getServiceControl().isServing()) {
            success = pollClient.getPollManager().vote(pollId, questionId, answerId);
        }
        return success;
    }

    private PollCouchClient getPollClient() throws IllegalApplicationStateException {
        return ApplicationServletUtil.getApplication(servletContext)
                .getPreferredApplicationComponent(PollCouchClient.class);
    }

    /**
     * Visibility protected in order to facilitate test.
     */
    protected boolean isCookieValid() throws CMException {
        String cookieCheckCookie = FraudDetector.getCookiesEnabledCheckCookie(request);
        if (cookieCheckCookie == null) {
            return false;
        }
        long cookieTime = FraudDetector.decodeCookie(cookieCheckCookie);
        long currentTime = System.currentTimeMillis();
        return cookieTime <= currentTime && (cookieTime + ONE_MINUTE_IN_MILLIS) > currentTime;
    }
}
