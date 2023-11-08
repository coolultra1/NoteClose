package de.emilius123.noteclose.web;

import de.emilius123.noteclose.Main;
import de.emilius123.noteclose.osm.exception.OSMApiException;
import de.emilius123.noteclose.osm.exception.OSMDataException;
import de.emilius123.noteclose.osm.exception.OSMInvalidAuthException;
import de.emilius123.noteclose.util.Path;
import io.javalin.http.ExceptionHandler;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller with handlers for possible errors and exceptions
 */
public class ErrorController {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static ExceptionHandler<Exception> handleException = (e, ctx) -> {
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        ctx.result(String.format("An exception has occurred: %s: %s", e.getClass().getSimpleName(), e.getMessage()));

        logger.error("An exception has occurred handling a request!", e);
    };

    public static ExceptionHandler<OSMApiException> handleOsmApiException = (e, ctx) -> {
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        ctx.result(String.format("An error has occurred contacting the OpenStreetMap API: %d, %s", e.getStatusCode(), e.getMessage()));

        logger.error("An exception has occurred contacting the OSM API!", e);
    };

    public static ExceptionHandler<OSMDataException> handleOsmDataException = (e, ctx) -> {
        ctx.status(HttpStatus.INTERNAL_SERVER_ERROR);
        ctx.result(String.format("An error has occurred processing the data from the OpenStreetMap API: %s", e.getMessage()));

        logger.error("An exception has occurred in processing of data retrieved from the OSM API!", e);
    };

    public static ExceptionHandler<OSMInvalidAuthException> handleOsmInvalidAuthException = (e, ctx) -> {
        ctx.status(HttpStatus.CONFLICT);
        ctx.result("Your request to the OpenStreetMap API could not be fulfilled as your OAuth token is invalid. Please re-authenticate.");
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
