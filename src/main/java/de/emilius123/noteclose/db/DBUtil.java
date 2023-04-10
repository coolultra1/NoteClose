package de.emilius123.noteclose.db;

import de.emilius123.noteclose.osm.note.ScheduledNote;
import de.emilius123.noteclose.osm.note.ScheduledNoteStatus;

import java.sql.*;
import java.util.ArrayList;

/**
 * Has functions for writing/reading specifig data to/from the database
 */
public class DBUtil {
    private final Connection connection;

    public DBUtil(Connection connection) {
        this.connection = connection;
    }

    /**
     * Retrieves a user's OAuth token from the database
     *
     * @param userId The OSM id of the user
     */
    public String getUserToken(int userId) throws SQLException {
        // Prepare and execute statement
        PreparedStatement statement = connection.prepareStatement("select token from user_token where id=?");
        statement.setInt(1, userId);
        ResultSet result = statement.executeQuery();

        // Get token and return it
        if(!result.next()) {
            // Return null, if the ResultSet is empty, meaning there is no saved token for that user
            return null;
        }

        // Return token
        return result.getString(1);
    }

    /**
     * Writes a user's OAuth token to the database
     *
     * @param userId The user's OSM id
     * @param token The user's token
     */
    public void writeUserToken(int userId, String token) throws SQLException {
        // Prepare and execute statement
        PreparedStatement statement = connection.prepareStatement("replace into user_token(id,token) values(?,?)");
        statement.setInt(1, userId);
        statement.setString(2, token);
        statement.execute();
    }

    /**
     * Retrieves a given user's scheduled notes (currently max. 50)
     *
     * @param user The user, of whom the schedules notes are to retrieve
     * @return An ArrayList containing the user's most recent 50 note schedules
     */
    public ArrayList<ScheduledNote> getUserNotes(int user) throws SQLException {
        // Prepare and execute statement
        PreparedStatement statement = connection.prepareStatement("select * from note where osm_user=? order by schedule_date desc limit 50");
        statement.setInt(1, user);
        ResultSet result = statement.executeQuery();

        // Loop through ResultSet, adding all notes from there into the ArrayList
        ArrayList<ScheduledNote> userNotes = new ArrayList<>();
        while(result.next()) {
            userNotes.add(new ScheduledNote(
                    result.getInt(1), // Note ID
                    result.getInt(2), // OSM User ID
                    result.getTimestamp(3), // Schedule date
                    result.getTimestamp(4), // Close date
                    result.getString(5), // Close message
                    ScheduledNoteStatus.valueOf(result.getString(6)))); // Schedule status
        }

        // Finally, return the ArrayList
        return userNotes;
    }

    /**
     * Writes a new scheduled note to the database
     *
     * @param note The note to write to the database
     */
    public void writeNote(ScheduledNote note) throws SQLException {
        // Prepare and execute statement
        PreparedStatement statement = connection.prepareStatement("insert into note(note,osm_user,close_date) values(?,?,?)");
        statement.setInt(1, note.note());
        statement.setInt(2, note.osm_user());
        statement.setTimestamp(3, note.close_date());
        statement.execute();
    }

    public void updateNoteStatus(int id, ScheduledNoteStatus status) throws SQLException {
        // Prepare and execute statement
        PreparedStatement statement = connection.prepareStatement("update note set info=? where note=?");
        statement.setString(1, status.name());
        statement.setInt(2, id);
        statement.execute();
    }
}
