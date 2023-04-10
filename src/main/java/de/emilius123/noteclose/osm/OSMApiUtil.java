package de.emilius123.noteclose.osm;

import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.emilius123.noteclose.osm.note.ScheduledNote;
import de.emilius123.noteclose.osm.exception.OSMApiException;
import de.emilius123.noteclose.osm.exception.OSMDataException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Static utility for making requests to the OSM Api
 */
public class OSMApiUtil {
    private final String OSM_API;
    private final OAuth20Service service;
    private final SimpleDateFormat API_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    public OSMApiUtil(String OSM_API, OAuth20Service service) {
        this.OSM_API = OSM_API;
        this.service = service;
    }

    /**
     * Get the info of a user via the API
     *
     * @return The user
     */
    public OAuthUser getUser(String token) throws IOException, ExecutionException, InterruptedException, OSMApiException {
        // Create and run request
        OAuthRequest request = new OAuthRequest(Verb.GET, OSM_API + "user/details.json");
        service.signRequest(token, request);
        Response response = service.execute(request);

        // Throw APIException if Status Code isn't 200
        if(response.getCode() != 200) {
            throw new OSMApiException(response.getMessage(), response.getCode());
        }

        // Parse user data from response
        JsonObject userData = JsonParser.parseString(response.getBody()).getAsJsonObject().get("user").getAsJsonObject();
        OAuthUser user = new OAuthUser(userData.get("id").getAsInt(), userData.get("display_name").getAsString());

        return user;
    }

    /**
     * Checks for activity on a scheduled note since the note has been scheduled and whether it can be closed at all
     *
     * @param note The note to check for activity
     * @return Whether the note has had activity since scheduling (false = no). Will always be true when the note has already been closed
     */
    private boolean checkNoteCloseability(ScheduledNote note, boolean skipActivityCheck) throws IOException, ExecutionException, InterruptedException, OSMApiException, OSMDataException {
        // Create and run request
        OAuthRequest request = new OAuthRequest(Verb.GET, OSM_API + String.format("notes/%s.json", note.note()));
        Response response = service.execute(request);

        // Throw APIException if Status Code isn't 200
        if(response.getCode() != 200) {
            throw new OSMApiException(response.getMessage(), response.getCode());
        }

        // Parse data and get latest comment
        JsonObject noteProperties = JsonParser.parseString(response.getBody()).getAsJsonObject()
                .get("properties").getAsJsonObject();
        // Check if the note is closed
        if(!Objects.equals(noteProperties.get("status").getAsString(), "open")) {
            // For this, note closure counts as activity, so return true when the note is closed
            return true;
        } else {
            // Skip the activity check if skipActivityCheck
            if(skipActivityCheck) {
                return false;
            }
        }

        // Check comment activity
        JsonArray noteComments = noteProperties.getAsJsonArray("comments");
        JsonObject latestComment = noteComments.get(noteComments.size() - 1).getAsJsonObject();
        String latestCommentDate = latestComment.get("date").getAsString();
        Date latestCommenttimestamp;

        // Try to parse latestCommentDate to a Timestamp
        try {
            latestCommenttimestamp = API_DATE_FORMAT.parse(latestCommentDate);
        } catch(ParseException e) {
            // This should absolutely never happen
            throw new OSMDataException(String.format("Couldn't parse timestamp %s", latestCommentDate), e);
        }

        return note.schedule_date().compareTo(latestCommenttimestamp) <= 0;
    }

    /**
     * Close a note
     *
     * @param note The scheduledNote to close
     * @param token The OAuth token to close the note with
     * @param force Whether to skip the activity check
     * @return Whether the note has successfully been closed (true=yes, false=no)
     */
    public boolean closeNote(ScheduledNote note, String token, boolean force) throws IOException, ExecutionException, OSMDataException, InterruptedException, OSMApiException {
        // First, check if the note is closeable
        if(checkNoteCloseability(note, force)) {
            // Note can't be closed, either due to having activity (if force is true), or due to being closed already
            return false;
        }

        // Second, create and sign request
        OAuthRequest request = new OAuthRequest(Verb.POST, OSM_API + String.format("notes/%s/close", note.note()));
        if(note.message() != null) {
            // Add closing text, if existent
            request.addBodyParameter("text", note.message());
        }

        service.signRequest(token, request);
        Response response = service.execute(request);

        // Finally, check the response code and return
        if(response.getCode()  != 200) {
            // If the status code isn't 200, that wasn't successful
            throw new OSMApiException(response.getMessage(), response.getCode());
        }

        // On success, return true
        return true;
    }
}
