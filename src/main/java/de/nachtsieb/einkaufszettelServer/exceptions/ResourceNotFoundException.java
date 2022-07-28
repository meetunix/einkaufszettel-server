package de.nachtsieb.einkaufszettelServer.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ResourceNotFoundException extends WebApplicationException {

  /** Create an HTTP response with status code 404 . */
  public ResourceNotFoundException() {
    super(Response.noContent().status(Status.NOT_FOUND).build());
  }

  /**
   * Create a HTTP 404 (Not Found) exception.
   *
   * @param message the String that is the entity of the 404 response.
   */
  public ResourceNotFoundException(String message) {
    super(
        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(message)
            .type(MediaType.TEXT_PLAIN)
            .build());
  }
}
