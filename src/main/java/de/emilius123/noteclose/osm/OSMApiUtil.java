package de.emilius123.noteclose.osm;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Static utility for making requests to the OSM Api
 */
public class OSMApiUtil {
    private final String OSM_API;
    private final OAuth20Service service;

    public OSMApiUtil(String OSM_API, OAuth20Service service) {
        this.OSM_API = OSM_API;
        this.service = service;
    }

    /**
     * Get the info of a user via the API
     *
     * @return The user
     */
    public OAuthUser getUser(String token) throws IOException, ExecutionException, InterruptedException {
        // Create and run request
        OAuthRequest request = new OAuthRequest(Verb.GET, OSM_API + "user/details.json");
        service.signRequest(token, request);
        String response = service.execute(request).getBody();

        JsonObject userData = JsonParser.parseString(respo)


        return null;
    }
}
