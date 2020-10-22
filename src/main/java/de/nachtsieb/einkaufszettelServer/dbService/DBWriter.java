package de.nachtsieb.einkaufszettelServer.dbService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;

import de.nachtsieb.einkaufszettelServer.entities.Category;
import de.nachtsieb.einkaufszettelServer.entities.Einkaufszettel;
import de.nachtsieb.einkaufszettelServer.entities.ErrorMessage;
import de.nachtsieb.einkaufszettelServer.entities.Item;
import de.nachtsieb.einkaufszettelServer.exceptions.EZDBException;

import org.apache.logging.log4j.LogManager;

/**
 * This class implements methods for writing or updating an einkaufszettel (EZ) into an
 * relational database system using jdbc.
 * 
 */

public final class DBWriter{

    private static Logger logger = LogManager.getLogger(DBWriter.class);

	/**
	 * Insert an existing Einkaufszettel object to the database and returns true if the database
	 * transaction was successfuly otherwise false. The included item list will be written to
	 * database, too. If existing categories (in database) are altered by the new Einkaufszettel,
	 * they will be updatet. Also new categories will be created.
	 * 
	 * @param ez - Einkaufszettel
	 * @return boolean
	 * @throws EZException 
	 * @throws SQLException 
	 */
	public static void writeEZ(Einkaufszettel ez) {
		
		try (Connection conn = DBConnPool.getConnection()) {

			// starting transaction for writing whole EZ, Items and maybe Category 
			logger.debug("START OF TRANSACTION for the CREATION of EZ: {}", ez.getEid());
			conn.setAutoCommit(false);
		
			PreparedStatement ps = conn.prepareStatement(
					"INSERT INTO einkaufszettel VALUES (?, ?, ?, ?, ?)");
			
			ps.setObject(1, ez.getEid());
			ps.setTimestamp(2, new Timestamp(ez.getCreated()));
			ps.setTimestamp(3, new Timestamp(ez.getModified()));
			ps.setString(4, ez.getName());
			ps.setInt(5, ez.getVersion());
			
			int insertVal = ps.executeUpdate();
			ps.close();
			
			if (insertVal < 1 ) { // no tupel was written to database
			
				logger.debug("ROLLBACK OF TRANSACTION for EZ: {}", ez.getEid());
				conn.rollback();
				logger.debug("unable to write EZ {} to database", ez.getEid());
				throw new EZDBException(ErrorMessage.getJsonString(
						new ErrorMessage("E_DB_WRITE", "unable to write EZ to database")));
			}	
			
			/*
			 * creating or updating categories
			 */
			if (! writeOrUpdateCategory(buildCategoryMap(ez.getItems()), conn)) {
				logger.debug("ROLLBACK OF TRANSACTION for EZ: {}", ez.getEid());
				conn.rollback();
				logger.debug("unable to write EZ {} to database (creat/update categories)",
						ez.getEid());
				throw new EZDBException(ErrorMessage.getJsonString(
						new ErrorMessage("E_DB_WRITE", "unable to write EZ to database")));
			}
		
			/*
			 *  writing all items to database inside transaction
			 */
			if (writeItemList(ez.getItems(), ez, conn)) {
				
				// finalyzing transaction
				conn.commit();
				conn.setAutoCommit(true);
				logger.debug("END OF TRANSACTION for the CREATION of EZ: {}", ez.getEid());
			
			} else {
				
				logger.debug("ROLLBACK OF TRANSACTION for EZ: {}", ez.getEid());
				conn.rollback();
				logger.debug("unable to write EZ {} to database", ez.getEid());
				throw new EZDBException(ErrorMessage.getJsonString(
						new ErrorMessage("E_DB_WRITE", "unable to write EZ to database")));
			}
		
		} catch (SQLException e) {
			logger.error(e.toString());
			throw new EZDBException(ErrorMessage.getJsonString(
					new ErrorMessage("E_DB", "unable to create EZ in database")));
		}

	}
	
	
	/**
	 * Inserts a list of items to the database an returns true if the actions inside an existing
	 * transaction were successfull, otherwise false.
	 * 
	 * Only used when creating a new Einkaufszettel. 
	 * 
	 * @param items - A list of items. 
	 * @param ez - The Einkaufszettel the item list belongs to.
	 * @param conn - The database connection object with an existing transaction.
	 * @return true if transaction was successful, otherwise false.
	 * @throws SQLException
	 */
	private static boolean writeItemList(List<Item> items, Einkaufszettel ez, Connection conn) {
		
		logger.debug("trying to CREATE {} items to database", items.size());

		try {

			PreparedStatement ps = conn.prepareStatement(
					"INSERT INTO items VALUES (?, ?, ?, ?, ?, ?, ?, ? )");
			
			int amntStatements = items.size();
			int n;
			for (Item item: items) {
				
				n = 1;
				ps.setObject(n, item.getIid());
				ps.setObject(++n, ez.getEid());
				ps.setObject(++n, item.getCid());
				ps.setString(++n, item.getItemName());
				ps.setInt(++n,item.getOrdinal());
				ps.setInt(++n,item.getAmount());
				ps.setFloat(++n, item.getSize());
				ps.setString(++n, item.getUnit());
				ps.addBatch();
			}

			
			int[] amntUpdates = ps.executeBatch();
			ps.close();
			
			if (Arrays.stream(amntUpdates).sum() != amntStatements ) {
				logger.error("Not all listed items where written to database");
				return false;
			}

			return true;

		} catch (SQLException e) {
			logger.error(e.toString());
			throw new EZDBException(ErrorMessage.getJsonString(
					new ErrorMessage("E_DB_WRITE", "unable to write item list to database")));
		}
	}
	
