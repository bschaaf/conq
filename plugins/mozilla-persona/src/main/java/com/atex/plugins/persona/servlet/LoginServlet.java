package com.atex.plugins.persona.servlet;

import com.atex.onecms.content.ContentId;
import com.atex.plugins.users.AccessToken;
import com.atex.plugins.users.AuthenticationManager;
import com.atex.plugins.users.User;
import com.atex.plugins.users.WebClientUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.application.servlet.ApplicationServletUtil;
import com.polopoly.cm.client.CmClient;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Logger;


@WebServlet(value = "/persona/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());
    private static final String VERIFIER_URL = "https://verifier.login.persona.org/verify";
    private static final int STATUS_OK = 200;
    private CmClient cmClient;

    public LoginServlet() {
        LOG.info("Mozilla Persona login servlet started, using verifier at " + VERIFIER_URL);
    }

    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        try {
            cmClient = ApplicationServletUtil.getApplication(config.getServletContext())
                .getPreferredApplicationComponent(CmClient.class);
        } catch (IllegalApplicationStateException e) {
            throw new ServletException("Failed to get CmClient", e);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {

        final String audience = request.getServerName();
        final String assertion = request.getParameter("assertion");
        final VerifyResponse verifyResponse = verify(audience, assertion);
        LOG.fine("BrowserID response: " + verifyResponse);

        if (verifyResponse.isSuccess()) {

            String email = verifyResponse.getEmail();
            LOG.info("Login succeeded for " + email);

            AuthenticationManager authenticationManager = new AuthenticationManager(cmClient);
            ContentId userId = authenticationManager.getUserId("persona", email);
            if (userId == null) {
                User user = new User();
                user.setUsername(email);
                userId = authenticationManager.createUser("persona", email, user);
            }
            AccessToken token = authenticationManager.getToken(userId);
            WebClientUtil.onLogin(userId, token, response, cmClient);

            PrintWriter writer = response.getWriter();
            writer.print("{ \"status\": 1, \"email\": \"" + email + "\" }");
            writer.flush();
        } else {
            LOG.info("Login failed: " + verifyResponse.getReason());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private VerifyResponse verify(final String audience, final String assertion) throws IOException {

        HttpsURLConnection connection = (HttpsURLConnection) new URL(VERIFIER_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("Accept", "application/json; charset=utf-8");
        connection.setDoOutput(true);

        OutputStream outputStream = connection.getOutputStream();

        Gson gson = new Gson();
        JsonObject request = new JsonObject();
        request.add("audience", new JsonPrimitive(audience));
        request.add("assertion", new JsonPrimitive(assertion));
        outputStream.write(gson.toJson(request).getBytes());
        outputStream.flush();

        if (connection.getResponseCode() != STATUS_OK) {
            return new VerifyResponse(String.format("HTTP code %s: %s",
                                                    connection.getResponseCode(),
                                                    connection.getResponseMessage()));
        }

        InputStream inputStream = connection.getInputStream();
        try (Scanner scanner = new Scanner(inputStream, "UTF-8")) {
            String responseString = scanner.useDelimiter("\\A").next();
            VerifyResponse response = gson.fromJson(responseString, VerifyResponse.class);
            if (response == null) {
                return new VerifyResponse("Failed to parse response: " + responseString);
            }
            return response;
        }
    }

    /* Gson uses these by reflection */
    @SuppressWarnings("unused")
    private static class VerifyResponse {
        private String status;
        private String email;
        private String audience;
        private Long expires;
        private String issuer;
        private String reason;

        public VerifyResponse() {
        }

        public VerifyResponse(final String failReason) {
            status = "failure";
            reason = failReason;
        }

        public boolean isSuccess() {
            return "okay".equals(status) && email != null;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(final String status) {
            this.status = status;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(final String email) {
            this.email = email;
        }

        public String getAudience() {
            return audience;
        }

        public void setAudience(final String audience) {
            this.audience = audience;
        }

        public Long getExpires() {
            return expires;
        }

        public void setExpires(final Long expires) {
            this.expires = expires;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(final String issuer) {
            this.issuer = issuer;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(final String reason) {
            this.reason = reason;
        }

        @Override
        public String toString() {
            return "VerifyResponse{"
                + "status='" + status + '\''
                + ", email='" + email + '\''
                + ", audience='" + audience + '\''
                + ", expires=" + expires
                + ", issuer='" + issuer + '\''
                + ", reason='" + reason + '\''
                + '}';
        }
    }
}
