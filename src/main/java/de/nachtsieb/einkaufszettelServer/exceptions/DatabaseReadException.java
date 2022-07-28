package de.nachtsieb.einkaufszettelServer.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class DatabaseReadException extends WebApplicationException {

  public DatabaseReadException() {
    super(Response.noContent().status(Response.Status.INTERNAL_SERVER_ERROR).build());
  }

  public DatabaseReadException(String message) {
    super(
        Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(message)
            .type(MediaType.TEXT_PLAIN)
            .build());
  }
}
