package de.emilius123.noteclose.osm.exception;

/**
 * An anomaly in data returned by the OSM API
 */
public class OSMDataException extends Exception {
    public OSMDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public OSMDataException(String message) {
        super(message);
    }
}
