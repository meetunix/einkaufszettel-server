package de.nachtsieb.einkaufszettelServer.jsonValidation;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.InputStream;
import java.util.Set;

public class JsonValidatorNetworknt implements JsonValidator {

  private static final String EZ_SCHEMA_FILE = "ezschema.json";

  private final JsonSchema jsonSchema;

  public JsonValidatorNetworknt() {
    this.jsonSchema = getJsonSchemaFromClasspath();
  }

  private JsonSchema getJsonSchemaFromClasspath() {
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
    InputStream is = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream(JsonValidatorNetworknt.EZ_SCHEMA_FILE);
    return factory.getSchema(is);
  }

  @Override
  public synchronized boolean isValid(JsonNode jsonNode) {
    Set<ValidationMessage> validationMessages = jsonSchema.validate(jsonNode);
    return validationMessages.size() == 0;
  }
}
