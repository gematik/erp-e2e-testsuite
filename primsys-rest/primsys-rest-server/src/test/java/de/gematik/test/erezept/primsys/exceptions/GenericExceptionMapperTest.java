package de.gematik.test.erezept.primsys.exceptions;

import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import de.gematik.test.erezept.primsys.data.error.ErrorType;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GenericExceptionMapperTest {

  @Test
  void shouldMapToResponse() {
    val mapper = new GenericExceptionMapper();
    val exception = new NullPointerException("test exception");
    try (val response = mapper.toResponse(exception)) {
      val entity = response.getEntity();
      assertEquals(500, response.getStatus());
      assertEquals(ErrorDto.class, entity.getClass());
      val dto = (ErrorDto) entity;
      assertEquals(ErrorType.INTERNAL, dto.getType());
      assertTrue(dto.getMessage().contains("Unexpected Error"));
      assertTrue(dto.getMessage().contains("test exception"));
    }
  }
}
