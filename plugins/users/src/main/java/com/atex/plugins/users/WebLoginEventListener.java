package com.atex.plugins.users;

import com.atex.onecms.content.ContentId;
import com.polopoly.cm.client.CmClient;

import javax.servlet.http.HttpServletResponse;

public interface WebLoginEventListener {
    void onLogin(final ContentId user,
                 final AccessToken accessToken,
                 final HttpServletResponse response,
                 final CmClient cmClient);
}
