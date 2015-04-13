package com.atex.plugins.poll;

import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.model.DescribesModelType;
import com.polopoly.poll.policy.QuestionPolicy;
import com.polopoly.poll.policy.SinglePollArticlePolicy;
import com.polopoly.poll.policy.SinglePollPolicy;
import com.polopoly.siteengine.layout.Element;

/**
 * Policy for poll elements with one question. The class implements required
 * {@link com.polopoly.poll.policy.SinglePollArticlePolicy}
 */
@DescribesModelType
public class PollElementPolicy extends BaselinePolicy implements Element, SinglePollArticlePolicy {

    @Override
    public String getName() throws CMException {
        String name = getQuestion().getQuestion();
        if (name == null) {
            name = getContentId().getContentId().getContentIdString();
        }
        return name;
    }

    public QuestionPolicy getQuestion() throws CMException {
        return (QuestionPolicy) this.getChildPolicy("questionField");
    }

    public SinglePollPolicy getSinglePoll() throws CMException {
        return (SinglePollPolicy) this.getChildPolicy("singlePollField");
    }
}
