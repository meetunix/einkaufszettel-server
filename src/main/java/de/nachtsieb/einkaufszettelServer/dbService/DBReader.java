package de.nachtsieb.einkaufszettelServer.dbService;

import de.nachtsieb.einkaufszettelServer.entities.Category;
import de.nachtsieb.einkaufszettelServer.entities.Einkaufszettel;
import de.nachtsieb.einkaufszettelServer.entities.ErrorMessage;
import de.nachtsieb.einkaufszettelServer.entities.Item;
import de.nachtsieb.einkaufszettelServer.exceptions.EZDBException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Implementation of DataReader using JDBC with a relational database. */
public final class DBReader {

  private static final Logger logger = LogManager.getLogger(DBReader.class);

  public static final String TABLE_EINKAUFSZETTEL = "einkaufszettel";

  public static boolean tableExists(String table) {

    boolean tableExists = false;

    try (Connection conn = DBConnPool.getConnection()) {

      // check if table already exists
      DatabaseMetaData dbmeta = conn.getMetaData();
      ResultSet metaRes = dbmeta.getTables(null, null, table, new String[] {"TABLE"});

      while (metaRes.next()) {
        if (metaRes.getString("TABLE_NAME").toLowerCase().equals(table)) {
          tableExists = true;
        }
      }
      metaRes.close();

    } catch (Exception e) {
      logger.error("Unable to fetch meta data from database");
    }
    return tableExists;
  }

  /**
   * Returns all iids that belongs to an Einkaufszettel.
   *
   * @return list of IIDs
   */
  public static List<UUID> getIIDs(Einkaufszettel ez, Connection conn) {

    List<UUID> iids = new ArrayList<>();

    try {

      PreparedStatement ps = conn.prepareStatement("SELECT iid FROM items WHERE eid = ?;");

      ps.setObject(1, ez.getEid());

      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        iids.add((UUID) rs.getObject("iid"));
      }

    } catch (SQLException e) {
      logger.error(e.toString());
      throw new EZDBException(
          ErrorMessage.getJsonString(
              new ErrorMessage("E_READ_DB", "unable to get iid list from database")));
    }

    return iids;
  }

  /**
   * Return true if the category (cid) exists, otherwise false.
   *
   * @param cid - a cid uuid which will be checked for existence
   * @param conn - the database connection
   * @return - true if the category exists
   */
  public static boolean categoryExists(UUID cid, Connection conn) {

    try {
      PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM category WHERE cid = ?;");

      ps.setObject(1, cid);

      ResultSet rs = ps.executeQuery();

      return rs.next();

    } catch (SQLException e) {
      logger.error(e.toString());
      throw new EZDBException(
          ErrorMessage.getJsonString(
              new ErrorMessage("E_READ_DB", "unable to ask for existence of cid.")));
    }
  }

  /**
   * Creates a Einkaufszettel object from the given uuid. If there is no Einkaufszettel with eid in
   * the Database null will be returned.
   *
   * @param eid - UUID
   * @return Einkaufszettel or null
   */
  public static Einkaufszettel getEZ(UUID eid) {

    try (Connection conn = DBConnPool.getConnection()) {

      Einkaufszettel ez;

      PreparedStatement ps = conn.prepareStatement("SELECT * FROM  einkaufszettel WHERE eid = ?;");

      ps.setObject(1, eid);

      ResultSet rs = ps.executeQuery();

      if (rs.next()) {

        ez =
            new Einkaufszettel(
                eid,
                rs.getTimestamp("created").getTime(),
                rs.getTimestamp("modified").getTime(),
                rs.getString("name"),
                rs.getInt("version"));

        rs.close();
        ps.close();

        // storing Items
        ez.setItems(getItems(ez, conn));

      } else {
        // no Einkaufszettel with eid in database
        return null;
      }

      return ez;

    } catch (SQLException e) {
      logger.error(e.toString());
      throw new EZDBException(
          ErrorMessage.getJsonString(
              new ErrorMessage("E_READ_DB", "unable to read EZ from database")));
    }
  }

  /**
   * Returns a list of Items that belongs to a given Einkaufszettel ez. Each item includes
   * information from the corresponding category. The list may be empty.
   *
   * @param ez Einkaufszettel
   * @return List of Items
   */
  private static List<Item> getItems(Einkaufszettel ez, Connection conn) {

    try {

      List<Item> items = new ArrayList<>();

      PreparedStatement ps =
          conn.prepareStatement("SELECT * FROM items NATURAL JOIN category WHERE eid = ?");

      ps.setObject(1, ez.getEid());

      ResultSet rs = ps.executeQuery();

      // fill item list with new items from database
      while (rs.next()) {

        Item item =
            new Item(
                (UUID) rs.getObject("iid"),
                rs.getString("item_name"),
                rs.getInt("ordinal"),
                rs.getInt("amount"),
                rs.getFloat("size"),
                rs.getString("unit"),
                new Category(
                    (UUID) rs.getObject("cid"),
                    rs.getString("color"),
                    rs.getString("description")));

        items.add(item);
      }

      rs.close();
      ps.close();

      return items;

    } catch (SQLException e) {
      logger.error(e.toString());
      throw new EZDBException(
          ErrorMessage.getJsonString(
              new ErrorMessage("E_READ_DB", "unable to read items from database")));
    }
  }

  /**
   * Creates a Category object from the given cid. If no category with cid in database null will be
   * returned.
   *
   * @param cid - Long
   * @return Category or null
   */
  public static Category getCategory(UUID cid) {

    try (Connection conn = DBConnPool.getConnection()) {
      Category cat;

      PreparedStatement ps = conn.prepareStatement("SELECT * FROM category WHERE cid = ?");
      ps.setObject(1, cid);

      ResultSet rs = ps.executeQuery();

      if (rs.next()) {

        cat = new Category(cid, rs.getString("color"), rs.getString("description"));
      } else {
        // no Category with this cid in database.
        return null;
      }
      return cat;

    } catch (SQLException e) {
      logger.error(e.toString());
      throw new EZDBException(
          ErrorMessage.getJsonString(
              new ErrorMessage("E_READ_DB", "unable to read category from database")));
    }
  }
}
