package de.emilius123.noteclose.osm.note;

import java.sql.Timestamp;

public record ScheduledNote(int note, int osm_user, Timestamp schedule_date,
                            Timestamp close_date, String message,
                            ScheduledNoteStatus status) {
}
