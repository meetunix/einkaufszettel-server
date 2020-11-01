package de.nachtsieb.einkaufszettelServer;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.nachtsieb.einkaufszettelServer.dbService.DBConnPool;
import de.nachtsieb.einkaufszettelServer.dbService.DBWriter;

public class EZTestDB {

    private static Logger logger = LogManager.getLogger(EZTestDB.class);

	private final static String[] tables = {"items", "category", "einkaufszettel", "ez_cleanup"};
	
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
	
	public static void resetDatabase(Connection conn) {
		
		try {

			ResourceLoader resLoader = new ResourceLoader();
			File file = resLoader.getFileFromResources("pgDBSchema.sql");
			String schema = resLoader.getFileasString(file);

			DBWriter.deleteTables(tables);
			DBWriter.ceateTables(schema.replace("\n", " "));

		} catch (IOException e) {
			logger.error("TEST: unable to reset test database : {}", e.toString());
		}
	}
}
