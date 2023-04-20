package de.emilius123.noteclose.osm.timer;

import de.emilius123.noteclose.Main;
import de.emilius123.noteclose.db.DBUtil;
import de.emilius123.noteclose.osm.OSMApiUtil;
import de.emilius123.noteclose.osm.TokenStatus;
import de.emilius123.noteclose.osm.UserToken;
import de.emilius123.noteclose.osm.exception.OSMApiException;
import de.emilius123.noteclose.osm.exception.OSMDataException;
import de.emilius123.noteclose.osm.note.ScheduledNote;
import de.emilius123.noteclose.osm.note.ScheduledNoteStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class NoteCloseTimerTask extends TimerTask {
    private final ScheduledNote scheduledNote;
    private final OSMApiUtil apiUtil;
    private final DBUtil dbUtil;
    private final Logger logger = LoggerFactory.getLogger(Main.class);

    public NoteCloseTimerTask(ScheduledNote scheduledNote, OSMApiUtil apiUtil, DBUtil dbUtil) {
        this.scheduledNote = scheduledNote;
        this.apiUtil = apiUtil;
        this.dbUtil = dbUtil;
    }

    @Override
    public void run() {
        try {
            UserToken token = dbUtil.getUserToken(scheduledNote.osm_user());
            if(token == null) {
                logger.warn(String.format("Found token for user %d is null", scheduledNote.osm_user()));
                return;
            } else if(token.tokenStatus() == TokenStatus.REVOKED) {
                // If the token is revoked, just set the note status to failed
                //Set status to failed
                dbUtil.updateNoteStatus(scheduledNote.note(), ScheduledNoteStatus.FAILED);

                //And log it
                logger.info(String.format("Token for user %d revoked, can't close note %d.", scheduledNote.osm_user(), scheduledNote.note()));
                return;
            }

            apiUtil.closeNote(scheduledNote, token.token(), false);
            dbUtil.updateNoteStatus(scheduledNote.note(), ScheduledNoteStatus.EXECUTED);
        } catch (IOException | ExecutionException | OSMDataException | InterruptedException | OSMApiException | SQLException e) {
            e.printStackTrace();
        }
    }
}
