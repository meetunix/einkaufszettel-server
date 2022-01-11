package de.nachtsieb.einkaufszettelServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.nachtsieb.einkaufszettelServer.dbService.DBReader;
import de.nachtsieb.einkaufszettelServer.dbService.DBWriter;
import de.nachtsieb.einkaufszettelServer.entities.ConflictMessage;
import de.nachtsieb.einkaufszettelServer.entities.Einkaufszettel;
import de.nachtsieb.einkaufszettelServer.entities.ErrorMessage;
import de.nachtsieb.einkaufszettelServer.exceptions.EZException;
import de.nachtsieb.einkaufszettelServer.jsonValidation.JsonValidator;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/ez/")
public class EZRessource {

  private static final Logger logger = LogManager.getLogger(EZRessource.class);

  public final String UUID_REGEX =
      "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

  @Inject
  JsonValidator jsonValidator;

  /**
   * RESTFul API end point for downloading a EZ from the server.
   *
   * @param eid
   */
  @GET
  @Path("{eid: " + UUID_REGEX + "}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getEZ(@PathParam("eid") String eid) {

    ObjectMapper mapper = new ObjectMapper();

    try {

      logger.debug("eid {} requested", eid);

      Einkaufszettel ez = DBReader.getEZ(UUID.fromString(eid));

      if (ez == null) {
        logger.debug("Requested eid {} not in database", eid);
        return Response.noContent().status(Response.Status.NOT_FOUND).build();
      } else {
        String jsonResponse = mapper.writeValueAsString(ez);
        return Response.ok(jsonResponse).build();
      }

    } catch (JsonProcessingException e) {
      logger.error("Unable to (de)serialize. Exception was thrown: {}", e.getMessage());
      throw new EZException(ErrorMessage
          .getJsonString(new ErrorMessage("E_JSON", "unable to perform serialization")));
    }
  }


  /**
   * RESTFul API end point for creating new EZ instances on the server.
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
      if (!jsonValidator.isValid(jsonNode)) {

        ErrorMessage err = new ErrorMessage("E_INVALID_EZ", "The provided EZ was invalid");
        String jsonErrorMessage = mapper.writeValueAsString(err);

        logger.debug("Received invalid EZ: {} . " + "Sending message to client: {}", eid,
            jsonErrorMessage);

        return Response.status(Response.Status.BAD_REQUEST).entity(jsonErrorMessage)
            .type(MediaType.APPLICATION_JSON).build();
      }

      // convert to object and check if eid from url and eid from object matches
      Einkaufszettel newEZ = mapper.readValue(putString, Einkaufszettel.class);

      if (!eid.toLowerCase().equals(newEZ.getEid().toString().toLowerCase())) {

        ErrorMessage err = new ErrorMessage("E_EID_NOT_MATCH",
            "The eid from the received EZ does not match the eid from the URL");
        String jsonErrorMessage = mapper.writeValueAsString(err);

        logger.debug("Eid ({}) from request does not match eid from URL ({}). "
            + "Sending message to client: {}", newEZ.getEid(), eid, jsonErrorMessage);

        return Response.status(Response.Status.BAD_REQUEST).entity(jsonErrorMessage)
            .type(MediaType.APPLICATION_JSON).build();
      }

      // check if the new EZ exists in database
      Einkaufszettel oldEZ = DBReader.getEZ(UUID.fromString(eid));
      if (oldEZ == null) {

        DBWriter.writeEZ(newEZ);

        return Response.ok().build();
      }

      // check if the EZ versions differ
      if (oldEZ.getVersion() == newEZ.getVersion()) {

        logger.debug("requested EZ ({}) and EZ in database are the same", newEZ.getEid());

        return Response.noContent().status(Response.Status.NOT_MODIFIED).build();

      } else if (oldEZ.getVersion() > newEZ.getVersion()) {

        ConflictMessage cm = new ConflictMessage();
        cm.setLocalVersion(newEZ.getVersion());
        cm.setServerVersion(oldEZ.getVersion());
        String jsonConflictMessage = mapper.writeValueAsString(cm);

        logger.debug("Versions from the requested EZ {} differ. " + "Sending message to client: {}",
            newEZ.getEid(), jsonConflictMessage);

        return Response.status(Response.Status.CONFLICT).entity(jsonConflictMessage)
            .type(MediaType.APPLICATION_JSON).build();
      } else {
        // update an existing EZ in database

        logger.debug("Going to UPDATE the EZ with eid: {}", newEZ.getEid());
        DBWriter.updateEZ(newEZ);
        // logger.info("EZ ({}) updated successfully", newEZ.getEid());

        return Response.ok().build();
      }

    } catch (JsonProcessingException e) {
      logger.error("Unable to (de)serialize. Exception was thrown: {}", e.getMessage());
      throw new EZException(ErrorMessage
          .getJsonString(new ErrorMessage("E_JSON", "unable to perform serialization")));
    }
  }

  /**
   * RESTful API end point for deleting an existing EZ
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
    if (ez == null) {

      logger.debug("Requested eid {} not in database", eid);
      return Response.noContent().status(Response.Status.NOT_FOUND).build();

    } else {

      DBWriter.deleteEZ(ez);
      return Response.ok().build();

    }

  }
}
