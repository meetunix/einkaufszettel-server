package de.nachtsieb.einkaufszettelServer.interceptors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.nachtsieb.einkaufszettelServer.exceptions.PayloadToLargeException;
import de.nachtsieb.einkaufszettelServer.exceptions.ResourceInvalidException;
import de.nachtsieb.einkaufszettelServer.jsonValidation.JsonValidator;
import de.nachtsieb.einkaufszettelServer.jsonValidation.JsonValidatorNetworknt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Priority(Integer.MAX_VALUE)
@InputValidation
public class ReaderValidationInterceptor implements ReaderInterceptor {

  private static final Logger logger = LogManager.getLogger(ReaderValidationInterceptor.class);
  ObjectMapper mapper = new ObjectMapper();
  JsonValidator validator = new JsonValidatorNetworknt();
  private HttpHeaders context;

  public ReaderValidationInterceptor() {
    super();
  }

  public ReaderValidationInterceptor(@Context HttpHeaders context) {
    this.context = context;
  }

  @Override
  public Object aroundReadFrom(ReaderInterceptorContext readerInterceptorContext)
      throws IOException, WebApplicationException {

    final int lengthLimit = 2 * 1024 * 1024; // 2 MiB
    if (context.getLength() > lengthLimit) {
      throw new PayloadToLargeException(
          "Payload exceeds length limit of " + lengthLimit + " MiB\n");
    }

    // Copy the input stream.
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    readerInterceptorContext.getInputStream().transferTo(baos);
    // one copy os for json validation
    String json = baos.toString(StandardCharsets.UTF_8);
    JsonNode node = mapper.readTree(json);

    if (!validator.isValid(node)) {
      logger.debug("Received invalid Einkaufszettel.");
      throw new ResourceInvalidException(
          mapper.writeValueAsString("The provided ressource is not a valid Einkaufszettel\n"));
    }
    // the other copy is injected back to the pipe
    readerInterceptorContext.setInputStream(new ByteArrayInputStream(baos.toByteArray()));
    logger.debug("Einkaufszettel was validated");
    return readerInterceptorContext.proceed();
  }
}