	/**
	 * Inserts a not existing catagory to database or performs an update on an 
	 * existing category.
	 * 
	 * May be replaced with a stored procedure in the future (TODO)
	 * 
	 * @param list of categories
	 * @param conn a database connection with open transaction
	 * @return true if the catagory was successful created or updated
	 */
	private static boolean writeOrUpdateCategory(Map<UUID,Category> catMap, Connection conn) {
		
		// get all cids from database to check wich category has to be created or updated
		List<UUID> cidList = DBReader.getCIDs(conn);
	
		// if a cid is already in database add it for updating. The rest of the map must be created
		List<Category> updateList = new ArrayList<>();

		for (UUID cid : cidList) {
			Category cat = catMap.get(cid);
			if (cat != null) {
				updateList.add(cat);
				catMap.remove(cid);
			}
		}
		
		if(catMap.size() > 0) {
			if (! writeCategory(catMap, conn)) {return false;};
		}

		if(updateList.size() > 0) {
			if (! updateCategory(updateList, conn)) {return false;};
		}
		
		return true;
	}
		
	private static boolean writeCategory(Map<UUID,Category> catMap, Connection conn) {

		try {
		
			logger.debug("trying to INSERT {} categories to database", catMap.size());

			PreparedStatement psInsert = conn.prepareStatement(
					"INSERT INTO category VALUES (?, ?, ?)");

			// insert new categories
			int amntInsertStatements = catMap.size();

			for (UUID catID : catMap.keySet()) { 
				Category cat = catMap.get(catID);
				
				psInsert.setObject(1, cat.getCid());
				psInsert.setString(2, cat.getColor());
				psInsert.setString(3, cat.getDescription());
				psInsert.addBatch();
				
			}

			int[] amntInserted = psInsert.executeBatch();
			psInsert.close();
			
			if (Arrays.stream(amntInserted).sum() != amntInsertStatements ) {
				logger.error("catagories could not be INSERTED to database");
				return false;
			}
			
			return true;
			
		} catch (SQLException e) {
			logger.error(e.toString());
			throw new EZDBException(ErrorMessage.getJsonString(
					new ErrorMessage("E_DB_WRITE", "unable to insert categories to database")));
		}
	}

