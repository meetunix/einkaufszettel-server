package de.nachtsieb.einkaufszettelServer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NameBinding;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.nachtsieb.einkaufszettelServer.dbService.DBReader;
import de.nachtsieb.einkaufszettelServer.dbService.DBWriter;
import de.nachtsieb.einkaufszettelServer.entities.ConflictMessage;
import de.nachtsieb.einkaufszettelServer.entities.Einkaufszettel;
import de.nachtsieb.einkaufszettelServer.entities.ErrorMessage;
import de.nachtsieb.einkaufszettelServer.exceptions.EZException;
import de.nachtsieb.einkaufszettelServer.jsonValidation.JsonValidator;

/*
 * TODO: 	jersey manual chapter 12 - URIs and Links (App needs to share link to a Einkaufszettel)
 */


//@Compress annotation is the name binding annotation
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@interface Compress {}

@Path("/ez/")
public class EZRessource {
	
    private static Logger logger = LogManager.getLogger(EZRessource.class);

	public final String UUID_REGEX = 
			"[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

	@Inject
	JsonValidator jsonValidator;
	
	/**
	 * RESTFul API endpoint for downloading a EZ from the server. 
	 * 
	 * @param eid
	 */
	@GET
	@Path("{eid: " + UUID_REGEX + "}")
	@Compress
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEZ(@PathParam("eid") String eid){
		
		ObjectMapper mapper = new ObjectMapper();

		try {

			logger.debug("eid {} requested", eid);

			Einkaufszettel ez = DBReader.getEZ(UUID.fromString(eid));

			if (ez == null ) {
				logger.debug("Requested eid {} not in database", eid);
				return Response.noContent().status(Response.Status.NOT_FOUND).build();
			} else {
				String jsonResponse = mapper.writeValueAsString(ez);
				return Response.ok(jsonResponse).build();
			}

		} catch (JsonProcessingException e) {
			logger.error("Unable to (de)serialize. Exception was thrown: {}", e.toString());
			throw new EZException(ErrorMessage.getJsonString(
					new ErrorMessage("E_JSON", "unable to perform serialization")));
		}
	}
	
	
	/**
	 * 
	 * RESTFul API endpoint for creating new EZ instances on the server. 
	 * 
	 * @param putString
	 * @param eid
	 */
	@PUT
	@Consumes({MediaType.APPLICATION_JSON})
	@Path("{eid: " + UUID_REGEX + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveEZ(String putString, @PathParam("eid") String eid) {
		
		ObjectMapper mapper = new ObjectMapper();
			
		try {
			
			JsonNode jsonNode = mapper.readTree(putString);
			
			// at first check if a valid EZ was sent
			if(! jsonValidator.isValid(jsonNode)) {

				ErrorMessage err = new ErrorMessage("E_INVALID_EZ", "The provided EZ was invalid");
				String jsonErrorMessage = mapper.writeValueAsString(err);

				logger.debug("Received invalid EZ: {} . "
						+ "Sending message to client: {}", eid, jsonErrorMessage);
				
				return Response.status(Response.Status.BAD_REQUEST)
						.entity(jsonErrorMessage)
						.type(MediaType.APPLICATION_JSON)
						.build();
			} 
			
			// convert to object and check if eid from url and eid from object matches
			Einkaufszettel newEZ = mapper.readValue(putString, Einkaufszettel.class);

			if(! eid.toLowerCase().equals(newEZ.getEid().toString().toLowerCase()) ) {

				ErrorMessage err = new ErrorMessage("E_EID_NOT_MATCH",
						"The eid from the received EZ does not match the eid from the URL");
				String jsonErrorMessage = mapper.writeValueAsString(err);

				logger.debug("Eid ({}) from request does not match eid from URL ({}). "
						+ "Sending message to client: {}", newEZ.getEid(), eid, jsonErrorMessage);

				return Response.status(Response.Status.BAD_REQUEST)
						.entity(jsonErrorMessage)
						.type(MediaType.APPLICATION_JSON)
						.build();
			}
			
			// check if the new EZ exists in database
			Einkaufszettel oldEZ = DBReader.getEZ(UUID.fromString(eid));
			if (oldEZ == null ) {

				DBWriter.writeEZ(newEZ);

				return Response.ok().build();
			}

			// check if the EZ versions differ
			if(oldEZ.getVersion() == newEZ.getVersion()) {

				logger.debug("requested EZ ({}) and EZ in database are the same", newEZ.getEid());

				return Response.noContent().status(Response.Status.NOT_MODIFIED).build();				
				
			} else if(oldEZ.getVersion() > newEZ.getVersion()) {

				ConflictMessage cm = new ConflictMessage();
				cm.setLocalVersion(newEZ.getVersion());
				cm.setServerVersion(oldEZ.getVersion());
				String jsonConflictMessage = mapper.writeValueAsString(cm);
				
				logger.debug("Versions from the requested EZ {} differ. "
						+ "Sending message to client: {}", newEZ.getEid(), jsonConflictMessage);

				return Response.status(Response.Status.CONFLICT)
						.entity(jsonConflictMessage)
						.type(MediaType.APPLICATION_JSON)
						.build();
			} else {
				// update an existing EZ in database

				logger.debug("Going to UPDATE the EZ with eid: {}", newEZ.getEid());
				DBWriter.updateEZ(newEZ);
				logger.info("EZ ({}) updated successfully", newEZ.getEid());

				return Response.ok().build();
			}
				
		} catch (JsonProcessingException e) {
			logger.error("Unable to (de)serialize. Exception was thrown: {}", e.toString());
			throw new EZException(ErrorMessage.getJsonString(
					new ErrorMessage("E_JSON", "unable to perform serialization")));
		}
	}
	
	/**
	 * 
	 * RESTful API endpoint for deleting an existing EZ
	 * 
	 * @param eid
	 */

	@DELETE
	@Path("{eid: " + UUID_REGEX + "}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteEZ(@PathParam("eid") String eid) {
		
		logger.debug("DELETION of EZ {} requested", eid);
		

		// check if the new EZ exists in database
		Einkaufszettel ez = DBReader.getEZ(UUID.fromString(eid));
		if (ez == null ) {

			logger.debug("Requested eid {} not in database", eid);
			return Response.noContent().status(Response.Status.NOT_FOUND).build();
			
		} else {

			DBWriter.deleteEZ(ez);
			return Response.ok().build();
			
		}
		
	}
}	

@Compress
class GZIPWriterInterceptor implements WriterInterceptor {
	 
    @Override
    public void aroundWriteTo(WriterInterceptorContext context)
                    throws IOException, WebApplicationException {
    	
    	MultivaluedMap<String,Object> headers = context.getHeaders();
    	headers.add("Content-Encoding", "gzip");
    	
        final OutputStream outputStream = context.getOutputStream();
        context.setOutputStream(new GZIPOutputStream(outputStream));
        context.proceed();
    }
}