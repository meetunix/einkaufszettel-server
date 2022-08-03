package de.nachtsieb.einkaufszettelServer.dbService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.nachtsieb.einkaufszettelServer.entities.Einkaufszettel;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBReader {

  private static final Logger logger = LogManager.getLogger(DBReader.class);
  public static final String TABLE_EINKAUFSZETTEL = "einkaufszettel";
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final MapHandler mapResultHandler = new MapHandler();
  private static final QueryRunner runner = new QueryRunner();

  public static boolean ezExists(UUID eid) {
    try (Connection conn = DBConnPool.getConnection()) {

      Map<String, Object> result = runner.query(conn,"SELECT eid FROM einkaufszettel WHERE eid = ?", mapResultHandler, eid);
      return result.get("eid") != null;

    } catch (SQLException e) {
      //TODO
      throw new RuntimeException(e);
    }
  }

  public static Einkaufszettel getEZ(UUID eid) {
    try {

      return mapper.readValue(getEZAsString(eid), Einkaufszettel.class)

    } catch (JsonProcessingException e) {
      //TODO
      throw new RuntimeException(e);
    }
  }


  public static String getEZAsString(UUID eid) {
    try (Connection conn = DBConnPool.getConnection()) {

      Map<String, Object> result = runner.query(conn,"SELECT eid, data FROM einkaufszettel WHERE eid = ?", mapResultHandler, eid);
      return result.get("data") != null ? (String) result.get("data") : null;

    } catch (SQLException e) {
      //TODO
      throw new RuntimeException(e);
    }
  }

  public static boolean tableExists(String table) {
    try (Connection conn = DBConnPool.getConnection() {

      DatabaseMetaData dbmeta = conn.getMetaData();
      ResultSet metaRes = dbmeta.getTables(null, null, table, new String[] {"TABLE"});

      while (metaRes.next()) {
        if (metaRes.getString("TABLE_NAME").equalsIgnoreCase(table)) return true;
      }
      metaRes.close();
    } catch (Exception e) {
      logger.error("Unable to fetch meta data from database");
    }
    return false;
  }
}
