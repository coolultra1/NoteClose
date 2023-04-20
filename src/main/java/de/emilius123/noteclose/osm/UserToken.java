package de.emilius123.noteclose.osm;

/**
 * A user's OAuth token and it's status, as seen in the databse
 */
public record UserToken(String token, TokenStatus tokenStatus) {

}
