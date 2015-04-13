package com.atex.plugins.poll;

import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.model.ModelWrite;
import com.polopoly.poll.policy.QuestionPolicy;
import com.polopoly.render.CacheInfo;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

import java.util.logging.Level;
import java.util.logging.Logger;


public class PollElementController extends RenderControllerBase {
    private static final Logger LOG = Logger.getLogger(PollElementController.class.getName());


    public void populateModelAfterCacheKey(final RenderRequest request, final TopModel m, final CacheInfo cacheInfo,
                                           final ControllerContext context) {
        ModelWrite localModel = m.getLocal();
        try {
            PolicyCMServer cmServer = getCmClient(context).getPolicyCMServer();
            PollElementPolicy policy = (PollElementPolicy) cmServer.getPolicy(context.getContentId());
            String pollId = policy.getSinglePoll().getPollId();
            QuestionPolicy question = policy.getQuestion();
            QuestionPolicy.Value[] results = question.getResults(pollId);
            long totalVoteCount = question.getTotalVoteCount(pollId);
            localModel.setAttribute("question", question.getQuestion());
            localModel.setAttribute("results", results);
            localModel.setAttribute("totalVoteCount", totalVoteCount);
            localModel.setAttribute("pollId", pollId);
            localModel.setAttribute("questionId", question.getKey());
            localModel.setAttribute("options", question.getOptions());
            localModel.setAttribute("answerOptionsLayout", policy.getChildValue("answerOptionsLayout", "horizontal"));
        } catch (CMException e) {
            LOG.log(Level.WARNING, "Unable to render poll " + context.getContentId().getContentIdString(), e);
            cacheInfo.setCacheTime(0);
            localModel.setAttribute("renderError", e.getMessage());
        }
    }

}
