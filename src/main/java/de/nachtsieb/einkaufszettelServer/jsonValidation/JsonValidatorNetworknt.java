package de.nachtsieb.einkaufszettelServer.jsonValidation;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.InputStream;
import java.util.Set;
import java.util.StringJoiner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonValidatorNetworknt implements JsonValidator {

  private static final Logger logger = LogManager.getLogger(JsonValidator.class);

  private static final String EZ_SCHEMA_FILE = "ezschema.json";

  private final JsonSchema jsonSchema;

  public JsonValidatorNetworknt() {
    this.jsonSchema = getJsonSchemaFromClasspath();
  }

  private JsonSchema getJsonSchemaFromClasspath() {
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
    InputStream is =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream(JsonValidatorNetworknt.EZ_SCHEMA_FILE);
    return factory.getSchema(is);
  }

  @Override
  public synchronized boolean isValid(JsonNode jsonNode) {
    Set<ValidationMessage> validationMessages = jsonSchema.validate(jsonNode);
    if (logger.isDebugEnabled() && validationMessages.size() != 0) {
      StringJoiner sj = new StringJoiner("\n");
      for (ValidationMessage vm : validationMessages) sj.add(vm.getMessage());
      logger.debug("Validation errors:\n {}", sj);
    }
    return validationMessages.size() == 0;
  }
}
