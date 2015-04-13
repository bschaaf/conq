package com.atex.plugins.poll;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/pollresource-test")
public class PollResourceWrapper extends PollResource {

    private static boolean registerVoteSuccess;
    private static PollElementPolicy pollElementPolicy;
    private static boolean isCookieValid;


    public static void setRegisterVoteSuccess(final boolean registerVoteSuccess) {
        PollResourceWrapper.registerVoteSuccess = registerVoteSuccess;
    }

    public static void setPollElementPolicy(final PollElementPolicy pollElementPolicy) {
        PollResourceWrapper.pollElementPolicy = pollElementPolicy;
    }

    public static void setIsCookieValid(final boolean isCookieValid) {
        PollResourceWrapper.isCookieValid = isCookieValid;
    }

    @Override
    protected boolean registerVote(final String pollId, final String questionId, final String answerId) {
        return registerVoteSuccess;
    }

    @Override
    protected PollElementPolicy getPollElementPolicy(final String pollContentId) {
        return pollElementPolicy;
    }

    @Override
    protected boolean isCookieValid() {
        return isCookieValid;
    }

    @POST
    @Produces("application/json")
    public Response postVote(@QueryParam("pollId") final String pollId,
                         @QueryParam("pollContentId") final String pollContentId,
                         @QueryParam("questionId") final String questionId,
                         @QueryParam("answerId") final String answerId,
                         @QueryParam("cookieSet") @DefaultValue("false") final boolean cookieSet) {
        return super.postVote(pollId, pollContentId, questionId, answerId, cookieSet);
    }

    @GET
    @Produces("application/json")
    public Response getVote(@QueryParam("pollId") final String pollId,
                            @QueryParam("pollContentId") final String pollContentId,
                            @QueryParam("questionId") final String questionId,
                            @QueryParam("answerId") final String answerId,
                            @QueryParam("cookieSet") @DefaultValue("false") final boolean cookieSet) {
        return super.getVote(pollId, pollContentId, questionId, answerId, cookieSet);
    }


}
