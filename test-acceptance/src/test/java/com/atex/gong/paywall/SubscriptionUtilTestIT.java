package com.atex.gong.paywall;

import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.repository.ContentModifiedException;
import com.atex.plugins.users.AuthenticationManager;
import com.atex.plugins.users.AuthenticationMethodCreator;
import com.atex.plugins.users.User;
import com.atex.plugins.users.UserUtil;
import com.google.inject.Inject;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.paywall.Capability;
import com.polopoly.paywall.Offering;
import com.polopoly.paywall.Paywall;
import com.polopoly.paywall.PaywallProvider;
import com.polopoly.paywall.SubscriptionFieldPolicy;
import com.polopoly.testnext.base.ChangeList;
import com.polopoly.testnext.base.ImportTestContent;
import com.polopoly.testnj.TestNJRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(TestNJRunner.class)
@ImportTestContent(
        dir = "/com/atex/gong/paywall/",
        files = {
                "paywall.SubscriptionUtilTestIT.content" })
public class SubscriptionUtilTestIT {

    private static final String SUBSCRIPTION_ENGAGEMENT_NAME = "subscription";
    private static final ExternalContentId SUBSCRIPTION_TEMPLATE_ID = new ExternalContentId("p.Subscription");
    private static final int SUBSCRIPTION_MAJOR = 3;

    @Inject
    private ChangeList changelist;
    @Inject
    private CmClient cmClient;
    @Inject
    private PolicyCMServer cmServer;

    private Paywall paywall;
    private Capability capability;
    private UserUtil userUtil;
    private com.polopoly.cm.ContentId contentBundleInOffering1;
    private Collection<com.polopoly.cm.ContentId> contentBundlesInOffering1And2 = new ArrayList<>();
    private Offering offering1;
    private Offering offering2;
    private static final String EXTERNAL_ID_BASE = "paywall.SubscriptionUtilTestIT";

    @Before
    public void init() throws CMException {
        changelist.waitFor("preview");
        paywall = new PaywallProvider(cmServer).getPaywall();
        final String authenticationMethod = new AuthenticationMethodCreator(cmClient).create().getName();
        userUtil = new UserUtil(createUser(authenticationMethod), cmClient.getContentManager());
        capability = paywall.getCapability(Capability.ONLINE_ACCESS_CAPABILITY_ID);
        offering1 = paywall.getOffering(getContentId(EXTERNAL_ID_BASE + ".offering-1"));
        offering2 = paywall.getOffering(getContentId(EXTERNAL_ID_BASE + ".offering-2"));
        contentBundleInOffering1 = getContentId(EXTERNAL_ID_BASE + ".foo-bundle");
        contentBundlesInOffering1And2.add(getContentId(EXTERNAL_ID_BASE + ".foo-bundle"));
        contentBundlesInOffering1And2.add(getContentId(EXTERNAL_ID_BASE + ".bar-bundle"));
    }

    @Test
    public void testGetSubscriptionPolicy() throws CMException, ContentModifiedException {
        // Assert no subscriptionFieldPolicy
        assertNull("User should not yet have a subscriptionFieldPolicy.",
                SubscriptionUtil.getSubscriptionPolicy(userUtil.getUserId(), cmClient));

        // Create and add a subscriptionFieldPolicy to the user.
        SubscriptionFieldPolicy subscriptionFieldPolicy = createSubscriptionPolicyForUser();
        assertNotNull(subscriptionFieldPolicy);

        // Get SubscriptionFieldPolicy from user and verify that is the same as we created above.
        SubscriptionFieldPolicy subscriptionFieldPolicyFromUser =
                SubscriptionUtil.getSubscriptionPolicy(userUtil.getUserId(), cmClient);
        assertNotNull(subscriptionFieldPolicyFromUser);
        assertEquals(subscriptionFieldPolicy.getContentId().getContentId(),
                subscriptionFieldPolicyFromUser.getContentId().getContentId());
    }


    @Test
    @SuppressWarnings("checkstyle:methodname")
    public void testBuyOffering_and_testGetContentBundlesForUser() throws CMException, ContentModifiedException {
        // Assert no subscriptionFieldPolicy
        assertNull("User should not yet have a subscriptionFieldPolicy.",
                SubscriptionUtil.getSubscriptionPolicy(userUtil.getUserId(), cmClient));

        // Buy offering1
        SubscriptionUtil.buyOffering(offering1, userUtil, cmClient);

        // Verify offering bought by getting contentbundles and verify they are the expected ones.
        final Collection<com.polopoly.cm.ContentId> contentBundlesAfterFirstPurchase =
                SubscriptionUtil.getContentBundlesForUser(userUtil.getUserId(), cmClient, capability);
        assertTrue(contentBundlesAfterFirstPurchase.size() == 1);
        com.polopoly.cm.ContentId[] contentBundleArray = new com.polopoly.cm.ContentId[1];
        contentBundleArray = contentBundlesAfterFirstPurchase.toArray(contentBundleArray);
        assertEquals(contentBundleArray[0].getContentId(), contentBundleInOffering1);

        // Buy offering2
        SubscriptionUtil.buyOffering(offering2, userUtil, cmClient);

        // Verify offering bought by getting contentbundles and verify they are the expected ones.
        Collection<com.polopoly.cm.ContentId> contentBundlesAfterSecondPurchase =
                SubscriptionUtil.getContentBundlesForUser(userUtil.getUserId(), cmClient, capability);
        // If expected and actual collection both has size 2 and all items in one collection exist in the other
        // collection, the collections must be equal (Note: contentBundlesAfterSecondPurchase contains versioned
        // ContentIds and contentBundlesInOffering1And2 contains non versioned ContentIds).
        assertTrue(contentBundlesAfterSecondPurchase.size() == 2);
        for (com.polopoly.cm.ContentId contentBundle : contentBundlesAfterSecondPurchase) {
            com.polopoly.cm.ContentId unVersionedContentId = contentBundle.getContentId();
            assertTrue(contentBundlesInOffering1And2.contains(unVersionedContentId));
        }
        assertTrue(contentBundlesInOffering1And2.size() == 2);
    }

    private ContentId createUser(final String authenticationMethod) {
        final AuthenticationManager authenticationManager = new AuthenticationManager(cmClient);
        final String userLogin = "UserUtilTestIT-user-" + UUID.randomUUID().toString();
        // Assert user does not already exist.
        assertNull("User already exist. User should not already exist.",
                authenticationManager.getUserId(authenticationMethod, userLogin));
        return authenticationManager.createUser(authenticationMethod, userLogin, new User());
    }

    private SubscriptionFieldPolicy createSubscriptionPolicyForUser() throws CMException, ContentModifiedException {
        PolicyCMServer policyCMServer = cmClient.getPolicyCMServer();
        SubscriptionFieldPolicy subscriptionPolicy =
                (SubscriptionFieldPolicy) policyCMServer.createContent(SUBSCRIPTION_MAJOR,
                        SUBSCRIPTION_TEMPLATE_ID);
        userUtil.setEngagementId(SUBSCRIPTION_ENGAGEMENT_NAME,
                IdUtil.fromPolicyContentId(subscriptionPolicy.getContentId().getContentId()));
        subscriptionPolicy.commit();
        return subscriptionPolicy;
    }

    /**
     * Gets non versioned ContentId.
     */
    private com.polopoly.cm.ContentId getContentId(final String externalContentId) throws CMException {
        return cmServer.findContentIdByExternalId(new ExternalContentId(externalContentId)).getContentId();
    }

}
