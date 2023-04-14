package de.emilius123.noteclose.auth;

import io.javalin.security.RouteRole;

/**
 * A role a user can have
 */
public enum UserScheduleRole implements RouteRole {
    NOT_AUTHENTIFIED,
    AUTHENTIFIED,
    ADMIN
}
