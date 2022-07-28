package de.nachtsieb.einkaufszettelServer.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class DatabaseWriteException extends WebApplicationException {

  public DatabaseWriteException() {
    super(Response.noContent().status(Response.Status.INTERNAL_SERVER_ERROR).build());
  }

  public DatabaseWriteException(String message) {
    super(
        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(message)
            .type(MediaType.TEXT_PLAIN)
            .build());
  }
}
