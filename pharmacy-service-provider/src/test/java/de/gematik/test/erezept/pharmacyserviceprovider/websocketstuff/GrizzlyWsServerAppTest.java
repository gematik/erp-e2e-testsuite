/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.pharmacyserviceprovider.websocketstuff;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.pspwsclient.dataobjects.DeliveryOption;
import de.gematik.test.erezept.pspwsclient.dataobjects.PspMessage;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

class GrizzlyWsServerAppTest {

  @Test
  void shouldThrowExceptionWithoutASocekt() {
    GrizzlyWsServerApp grizzlyWsServerApp = new GrizzlyWsServerApp();
    assertThrows(
        WebApplicationException.class,
        () -> {
          grizzlyWsServerApp.send("123", "123");
        });
  }

  @Test
  void shouldThrowExceptionWithoutTelematikId() {
    GrizzlyWsServerApp grizzlyWsServerApp = new GrizzlyWsServerApp();
    var byteBody = "123".getBytes();
    PspMessage pspMessage = PspMessage.create(DeliveryOption.ON_PREMISE, "123", "123", byteBody);
    assertThrows(
        WebApplicationException.class,
        () -> {
          grizzlyWsServerApp.send(null, pspMessage);
        });
  }
}
