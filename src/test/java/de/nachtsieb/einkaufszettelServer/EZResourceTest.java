package de.nachtsieb.einkaufszettelServer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.nachtsieb.einkaufszettelServer.dbService.DBReader;
import de.nachtsieb.einkaufszettelServer.dbService.DBWriter;
import de.nachtsieb.einkaufszettelServer.entities.Category;
import de.nachtsieb.einkaufszettelServer.entities.Einkaufszettel;
import de.nachtsieb.einkaufszettelServer.entities.Item;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Main test cases for testing the RESTful API endpoints.
 */
public class EZResourceTest {

  private static Logger logger;

  private static HttpServer server;
  private Client client;

  private static EZServerConfig config;

  @BeforeClass
  public static void setUpBeforeClass() {
    // start the server
    server = EZServer.startServer("server-test-configuration.properties");
    Connection conn = EZTestDB.getConnection();
    config = EZServer.getConfiguration();
    logger = LogManager.getLogger(EZResourceTest.class);
  }

  @Before
  public void setUp() {

    EZTestDB.resetDatabase();
    client = ClientBuilder.newClient();
    logger.debug(config.getBaseURI());
    logger.debug(config.getLogLevel());
  }

  @AfterClass
  public static void tearDown() {
    server.shutdownNow();
  }

  private Einkaufszettel deleteEZ(Einkaufszettel ez, int expectedReturnCode) {

    // create url
    String requestURL = config.getBaseURI() + "ez/" + ez.getEid();
    WebTarget target = client.target(requestURL);

    Response response = target.request().delete();
    logger.debug(
        "RESPONSE: {} ({}) for EZ {}",
        response.getStatus(),
        response.getStatusInfo().getReasonPhrase(),
        ez.getEid());

    assertThat(response.getStatus() == expectedReturnCode, is(true));
    response.close();

    return ez;
  }

  private Einkaufszettel sendEZ(Einkaufszettel ez, int expectedReturnCode) {

    // create url
    String requestURL = config.getBaseURI() + "ez/" + ez.getEid();
    WebTarget target = client.target(requestURL);

    // send EZ via http to server
    Entity<Einkaufszettel> entity = Entity.entity(ez, MediaType.APPLICATION_JSON);
    Response response = target.request().put(entity);
    logger.debug(
        "RESPONSE: {} ({}) for EZ {} Header: {}",
        response.getStatus(),
        response.getStatusInfo().getReasonPhrase(),
        ez.getEid(),
        response.getStringHeaders());

    assertThat(response.getStatus() == expectedReturnCode, is(true));
    response.close();
    return ez;
  }

  private Einkaufszettel retrieveEZ(Einkaufszettel ez, int expectedReturnCode) {

    // create url
    String requestURL = config.getBaseURI() + "ez/" + ez.getEid();
    WebTarget target = client.target(requestURL);

    // request EZ from server via http
    Response response = target.request().accept(MediaType.APPLICATION_JSON).get();
    logger.debug(
        "RESPONSE: {} ({}) for EZ {}",
        response.getStatus(),
        response.getStatusInfo().getReasonPhrase(),
        ez.getEid());

    assertThat(response.getStatus() == expectedReturnCode, is(true));

    return response.readEntity(Einkaufszettel.class);
  }

  private void sendAsyncEZs(List<Einkaufszettel> ezs) {

    String BASE_PATH = config.getBaseURI() + "ez/";

    List<Future<Response>> respFutures = new ArrayList<>(ezs.size());

    try {

      // invoking http PUT asynchronous
      for (Einkaufszettel ez : ezs) {

        WebTarget target = client.target(BASE_PATH + ez.getEid());
        Entity<Einkaufszettel> entity = Entity.entity(ez, MediaType.APPLICATION_JSON);
        AsyncInvoker asyncInvoker = target.request().async();
        respFutures.add(asyncInvoker.put(entity));
      }

      // get all responses from futures and wait until current feature is done to get them all
      for (Future<Response> respFuture : respFutures) {
        Response response = respFuture.get();
        assertThat(response.getStatus() == 200, is(true));
      }

    } catch (InterruptedException | ExecutionException e) {
      logger.error(e.getMessage());
    }
  }

