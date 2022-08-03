package de.nachtsieb.einkaufszettelServer.dbService;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nachtsieb.einkaufszettelServer.entities.Einkaufszettel;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.commons.dbutils.QueryRunner;

public class DBWriter {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final QueryRunner runner = new QueryRunner();


  public static void writeEZ(Einkaufszettel newEZ) {
  }

  public static void updateEZ(Einkaufszettel newEZ) {
  }

  public static void deleteEZ(UUID eid) {
    try (Connection conn = DBConnPool.getConnection()) {

      int result = runner.update(conn, "DELETE FROM einkaufszettel WHERE eid = ?", eid);
      if (result < 1) throw new SQLException("unable to delete EZ " + eid);

    } catch (SQLException e) {
      //TODO
      throw new RuntimeException(e);
    }
  }

  public static void createTables(String replace) {
  }
}
