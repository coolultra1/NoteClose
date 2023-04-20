package de.emilius123.noteclose.osm;

/**
 * The known status of a stored user's OAuth token, as seen in the database
 */
public enum TokenStatus {
    AUTHORIZED, // Default value
    REVOKED
}
