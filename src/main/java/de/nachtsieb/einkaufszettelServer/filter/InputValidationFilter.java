package de.nachtsieb.einkaufszettelServer.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.nachtsieb.einkaufszettelServer.jsonValidation.JsonValidator;
import de.nachtsieb.einkaufszettelServer.jsonValidation.JsonValidatorNetworknt;
import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

@InputValidation
public class InputValidationFilter implements ContainerRequestFilter {

  ObjectMapper mapper = new ObjectMapper();
  JsonValidator validator = new JsonValidatorNetworknt();

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    System.out.println("FILTER (Method): " + requestContext.getMethod());
    System.out.println("FILTER (URI-Headers): " + requestContext.getHeaders());
    System.out.println("FILTER (addr): " + this);
  }
}
