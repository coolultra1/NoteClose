package de.emilius123.noteclose.auth;

import com.github.scribejava.core.model.OAuth2AccessTokenErrorResponse;
import com.github.scribejava.core.oauth.OAuth20Service;
import de.emilius123.noteclose.db.DBUtil;
import de.emilius123.noteclose.osm.OAuthUser;
import de.emilius123.noteclose.osm.OSMApiUtil;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;

/**
 * Handles all requests regarding auth
 */
public class AuthController {
    private OAuth20Service service;
    private OSMApiUtil apiUtil;
    private DBUtil dbUtil;

    public AuthController(OAuth20Service service, OSMApiUtil apiUtil, DBUtil dbUtil) {
        this.service = service;
        this.apiUtil = apiUtil;
        this.dbUtil = dbUtil;
    }

    // Called before all requests to /auth endpoints to
    public Handler beforeAuth = ctx -> {
        // If the user is already logged in, just redirect to index
        if(ctx.sessionAttribute("user") != null) {
            ctx.redirect("/");
        }
    };

    // Retrieve an authentication URL and redirect the user to it
    public Handler handleLogin = ctx -> {
        // Retrieve authorizationUrl and redirect to it
        ctx.redirect(service.getAuthorizationUrl());
    };

    // Retrieve final access token for user
    public Handler handleOAuthSuccess = ctx -> {
        String code = ctx.queryParam("code");
        // If code is null, bad request instantly
        if(code == null) {
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("No code provided!");
            return;
        }

        // Try to retrieve token by given code
        String token;
        try {
            token = service.getAccessToken(code).getAccessToken();
        } catch(OAuth2AccessTokenErrorResponse e) {
            // If token retrieval fails, return bad request
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("Invalid code provided!");
            return;
        }

        // If successful, get user data from API
        OAuthUser user = apiUtil.getUser(token);

        // Save user data to session and token to database
        ctx.sessionAttribute("user", user);
        dbUtil.writeUserToken(user.id(), token);

        // Finally, redirect to index
        ctx.redirect("/");
    };

    // Invalidate user's session
    public Handler handleLogout = ctx -> {
        ctx.req().getSession().invalidate();

        // Finally, redirect to index
        ctx.redirect("/");
    };
}
