package de.gematik.test.erezept.primsys.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonParseException;
import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import de.gematik.test.erezept.primsys.data.error.ErrorType;
import lombok.val;
import org.junit.jupiter.api.Test;

class JacksonExceptionMapperTest {

  @Test
  void shouldMapToResponse() {
    val mapper = new JacksonExceptionMapper();
    val exception = new JsonParseException("test error");
    try (val response = mapper.toResponse(exception)) {
      val entity = response.getEntity();
      assertEquals(400, response.getStatus());
      assertEquals(ErrorDto.class, entity.getClass());
      val dto = (ErrorDto) entity;
      assertEquals(ErrorType.INTERNAL, dto.getType());
      assertTrue(dto.getMessage().contains("Invalid JSON Body"));
      assertTrue(dto.getMessage().contains("test error"));
    }
  }
}
