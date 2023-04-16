package de.emilius123.noteclose;

import com.github.scribejava.core.builder.ScopeBuilder;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import de.emilius123.noteclose.auth.AuthController;
import de.emilius123.noteclose.auth.OSMAccessManager;
import de.emilius123.noteclose.auth.UserScheduleRole;
import de.emilius123.noteclose.db.DBConnection;
import de.emilius123.noteclose.db.DBUtil;
import de.emilius123.noteclose.osm.OSMApiUtil;
import de.emilius123.noteclose.osm.exception.OSMApiException;
import de.emilius123.noteclose.osm.exception.OSMDataException;
import de.emilius123.noteclose.osm.note.NoteScheduleController;
import de.emilius123.noteclose.osm.timer.ClosingNoteFinderTimerTask;
import de.emilius123.noteclose.util.NoteCloseProperties;
import de.emilius123.noteclose.util.Path;
import de.emilius123.noteclose.web.ErrorController;
import de.emilius123.noteclose.web.IndexHandler;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.runtime.Template;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;
import org.eclipse.jetty.server.session.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Properties;
import java.util.Timer;
import java.util.function.Supplier;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Main {
    private static final String PROPERTIES_NAME = "noteclose.properties";

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Main.class);

        // Before anything, load the properties
        File propertiesFile = new File(PROPERTIES_NAME);
        Properties properties = new Properties(new NoteCloseProperties());

        try {
            if(propertiesFile.exists()) {
                properties.load(new FileInputStream(propertiesFile));
            } else {
                properties = new NoteCloseProperties();
                properties.store(new FileOutputStream(propertiesFile), "NoteClose Settings");
            }
        } catch (IOException e) {
            // There is no help anymore, die
            logger.error("Couldn't load config!");
            e.printStackTrace();
            return;
        }

        // Properties available now! Start everything up now.
        // Initializing various things
        OAuth20Service service = new ServiceBuilder(properties.getProperty("oauth.client-key"))
                .apiSecret(properties.getProperty("oauth.client-secret"))
                .callback(properties.getProperty("oauth.callback"))
                .defaultScope(new ScopeBuilder("read_prefs", "write_notes").build())
                .build(new de.emilius123.noteclose.auth.OSMOAuthApi(properties.getProperty("oauth.baseurl")));
        OSMApiUtil apiUtil = new OSMApiUtil(properties.getProperty("osm.api"), service);

        // MySQL connection
        DBConnection dbConnection = new DBConnection(properties);
        Connection connection = dbConnection.connect();
        DBUtil dbUtil = new DBUtil(connection);

        // Start the Timers
        //Read delay from the config
        int delay;
        try {
            delay = Integer.parseInt(properties.getProperty("timer.interval"));
        }  catch (NumberFormatException e) {
            delay = 900;
            logger.warn(String.format("Couldn't read timer interval from config, defaulting to %d.", delay));
        }

        //Now, start the timer
        Timer timer = new Timer("Closing notes finder");
        timer.scheduleAtFixedRate(new ClosingNoteFinderTimerTask(apiUtil, dbUtil, delay), 0, delay * 1000L);

        // Setup and start Javalin
        Supplier<SessionHandler> sessionHandlerSupplier = dbConnection.databaseSessionHandler();

        Javalin app = Javalin.create(config -> {
            config.jetty.sessionHandler(sessionHandlerSupplier);
            config.accessManager(new OSMAccessManager());
            config.staticFiles.add(staticFileConfig -> {
                staticFileConfig.hostedPath = "/assets";
                staticFileConfig.directory = "/web/public";
                staticFileConfig.location = Location.CLASSPATH;
            });
        }).start(Integer.parseInt(properties.getProperty("web.port")));

        // Set up JTE
        JavalinJte.init(TemplateEngine.createPrecompiled(ContentType.Html));

        // Set up routes
        IndexHandler indexHandler = new IndexHandler(dbUtil);
        AuthController authController = new AuthController(service, apiUtil, dbUtil);
        NoteScheduleController scheduleController = new NoteScheduleController(dbUtil, apiUtil);
        app.routes(() -> {
            get("/", indexHandler.handleIndex, UserScheduleRole.NOT_AUTHENTIFIED);

            get("/login", ctx -> {ctx.redirect(Path.Web.OSM_LOGIN, HttpStatus.MOVED_PERMANENTLY);}, UserScheduleRole.NOT_AUTHENTIFIED);
            get(Path.Web.OSM_LOGIN, authController.handleLogin, UserScheduleRole.NOT_AUTHENTIFIED);
            get(Path.Web.OAUTH_CALLBACK, authController.handleOAuthSuccess, UserScheduleRole.NOT_AUTHENTIFIED);
            get(Path.Web.OSM_LOGOUT, authController.handleLogout, UserScheduleRole.NOT_AUTHENTIFIED);

            post(Path.Web.NOTE_SCHEDULE, scheduleController.handleNoteScheduleCreation, UserScheduleRole.AUTHENTIFIED);
            post(Path.Web.NOTE_CANCEL, scheduleController.handleNoteScheduleCancellation, UserScheduleRole.AUTHENTIFIED);
            post(Path.Web.NOTE_CLOSE, scheduleController.handleNoteClosure, UserScheduleRole.AUTHENTIFIED);
        });

        app.before(Path.Web.AUTH_PREFIX + "*", authController.beforeAuth);

        app.exception(OSMApiException.class, ErrorController.handleOsmApiException);
        app.exception(OSMDataException.class, ErrorController.handleOsmDataException);
        app.exception(Exception.class, ErrorController.handleException);
        app.error(HttpStatus.UNAUTHORIZED.getCode(), ErrorController.handle401);
    }
}