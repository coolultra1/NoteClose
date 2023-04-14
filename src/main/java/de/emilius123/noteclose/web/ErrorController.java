package de.emilius123.noteclose.web;

import de.emilius123.noteclose.osm.exception.OSMApiException;
import de.emilius123.noteclose.osm.exception.OSMDataException;
import de.emilius123.noteclose.util.Path;
import io.javalin.http.ExceptionHandler;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;

/**
 * Controller with handlers for possible errors and exceptions
 */
public class ErrorController {
    public static ExceptionHandler handleException = (e, ctx) -> {
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        ctx.result(String.format("An exception has occurred: %s: %s", e.getClass().getSimpleName(), e.getMessage()));

        e.printStackTrace();
    };

    public static ExceptionHandler<OSMApiException> handleOsmApiException = (e, ctx) -> {
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        ctx.result(String.format("An error has occurred contacting the OpenStreetMap API: %d, %s", e.getStatusCode(), e.getMessage()));

        e.printStackTrace();
    };

    public static ExceptionHandler<OSMDataException> handleOsmDataException =(e, ctx) -> {
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        ctx.result(String.format("An error has occurred processing the data from the OpenStreetMap API: %s", e.getMessage()));

        e.printStackTrace();
    };

    public static Handler handle401 = ctx -> {
        if(ctx.sessionAttribute("user") == null) {
            // User is not logged in, redirect to login
            ctx.redirect(Path.Web.OSM_LOGIN);
        } else {
            // User is logged in and still not permitted
            ctx.result("You are not allowed to do this.");
        }
    };
}
