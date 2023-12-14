package de.gematik.test.erezept.primsys.exceptions;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.core.JacksonException;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.val;

@Provider
public class JacksonExceptionMapper implements ExceptionMapper<JacksonException> {
  
  @Override
  public Response toResponse(JacksonException exception) {
    val message = format("Invalid JSON Body: {0}", exception.getMessage());
    return ErrorResponseBuilder.createInternalErrorException(400, message).getResponse();
  }
}