	private static boolean updateCategory(List<Category> updateList, Connection conn) {

		try {

			// update categories 
			logger.debug("trying to UPDATE {} categories to database", updateList.size());
			
			PreparedStatement psUpdate = conn.prepareStatement(
					"UPDATE category SET color = ?, description = ? WHERE cid = ?");

			int amntUpdateStatements = updateList.size();
			for (Category cat : updateList) { 
			
				psUpdate.setString(1, cat.getColor());
				psUpdate.setString(2, cat.getDescription());
				psUpdate.setObject(3, cat.getCid());
				psUpdate.addBatch();
					
			}
			
			int[] amntupdated = psUpdate.executeBatch();
			psUpdate.close();
			
			if (Arrays.stream(amntupdated).sum() != amntUpdateStatements ) {
				logger.error("catagories could not be UPDATET to database");
				return false;
			}
			
			return true;			
			
		} catch (SQLException e) {
			logger.error(e.toString());
			throw new EZDBException(ErrorMessage.getJsonString(
					new ErrorMessage("E_DB_WRITE", "unable to update categories in database")));
		}
	}
	
	/**
	 * Updates an existing Einkaufszettel ez in database.
	 * 
	 * @param ez the einkaufszettel that needs to be updated
	 * @return true if transaction was successful, otherwise false.
	 * @throws SQLException
	 */

	public static void updateEZ(Einkaufszettel ez) {

		try (Connection conn = DBConnPool.getConnection()) {

			/*
			 * starting transaction for updating whole EZ, Items and maybe Categories
			 */
			logger.debug("START OF TRANSACTION for UPDATING the EZ: {}", ez.getEid());
			conn.setAutoCommit(false);
		
			PreparedStatement ps = conn.prepareStatement(
					"UPDATE einkaufszettel SET modified = ?, name = ?, version = ? WHERE eid = ?");
			
			ps.setTimestamp(1, new Timestamp(ez.getModified()));
			ps.setString(2, ez.getName());
			ps.setInt(3, ez.getVersion());
			ps.setObject(4, ez.getEid());
			
			int insertVal = ps.executeUpdate();
			ps.close();
			
			// no update was performed
			if (insertVal < 1 ) { 
			
				logger.debug("ROLLBACK OF TRANSACTION for UPDATE of EZ: {}", ez.getEid());
				conn.rollback();
				logger.debug("unable to update EZ {} to database", ez.getEid());
				throw new EZDBException(ErrorMessage.getJsonString(
						new ErrorMessage("E_DB_UPDATE", "unable to update EZ in database")));
			}	

			/*
			 * creating or updating categories
			 */
			if (! writeOrUpdateCategory(buildCategoryMap(ez.getItems()), conn)) {
				logger.debug("ROLLBACK OF TRANSACTION for UPDATING EZ: {}", ez.getEid());
				conn.rollback();
				logger.debug("unable to update EZ {} to database (create/update categories)",
						ez.getEid());
				throw new EZDBException(ErrorMessage.getJsonString(
						new ErrorMessage("E_DB_WRITE", "unable to update EZ to database")));
			}

			/* 
			 * updating all items to database inside transaction
			 */
			if (writeOrUpdateItemList(ez, conn)) {
				
				// finalyzing transaction
				conn.commit();
				conn.setAutoCommit(true);
				logger.debug("END OF TRANSACTION for UPDATING the EZ: {}", ez.getEid());
			
			} else {
				
				logger.debug("ROLLBACK OF TRANSACTION for UPDATING EZ: {}", ez.getEid());
				conn.rollback();
				logger.debug("unable to update EZ {} to database", ez.getEid());
				throw new EZDBException(ErrorMessage.getJsonString(
						new ErrorMessage("E_DB_UPDATE", "unable to update EZ to database")));
			}

		} catch (SQLException e) {
			logger.error(e.toString());
			throw new EZDBException(ErrorMessage.getJsonString(
					new ErrorMessage("E_DB", "unable to update EZ in database")));
		}
	}