  private void sendSyncEZs(List<Einkaufszettel> ezs) {

    for (Einkaufszettel ez : ezs) {
      sendEZ(ez, 200);
    }
  }

  /**
   * API CASE 01
   *
   * <p>Create a random EZ from model write it directly to database and read it via http get and
   * compare them.
   */
  @Test
  public void api01() {

    logger.debug("TEST: START api testcase 01");

    Einkaufszettel ez = TestUtils.genRandomEZ();

    // write EZ directly to database
    DBWriter.writeEZ(ez);

    // retrieve EZ from server via http and compare
    Einkaufszettel respondedEZ = retrieveEZ(ez, 200);
    assertThat(ez.equals(respondedEZ), is(true));

    logger.debug("TEST: END api testcase 01");
  }

  /**
   * API CASE 02
   *
   * <p>Create a random EZ from model, send it via http to the server, read it via database and
   * compare them.
   */
  @Test
  public void api02() {

    logger.debug("TEST: START api testcase 02");

    Einkaufszettel ez = TestUtils.genRandomEZ();

    // send EZ via http to server
    sendEZ(ez, 200);

    // retrieve EZ directly from database
    Einkaufszettel serverEZ = DBReader.getEZ(ez.getEid());

    // compare them
    assertNotNull(serverEZ);
    assertThat(ez.equals(serverEZ), is(true));

    logger.debug("TEST: END api testcase 02");
  }

  /**
   * API CASE 03
   *
   * <p>Create a random EZ from model, send it via http to the server, read it via http and compare
   * them.
   */
  @Test
  public void api03() {

    logger.debug("TEST: START api testcase 03");

    Einkaufszettel ez = TestUtils.genRandomEZ();

    // send EZ via http to server
    sendEZ(ez, 200);

    // retrieve EZ via http from server
    Einkaufszettel serverEZ = retrieveEZ(ez, 200);

    // compare them
    assertThat(ez.equals(serverEZ), is(true));

    logger.debug("TEST: END api testcase 03");
  }

  /**
   * API CASE 04 (Adding item to Einkaufszettel)
   *
   * <p>- Create a random EZ from model and send it via http to the server - Adding an item to EZ -
   * Send EZ to server via http (update required) - read it via http and compare them.
   */
  @Test
  public void api04() {

    logger.debug("TEST: START api testcase 04");

    Einkaufszettel ez = TestUtils.genRandomEZ();

    // send first version of EZ via http to server
    sendEZ(ez, 200);

    // add some item but not increment version, send retrieve from server and compare
    ez.addItem(TestUtils.genRandomItem());
    sendEZ(ez, 304); // 304 not modified

    Einkaufszettel oldEZ = retrieveEZ(ez, 200);
    assertThat(ez.equals(oldEZ), is(false));

    // increment version to force updating, send, retrieve and compare
    ez.incrementVersion();
    sendEZ(ez, 200);

    Einkaufszettel serverEZ = retrieveEZ(ez, 200);
    assertThat(ez.equals(serverEZ), is(true));

    logger.debug("TEST: END api testcase 04");
  }

