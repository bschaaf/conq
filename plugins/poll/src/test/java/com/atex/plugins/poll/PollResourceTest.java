package com.atex.plugins.poll;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.polopoly.cm.client.CMException;
import com.polopoly.poll.policy.QuestionPolicy;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.test.framework.JerseyTest;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class PollResourceTest extends JerseyTest {

    public static final int HTTP_STATUS_FORBIDDEN = 403;
    public static final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500;
    public static final int HTTP_STATUS_SEE_OTHER = 303;
    public static final long TOTAL_VOTE_COUNT = 100;

    @Mock
    private PollElementPolicy pollElementPolicy;
    @Mock
    private QuestionPolicy questionPolicy;
    private QuestionPolicy.Value[] results = {
            new QuestionPolicy.Value("Yes", "yes-id", 0, 70, TOTAL_VOTE_COUNT, 0),
            new QuestionPolicy.Value("No", "no-id", 0, 30, TOTAL_VOTE_COUNT, 0)};


    @Before
    public void setUp() throws CMException {
        MockitoAnnotations.initMocks(this);

        when(pollElementPolicy.getQuestion()).thenReturn(questionPolicy);
        when(questionPolicy.getResults(anyString())).thenReturn(results);
        when(questionPolicy.getTotalVoteCount(anyString())).thenReturn(TOTAL_VOTE_COUNT);

        PollResourceWrapper.setRegisterVoteSuccess(true);
        PollResourceWrapper.setPollElementPolicy(pollElementPolicy);
        PollResourceWrapper.setIsCookieValid(true);
    }

    /**
     * Tell test frame work that where to find resource and provider classes.
     */
    public PollResourceTest() throws Exception {
        super("com.atex.plugins.poll");
    }

    @Test
    public void voteGivenCookiesNotEnabled() {
        PollResourceWrapper.setIsCookieValid(false);
        ClientResponse response = postVote(ClientResponse.class);
        JsonObject jsonObject = toJson(response);

        assertEquals(HTTP_STATUS_FORBIDDEN, response.getStatus());
        assertEquals("error", jsonObject.get("status").getAsString());
        assertEquals(PollResource.YOU_MUST_HAVE_COOKIES_ENABLED,
                jsonObject.get("message").getAsString());
    }

    @Test
    public void voteGivenWeHaveAlreadyVoted() {
        ClientResponse response1 = postVote(ClientResponse.class);
        List<Cookie> cookies = new ArrayList<>();
        for (NewCookie cookieFromResponse : response1.getCookies()) {
            cookies.add(new Cookie(cookieFromResponse.getName(),
                    cookieFromResponse.getValue()));
        }
        ClientResponse response2 = postVote(ClientResponse.class, cookies);
        JsonObject jsonObject = toJson(response2);

        assertEquals(HTTP_STATUS_FORBIDDEN, response2.getStatus());
        assertEquals("error", jsonObject.get("status").getAsString());
        assertEquals(PollResource.YOU_HAVE_ALREADY_VOTED,
                jsonObject.get("message").getAsString());
    }

    @Test
    public void voteSuccess() throws ParseException {
        JsonObject response = postVote();
        JsonObject theResults = response.get("results").getAsJsonObject();

        assertEquals("ok", response.get("status").getAsString());
        assertEquals(TOTAL_VOTE_COUNT, response.get("totalVoteCount").getAsLong());

        assertTrue(theResults.has("yes-id"));
        assertTrue(theResults.has("no-id"));

        JsonObject yesOption = theResults.get("yes-id").getAsJsonObject();
        JsonObject noOption = theResults.get("no-id").getAsJsonObject();

        assertEquals(70, yesOption.get("count").getAsInt());
        assertEquals(70, yesOption.get("percent").getAsInt());

        assertEquals(30, noOption.get("count").getAsInt());
        assertEquals(30, noOption.get("percent").getAsInt());
    }

    @Test
    public void voteNotSuccessful() {
        PollResourceWrapper.setRegisterVoteSuccess(false);
        ClientResponse response = postVote(ClientResponse.class);
        JsonObject jsonObject = toJson(response);

        assertEquals(HTTP_STATUS_INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("error", jsonObject.get("status").getAsString());
        assertEquals(PollResource.UNABLE_TO_SUBMIT_VOTE,
                jsonObject.get("message").getAsString());
    }

    /**
     * Verifies that we will be redirected when parameter cookieSet has value false.
     */
    @Test
    public void voteGivenCookieSetFalse() {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("pollId", "pollId");
        queryParams.add("pollContentId", "pollContentId");
        queryParams.add("questionId", "questionId");
        queryParams.add("answerId", "answerId");
        queryParams.add("cookieSet", "false");
        WebResource webResource = resource().
                queryParams(queryParams).
                path("/pollresource-test");
        // Turn of redirect so that we can verify the response we want to verify.
        webResource.setProperty(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, false);
        ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);

        assertEquals(HTTP_STATUS_SEE_OTHER, response.getStatus());
    }

    private JsonObject postVote() {
        String response = postVote(MediaType.APPLICATION_JSON.getClass());
        JsonElement jsonElement = new JsonParser().parse(response);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return jsonObject;
    }

    private <T> T postVote(final Class<T> clazz) {
        return postVote(clazz, new ArrayList<Cookie>());
    }

    private <T> T postVote(final Class<T> clazz, final List<Cookie> cookies) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("pollId", "pollId");
        queryParams.add("pollContentId", "pollContentId");
        queryParams.add("questionId", "questionId");
        queryParams.add("answerId", "answerId");
        queryParams.add("cookieSet", "true");
        WebResource.Builder builder = resource().
                queryParams(queryParams).
                path("/pollresource-test").
                type(MediaType.APPLICATION_JSON);
        for (Cookie cookie : cookies) {
            builder = builder.cookie(cookie);
        }
        return builder.get(clazz);
    }

    private JsonObject toJson(final ClientResponse response) {
        JsonElement jsonElement = new JsonParser().parse(response.getEntity(String.class));
        return jsonElement.getAsJsonObject();
    }


}
