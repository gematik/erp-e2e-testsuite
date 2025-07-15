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
import static org.mockito.Mockito.*;

import lombok.val;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.junit.jupiter.api.Test;

class PrimSysErrorPageGeneratorTest {

  @Test
  void shouldHideInternalInformation() {
    val response = mock(Response.class);
    val request = mock(Request.class);
    when(request.getResponse()).thenReturn(response);

    val epg = new PrimSysErrorPageGenerator();
    val msg = epg.generate(request, 500, "internal_info", null, null);

    // make sure no internal information is leaking out
    assertFalse(msg.contains("500"));
    assertFalse(msg.contains("internal_info"));
    assertTrue(msg.contains("Bad Request"));

    // make sure the response was manipulated correctly
    verify(response, times(1)).setStatus(400);
    verify(response, times(1)).setContentType("application/json");
  }
}
