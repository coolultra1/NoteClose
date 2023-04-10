package de.emilius123.noteclose;

import com.github.scribejava.core.builder.ScopeBuilder;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import de.emilius123.noteclose.auth.AuthController;
import de.emilius123.noteclose.db.DBConnection;
import de.emilius123.noteclose.db.DBUtil;
import de.emilius123.noteclose.osm.OSMApiUtil;
import de.emilius123.noteclose.osm.exception.OSMApiException;
import de.emilius123.noteclose.osm.exception.OSMDataException;
import de.emilius123.noteclose.osm.note.ScheduledNote;
import de.emilius123.noteclose.osm.note.ScheduledNoteStatus;
import de.emilius123.noteclose.util.Path;
import io.javalin.Javalin;
import org.eclipse.jetty.server.session.SessionHandler;
import org.xml.sax.ErrorHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static io.javalin.apibuilder.ApiBuilder.get;

public class Main {
    private static final String PROPERTIES_NAME = "noteclose.properties";

    public static void main(String[] args) {
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

        // Setup and start Javalin
        Supplier<SessionHandler> sessionHandlerSupplier = dbConnection.databaseSessionHandler();

        Javalin app = Javalin.create(config -> {
            config.sessionHandler(sessionHandlerSupplier);
        }).start(Integer.parseInt(properties.getProperty("web.port")));

        // Set up routes
        AuthController authController = new AuthController(service, apiUtil, dbUtil);
        app.routes(() -> {
            get(Path.Web.OSM_LOGIN, authController.handleLogin);
            get(Path.Web.OAUTH_CALLBACK, authController.handleOAuthSuccess);
            get(Path.Web.OSM_LOGOUT, authController.handleLogout);
        });

        app.exception(OSMApiException.class, ErrorController.handleOsmApiException);
        app.exception(OSMDataException.class, ErrorController.handleOsmDataException);
        app.exception(Exception.class, ErrorController.handleException);
    }
}