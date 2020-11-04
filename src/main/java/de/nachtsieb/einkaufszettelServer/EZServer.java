package de.nachtsieb.einkaufszettelServer;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import de.nachtsieb.einkaufszettelServer.dbService.DatabaseCleanerThread;
import de.nachtsieb.einkaufszettelServer.interceptors.GZIPReaderInterceptor;
import de.nachtsieb.einkaufszettelServer.interceptors.GZIPWriterInterceptor;
import de.nachtsieb.einkaufszettelServer.jsonValidation.JsonValidator;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Callable;

/*
 * Copyright © 2020 Martin Steinbach
 *
 * See file LICENSE for license information
 *
 */
@Command(description = "Einkaufszettel Server Application",
			mixinStandardHelpOptions = true,
			name = "EinkaufszettelServer",
			version = "EinkaufszettelServer 0.1.0-alpha")

public class EZServer implements Callable<String>  {
	
    private static Logger logger = LogManager.getLogger(EZServer.class);
    
	@Option(names = { "-l", "--log-path"}, description = "Path to the log directory")
	private String logDir = null;

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
        
        // load local config
        config = new EZServerConfig();
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
    	
    	int exitCode = new CommandLine(new EZServer()).execute(args);
    	System.exit(exitCode);
    }

	@Override
	public String call() throws Exception {
		
    	logger.info("EinkaufzettelServer started at " + BASE_URI);
    	
    	// start database cleaning thread
    	Thread cleaner = new Thread(new DatabaseCleanerThread(), "DB-CLEANER");
    	cleaner.start();
    	
    	
        final HttpServer server = startServer();
        System.out.println(String.format(
        		"\nEinkaufszettelServer started and listen on %s\nHit enter to stop it...",
        		BASE_URI));
        
        System.in.read();
        cleaner.interrupt();
        server.shutdownNow();
        return null;
		
	}
}