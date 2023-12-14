package de.gematik.test.erezept.primsys.exceptions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import jakarta.annotation.Priority;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.val;

@Provider
@Priority(100)
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
  
  @Override
  public Response toResponse(Throwable exception) {
    val message = format("Unexpected Error: {0}", exception.getMessage());
    return ErrorResponseBuilder.createInternalErrorException(500, message).getResponse();
  }
}
