package de.emilius123.noteclose.auth;

import de.emilius123.noteclose.util.Path;
import io.javalin.security.RouteRole;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import org.jetbrains.annotations.NotNull;;

import java.util.Set;

/**
 * Manages access to pages restricted to users logged in with OSM
 */
public class OSMAccessManager implements io.javalin.security.AccessManager {
    @Override
    public void manage(@NotNull Handler handler, @NotNull Context ctx, @NotNull Set<? extends RouteRole> routeRoles) throws Exception {
        if(routeRoles.contains(UserScheduleRole.NOT_AUTHENTIFIED)) {
            // Page doesn't require auth, just handle
            handler.handle(ctx);
            return;
        } else if(routeRoles.contains(UserScheduleRole.AUTHENTIFIED)) {
            if(ctx.sessionAttribute("user") != null) {
                // User is authenticated, pass
                handler.handle(ctx);
                return;
            }
        } else if(routeRoles.contains(UserScheduleRole.ADMIN)) {
            // TODO: Check for admin
        }

        // If request isn't already handled, user isn't authorized. Redirect to login page
        ctx.status(HttpStatus.UNAUTHORIZED);
    }
}
