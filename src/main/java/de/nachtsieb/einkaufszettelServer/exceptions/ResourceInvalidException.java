package de.nachtsieb.einkaufszettelServer.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ResourceInvalidException extends WebApplicationException {

  /** Create an HTTP response with status code 400 . */
  public ResourceInvalidException() {
    super(Response.noContent().status(Status.BAD_REQUEST).build());
  }

  /**
   * Create a HTTP 400 (Bad Request) exception.
   *
   * @param message the String that is the entity of the 400 response.
   */
  public ResourceInvalidException(String message) {
    super(Response.status(Status.BAD_REQUEST).entity(message).type(MediaType.TEXT_PLAIN).build());
  }
}
