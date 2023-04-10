package de.emilius123.noteclose.osm.exception;

/**
 * An anomaly in a response by the OSM API
 */
public class OSMApiException extends Exception {
    private final int statusCode;

    public OSMApiException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public OSMApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
