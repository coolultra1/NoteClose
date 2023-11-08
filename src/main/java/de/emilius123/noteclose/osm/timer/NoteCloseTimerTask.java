package de.emilius123.noteclose.osm.timer;

import de.emilius123.noteclose.Main;
import de.emilius123.noteclose.db.DBUtil;
import de.emilius123.noteclose.osm.OSMApiUtil;
import de.emilius123.noteclose.osm.TokenStatus;
import de.emilius123.noteclose.osm.UserToken;
import de.emilius123.noteclose.osm.exception.OSMApiException;
import de.emilius123.noteclose.osm.exception.OSMDataException;
import de.emilius123.noteclose.osm.exception.OSMInvalidAuthException;
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
    private final Logger logger = LoggerFactory.getLogger(NoteCloseTimerTask.class);

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

            // Close note
            boolean result;
            try {
                result = apiUtil.closeNote(scheduledNote, token.token(), false);
            } catch (OSMInvalidAuthException e) {
                // If the auth is invalid, mark the user token as invalid in the DB and note as failed
                dbUtil.setUserTokenRevoked(scheduledNote.osm_user());
                dbUtil.updateNoteStatus(scheduledNote.note(), ScheduledNoteStatus.FAILED);
                logger.info(String.format("Couldn't close note %d as user %d as user token has been revoked.", scheduledNote.note(), scheduledNote.osm_user()));
                return;
            }

            if(!result) {
                // Note couldn't be closed due to activity, set status to cancelled
                dbUtil.updateNoteStatus(scheduledNote.note(), ScheduledNoteStatus.CANCELLED);
                logger.info(String.format("Didn't close note %d due to activity.", scheduledNote.note()));
                return;
            }

            dbUtil.updateNoteStatus(scheduledNote.note(), ScheduledNoteStatus.EXECUTED);
            logger.info(String.format("Closed note %d as user %d", scheduledNote.note(), scheduledNote.osm_user()));
        } catch (IOException | ExecutionException | OSMDataException | InterruptedException | OSMApiException | SQLException e) {
            logger.error("Could not close note!", e);
        }
    }
}
