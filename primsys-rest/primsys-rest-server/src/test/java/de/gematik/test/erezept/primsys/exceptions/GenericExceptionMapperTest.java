/*
 * Copyright 2023 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.primsys.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import de.gematik.test.erezept.primsys.data.error.ErrorType;
import lombok.val;
import org.junit.jupiter.api.Test;

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
