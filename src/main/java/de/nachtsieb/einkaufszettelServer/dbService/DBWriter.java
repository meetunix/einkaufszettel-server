package de.nachtsieb.einkaufszettelServer.dbService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.nachtsieb.einkaufszettelServer.entities.Einkaufszettel;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBWriter {

  private static final Logger logger = LogManager.getLogger(DBWriter.class);

  private static final ObjectMapper mapper = new ObjectMapper();
  private static final QueryRunner runner = new QueryRunner();

  private static String getJsonValue(Einkaufszettel ez) {
    try {
      return mapper.writeValueAsString(ez);
    } catch (JsonProcessingException e) {
      logger.error("Unable to serialize for db storage ({})", ez.getEid());
    }
    return null;
  }

  public static void writeEZ(Einkaufszettel ez) {
    try (Connection conn = DBConnPool.getConnection()) {
      String json = getJsonValue(ez);
      runner.update(conn, "INSERT INTO einkaufszettel VALUES (?,?,?,?,?)", ez.getEid(),
          new Timestamp(ez.getCreated()), new Timestamp(ez.getModified()), ez.getVersion(), json);
    } catch (SQLException e) {
      logger.error("Unable to write new Einkaufszettel {} to database ", ez.getEid());
      throw new RuntimeException(e);
    }
  }

  public static void updateEZ(Einkaufszettel ez) {
    try (Connection conn = DBConnPool.getConnection()) {
      String json = getJsonValue(ez);
      runner.update(conn,
          "UPDATE einkaufszettel SET modified = ?, version = ?, data = ? WHERE eid = ?",
          new Timestamp(ez.getModified()), ez.getVersion(), json);
    } catch (SQLException e) {
      logger.error("Unable to update new Einkaufszettel {} on database ", ez.getEid());
      throw new RuntimeException(e);
    }
  }

  public static void deleteEZ(UUID eid) {
    try (Connection conn = DBConnPool.getConnection()) {
      runner.update(conn, "DELETE FROM einkaufszettel WHERE eid = ?", eid);
    } catch (SQLException e) {
      logger.error("Unable to delete Einkaufszettel {} on database ", eid);
      throw new RuntimeException(e);
    }
  }

  public static void createTables(String schema) {
    try (Connection conn = DBConnPool.getConnection()) {
      runner.update(conn, schema);
    } catch (SQLException e) {
      logger.error("Unable to create tables for statement: " + schema + "\n");
      throw new RuntimeException(e);
    }
  }

  public static void deleteTables(String[] tables) {
    try (Connection conn = DBConnPool.getConnection()) {
      for (String table : tables) {
        runner.update(conn, "DROP TABLE IF EXISTS " + table);
      }
    } catch (SQLException e) {
      logger.error("Unable to delete tables");
      throw new RuntimeException(e);
    }
  }
}
