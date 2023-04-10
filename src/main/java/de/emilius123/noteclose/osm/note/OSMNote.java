package de.emilius123.noteclose.osm.note;

import java.util.Date;

/**
 * A note, as represented on OpenStreetMap
 */
public class OSMNote {
    private final int id;
    private final boolean open;
    private final Date mostRecentComment;

    public OSMNote(int id, boolean open, Date mostRecentComment) {
        this.id = id;
        this.open = open;
        this.mostRecentComment = mostRecentComment;
    }

    public int getId() {
        return id;
    }

    public boolean isOpen() {
        return open;
    }

    public Date getMostRecentComment() {
        return mostRecentComment;
    }
}
