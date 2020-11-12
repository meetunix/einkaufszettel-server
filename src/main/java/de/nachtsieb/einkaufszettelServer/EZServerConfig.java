package de.nachtsieb.einkaufszettelServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class EZServerConfig {

	public static final String PROPERTY_BASE_URI = "BASE_URI";
	public static final String PROPERTY_LOG_LEVEL = "LOG_LEVEL";
	public static final String PROPERTY_LOG_PATH = "LOG_PATH";
	public static final String PROPERTY_JDBC_URL = "JDBC_URL";
	public static final String PROPERTY_DATABASE_USERNAME = "DATABASE_USERNAME";
	public static final String PROPERTY_DATABASE_PASSWORD = "DATABASE_PASSWORD";

	public static final String LOG_LEVEL_WARN = "WARN";
	public static final String LOG_LEVEL_INFO = "INFO";
	public static final String LOG_LEVEL_DEBUG = "DEBUG";
	
	private	Properties propertiesFromFile = new Properties();
	private Map<String,String> configMap = new HashMap<>();
	private List<String> allowedLogLevel;
	private List<String> allowedConfigPropertyList;
	
	public EZServerConfig(String serverConfPath) {
		
		// initialize allowed log level
		allowedLogLevel = Stream.of(
			LOG_LEVEL_DEBUG,
			LOG_LEVEL_INFO,
			LOG_LEVEL_WARN)
				.collect(Collectors.toList());
		
		// initialize allowed property keys
		allowedConfigPropertyList = Stream.of(
			PROPERTY_BASE_URI,
			PROPERTY_LOG_LEVEL,
			PROPERTY_LOG_PATH,
			PROPERTY_JDBC_URL,
			PROPERTY_DATABASE_USERNAME,
			PROPERTY_DATABASE_PASSWORD)
				.collect(Collectors.toList());
		
		loadConfFile(serverConfPath.trim());
	}

	private void loadConfFile(String confFilePath) {


		// read properties from config file
		try {

			Path path = Paths.get(confFilePath);
			InputStream is = Files.newInputStream(path, StandardOpenOption.READ);
			propertiesFromFile.load(is);
			is.close();
			
		} catch (InvalidPathException | IOException e) {
			System.err.println("Config-Error: unable to read configuration file: " + confFilePath);
			System.exit(-1);
		}
		
			
		// check if all needed properties from file are present
		List<String> filePropertyList = Stream.of(propertiesFromFile.keySet().toArray())
				.map(v -> v.toString())
				.collect(Collectors.toList());

		for (String fileProp : filePropertyList) {
			if (! allowedConfigPropertyList.contains(fileProp)) {
				System.err.println(String.format(
						"Config-Error: property %s is unkown, possible properties are:\n%s ",
						fileProp, allowedConfigPropertyList));
				System.exit(-1);
			}
		}
		// write all property values to the configMap
		allowedConfigPropertyList.forEach(k -> setProperty(k));
		System.out.println("\nThe following configuration parameters are used:\n");
		for (String key : allowedConfigPropertyList) {
			if (! key.equals(PROPERTY_DATABASE_PASSWORD))
				System.out.println(String.format("%-20s: %s",key, configMap.get(key)));
		}
		System.out.println();
	}
	
	private void setProperty(String propyKey) {
		
		// check for valid log level
		if (propyKey.equals(PROPERTY_LOG_LEVEL)) {

			String logLevelFromFile = propertiesFromFile.getProperty(propyKey);

			if (! allowedLogLevel.contains(logLevelFromFile.toUpperCase())) {

				System.err.println(String.format(
						"Config-Error: log level %s unkown, possible levels are:\n%s ",
						logLevelFromFile, allowedLogLevel));
				System.exit(-1);
			}
		}
		
		// check if log path ist present and writable
		if (propyKey.equals(PROPERTY_LOG_PATH)) {

			Path path = Paths.get(propertiesFromFile.getProperty(PROPERTY_LOG_PATH));
			File filePath = path.toFile();
			
			validatePath(filePath);
		}
			
		// if H2 in file mode is used, check if the path is writeable 
		if (propyKey.equals(PROPERTY_JDBC_URL)) {

			String jdbcString = propertiesFromFile.getProperty(PROPERTY_JDBC_URL);

			if ( jdbcString.contains("file")) {

				String[] splittedString = jdbcString.split(":");
				String filePathString = splittedString[splittedString.length - 1];
				File filePath = Path.of(filePathString).getParent().toFile();
				validatePath(filePath);
		}
	}
		
		// copy the left properties
		configMap.put(propyKey, propertiesFromFile.getProperty(propyKey));
		
	}
	
	private void validatePath(File filePath) {

		if (! (filePath.exists() && filePath.isDirectory() && filePath.canWrite()) ) {

			System.err.println(String.format(
					"Config-Error: Path %s does not exists or is not writeable.",
					filePath.toString()));
			System.exit(-1);
		}
		
	}

	public String getBaseURI() {
		return configMap.get(PROPERTY_BASE_URI);
	}

	public String getLogLevel() {
		return configMap.get(PROPERTY_LOG_LEVEL);
	}

	public String getLogPath() {
		return configMap.get(PROPERTY_LOG_PATH);
	}

	public String getJdbcURL() {
		return configMap.get(PROPERTY_JDBC_URL);
	}

	public String getDbUsername() {
		return configMap.get(PROPERTY_DATABASE_USERNAME);
	}

	public String getDbPassword() {
		String password = configMap.get(PROPERTY_DATABASE_PASSWORD);
		configMap.put(PROPERTY_DATABASE_PASSWORD, null);
		return password;
	}
}