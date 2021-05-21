package de.nachtsieb.einkaufszettelServer.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class EZDBException extends WebApplicationException {

  public EZDBException() {
    super(Response.noContent().status(Response.Status.INTERNAL_SERVER_ERROR).build());
  }

  public EZDBException(String message) {
    super(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message)
        .type(MediaType.APPLICATION_JSON).build());
  }
}
