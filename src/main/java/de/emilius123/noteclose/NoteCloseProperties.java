package de.emilius123.noteclose;

import java.util.Properties;

/**
 * Default Configuration for EZPref
 */
public class NoteCloseProperties extends Properties {
    public NoteCloseProperties() {
        // OSM API and OAuth
        this.setProperty("osm.api", "https://master.apis.dev.openstreetmap.org/api/0.6/");
        this.setProperty("oauth.baseurl", "https://master.apis.dev.openstreetmap.org/oauth2/");
        this.setProperty("oauth.client-key", "");
        this.setProperty("oauth.client-secret", "");
        this.setProperty("oauth.callback", "http://127.0.0.1:7777/oauth_complete");

        // Web
        this.setProperty("web.port", "7777");

        // MySQL
        this.setProperty("mysql.host", "127.0.0.1");
        this.setProperty("mysql.port", "3306");
        this.setProperty("mysql.database", "");
        this.setProperty("mysql.user", "");
        this.setProperty("mysql.password", "");
    }
}
