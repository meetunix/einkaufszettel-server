package de.nachtsieb.einkaufszettelServer.dbService;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DBConnPool {

	private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;
 
    static {
        config.setJdbcUrl(System.getProperty("jdbcURL") );
        config.setUsername(System.getProperty("databaseUsername"));
        config.setPassword(System.getProperty("databasePassword"));
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        ds = new HikariDataSource( config );
    }
 
    private DBConnPool() {}
 
    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
	
	

	/*
    private static HikariConfig config = new HikariConfig(
    		"/etc/ez-server/db.properties");
    private static HikariDataSource ds;

    static {
        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
    */

}
