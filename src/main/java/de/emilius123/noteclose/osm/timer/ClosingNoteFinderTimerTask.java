package de.emilius123.noteclose.osm.timer;

import de.emilius123.noteclose.Main;
import de.emilius123.noteclose.db.DBUtil;
import de.emilius123.noteclose.osm.OSMApiUtil;
import de.emilius123.noteclose.osm.note.ScheduledNote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ClosingNoteFinderTimerTask extends TimerTask {
    private final OSMApiUtil apiUtil;
    private final DBUtil dbUtil;
    private final int future;
    private final Logger logger = LoggerFactory.getLogger(ClosingNoteFinderTimerTask.class);

    public ClosingNoteFinderTimerTask(OSMApiUtil apiUtil, DBUtil dbUtil, int future) {
        this.apiUtil = apiUtil;
        this.dbUtil = dbUtil;
        this.future = future;
    }

    @Override
    public void run() {
        logger.info("Looking for closing notes");

        ArrayList<ScheduledNote> closingNotes;
        // Try to get closingNotes
        try {
            closingNotes = dbUtil.getClosingNotes(future);
        } catch (SQLException e) {
            logger.error("Could not find notes to close!", e);
            return;
        }

        // Schedule all closing notes for closure at their exact times
        Timer timer = new Timer("Note closures");
        for(ScheduledNote currentNote : closingNotes) {
            timer.schedule(new NoteCloseTimerTask(currentNote, apiUtil, dbUtil), new Date(currentNote.close_date().getTime()));
        }

        // Log success
        logger.info(String.format("Scheduled %d notes for closure", closingNotes.size()));
    }
}
