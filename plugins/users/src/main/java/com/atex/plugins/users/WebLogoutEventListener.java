package com.atex.plugins.users;

import javax.servlet.http.HttpServletResponse;

public interface WebLogoutEventListener {
    void onLogout(final HttpServletResponse response);
}
