package de.nachtsieb.einkaufszettelServer;

import de.nachtsieb.einkaufszettelServer.dbService.DBConnPool;
import de.nachtsieb.einkaufszettelServer.dbService.DBWriter;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EZTestDB {

  private static final Logger logger = LogManager.getLogger(EZTestDB.class);

  private static final String[] tables = {"einkaufszettel"};

  public static Connection getConnection() {

    try {

      Connection conn = DBConnPool.getConnection();

      if (conn != null) {
        logger.info("TEST: database connection established");
        return conn;
      }

    } catch (SQLException w) {
      logger.error("TEST: database connection could not be established: {}", w.toString());
    }
    return null;
  }

  public static void resetDatabase() {

    RessourceLoader resl = new RessourceLoader();
    InputStream is = resl.getFileFromResourceAsStream("sql-json-schema.sql");
    String schema = resl.getStringFromInputStream(is);

    DBWriter.deleteTables(tables);
    DBWriter.createTables(schema.replace("\n", " "));
  }
}
