package de.emilius123.noteclose;

import com.github.scribejava.core.builder.ScopeBuilder;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import de.emilius123.noteclose.auth.AuthController;
import de.emilius123.noteclose.auth.DBConnection;
import de.emilius123.noteclose.osm.OSMApiUtil;
import de.emilius123.noteclose.util.Path;
import io.javalin.Javalin;
import org.eclipse.jetty.server.session.SessionHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
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

        // Setup and start Javalin
        Supplier<SessionHandler> sessionHandlerSupplier = new DBConnection(properties).databaseSessionHandler();

        Javalin app = Javalin.create(config -> {
            config.sessionHandler(sessionHandlerSupplier);
        }).start(Integer.parseInt(properties.getProperty("web.port")));

        AuthController authController = new AuthController(service, apiUtil);
        app.routes(() -> {
            get(Path.Web.OSM_LOGIN, authController.handleLogin);
            get(Path.Web.OAUTH_CALLBACK, authController.handleOAuthSuccess);
            get(Path.Web.OSM_LOGOUT, authController.handleLogout);
        });
    }
}