  /**
   * API CASE 05 (deleting item from Einkaufszettel)
   *
   * <p>- Create a random EZ from model and send it via http to the server - deleting an item to EZ
   * - Send EZ to server via http (update required) - read it via http and compare them.
   */
  @Test
  public void api05() {

    logger.debug("TEST: START api testcase 05");

    Einkaufszettel ez = TestUtils.genRandomEZ();

    // send first version of EZ via http to server
    sendEZ(ez, 200);

    // delete first item but not increment version, send, retrieve from server and compare
    ez.getItems().remove(1);
    sendEZ(ez, 304); // 304 not modified

    Einkaufszettel oldEZ = retrieveEZ(ez, 200);
    assertThat(ez.equals(oldEZ), is(false));

    // increment version to force updating, send, retrieve and compare
    ez.incrementVersion();
    sendEZ(ez, 200);

    Einkaufszettel serverEZ = retrieveEZ(ez, 200);
    assertThat(ez.equals(serverEZ), is(true));

    logger.debug("TEST: END api testcase 05");
  }

  /**
   * API CASE 06 (update item inside an Einkaufszettel)
   *
   * <p>- create an random EZ from model and send it via http to the server - alter an item from
   * this EZ - send EZ to server via http (update required) - read it via http and compare them. -
   * read it via database and compare them.
   */
  @Test
  public void api06() {

    logger.debug("TEST: START api testcase 06");

    Einkaufszettel ez = TestUtils.genRandomEZ();

    // send first version of EZ via http to server
    sendEZ(ez, 200);

    // alter first item
    ez.getItems().get(1).setAmount(333);

    // compare items directly from database
    Item dbItem = Objects.requireNonNull(DBReader.getEZ(ez.getEid())).getItems().get(1);
    Item localItem = ez.getItems().get(1);
    assertThat(localItem.equals(dbItem), is(false));

    // increment version to force updating, send, retrieve and compare
    ez.incrementVersion();
    sendEZ(ez, 200);

    Einkaufszettel serverEZ = retrieveEZ(ez, 200);
    assertThat(ez.equals(serverEZ), is(true));

    // compare items directly  from database
    dbItem = Objects.requireNonNull(DBReader.getEZ(ez.getEid())).getItems().get(1);
    localItem = ez.getItems().get(1);
    assertThat(localItem.equals(dbItem), is(true));

    logger.debug("TEST: END api testcase 06");
  }

  /**
   * API CASE 07 (update category inside an Einkaufszettel)
   *
   * <p>- create a random EZ from model and send it via http to the server - alter an item (here
   * Category specific attributes) from this EZ - send EZ to server via http (update required) -
   * read it via http and compare them. - read it via database and compare them.
   */
  /*
  @Test
  @Ignore
  //TODO rewrite if necessary
  public void api07() {

    logger.debug("TEST: START api testcase 07 (update category)");

    Einkaufszettel ez = TestUtils.genRandomEZ();

    // send first version of EZ via http to server
    sendEZ(ez, 200);

    // alter category of first item
    ez.getItems().get(1).setCatColor("AAAAAA");
    UUID cid = ez.getItems().get(1).getCid();

    // compare categories directly from database
    Category dbCat = DBReader.getCategory(cid);
    Item localItem = ez.getItems().get(1);
    Category localCat =
        new Category(localItem.getCid(), localItem.getCatColor(), localItem.getCatDescription());

    assertNotNull(dbCat);
    assertThat(localCat.equals(dbCat), is(false));

    // increment version to force updating, send, retrieve and compare
    ez.incrementVersion();
    sendEZ(ez, 200);

    Einkaufszettel serverEZ = retrieveEZ(ez, 200);
    assertThat(ez.equals(serverEZ), is(true));

    // compare categories directly from database
    dbCat = DBReader.getCategory(cid);
    assertNotNull(dbCat);
    assertThat(localCat.equals(dbCat), is(true));

    logger.debug("TEST: END api testcase 07 (update category)");
  }

   */

