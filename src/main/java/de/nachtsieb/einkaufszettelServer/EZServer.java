package de.nachtsieb.einkaufszettelServer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import de.nachtsieb.einkaufszettelServer.dbService.DatabaseCleanerThread;
import de.nachtsieb.einkaufszettelServer.interceptors.GZIPReaderInterceptor;
import de.nachtsieb.einkaufszettelServer.interceptors.GZIPWriterInterceptor;
import de.nachtsieb.einkaufszettelServer.jsonValidation.JsonValidator;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/*
 * Copyright © 2020 Martin Steinbach
 *
 * See file LICENSE for license information
 *
 */

public class EZServer {
	

	private static JsonValidator jsonValidator;
	private static EZServerConfig config;
	
	public static String baseURI;
	
    // Base URI the Grizzly HTTP server will listen on
    //private static final String BASE_URI = "http://localhost:8081/r0/";
    private static String BASE_URI;

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in de.nachtsieb.einkaufszettelServer package
        final ResourceConfig rc = new ResourceConfig()
        		.packages("de.nachtsieb.einkaufszettelServer");
        rc.property(ServerProperties.WADL_FEATURE_DISABLE, true);
        
        BASE_URI = config.getBaseURI();
        
        /*
         *  Injecting an instance of the class JsonValidator to the application. Results in much
         *  faster json validation and a smaller footprint, because it is not necessary to
         *  create a new validator object on each request. At 10000 serial requests the validation
         *  on my system takes 45 seconds. Without using a singleton object it needs 55 seconds
         *  to process all requests.
         *  
         *  doing the same for the config object
         */
    	jsonValidator = new JsonValidator();
        rc.register(new AbstractBinder() {
        	@Override
        	protected void configure() {
        		bind(jsonValidator).to(JsonValidator.class);
        		bind(config).to(EZServerConfig.class);
        	}
        });
        
        // register the interceptor classes for compressed
        rc.register(GZIPWriterInterceptor.class);
        rc.register(GZIPReaderInterceptor.class);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
    	
        // load local config
        config = new EZServerConfig();

    	System.setProperty("logPath", config.getLogPath());
    	System.out.println("log directory: " + System.getProperty("logPath"));

    	LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    	Configuration config = ctx.getConfiguration();
    	LoggerConfig loggerConfig = config.getLoggerConfig("de.nachtsieb"); 
    	Map<String,Appender> apps = loggerConfig.getAppenders();
    	Appender rfa = apps.get("rolling_file");
    	System.out.println(rfa.getName());
    	
    	

    	System.out.println("LOADED LOGGER CONFIG : " + loggerConfig.getName() );
    	System.out.println("LEVEL BEFORE : " + loggerConfig.getLevel() );

    	loggerConfig.setLevel(Level.WARN);

    	System.out.println("LEVEL AFTER : " + loggerConfig.getLevel() );
    	ctx.updateLoggers();
    	
    	// starts the grizzly web server
        final HttpServer server = startServer();
        System.out.println(String.format(
        		"\nEinkaufszettelServer started and listen on %s\n", BASE_URI));

    	// start database cleaning thread
    	Thread cleaner = new Thread(new DatabaseCleanerThread(), "DB-CLEANER");
    	cleaner.start();

    	// if the JVM shuts down the following thread is executed
        Runtime.getRuntime().addShutdownHook(new Thread() 
        { 
          public void run() 
          { 
            System.out.println("closing database cleaner thread"); 
            cleaner.interrupt();
            System.out.println("shutting down web server"); 
            server.shutdownNow();
            System.out.println("goodbye"); 
          } 
        }); 
        

    
		
    	while(Thread.currentThread().isAlive())
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
}