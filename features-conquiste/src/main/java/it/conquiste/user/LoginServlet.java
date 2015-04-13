package it.conquiste.user;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName()); 
    private static String password;

    
    @Override
    public void init() throws ServletException{
    	LoginServlet.password = getInitParameter("password");
    	LOG.info("Login servlet started.");
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {
    	
        String user = request.getParameter("username");
        String password = request.getParameter("password");
		String url = request.getParameter("thisUrl");
        HttpSession session= request.getSession();

        if(password.equals(LoginServlet.password)) session.setAttribute("applixLogin", user);
        else session.setAttribute("applixLogin", "null");

	    response.sendRedirect(url);
    }
}
