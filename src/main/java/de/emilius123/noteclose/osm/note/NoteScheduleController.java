package de.emilius123.noteclose.osm.note;

import de.emilius123.noteclose.db.DBUtil;
import de.emilius123.noteclose.osm.OAuthUser;
import de.emilius123.noteclose.osm.OSMApiUtil;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

public class NoteScheduleController {
    private DBUtil dbUtil;
    private OSMApiUtil apiUtil;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public NoteScheduleController(DBUtil dbUtil, OSMApiUtil apiUtil) {
        this.dbUtil = dbUtil;
        this.apiUtil = apiUtil;
    }

    private Timestamp parseHtmlTime(String input) {
        String inputFormatted = input.replace("T", " ");

        try {
            return Timestamp.valueOf(LocalDateTime.parse(inputFormatted, formatter));
        } catch (DateTimeParseException e) {
            return null;
        }
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
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.result("Invalid note provided");
                return;
            }
        } else {
            // No note ID provided
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("No note provided!");
            return;
        }


        // Close timestamp
        String timestampValue = ctx.formParam("close_date");
        Timestamp close_date;

        if(timestampValue != null) {
            close_date = parseHtmlTime(timestampValue);
        } else {
            // No timestamp provided
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("No timestamp provided!");
            return;
        }

        if(close_date == null) {
            // Invalid timestamp provided
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("Invalid timestamp provided!");
            return;
        }

        // All necessary params checked
        // First, check whether the note even  and whether it's still open and whether it exists
        OSMNote osmNote = apiUtil.getNote(noteId);
        if(osmNote == null || !osmNote.open()) {
            // Note doesn't exist
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("Requested note doesn't exist/is closed!");
            return;
        }

        // Now check, whether the note has already been scheduled
        if(dbUtil.isNoteScheduled(noteId)) {
            // Note is already scheduled, abort
            ctx.status(HttpStatus.CONFLICT);
            ctx.result("Note already scheduled!");
            return;
        }

        // Lastly, check whether the timestamp is in the future
        if(close_date.compareTo(new Date()) < 0) {
            // Requested close date lies in the past, abort
            ctx.status(HttpStatus.CONFLICT);
            ctx.result("Close date must lie in the future!");
        }

        // Create the scheduled note and write it to the database
        ScheduledNote note = new ScheduledNote(noteId, user.id(), null, close_date, message, null);
        dbUtil.writeNote(note);

        // Add a success message and redirect to index
        ctx.sessionAttribute("statusMessage", "Scheduled note successfully.");
        ctx.redirect("/");
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
                ctx.result("Invalid note provided!");
                ctx.status(HttpStatus.BAD_REQUEST);
                return;
            }
        } else {
            // No note ID provided
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("No note provided!");
            return;
        }

        // Now, get the note schedule from the database
        ScheduledNote note = dbUtil.getNoteSchedule(noteId);

        if(note == null || note.status() != ScheduledNoteStatus.SCHEDULED) {
            // Note isn't even scheduled, abort
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("Note not scheduled!");
            return;
        }

        if(note.osm_user() != user.id()) {
            // User didn't schedule that note, abort
            ctx.status(HttpStatus.UNAUTHORIZED);
            return;
        }

        // Note is scheduled and user is allowed, now cancel the schedule
        dbUtil.updateNoteStatus(noteId, ScheduledNoteStatus.CANCELLED);

        // Add a success message and redirect to index
        ctx.sessionAttribute("statusMessage", "Cancelled schedule successfully.");
        ctx.redirect("/");
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
                ctx.status(HttpStatus.BAD_REQUEST);
                ctx.result("Invalid note provided!");
                return;
            }
        } else {
            // No note ID provided
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("No note provided!");
            return;
        }

        // Now, get the note schedule from the database
        ScheduledNote note = dbUtil.getNoteSchedule(noteId);

        if(note == null || note.status() == ScheduledNoteStatus.CANCELLED || note.status() == ScheduledNoteStatus.EXECUTED) {
            // Note isn't even scheduled/failed, abort
            ctx.status(HttpStatus.BAD_REQUEST);
            ctx.result("Note not scheduled!");
            return;
        }

        if(note.osm_user() != user.id()) {
            // User didn't schedule that note, abort
            ctx.status(HttpStatus.UNAUTHORIZED);
            return;
        }

        apiUtil.closeNote(note, dbUtil.getUserToken(user.id()).token(), true);
        dbUtil.updateNoteStatus(noteId, ScheduledNoteStatus.EXECUTED);

        // Add a success message and redirect to index
        ctx.sessionAttribute("statusMessage", "Closed note successfully.");
        ctx.redirect("/");
    };
}
