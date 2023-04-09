package de.emilius123.noteclose.osm;

/**
 * An OSM user, as OAuth registered for this app via OAuth
 */
public class OAuthUser {
    private final String username;
    private final int id;

    public OAuthUser(String username, int id) {
        this.username = username;
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public int getId() {
        return id;
    }
}
