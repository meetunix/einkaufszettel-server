package de.nachtsieb.einkaufszettelServer.jsonValidation;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import de.nachtsieb.einkaufszettelServer.entities.ErrorMessage;
import de.nachtsieb.einkaufszettelServer.exceptions.EZException;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class validates json files against a json schema.
 * <a href="https://tools.ietf.org/html/draft-fge-json-schema-validation-00">draft-04</a> is used.
 */
public class JsonValidatorJsonTools implements JsonValidator {

  private static final Logger logger = LogManager.getLogger(JsonValidatorJsonTools.class);
  private static final String EZ_SCHEMA_FILE = "/ezschema.json";

  private JsonNode ezSchemaNode;
  private JsonSchema ezSchema;
  private JsonSchemaFactory factory;


  public JsonValidatorJsonTools() {

    try {

      this.ezSchemaNode = JsonLoader.fromResource(EZ_SCHEMA_FILE);

      this.factory = JsonSchemaFactory.byDefault(); // json-schema draft v4
      this.ezSchema = factory.getJsonSchema(ezSchemaNode);

    } catch (ProcessingException e) {
      logger.error("unable to generate json validator");
      throw new EZException(ErrorMessage
          .getJsonString(new ErrorMessage("E_VALIDATOR", "unable to create json validator")));
    } catch (IOException e) {
      logger.error("unable to read ez schema file");
      throw new EZException(ErrorMessage
          .getJsonString(new ErrorMessage("E_VALIDATOR", "unable to read ez schema file")));
    }

    logger.debug("a JsonValidator object was generated");
  }

  /**
   * Returns a ProcessingReport object for debugging purposes.
   *
   * @param json
   * @return ProcessingReport
   * @throws ProcessingException
   */
  public ProcessingReport getReport(JsonNode json) throws ProcessingException {

    return ezSchema.validate(json);
  }

  /**
   * Returns true if a valid document is passed.
   *
   * @param json - the json instance to validate against previously loaded schema
   * @return true if the json document is valid
   * @throws ProcessingException
   */
  @Override
  public synchronized boolean isValid(JsonNode json) {

    logger.debug("validation requested");

    try {

      return ezSchema.validInstance(json);

    } catch (ProcessingException e) {
      logger.error("unable to perform json validation");
      throw new EZException(ErrorMessage
          .getJsonString(new ErrorMessage("E_VALIDATOR", "unable to create json validator")));
    }
  }
}
