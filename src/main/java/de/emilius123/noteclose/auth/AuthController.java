package de.emilius123.noteclose.auth;

import com.github.scribejava.core.builder.ScopeBuilder;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import de.emilius123.noteclose.osm.OSMApiUtil;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;

import java.util.Properties;

/**
 * Handles all requests regarding auth
 */
public class AuthController {
    private OAuth20Service service;
    private OSMApiUtil apiUtil;

    public AuthController(OAuth20Service service, OSMApiUtil apiUtil) {
        this.service = service;
        this.apiUtil = apiUtil;
    }

    // Retreive an authentification URL and redirect the user to it
    public Handler handleLogin = ctx -> {
        ctx.redirect(service.getAuthorizationUrl());
    };

    // Retreive final access token for user
    public Handler handleOAuthSuccess = ctx -> {
        String code = ctx.queryParam("code");
        if(code == null) {
            ctx.status(HttpCode.BAD_REQUEST);
            return;
        }

        String token = service.getAccessToken(code).getAccessToken();
        ctx.sessionAttribute("token", token);

        apiUtil.getUser(token);

        ctx.redirect("/");
    };

    // Invalidate user's session
    public Handler handleLogout = ctx -> {
        ctx.req.getSession().invalidate();
        ctx.redirect("/");
    };
}
