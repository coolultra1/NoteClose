package de.emilius123.noteclose;

import de.emilius123.noteclose.osm.exception.OSMApiException;
import de.emilius123.noteclose.osm.exception.OSMDataException;
import io.javalin.http.ExceptionHandler;
import io.javalin.http.HttpCode;

/**
 * Controller with handlers for possible errors and exceptions
 */
public class ErrorController {
    public static ExceptionHandler handleException = (e, ctx) -> {
        ctx.status(HttpCode.INTERNAL_SERVER_ERROR);
        ctx.result(String.format("An exception has occurred: %s: %s", e.getClass().getSimpleName(), e.getMessage()));
    };

    public static ExceptionHandler<OSMApiException> handleOsmApiException = (e, ctx) -> {
        ctx.status(HttpCode.INTERNAL_SERVER_ERROR);
        ctx.result(String.format("An error has occurred contacting the OpenStreetMap API: %d, %s", e.getStatusCode(), e.getMessage()));
    };

    public static ExceptionHandler<OSMDataException> handleOsmDataException =(e, ctx) -> {
        ctx.status(HttpCode.INTERNAL_SERVER_ERROR);
        ctx.result(String.format("An error has occurred processing the data from the OpenStreetMap API: %s", e.getMessage()));
    };
}
