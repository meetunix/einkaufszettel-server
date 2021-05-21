package de.nachtsieb.einkaufszettelServer.entities;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@XmlRootElement
public class ConflictMessage {

  String message;
  int localVersion;
  int serverVersion;

  public ConflictMessage() {
    this.message = "conflicting versions";
  }

  public static String getJsonString(ConflictMessage conflictMessage) {

    try {
      return new ObjectMapper().writeValueAsString(conflictMessage);
    } catch (JsonProcessingException e) {
      // TODO LOGGER
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getLocalVersion() {
    return localVersion;
  }

  public void setLocalVersion(int localVersion) {
    this.localVersion = localVersion;
  }

  public int getServerVersion() {
    return serverVersion;
  }

  public void setServerVersion(int serverVersion) {
    this.serverVersion = serverVersion;
  }
}
