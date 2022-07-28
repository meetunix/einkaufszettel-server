package de.nachtsieb.einkaufszettelServer.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class PayloadToLargeException extends WebApplicationException {

  /** Create an HTTP response with status code 413 . */
  public PayloadToLargeException() {
    super(Response.noContent().status(Status.REQUEST_ENTITY_TOO_LARGE).build());
  }

  /**
   * Create a HTTP 413 (Payload to large) exception.
   *
   * @param message the String that is the entity of the 413 response.
   */
  public PayloadToLargeException(String message) {
    super(
        Response.status(Status.REQUEST_ENTITY_TOO_LARGE)
            .entity(message)
            .type(MediaType.TEXT_PLAIN)
            .build());
  }
}