  /**
   * API CASE 08 (substitute all categories)
   *
   * <p>- create a random EZ from model and send it via http to the server - change the category
   * from all item to the category from the first item - send EZ to server via http (update
   * required) - read it via http and compare them.
   */
  @Test
  public void api08() {

    logger.debug("TEST: START api testcase 08 (substitue all categories)");

    Einkaufszettel ez = TestUtils.genRandomEZ();

    // send first version of EZ via http to server
    sendEZ(ez, 200);

    // substitute all categories
    Category cat =
        new Category(
            ez.getItems().get(1).getCid(),
            ez.getItems().get(1).getCatColor(),
            ez.getItems().get(1).getCatDescription());

    ez.getItems().forEach(i -> i.setCategoryValues(cat));

    // increment version to force updating, send, retrieve and compare
    ez.incrementVersion();
    sendEZ(ez, 200);

    Einkaufszettel serverEZ = retrieveEZ(ez, 200);
    assertThat(ez.equals(serverEZ), is(true));

    logger.debug("TEST: END api testcase 08 (substitue all categories)");
  }

  /**
   * API CASE 09 (simple work case)
   *
   * <p>- create a random EZ (A) to the server - create an EZ (B) by receiving A from server -
   * alter B and send it to server - update A and compare
   */
  @Test
  public void api09() {

    logger.debug("TEST: START api testcase 09 (simple work case)");

    Einkaufszettel A = TestUtils.genRandomEZ();

    // send first version of EZ via http to server
    sendEZ(A, 200);

    // create B by receiving A from server
    Einkaufszettel B = retrieveEZ(A, 200);

    // alter B and send it to server
    List<Item> newItems = TestUtils.genItemList(3);
    B.setItems(newItems);
    B.incrementVersion();

    assertThat(B.equals(A), is(false));

    sendEZ(B, 200);

    // retrieve A and compare with B
    A = retrieveEZ(A, 200);
    assertThat(B.equals(A), is(true));

    logger.debug("TEST: END api testcase 09 (simple work case)");
  }

  /**
   * API CASE 20 (Benchmark sync and async writes)
   */
  @Test
  public void api20() {

    logger.debug("TEST: START asyncIO testcase 20");

    int numberOfEZs = 100;

    List<Einkaufszettel> ezsSync = new ArrayList<>(numberOfEZs);
    List<Einkaufszettel> ezsAsync = new ArrayList<>(numberOfEZs);

    for (int i = 0; i < numberOfEZs; i++) {
      ezsSync.add(TestUtils.genRandomEZ());
      ezsAsync.add(TestUtils.genRandomEZ());
    }

    long start, end;

    // sync write
    start = System.currentTimeMillis();
    sendSyncEZs(ezsAsync);
    end = System.currentTimeMillis();
    logger.debug(
        "TEST: Adding {} EZs synchronously takes {} seconds",
        numberOfEZs,
        (double) (end - start) / 1000);

    // async write
    start = System.currentTimeMillis();
    sendAsyncEZs(ezsSync);
    end = System.currentTimeMillis();
    logger.debug(
        "TEST: Adding {} EZs asynchronously takes {} seconds",
        numberOfEZs,
        (double) (end - start) / 1000);

    logger.debug("TEST: END asyncIO testcase 20");
  }

