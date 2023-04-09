package de.emilius123.noteclose.auth;

import com.github.scribejava.core.builder.api.DefaultApi20;

/**
 * OpenStreetMap OAuth2 API implemented as DefaultApi20
 */
public class OSMOAuthApi extends DefaultApi20 {
    public OSMOAuthApi(String API_ENDPOINT_BASE) {
        this.API_ENDPOINT_BASE = API_ENDPOINT_BASE;
    }
    private final String API_ENDPOINT_BASE;

    @Override
    public String getAccessTokenEndpoint() {
        return API_ENDPOINT_BASE + "token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return API_ENDPOINT_BASE + "authorize";
    }
}
