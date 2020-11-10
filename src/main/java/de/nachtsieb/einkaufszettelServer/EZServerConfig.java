package de.nachtsieb.einkaufszettelServer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public final class EZServerConfig {
	
	private String baseURI;
	private String logLevel;
	private String logPath;
	private String jdbcURL;
	private String dbUsername;
	private String dbPassword;
	
	public EZServerConfig(String serverConfPath) {
		loadConfFile(serverConfPath);
	}

	private void loadConfFile(String confFilePath) {

		Properties props = new Properties();

		try {

			Path path = Paths.get(confFilePath);
			InputStream is = Files.newInputStream(path, StandardOpenOption.READ);
			props.load(is);

			this.baseURI = props.getProperty("BASE_URI");
			this.logLevel = props.getProperty("LOG_LEVEL"); //TODO: check for valid log level
			this.logPath = props.getProperty("LOG_PATH"); 
			this.jdbcURL = props.getProperty("JDBC_URL"); 
			this.dbUsername = props.getProperty("DATABASE_USERNAME"); 
			this.dbPassword = props.getProperty("DATABASE_PASSWORD"); 

		} catch (InvalidPathException | IOException e) {
			System.err.println("could not read server.properties");
			System.exit(-1);
		}
	}

	public String getBaseURI() {
		return baseURI;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public String getLogPath() {
		return logPath;
	}

	public String getJdbcURL() {
		return jdbcURL;
	}

	public String getDbUsername() {
		return dbUsername;
	}

	public String getDbPassword() {
		String password = dbPassword;
		this.dbPassword = null;
		return password;
	}
	
	
	
}