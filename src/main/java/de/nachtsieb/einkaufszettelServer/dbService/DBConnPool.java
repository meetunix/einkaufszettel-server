package de.nachtsieb.einkaufszettelServer.dbService;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnPool {

	
    private static HikariConfig config = new HikariConfig(
    		"/etc/ez-server/db.properties");
    private static HikariDataSource ds;

    static {
        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

}
