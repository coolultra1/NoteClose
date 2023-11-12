package de.emilius123.noteclose.db;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.eclipse.jetty.server.session.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * Kinda deals with most DB things
 * Not really that useful, quickly passed on to Javalin
 */
public class DBConnection {
    private final MysqlDataSource dataSource;
    private Connection connection;
    private static final File SESSION_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "noteclose-sessions").toFile();
    private final Logger logger = LoggerFactory.getLogger(DBConnection.class);

    public DBConnection(Properties properties) {
        String host = properties.getProperty("mysql.host");
        int port = Integer.parseInt(properties.getProperty("mysql.port"));
        String database = properties.getProperty("mysql.database");
        String user = properties.getProperty("mysql.user");
        String password = properties.getProperty("mysql.password");
        dataSource = new MysqlDataSource();
        connection = null;

        // Setup dataSource and connect
        try {
            dataSource.setServerName(host);
            dataSource.setPort(port);
            dataSource.setDatabaseName(database);
            dataSource.setUser(user);
            dataSource.setPassword(password);
            dataSource.setAutoReconnect(true);

            connection = dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("Could not connect to MySQL!", e);
        }
    }

    /**
     * @return A SessionHandler that stores session data in the configured DB
     * If not connected to DB, returns a FileSessionDataStore
     */
    public Supplier<SessionHandler> databaseSessionHandler() {
        SessionHandler sessionHandler = new SessionHandler();
        SessionCache sessionCache = new DefaultSessionCache(sessionHandler);

        JDBCSessionDataStoreFactory dataStoreFactory = new JDBCSessionDataStoreFactory();
        DatabaseAdaptor adaptor = new DatabaseAdaptor();
        adaptor.setDatasource(dataSource);
        dataStoreFactory.setDatabaseAdaptor(adaptor);

        if(connection != null) {
            // If connected to DB, just use JDBCSessionDataStore
            sessionCache.setSessionDataStore(
                    dataStoreFactory.getSessionDataStore(sessionHandler)
            );
        } else {
            return getFileSessionDataStore();
        }

        sessionHandler.setSessionCache(sessionCache);
        return () -> sessionHandler;
    }

    private static Supplier<SessionHandler> getFileSessionDataStore() {
        SessionHandler sessionHandler = new SessionHandler();
        SessionCache sessionCache = new DefaultSessionCache(sessionHandler);

        FileSessionDataStore fileSessionDataStore = new FileSessionDataStore();
        SESSION_DIR.mkdir();
        fileSessionDataStore.setStoreDir(SESSION_DIR);

        sessionCache.setSessionDataStore(fileSessionDataStore);
        sessionHandler.setSessionCache(sessionCache);

        return () -> sessionHandler;
    }


    public Connection connect() {
        return connection;
    }
}
