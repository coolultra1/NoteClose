package de.emilius123.noteclose.osm;

import java.io.Serializable;

/**
 * An OSM user, as OAuth registered for this app via OAuth
 */
public record OAuthUser(int id, String username) implements Serializable { }
