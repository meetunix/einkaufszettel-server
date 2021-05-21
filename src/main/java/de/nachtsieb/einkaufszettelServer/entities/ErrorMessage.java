package de.nachtsieb.einkaufszettelServer.entities;

import javax.ws.rs.WebApplicationException;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@XmlRootElement
public class ErrorMessage {

  private String errorCode;
  private String message;

  public ErrorMessage() {}

  public ErrorMessage(String errCode, String errMessage) {
    this.errorCode = errCode;
    this.message = errMessage;
  }

  /**
   * Maps an instance of ErrorMessage to a json string.
   * 
   * @param errMessage an instance of the class ErrorMessage
   * @return a representation of the class ErrorMessage as a json string
   */
  public static String getJsonString(ErrorMessage errMessage) {
    ObjectMapper mapper = new ObjectMapper();

    try {

      return mapper.writeValueAsString(errMessage);

    } catch (JsonProcessingException e) {
      // TODO LOGGER
      throw new WebApplicationException("500");
    }
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
