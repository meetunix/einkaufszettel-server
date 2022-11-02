package de.nachtsieb.einkaufszettelServer.dbService;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBConnPool {

  private static final HikariConfig config = new HikariConfig();
  private static final HikariDataSource ds;

  static {
    config.setDriverClassName("org.h2.Driver");
    config.setJdbcUrl(System.getProperty("jdbcURL"));
    config.setUsername(System.getProperty("databaseUsername"));
    config.setPassword(System.getProperty("databasePassword"));
    config.addDataSourceProperty("cachePrepStmts", "true");
    config.addDataSourceProperty("prepStmtCacheSize", "128");
    config.addDataSourceProperty("prepStmtCacheSqlLimit", "512");
    ds = new HikariDataSource(config);
  }

  private DBConnPool() {}

  public static Connection getConnection() throws SQLException {
    return ds.getConnection();
  }
}