  /**
   * API CASE 21 (Benchmark: async CRUD operations)
   */
  @Test
  public void api21() {

    int numberOfWorker = 10;
    int numberOfOperations = 200;
    long start, end;
    long sum = 0;

    logger.debug(
        "TEST: START asyncIO testcase 21 ({} operations of {} parallel clients)",
        numberOfOperations * 4,
        numberOfWorker);

    // test behavior on a full database
    /*
    sendAsyncEZs(Stream.generate(TestUtils::genRandomEZ)
    		.limit(500)
    		.collect(Collectors.toList()));
     */
    List<Einkaufszettel> ezList =
        Stream.generate(TestUtils::genRandomEZ)
            .limit(numberOfOperations)
            .collect(Collectors.toList());

    ExecutorService executor = Executors.newFixedThreadPool(numberOfWorker);
    CompletionService<Einkaufszettel> cse = new ExecutorCompletionService<>(executor);

    CrudOperation[] operationsList = {create, update, read, delete};

    for (CrudOperation crudOp : operationsList) {

      List<OperationCallable> tasks = new ArrayList<>(numberOfOperations);

      for (Einkaufszettel ez : ezList) {

        if (crudOp == update) {
          ez.setItems(TestUtils.genItemList(Einkaufszettel.MAX_ITEMS / 2)); // 64 items
          ez.incrementVersion();
        }

        OperationCallable task = new OperationCallable(crudOp, ez, 200);
        tasks.add(task);
      }

      // submit all tasks for the current operation to the executor service
      start = System.currentTimeMillis();
      tasks.forEach(cse::submit);

      // wait for all tasks of the current operation to be ready and count them
      tasks.forEach(
          t -> {
            try {
              cse.take().isDone();
            } catch (InterruptedException e) {
              logger.error("TEST: api21 test (simple async benchmark) failed");
            }
          });

      end = System.currentTimeMillis();

      String debugMessage;
      if (crudOp == create) {
        debugMessage = "TEST: creation of {} EZ takes {} sec";
      } else if (crudOp == update) {
        debugMessage = "TEST: updating of {} EZ takes {} sec";
      } else if (crudOp == read) {
        debugMessage = "TEST: reading of {} EZ takes {} sec";
      } else {
        debugMessage = "TEST: deletion of {} EZ takes {} sec";
      }

      logger.debug(debugMessage, numberOfOperations, (double) (end - start) / 1000);
      // sum up all times for each operation
      sum += end - start;
    }

    logger.debug(
        "TEST: simple benchmark of {} CRUD operations: {} seconds",
        (double) numberOfOperations * 4,
        sum / 1000);

    executor.shutdown();
    logger.debug(
        "TEST: END asyncIO testcase 21 ({} operations of {} parallel clients)",
        (double) numberOfOperations * 4,
        numberOfWorker);
  }

  // lambdas
  CrudOperation create = this::sendEZ;
  CrudOperation update = this::sendEZ;
  CrudOperation delete = this::deleteEZ;
  CrudOperation read = this::retrieveEZ;

  // the interface the lamdbas use
  interface CrudOperation {

    Einkaufszettel operate(Einkaufszettel a, int b);
  }

  // Callable for use as a task in CompletionService
  static class OperationCallable implements Callable<Einkaufszettel> {

    private final CrudOperation crudOperation;
    private final Einkaufszettel ez;
    private final int expectedReturnCode;

    OperationCallable(CrudOperation crudOperation, Einkaufszettel ez, int expReturnCode) {
      this.crudOperation = crudOperation;
      this.ez = ez;
      this.expectedReturnCode = expReturnCode;
    }

    @Override
    public Einkaufszettel call() {

      return crudOperation.operate(ez, expectedReturnCode);
    }
  }

