/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.primsys.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.idp.client.IdpClientRuntimeException;
import de.gematik.test.erezept.primsys.data.error.ErrorDto;
import de.gematik.test.erezept.primsys.data.error.ErrorType;
import lombok.val;
import org.junit.jupiter.api.Test;

class IdpClientExceptionMapperTest {

  @Test
  void shouldMapToResponse() {
    val mapper = new IdpClientExceptionMapper();
    val exception = new IdpClientRuntimeException("test exception");
    try (val response = mapper.toResponse(exception)) {
      val entity = response.getEntity();
      assertEquals(412, response.getStatus());
      assertEquals(ErrorDto.class, entity.getClass());
      val dto = (ErrorDto) entity;
      assertEquals(ErrorType.INTERNAL, dto.getType());
      assertTrue(dto.getMessage().contains("test exception"));
    }
  }
}
