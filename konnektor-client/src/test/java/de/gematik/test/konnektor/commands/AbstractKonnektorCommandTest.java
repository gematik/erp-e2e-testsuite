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

package de.gematik.test.konnektor.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.konnektor.soap.ServicePortProvider;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;

class AbstractKonnektorCommandTest {

  @SneakyThrows
  @Test
  void shouldHandleSoapActionExceptions() {
    val ctx = new ContextType();
    ctx.setClientSystemId("cs1");
    ctx.setMandantId("m1");
    ctx.setUserId("u1");
    ctx.setWorkplaceId("w1");

    val mockProvider = mock(ServicePortProvider.class);
    val mockEventService = mock(EventServicePortType.class);
    when(mockProvider.getEventService()).thenReturn(mockEventService);
    when(mockEventService.getCards(any())).thenThrow(new RuntimeException("TEST"));

    val cmd = new TestCommand();
    assertThrows(RuntimeException.class, () -> cmd.execute(ctx, mockProvider));
    // Note: see AbstractKonnektorCommand#executeAction
    //    assertThrows(SOAPRequestException.class, () -> cmd.execute(ctx, mockProvider));
  }

  private static class TestCommand extends AbstractKonnektorCommand<Boolean> {
    @SneakyThrows
    @Override
    public Boolean execute(ContextType ctx, ServicePortProvider serviceProvider) {
      val eventService = serviceProvider.getEventService();

      this.executeAction(() -> eventService.getCards(null));
      return true;
    }
  }
}
