package de.emilius123.noteclose.db;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.eclipse.jetty.server.session.*;

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
            e.printStackTrace();
        }
    }

    /**
     * @return A SessionHandler that stores session data in the configured DB
     */
    public Supplier<SessionHandler> databaseSessionHandler() {
        SessionHandler sessionHandler = new SessionHandler();
        SessionCache sessionCache = new DefaultSessionCache(sessionHandler);

        JDBCSessionDataStoreFactory dataStoreFactory = new JDBCSessionDataStoreFactory();
        DatabaseAdaptor adaptor = new DatabaseAdaptor();
        adaptor.setDatasource(dataSource);
        dataStoreFactory.setDatabaseAdaptor(adaptor);
        sessionCache.setSessionDataStore(
                dataStoreFactory.getSessionDataStore(sessionHandler)
        );
        sessionHandler.setSessionCache(sessionCache);

        return () -> sessionHandler;
    }

    public Connection connect() {
        return connection;
    }
}
