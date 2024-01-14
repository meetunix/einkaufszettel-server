/*
 * Copyright © 2020 Martin Steinbach
 *
 * See file LICENSE for license information
 *
 */

package de.nachtsieb.einkaufszettelServer;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nachtsieb.einkaufszettelServer.dbService.DBReader;
import de.nachtsieb.einkaufszettelServer.dbService.DBWriter;
import de.nachtsieb.einkaufszettelServer.interceptors.GZIPReaderInterceptor;
import de.nachtsieb.einkaufszettelServer.interceptors.GZIPWriterInterceptor;
import de.nachtsieb.einkaufszettelServer.interceptors.ReaderValidationInterceptor;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    description = "Einkaufszettel Server Application",
    mixinStandardHelpOptions = true,
    name = "EinkaufszettelServer",
    version = "EinkaufszettelServer 0.4.1")
public class EZServer implements Callable<String> {

  @SuppressWarnings("FieldMayBeFinal")
  @Option(
      names = {"-c", "--config-path"},
      description = "Path to server config file")
  private static String serverConfigPath = "/etc/ez-server/server.properties";

  private static EZServerConfig config;

  // Base URI the Grizzly HTTP server will listen on
  // private static final String BASE_URI = "http://localhost:8081/r0/";
  private static String BASE_URI;

  public static EZServerConfig getConfiguration() {
    return config;
  }

  /**
   * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
   *
   * @return Grizzly HTTP server.
   */
  public static HttpServer startServer(String serverConfigPath) {

    // load server config file
    config = new EZServerConfig(serverConfigPath);

    // set log configuration
    System.setProperty("logPath", config.getLogPath());
    // get log level from environment if available, otherwise use value from config
    Optional<String> logLevel = Optional.ofNullable(System.getenv().get("EZSERVER_LOG_LEVEL"));
    if (logLevel.isPresent()) {
      System.setProperty("logLevel", logLevel.get());
    } else {
      System.setProperty("logLevel", config.getLogLevel());
    }

    System.setProperty("jdbcURL", config.getJdbcURL());
    System.setProperty("databaseUsername", config.getDbUsername());
    System.setProperty("databasePassword", config.getDbPassword());

    BASE_URI = config.getBaseURI();

    final ResourceConfig rc =
        new ResourceConfig()
            .packages("de.nachtsieb.einkaufszettelServer")
            .property(ServerProperties.WADL_FEATURE_DISABLE, true)
            .register(JacksonFeature.class)
            .register(GZIPReaderInterceptor.class)
            .register(GZIPWriterInterceptor.class)
            .register(ReaderValidationInterceptor.class);

    // Create object mapper instance for unmarshalling from database
    ObjectMapper mapper = new ObjectMapper();
    rc.register(
        new AbstractBinder() {
          @Override
          protected void configure() {
            bind(mapper).to(ObjectMapper.class);
          }
        });

    // create and start a new instance of grizzly http server
    // exposing the Jersey application at BASE_URI
    return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
  }

  public static void main(String[] args) {

    // parse cli arguments
    int exitCode = new CommandLine(new EZServer()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public String call() throws Exception {

    // starts the grizzly web server
    final HttpServer server = startServer(serverConfigPath);
    System.out.printf("\nEinkaufszettelServer started and listen on %s\n%n", BASE_URI);

    // create database schema if main table does not exist in database
    if (!DBReader.tableExists(DBReader.TABLE_EINKAUFSZETTEL)) {
      System.out.println("\nCreate database schema\n");
      RessourceLoader resl = new RessourceLoader();
      InputStream is = resl.getFileFromResourceAsStream("sql-json-schema.sql");
      String schema = resl.getStringFromInputStream(is);
      DBWriter.createTables(schema.replace("\n", " "));
    }

    // if the JVM shuts down the following thread is executed
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  System.out.println("shutting down web server");
                  server.shutdownNow();
                  System.out.println("goodbye");
                }));

    while (Thread.currentThread().isAlive()) {
      //noinspection BusyWait
      Thread.sleep(5000);
    }
    return null;
  }
}
