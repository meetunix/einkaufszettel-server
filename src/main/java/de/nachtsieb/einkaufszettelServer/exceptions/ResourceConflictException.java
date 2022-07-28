package de.nachtsieb.einkaufszettelServer.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ResourceConflictException extends WebApplicationException {

  /** Create an HTTP response with status code 409 . */
  public ResourceConflictException() {
    super(Response.noContent().status(Status.CONFLICT).build());
  }

  /**
   * Create a HTTP 409 (Conflict) exception.
   *
   * @param message the String that is the entity of the 409 response.
   */
  public ResourceConflictException(String message) {
    super(Response.status(Status.CONFLICT).entity(message).type(MediaType.TEXT_PLAIN).build());
  }
}
