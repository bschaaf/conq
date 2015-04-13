package com.atex.gong.paywall;

import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.repository.ContentModifiedException;
import com.atex.plugins.users.UserUtil;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.paywall.Capability;
import com.polopoly.paywall.ContentBundle;
import com.polopoly.paywall.Offering;
import com.polopoly.paywall.SubscriptionFieldPolicy;
import com.polopoly.user.server.Caller;
import com.polopoly.user.server.UserId;

import java.util.Collection;
import java.util.HashSet;

public final class SubscriptionUtil {

    private static final String SUBSCRIPTION_ENGAGEMENT_NAME = "subscription";
    private static final ExternalContentId SUBSCRIPTION_TEMPLATE_ID = new ExternalContentId("p.Subscription");
    private static final int SUBSCRIPTION_MAJOR = 3;

    private SubscriptionUtil() { }

    public static Collection<ContentId> getContentBundlesForUser(
            final com.atex.onecms.content.ContentId userId,
            final CmClient cmClient,
            final Capability capability) throws CMException {
        Collection<com.polopoly.cm.ContentId> accessibleBundleIds = new HashSet<>();
        SubscriptionFieldPolicy subscriptionPolicy = getSubscriptionPolicy(userId, cmClient);
        if (subscriptionPolicy != null) {
            Collection<ContentBundle> bundles = subscriptionPolicy.getContentBundlesByCapability(capability);
            for (ContentBundle bundle : bundles) {
                accessibleBundleIds.add(bundle.getContentId());
            }
        }
        return accessibleBundleIds;
    }

    public static SubscriptionFieldPolicy getSubscriptionPolicy(final com.atex.onecms.content.ContentId userId,
                                                                final CmClient cmClient) throws CMException {
        UserUtil userUtil = new UserUtil(userId, cmClient.getContentManager());
        com.atex.onecms.content.ContentId id = userUtil.getUser().getEngagements().get(SUBSCRIPTION_ENGAGEMENT_NAME);
        if (id == null) {
            return null;
        }
        return (SubscriptionFieldPolicy) cmClient.getPolicyCMServer().getPolicy(IdUtil.toPolicyContentId(id));
    }

    public static void buyOffering(final Offering offering,
                                   final UserUtil userUtil,
                                   final CmClient cmClient) throws CMException, ContentModifiedException {

        PolicyCMServer cmServer = cmClient.getPolicyCMServer();
        Caller savedCaller = cmServer.getCurrentCaller();
        cmServer.setCurrentCaller(new Caller(new UserId("98")));
        try {
            SubscriptionFieldPolicy subscriptionPolicy = getSubscriptionPolicy(userUtil.getUserId(), cmClient);
            try {
                if (subscriptionPolicy == null) {
                    subscriptionPolicy = (SubscriptionFieldPolicy) cmServer.createContent(SUBSCRIPTION_MAJOR,
                                                                                          SUBSCRIPTION_TEMPLATE_ID);
                    userUtil.setEngagementId(SUBSCRIPTION_ENGAGEMENT_NAME,
                                             IdUtil.fromPolicyContentId(
                                                     subscriptionPolicy.getContentId().getContentId()));
                } else {
                    subscriptionPolicy =
                        (SubscriptionFieldPolicy) cmServer.createContentVersion(subscriptionPolicy.getContentId());
                }
                subscriptionPolicy.buy(offering);
            } catch (CMException e) {
                if (subscriptionPolicy != null) {
                    cmServer.abortContent(subscriptionPolicy);
                }
                throw e;
            }
            cmServer.commitContent(subscriptionPolicy);
        } finally {
            cmServer.setCurrentCaller(savedCaller);
        }
    }

}
