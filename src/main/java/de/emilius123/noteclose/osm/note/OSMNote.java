package de.emilius123.noteclose.osm.note;

import java.util.Date;

/**
 * A note, as represented on OpenStreetMap
 */
public record OSMNote(int id, boolean open, Date mostRecentComment) {
}