  /**
   * API CASE 30 (compression of response)
   *
   * <p>Tests if a requested gzipped ez is responded correctly.
   *
   * <p>- create Einkaufszettel and send to application - receive einkaufszettel with header:
   * "Accept-Encoding: gzip" - check correct header of the response - unzip and unmarshal - compare
   * ez objects - compare json strings
   */
  @Test
  public void api30() {

    Einkaufszettel ez = TestUtils.genRandomEZ();
    Einkaufszettel unzippedEZ = null;

    sendEZ(ez, 200);

    /*
     * configure client for reading ez with gzip Encoding
     */

    String requestURL = config.getBaseURI() + "ez/" + ez.getEid();
    WebTarget target = client.target(requestURL);

    Response response =
        target.request().accept(MediaType.APPLICATION_JSON).acceptEncoding("gzip").get();

    logger.debug(
        "RESPONSE: {} ({}) for EZ {}",
        response.getStatus(),
        response.getStatusInfo().getReasonPhrase(),
        ez.getEid());

    // check if the correct response header was set
    String encodingValue = response.getHeaderString("Content-Encoding");
    logger.debug("TEST: response header Content-Encoding -> {}", encodingValue);
    assertThat(encodingValue.equalsIgnoreCase("gzip"), is(true));
    logger.debug("TEST: received {} bytes (compressed)", response.getLength());

    String unzippedBody = null;
    String ezAsJson = null;
    ObjectMapper mapper = new ObjectMapper();
    try {

      // decode gzipped body to string
      GZIPInputStream gis = new GZIPInputStream((InputStream) response.getEntity());
      InputStreamReader reader = new InputStreamReader(gis, StandardCharsets.UTF_8);
      BufferedReader bufferedReader = new BufferedReader(reader);
      unzippedBody = bufferedReader.readLine();
      logger.debug(
          "TEST: received {} bytes (uncompressed)",
          unzippedBody.getBytes(StandardCharsets.UTF_8).length);

      // unmarshalling string to einkaufszettel
      unzippedEZ = mapper.readValue(unzippedBody, Einkaufszettel.class);
      ezAsJson = mapper.writeValueAsString(ez);

    } catch (JsonProcessingException e) {
      logger.error("TEST: exception occured due to conversion string to ez: {}", e.toString());
    } catch (IOException e) {
      logger.error(
          "TEST: exception occured due to conversion from gzip to string: {}", e.toString());
    }

    // compare einkaufszettel
    assertNotNull(unzippedEZ);
    assertThat(ez.equals(unzippedEZ), is(true));
    // compare whole json string
    assertNotNull(unzippedBody);
    assertNotNull(ezAsJson);
    assertThat(ezAsJson.equals(unzippedBody), is(true));
  }

  /**
   * API CASE 31 (compression of request)
   *
   * <p>Tests if a gzipped request is handled correctly by the server
   *
   * <p>- Create a simple Einkaufszettel and send to application (unzipped) - alter some fields of
   * the Einkaufszettel - send it gzipped to the server (zipped) - retrieve the hopefully altered
   * Einkaufszettel from server (unzipped) - compare the EZ
   */
  @Test
  public void api31() {

    ObjectMapper mapper = new ObjectMapper();

    Category cat = new Category("00AA00", "irgendeine kategorie");
    Einkaufszettel ez = new Einkaufszettel("werden");
    ez.addItem(new Item("Käse (viel)", cat));
    ez.addItem(new Item("Ribiseln", cat));
    ez.addItem(new Item("Paradeiser", cat));

    sendEZ(ez, 200);

    // alter some attributes
    ez.incrementVersion();
    ez.addItem(new Item("Kren", cat));

    // convert ez to json-string
    String ezString;
    try {
      ezString = mapper.writeValueAsString(ez);

      // gzip the string TODO
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      GZIPOutputStream gos = new GZIPOutputStream(out);
      logger.debug("uncompressed: {} bytes", ezString.getBytes(StandardCharsets.UTF_8).length);
      gos.write(ezString.getBytes(StandardCharsets.UTF_8));
      gos.close();
      byte[] ezStringZipped = out.toByteArray();

      logger.debug("compressed: {} bytes", ezStringZipped.length);

      InputStream in = new ByteArrayInputStream(ezStringZipped);

      /*
       * configure client for writing EZ with gzip Encoding
       */

      String requestURL = config.getBaseURI() + "ez/" + ez.getEid();
      WebTarget target = client.target(requestURL);

      Variant variant = new Variant(MediaType.APPLICATION_JSON_TYPE, "en", "gzip");
      Entity<InputStream> entity = Entity.entity(in, variant);

      // send compressed EZ to server
      Response response = target.request().put(entity);

      logger.debug(
          "RESPONSE: {} ({}) for EZ {}",
          response.getStatus(),
          response.getStatusInfo().getReasonPhrase(),
          ez.getEid());

      // get altered EZ from server
      Einkaufszettel alteredEZ = retrieveEZ(ez, 200);

      // compare local altered EZ with remote EZ
      assertThat(ez.equals(alteredEZ), is(true));

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
