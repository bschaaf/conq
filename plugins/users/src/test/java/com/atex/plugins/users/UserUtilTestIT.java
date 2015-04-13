package com.atex.plugins.users;

import com.atex.onecms.content.ContentId;
import com.google.inject.Inject;
import com.polopoly.cm.client.CmClient;
import com.polopoly.testnj.TestNJRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(TestNJRunner.class)
public class UserUtilTestIT {

    private static final String MY_SECRET_ENGAGEMENT_NAME = "A foo is a foo and not a bar.";
    private static final String ENGAGEMENT_NAME = "UserUtilTestIT-test-engagement";

    @Inject
    private CmClient cmClient;
    private UserUtil userUtil;

    @Before
    public void init() {
        final String authenticationMethod = new AuthenticationMethodCreator(cmClient).create().getName();
        userUtil = new UserUtil(createUser(authenticationMethod), cmClient.getContentManager());
    }

    @Test
    public void testSetGetEngagement() throws Exception {
        // Verify no engagement with name ENGAGEMENT_NAME.
        assertNull(userUtil.getEngagement(User.class, ENGAGEMENT_NAME));

        // Set engagement. Use an instance of User as engagement.
        final User testEngagement = new User();
        testEngagement.setUsername(MY_SECRET_ENGAGEMENT_NAME);
        userUtil.setEngagement(User.class, testEngagement, ENGAGEMENT_NAME);

        // Get engagement.
        final User readEngagement = userUtil.getEngagement(User.class, ENGAGEMENT_NAME);
        assertEquals(MY_SECRET_ENGAGEMENT_NAME, readEngagement.getUsername());
    }

    private ContentId createUser(final String authenticationMethod) {
        final AuthenticationManager authenticationManager = new AuthenticationManager(cmClient);
        final String userLogin = "UserUtilTestIT-user-" + UUID.randomUUID().toString();
        // Assert user does not already exist.
        assertNull("User already exist. User should not already exist.",
                authenticationManager.getUserId(authenticationMethod, userLogin));
        return authenticationManager.createUser(authenticationMethod, userLogin, new User());
    }

}