	/**
	 * Udates the items that belongs to a Einkaufszettel ez. Only altered items will be updated
	 * in the database. Items that exists in the database but not in the item list will be erased. 
	 * 
	 * @param ez 	Einkaufszettel
	 * @param conn a database connection with an open transaction
	 * @return true if transaction was successful, otherwise false.
	 * @throws SQLException
	 */
	private static boolean writeOrUpdateItemList(Einkaufszettel ez, Connection conn) {

		
		// get all iids from database to check wich category has to be created or updated
		List<UUID> iidsFromDB = DBReader.getIIDs(ez,conn);
		
		// needed for a later lookup
		Map<UUID,Item> itemMap = ez.getItems().stream()
				.collect(Collectors.toMap(Item::getIid, item -> item));
		
		/*
		 * Create Lists for CUD operations on items
		 */
		List<UUID> itemsToDelete = new ArrayList<>(ez.getItems().size()/2);
		List<UUID> itemsToUpdate = new ArrayList<>(ez.getItems().size()/2);
		
		List<UUID> itemsToInsert = ez.getItems().stream()
				.map( item -> item.getIid())
				.collect(Collectors.toList());
		
		for(UUID iidDB : iidsFromDB) {
			if (itemsToInsert.contains(iidDB)) {
				itemsToUpdate.add(iidDB);
			} else {
				itemsToDelete.add(iidDB);
			}
			itemsToInsert.remove(iidDB);
		}

		/*
		 * create new items
		 * 
		 * convert uuids to items, because writeItemList needs a list as parameter
		 */
		List<Item> newItems = new ArrayList<>(itemMap.size());
		newItems = itemsToInsert.stream()
				.map(uuid -> itemMap.get(uuid))
				.collect(Collectors.toList());
		
		// write new items to database
		if (newItems.size() > 0) {
			if (! writeItemList(newItems, ez, conn)) {
				return false;
			}
		}
		
		/*
		 * UPDATE items
		 */
		if (itemsToUpdate.size() > 0) {
			if (! updateItemList(itemsToUpdate, itemMap, conn)) {
				return false;
			}
		}
		
		/*
		 * DELETE old items from database
		 */
		if (itemsToDelete.size() > 0) {			
			if (! deleteItemList(itemsToDelete, conn)) {
				return false;
			}
		}
		
		return true;
	}
			
	private static boolean updateItemList(
			List<UUID> itemsToUpdate, Map<UUID, Item> itemMap,  Connection conn) {
	
		try {
			
			logger.debug("trying to UPDATE {} items to database", itemsToUpdate.size());
			
			PreparedStatement psUpdate = conn.prepareStatement(
					"UPDATE items SET cid = ?, item_name = ?, ordinal = ?, amount = ?, "
					+ "size = ?, unit = ? WHERE iid = ?");

			int amntUpdateStatements = itemsToUpdate.size();
			for (UUID iid: itemsToUpdate) { 
				
				Item item = itemMap.get(iid);
								
				psUpdate.setObject(1, item.getCid());
				psUpdate.setString(2, item.getItemName());
				psUpdate.setInt(3, item.getOrdinal());
				psUpdate.setInt(4, item.getAmount());
				psUpdate.setFloat(5, item.getSize());
				psUpdate.setString(6, item.getUnit());
				psUpdate.setObject(7, item.getIid());
				psUpdate.addBatch();
			}
			
			int[] amntupdated = psUpdate.executeBatch();
			psUpdate.close();
			
			if (Arrays.stream(amntupdated).sum() != amntUpdateStatements ) {
				logger.error("items could not be UPATED to database");
				return false;
			}
			
			return true;			

		} catch (SQLException e) {
			logger.error(e.toString());
			throw new EZDBException(ErrorMessage.getJsonString(
					new ErrorMessage("E_DB", "unable to UPDATE item list in database")));
		}
	}
	
