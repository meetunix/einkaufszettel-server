package de.nachtsieb.einkaufszettelServer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
public final class EZServerConfig {
	
    private static Logger logger = LogManager.getLogger(EZServerConfig.class);

	public static final String CONF_PATH = "/etc/ez-server/server.properties";

	private String baseURI;
	private String logLevel;
	
	public EZServerConfig() {
		loadConfFile(CONF_PATH);
	}

	private void loadConfFile(String confFilePath) {

		Properties props = new Properties();

		try {

			Path path = Paths.get(confFilePath);
			InputStream is = Files.newInputStream(path, StandardOpenOption.READ);
			props.load(is);

			this.baseURI = props.getProperty("BASE_URI");
			this.logLevel = props.getProperty("LOG_LEVEL"); //TODO: check for valid log level

		} catch (InvalidPathException | IOException e) {
			logger.error("unable to load config file from given path " + confFilePath);
			System.exit(-1);
		}
	}

	public String getBaseURI() {
		return baseURI;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}
}