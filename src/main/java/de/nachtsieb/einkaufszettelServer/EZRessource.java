package de.nachtsieb.einkaufszettelServer;

import de.nachtsieb.einkaufszettelServer.dbService.DBReader;
import de.nachtsieb.einkaufszettelServer.dbService.DBWriter;
import de.nachtsieb.einkaufszettelServer.entities.Einkaufszettel;
import de.nachtsieb.einkaufszettelServer.exceptions.ResourceConflictException;
import de.nachtsieb.einkaufszettelServer.exceptions.ResourceInvalidException;
import de.nachtsieb.einkaufszettelServer.exceptions.ResourceNotFoundException;
import de.nachtsieb.einkaufszettelServer.interceptors.InputValidation;
import java.util.UUID;
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

  /**
   * RESTFul API end point for downloading a EZ from the server.
   *
   * @param eid - the requested eid
   */
  @GET
  @Path("{eid: " + UUID_REGEX + "}")
  @Produces(MediaType.APPLICATION_JSON)
  public Einkaufszettel getEZ(@PathParam("eid") String eid) {
    logger.debug("eid {} requested", eid);

    Einkaufszettel ez = DBReader.getEZ(UUID.fromString(eid));
    if (ez == null) {
      logger.debug("Requested eid {} not in database", eid);
      throw new ResourceNotFoundException("eid " + eid + " not found");
    }
    return ez;
  }

  /**
   * RESTFul API end point for creating new EZ instances on the server.
   *
   * @param newEZ the Einkaufszettel inside the body
   * @param eid - the eid under which the Einkaufszettel will be created must match the eid inside
   *     the Einkaufszettel
   */
  @Path("{eid: " + UUID_REGEX + "}")
  @Consumes(MediaType.APPLICATION_JSON)
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @InputValidation
  public Response saveEZ(final Einkaufszettel newEZ, @PathParam("eid") String eid) {
    logger.debug("New EZ with eid {} comes in ", newEZ.getEid());

    if (!eid.equalsIgnoreCase(newEZ.getEid().toString())) {
      logger.debug("Eid ({}) from request does not match eid from URL ({}). ", newEZ.getEid(), eid);
      throw new ResourceInvalidException("eid from request does not math eid from body");
    }

    // check if the new EZ exists in database
    Einkaufszettel oldEZ = DBReader.getEZ(UUID.fromString(eid));
    if (oldEZ == null) {
      logger.debug("Write new EZ with eid{} to database. ", newEZ.getEid());
      DBWriter.writeEZ(newEZ);
      return Response.ok().build();
    }

    // check if the EZ versions differ
    if (oldEZ.getVersion() == newEZ.getVersion()) {
      logger.debug("requested EZ ({}) and EZ in database are the same", newEZ.getEid());
      return Response.noContent().status(Response.Status.NOT_MODIFIED).build();
    } else if (oldEZ.getVersion() > newEZ.getVersion()) {
      logger.debug(
          "Versions from the requested EZ {} differ. remote: {} database {}",
          newEZ.getEid(),
          newEZ.getVersion(),
          oldEZ.getVersion());
      throw new ResourceConflictException(
          "version conflict the ez is older than the ez on the server");
    } else {
      logger.debug("Going to UPDATE the EZ with eid: {}", newEZ.getEid());
      DBWriter.updateEZ(newEZ);
      return Response.ok().build();
    }
  }

  /**
   * RESTful API end point for deleting an existing EZ
   *
   * @param eid - the eid which will be deleted
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