	/**
	 * 
	 * Delete all iids (UUIDs identifying items) from a given list from the database.
	 * 
	 * @param items
	 * @param ez
	 * @param conn
	 * @return
	 */
	private static boolean deleteItemList(List<UUID> items, Connection conn) {
	
		try {
			
			logger.debug("trying to DELETE {} items from database", items.size());
			
			PreparedStatement psDelete = conn.prepareStatement(
					"DELETE FROM items WHERE iid = ?;");

			int amntDeleteStatements = items.size();
			for (UUID iid: items) { 
				
				psDelete.setObject(1, iid);
				psDelete.addBatch();
			}
			
			int[] amntupdated = psDelete.executeBatch();
			psDelete.close();
			
			if (Arrays.stream(amntupdated).sum() != amntDeleteStatements ) {
				logger.error("items could not be DELETED from database");
				return false;
			}
			
			return true;

		} catch (SQLException e) {
			logger.error(e.toString());
			throw new EZDBException(ErrorMessage.getJsonString(
					new ErrorMessage("E_DB", "unable to DELETE item list from database")));
		}
	}
	
	
	public static void deleteEZ(Einkaufszettel ez) {
		
		try (Connection conn = DBConnPool.getConnection()) {
		
			/*
			 * starting transaction for deleting whole EZ and items
			 */
			logger.debug("START OF TRANSACTION for DELETING the EZ: {}", ez.getEid());
			conn.setAutoCommit(false);
			
			/*
			 * at first all items must be deleted
			 */
			List<UUID> iidsToDelete = ez.getItems().stream()
					.map(Item::getIid)
					.collect(Collectors.toList());
			
			if (! deleteItemList(iidsToDelete, conn)) {
				logger.error("items could not be DELETED from database");
				conn.rollback();
				logger.debug("ROLLBACK OF TRANSACTION for EZ: {}", ez.getEid());
				throw new EZDBException(ErrorMessage.getJsonString(
						new ErrorMessage("E_DB_DELETE", "unable to delete items from database")));
			}
			
			/*
			 * deleting the einkaufszettel
			 */
			
			PreparedStatement ps = conn.prepareStatement(
					"DELETE FROM einkaufszettel WHERE eid = ?");
			
			ps.setObject(1, ez.getEid());
			
			int insertVal = ps.executeUpdate();
			ps.close();
			

			if (insertVal < 1 ) {
			
				logger.debug("ROLLBACK OF TRANSACTION for EZ: {}", ez.getEid());
				conn.rollback();
				logger.error("unable to write EZ {} to database", ez.getEid());
				throw new EZDBException(ErrorMessage.getJsonString(
						new ErrorMessage("E_DB_DELETE", "unable to delete EZ from database")));
			}	
			
			conn.commit();
			conn.setAutoCommit(true);
			logger.debug("END OF TRANSACTION for DELETING of EZ: {}", ez.getEid());
		
		} catch (SQLException e) {
			logger.error(e.toString());
			throw new EZDBException(ErrorMessage.getJsonString(
					new ErrorMessage("E_DB_DELETE", "unable to delete EZ from database")));
		}
		
	}
	
	/**
	 * Creates a table for every statement inside String array.
	 * 
	 * @param statements String[]
	 * @return true if all tables where created sucessfully otherwise false.
	 * @throws SQLException
	 */
	public static boolean ceateTables(String schema) {
		
		try (Connection conn = DBConnPool.getConnection()) {

				Statement stmt = conn.createStatement();
				stmt.executeUpdate(schema);
				stmt.close();

			return true;

		} catch (SQLException e) {
			logger.error(e.toString());
			throw new EZDBException(ErrorMessage.getJsonString(
					new ErrorMessage("E_DB", "unable to create EZ in database")));
		}
	}
	
	/**
	 * Deletes the given table names from database.
	 * 
	 * @param tables
	 * @return true if deletion was successful
	 * @throws SQLException
	 */
	public static boolean deleteTables(String[] tables) {
		
		try (Connection conn = DBConnPool.getConnection()) {

			for(String table: tables) {
				String statement = "DROP TABLE IF EXISTS " + table;
				logger.trace(statement);
				Statement s = conn.createStatement();
				s.executeUpdate(statement);
				s.close();
			}
			
			return true;

		} catch (SQLException e) {
			logger.error(e.toString());
			throw new EZDBException(ErrorMessage.getJsonString(
					new ErrorMessage("E_DB", "unable to create EZ in database")));
		}
	}

	/**
	 * Creates a map containing all Categories from a list of Items. 
	 * 
	 * Used inernaly for faster identifying which category has to be updated or created.
	 * 
	 * @param list of items that are convertet to a map
	 * @return Map<UUID,Category>
	 */
	private static Map<UUID,Category> buildCategoryMap(List<Item> list) {
		
		Map<UUID, Category> map = new HashMap<>();
		
		for(Item item : list) {
			map.put(item.getCid(), new Category(
					item.getCid(),
					item.getCatColor(),
					item.getCatDescription()));
		}
		
		return map;
	}
}
