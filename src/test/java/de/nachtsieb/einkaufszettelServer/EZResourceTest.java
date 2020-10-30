package de.nachtsieb.einkaufszettelServer;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.nachtsieb.einkaufszettelServer.dbService.DBReader;
import de.nachtsieb.einkaufszettelServer.dbService.DBWriter;
import de.nachtsieb.einkaufszettelServer.entities.Category;
import de.nachtsieb.einkaufszettelServer.entities.Einkaufszettel;
import de.nachtsieb.einkaufszettelServer.entities.Item;
import de.nachtsieb.einkaufszettelServer.entities.Limits;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main test cases for testing the RESTful API endpoints. 
 *
 */
public class EZResourceTest {
	
    private static Logger logger = LogManager.getLogger(EZResourceTest.class);

    private static HttpServer server;
    private Client client ;
    private static  Connection conn;
    
    private static EZServerConfig config;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // start the server
        server = EZServer.startServer();
        conn = EZTestDB.getConnection();
        
        config = new EZServerConfig();
        
    }
    
    @Before
    public void setUp() {
    	
    	EZTestDB.resetDatabase(conn);

        // create the client
        client = ClientBuilder.newClient();

        // uncomment the following line if you want to enable
        // support for JSON in the client (you also have to uncomment
        // dependency on jersey-media-json module in pom.xml and Main.startServer())
        // --
        // c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());
        
        logger.debug(config.getBaseURI());
        logger.debug(config.getLogLevel());

    }

    @AfterClass
    public static void tearDown() throws Exception {
        server.shutdownNow();
    }
    
    private Einkaufszettel deleteEZ(Einkaufszettel ez, int expectedReturnCode) {

    	// create url
    	String requestURL = config.getBaseURI() + "ez/" + ez.getEid();
        WebTarget target = client.target(requestURL);
        
        Response response = target.request().delete();
        logger.debug("RESPONSE: {} ({}) for EZ {}", response.getStatus(),
        		response.getStatusInfo().getReasonPhrase(), ez.getEid() );

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
        logger.debug("RESPONSE: {} ({}) for EZ {}", response.getStatus(),
        		response.getStatusInfo().getReasonPhrase(), ez.getEid() );

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
        logger.debug("RESPONSE: {} ({}) for EZ {}", response.getStatus(),
        		response.getStatusInfo().getReasonPhrase(), ez.getEid() );

        assertThat(response.getStatus() == expectedReturnCode, is(true));
        
        return response.readEntity(Einkaufszettel.class);
    	
    }
    
    private void sendAsyncEZs(List<Einkaufszettel> ezs) {
    	
    	String BASE_PATH = config.getBaseURI() + "ez/";
    	
    	List<Future<Response>> respFutures = new ArrayList<>(ezs.size());
    	
		try {
			
			// invoking http PUT asynchronous 
			for(Einkaufszettel ez : ezs) {

				WebTarget target = client.target(BASE_PATH + ez.getEid());
				Entity<Einkaufszettel> entity = Entity.entity(ez, MediaType.APPLICATION_JSON);
				AsyncInvoker asyncInvoker = target.request().async();
				respFutures.add(asyncInvoker.put(entity));
			}
		
			// get all responses from futures and wait until current feature is done to get them all
			for(Future<Response> respFuture : respFutures) {
				Response response = respFuture.get();
				assertThat(response.getStatus() == 200, is(true));
			}

		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		} catch (ExecutionException e) {
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
    * Create an random EZ from model write it directly to database and read it via
    * http get and compare them. 
    *  
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
    * Create an random EZ from model, send it via http to the server, read it via
    * database and compare them. 
    *  
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
        assertThat(ez.equals(serverEZ), is(true));
        
    	logger.debug("TEST: END api testcase 02");
    }

   /**
    * API CASE 03
    * 
    * Create an random EZ from model, send it via http to the server, read it via
    * http and compare them. 
    *  
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
    * - Create an random EZ from model and send it via http to the server
    * - Adding an item to EZ
    * - Send EZ to server via http (update required)
    * - read it via http and compare them. 
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
    * - Create an random EZ from model and send it via http to the server
    * - deleting an item to EZ
    * - Send EZ to server via http (update required)
    * - read it via http and compare them. 
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
    * - create an random EZ from model and send it via http to the server
    * - alter an item from this EZ
    * - send EZ to server via http (update required)
    * - read it via http and compare them. 
    * - read it via database and compare them. 
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
        Item dbItem = DBReader.getEZ(ez.getEid()).getItems().get(1);
        Item localItem = ez.getItems().get(1);
        assertThat(localItem.equals(dbItem), is(false));
       
        // increment version to force updating, send, retrieve and compare
        ez.incrementVersion();
    	sendEZ(ez, 200);

    	Einkaufszettel serverEZ = retrieveEZ(ez, 200);
        assertThat(ez.equals(serverEZ), is(true));
        
        // compare items directly  from database
        dbItem = DBReader.getEZ(ez.getEid()).getItems().get(1);
        localItem = ez.getItems().get(1);
        assertThat(localItem.equals(dbItem), is(true));

    	logger.debug("TEST: END api testcase 06");
    }

   /**
    * API CASE 07 (update category inside an Einkaufszettel)
    * 
    * - create an random EZ from model and send it via http to the server
    * - alter an item (here Category specific attributes) from this EZ
    * - send EZ to server via http (update required)
    * - read it via http and compare them. 
    * - read it via database and compare them. 
    */
    @Test
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
        Category localCat = new Category(
        		localItem.getCid(),
        		localItem.getCatColor(),
        		localItem.getCatDescription());

        assertThat(localCat.equals(dbCat), is(false));
       
        // increment version to force updating, send, retrieve and compare
        ez.incrementVersion();
    	sendEZ(ez, 200);

    	Einkaufszettel serverEZ = retrieveEZ(ez, 200);
        assertThat(ez.equals(serverEZ), is(true));
        
        // compare categories directly from database
        dbCat = DBReader.getCategory(cid);
        localItem = ez.getItems().get(1);
        assertThat(localCat.equals(dbCat), is(true));

    	logger.debug("TEST: END api testcase 07 (update category)");
    }

   /**
    * API CASE 08 (substitute all categories)
    * 
    * - create an random EZ from model and send it via http to the server
    * - change the category from all item to the category from the first item 
    * - send EZ to server via http (update required)
    * - read it via http and compare them. 
    */
    @Test
    public void api08() {
    	
    	logger.debug("TEST: START api testcase 08 (substitue all categories)");

    	Einkaufszettel ez = TestUtils.genRandomEZ();

    	// send first version of EZ via http to server
    	sendEZ(ez, 200);
        
        // substitute all categories
        Category cat = new Category(
        		ez.getItems().get(1).getCid(),
        		ez.getItems().get(1).getCatColor(),
        		ez.getItems().get(1).getCatDescription());
        
        ez.getItems().stream().forEach(i -> i.setCategoryValues(cat));
    	
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
    * - create an random EZ (A) to the server
    * - create a EZ (B) by receiving A from server
    * - alter B and send it to server
    * - update A and compare
    * 
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
     * API CASE 30 (database trigger: delete_orphaned_categories)
     *
     * currently disabled
     * 
     * In consequence of benchmarking the database trigger, which is thrown by a deletion of an
     * Einkaufszettel took to long. Therefore the deletion of orphaned categories is done
     * by an own thread once a day.
     * 
     * - create two ez (A and B) with some items belonging to some categories
     * - delete A via http
     * - check if the categories used by the items from A are deleted in database
     * - check if the categories from B are present in database 
     * 
     */
    
    /*
     * 
    @Test
    public void api30() {

    	logger.debug("TEST: START api testcase 30 (database trigger)");

    	Category catA = new Category("AAAAAA", "category that belongs to A");
    	Category catB = new Category("BBBBBB", "category that belongs to B");
    	Category catAB = new Category("ABABAB", "category that belongs to A and B");
    	
    	Einkaufszettel A = new Einkaufszettel("Einkaufszettel A");
    	A.addItem(new Item("item A", catA));
    	A.addItem(new Item("item AB", catAB));
    	
    	Einkaufszettel B = new Einkaufszettel("Einkaufszettel B");
    	B.addItem(new Item("item B", catB));
    	B.addItem(new Item("item BA", catAB));
    	
    	sendEZ(A, 200);
    	sendEZ(B, 200);
   
    	// check if all categories are in database
    	assertThat(DBReader.getCategory(catA.getCid()).equals(catA), is(true));
    	assertThat(DBReader.getCategory(catB.getCid()).equals(catB), is(true));
    	assertThat(DBReader.getCategory(catAB.getCid()).equals(catAB), is(true));
        
        deleteEZ(A, 200);
        
        // check if the category only used by A is deleted
    	assertThat(DBReader.getCategory(catA.getCid()) == null, is(true));
    	
    	// check if the other two categories are still present
    	assertThat(DBReader.getCategory(catB.getCid()).equals(catB), is(true));
    	assertThat(DBReader.getCategory(catAB.getCid()).equals(catAB), is(true));
    	
    	logger.debug("TEST: END api testcase 30 (database trigger)");
    }
    */
    
    @Test
    public void api40() {

    	logger.debug("TEST: START asyncIO testcase 40");
    	
    	int numberOfEZs = 100;

    	List<Einkaufszettel> ezsSync = new ArrayList<>(numberOfEZs); 
    	List<Einkaufszettel> ezsAsync = new ArrayList<>(numberOfEZs); 
    			
    	for (int i = 0; i < numberOfEZs; i++) {
    		ezsSync.add(TestUtils.genRandomEZ());
    		ezsAsync.add(TestUtils.genRandomEZ());
    	}
    	
    	long start, end, sum;
    
    	// sync write
    	start = System.currentTimeMillis();
    	sendSyncEZs(ezsAsync);
    	end = System.currentTimeMillis();
    	logger.debug("TEST: Adding {} EZs synchronously takes {} seconds",
    			numberOfEZs, (end - start) / 1000);
    
    	// async write
    	start = System.currentTimeMillis();
    	sendAsyncEZs(ezsSync);
    	end = System.currentTimeMillis();
    	logger.debug("TEST: Adding {} EZs asynchronously takes {} seconds",
    			numberOfEZs, (end - start) / 1000);
    	
    	logger.debug("TEST: END asyncIO testcase 40");
    }
    
    @Test
    public void api41() {
    	
    	int numberOfWorker = 50;
    	int numberOfOperations = 200;
    	long start, end;
    	long sum = 0;

    	logger.debug("TEST: START asyncIO testcase 41 ({} operations of {} parallel clients)", 
    			numberOfOperations * 4,numberOfWorker);
    	
    	// test behavior on a full database
    	/*
    	sendAsyncEZs(Stream.generate(TestUtils::genRandomEZ)
    			.limit(500)
    			.collect(Collectors.toList()));
    	 */
    	List<Einkaufszettel> ezList = Stream.generate(TestUtils::genRandomEZ)
    			.limit(numberOfOperations)
    			.collect(Collectors.toList());
    
		ExecutorService executor = Executors.newFixedThreadPool(numberOfWorker);
		CompletionService<Einkaufszettel> cse = new ExecutorCompletionService<>(executor);

		CrudOperation[] operationsList = {create, update, read, delete};
		
		for (CrudOperation crudOp : operationsList) {

			List<OperationCallable> tasks = new ArrayList<>(numberOfOperations);

			for (Einkaufszettel ez : ezList) {
				
				if (crudOp == update ) {
					ez.setItems(TestUtils.genItemList(Limits.MAX_ITEMS / 2)); // 64 items
					ez.incrementVersion();
				}
				
				OperationCallable task = new OperationCallable(crudOp, ez, 200);
				tasks.add(task);
			}

			// submit all tasks for the current operation to the executor service
			start = System.currentTimeMillis();
			tasks.forEach(t -> cse.submit(t));
			
			// wait for all tasks of the current operation to be ready and count them
			tasks.forEach(t -> {

				try {
					cse.take().isDone();
				} catch (InterruptedException e) {
					logger.error("TEST: api41 test (simple async benchmark) failed");

			}});
			
			end = System.currentTimeMillis();

			String debugMessage;
			if (crudOp == create)
				debugMessage = "TEST: creation of {} EZ takes {} sec";
			else if (crudOp == update)
				debugMessage = "TEST: updating of {} EZ takes {} sec";
			else if (crudOp == read)
				debugMessage = "TEST: reading of {} EZ takes {} sec";
			else
				debugMessage = "TEST: deletion of {} EZ takes {} sec";

			logger.debug(debugMessage, numberOfOperations,  (end - start) / 1000);
			// sum up all times for each operations
			sum += end - start;
		}

		end = System.currentTimeMillis();
    	logger.debug("TEST: simple benchmark of {} CRUD operations: {} seconds",
    			numberOfOperations * 4, sum / 1000);

		executor.shutdown();
    	logger.debug("TEST: END asyncIO testcase 41 ({} operations of {} parallel clients)", 
    			numberOfOperations * 4,numberOfWorker);
    }
    
    // lambdas
    CrudOperation  create = (a, b) -> sendEZ(a, b);
    CrudOperation  update = (a, b) -> sendEZ(a, b);
    CrudOperation  delete = (a, b) -> deleteEZ(a, b);
    CrudOperation  read = (a, b) -> retrieveEZ(a, b);

    // the interface the lamdbas use
    interface CrudOperation {
    	Einkaufszettel operate(Einkaufszettel a, int b);
    }

    // Callable for use as a task in CompletionService
	class OperationCallable implements Callable<Einkaufszettel> {

		private CrudOperation crudOperation;
		private Einkaufszettel ez;
		private int expectedReturnCode;
		
		OperationCallable(CrudOperation crudOperation, Einkaufszettel ez , int expReturnCode) {
			this.crudOperation = crudOperation;
			this.ez = ez;
			this.expectedReturnCode = expReturnCode;
		}
		
		@Override
		public Einkaufszettel call() throws Exception {
			
			return  crudOperation.operate(ez, expectedReturnCode); 
		}
	}
}