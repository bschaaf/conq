package com.atex.plugins.persona.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

import com.atex.plugins.users.AccessToken;
import com.atex.plugins.users.WebClientUtil;


@WebServlet(value = "/persona/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(LogoutServlet.class.getName());

    public LogoutServlet() {
        LOG.info("Mozilla Persona logout servlet started");
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {

        AccessToken token = WebClientUtil.getToken(request);
        if (token != null) {
            WebClientUtil.onLogout(response);
            LOG.fine("Logging out user " + token.getToken());
        }
    }
}
