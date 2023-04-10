package de.emilius123.noteclose.osm.note;

import de.emilius123.noteclose.db.DBUtil;
import de.emilius123.noteclose.osm.OAuthUser;
import de.emilius123.noteclose.osm.OSMApiUtil;
import io.javalin.http.Handler;
import io.javalin.http.HttpCode;

import java.sql.Timestamp;

public class NoteScheduleController {
    private DBUtil dbUtil;
    private OSMApiUtil apiUtil;

    public NoteScheduleController(DBUtil dbUtil, OSMApiUtil apiUtil) {
        this.dbUtil = dbUtil;
        this.apiUtil = apiUtil;
    }

    public Handler handleNoteScheduleCreation = ctx -> {
        OAuthUser user = ctx.sessionAttribute("user");

        // Read Convert form params
        //Message
        String message = ctx.formParam("message");
        if(message != null) {
            if(message.equals("")) {
                message = null;
            }
        }

        // Note ID
        int noteId;
        String noteIdValue = ctx.formParam("note");

        if(noteIdValue != null) {
            try {
                noteId = Integer.parseInt(noteIdValue);
            } catch (NumberFormatException e) {
                // Provided note isn't a number, request can't be processed further
                ctx.status(HttpCode.BAD_REQUEST);
                return;
            }
        } else {
            // No note ID provided
            ctx.status(HttpCode.BAD_REQUEST);
            return;
        }


        // Close timestamp
        String timestampValue = ctx.formParam("close_date");
        Timestamp close_date;

        if(timestampValue != null) {
            close_date = new Timestamp(1);
        } else {
            // No timestamp provided
            ctx.status(HttpCode.BAD_REQUEST);
            return;
        }

        // All necessary params checked
        // First, check whether the note even  and whether it's still open and whether it exists
        OSMNote osmNote = apiUtil.getNote(noteId);
        if(osmNote == null || !osmNote.isOpen()) {
            // Note doesn't exist
            ctx.status(HttpCode.BAD_REQUEST);
            return;
        }

        // Now check, whether the note has already been scheduled
        if(dbUtil.isNoteScheduled(noteId)) {
            // Note is already scheduled, abort
            ctx.status(HttpCode.CONFLICT);
            ctx.result("Note already scheduled!");
            return;
        }

        // Create the scheduled note and write it to the database
        ScheduledNote note = new ScheduledNote(noteId, user.id(), null, close_date, message, null);
        dbUtil.writeNote(note);
    };

    public Handler handleNoteScheduleCancellation = ctx -> {
        OAuthUser user = (OAuthUser) ctx.sessionAttribute("user");

        // First, get note id from params
        int noteId;
        String noteIdValue = ctx.formParam("note");

        if(noteIdValue != null) {
            try {
                noteId = Integer.parseInt(noteIdValue);
            } catch (NumberFormatException e) {
                // Provided note isn't a number, request can't be processed further
                ctx.status(HttpCode.BAD_REQUEST);
                return;
            }
        } else {
            // No note ID provided
            ctx.status(HttpCode.BAD_REQUEST);
            return;
        }

        // Now, get the note schedule from the database
        ScheduledNote note = dbUtil.getNoteSchedule(noteId);

        if(note == null || note.status() != ScheduledNoteStatus.SCHEDULED) {
            // Note isn't even scheduled, abort
            ctx.status(HttpCode.BAD_REQUEST);
            return;
        }

        if(note.osm_user() != user.id()) {
            // User didn't schedule that note, abort
            ctx.status(HttpCode.UNAUTHORIZED);
            return;
        }

        // Note is scheduled and user is allowed, now cancel the schedule
        dbUtil.updateNoteStatus(noteId, ScheduledNoteStatus.CANCELLED);
    };

    /**
     * Handles the request to close a note early
     */
    public Handler handleNoteClosure = ctx -> {
        OAuthUser user = (OAuthUser) ctx.sessionAttribute("user");

        // First, get note id from params
        int noteId;
        String noteIdValue = ctx.formParam("note");

        if(noteIdValue != null) {
            try {
                noteId = Integer.parseInt(noteIdValue);
            } catch (NumberFormatException e) {
                // Provided note isn't a number, request can't be processed further
                ctx.status(HttpCode.BAD_REQUEST);
                return;
            }
        } else {
            // No note ID provided
            ctx.status(HttpCode.BAD_REQUEST);
            return;
        }

        // Now, get the note schedule from the database
        ScheduledNote note = dbUtil.getNoteSchedule(noteId);

        if(note == null || note.status() != ScheduledNoteStatus.SCHEDULED) {
            // Note isn't even scheduled, abort
            ctx.status(HttpCode.BAD_REQUEST);
            return;
        }

        if(note.osm_user() != user.id()) {
            // User didn't schedule that note, abort
            ctx.status(HttpCode.UNAUTHORIZED);
            return;
        }

        apiUtil.closeNote(note, dbUtil.getUserToken(user.id()), true);
        dbUtil.updateNoteStatus(noteId, ScheduledNoteStatus.EXECUTED);
    };
}
