package de.nachtsieb.einkaufszettelServer.dbService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This thread will be started, if the main application starts. Once a day it
 * will clean up the database for unused categories. And logs how long the
 * clean up took.
 * 
 * In Version 0.0.1 a database trigger was used. This trigger runs on every deletion of an
 * Einkaufszettel. On a big database (> 5000 Einkaufszettel) a deletion takes to long. 
 * Moreover it is not necessary to clean up categories that often. 
 * 
 */

public class DatabaseCleanerThread implements Runnable {
	
    private static Logger logger = LogManager.getLogger(DatabaseCleanerThread.class);
    
    private Duration oneDay = Duration.ofHours(24); 
    
	/**
	 * Deletes orphaned (not used by any item) categories from database.
	 * 
	 * @return Number of deleted categories
	 */
	private void deleteOrphanedCategories() {
		
		long start, end;
		
		try (Connection conn = DBConnPool.getConnection()) {
			
			PreparedStatement ps = conn.prepareStatement(
						"DELETE FROM category "
					+ 	"WHERE cid IN (SELECT cid FROM category EXCEPT (SELECT cid FROM items))");
			
			start = System.currentTimeMillis();
			int deletedCategories = ps.executeUpdate();
			end = System.currentTimeMillis();
			
			logger.info("Daily cleaning of database: deleted {} categories in {} seconds ",
					deletedCategories, (double) (end - start) / 1000);
			

		} catch (SQLException e) {
			logger.error("Unable to delete orphaned categories: {}", e.toString());
		}
		
		writeCleaningTime();
		
	}
	
	private void writeCleaningTime() {
		
		try (Connection conn = DBConnPool.getConnection()) {

			PreparedStatement psInsert = conn.prepareStatement(
					"INSERT INTO ez_cleanup VALUES (?)");
			
			psInsert.setTimestamp(1, Timestamp.from((Instant.now())));
			
			int inserts = psInsert.executeUpdate();
			
			if (inserts != 1) {
				logger.error("cleanup time could not be INSERTED to database");
			}
		
		} catch (SQLException e) {
			logger.error("Can not get write cleaning time to database", e.toString());
		}
		
	}
	
	
	private Optional<Timestamp> getLastCleaningTime() {
		
		Timestamp lastTime = null;

		try (Connection conn = DBConnPool.getConnection()) {
			
			PreparedStatement ps = conn.prepareStatement(
					"SELECT MAX(category_cleanup_time) FROM ez_cleanup");

			ResultSet rs = ps.executeQuery();
			
			rs.next();
			lastTime = rs.getTimestamp("max");
			
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("Can not get last cleaning time from database", e.toString());
		}
		
		return Optional.ofNullable(lastTime);
	}
	
	private void logDatabaseStatistics() { //TODO
		
		logger.info("Daily databases statistics: EZs, Items, Categories" );
		logger.info("Daily databases statistics values: {}, {}, {}", 0, 0 ,0 );
	}
	

	@Override
	public void run() {
	
		try {
			
			while(! Thread.currentThread().isInterrupted()) {
				
				if (getLastCleaningTime().isPresent()) {
					
					Instant dbTime = getLastCleaningTime().get().toInstant();
					Instant now = Instant.now();
					
					if (now.isAfter(dbTime)) {

						if (Duration.between(dbTime, now).compareTo(oneDay) >= 0)
							deleteOrphanedCategories();
						else
							logger.debug("no cleaning needed");

					} else {
						logger.warn("Time in database is newer than local time");
					}
					
				} else {
					deleteOrphanedCategories();
				}
				
				Thread.sleep(1000 * 60 * 60); // every hour
		//		Thread.sleep(10000); // 10 secs
			}

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.info("database cleaner thread was interrupted");
		} 
	}

}