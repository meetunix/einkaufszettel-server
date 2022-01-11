package de.nachtsieb.einkaufszettelServer.jsonValidation;

import com.fasterxml.jackson.databind.JsonNode;

public interface JsonValidator {


  boolean isValid(JsonNode jsonNode);

}
