/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fhir.r4.erp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxCommunicationBundleTest extends ErpFhirParsingTest {

  @Test
  void shouldEncodeSingleCommunicationBundle() {
    String path =
        "fhir/valid/erp/1.5.0/communicationbundle/Bundle-3102c63c-1f98-41b7-96e2-4e4b7dc2a8b9.xml";

    val content = ResourceLoader.readFileFromResource(path);
    val communicationBundle = parser.decode(ErxCommunicationBundle.class, content);

    assertNotNull(communicationBundle, "Valid ErxCommunicationBundle must be parseable");
    assertEquals(6, communicationBundle.getCommunications().size());
    assertEquals(0, communicationBundle.getCommunicationsFromSender("X110614233").size());
    assertEquals(6, communicationBundle.getCommunicationsForReceiver("X110614233").size());
    assertEquals(
        6, communicationBundle.getCommunicationsFromSender("5-2-KH-APO-Waldesrand-01").size());
    assertEquals(
        0, communicationBundle.getCommunicationsForReceiver("5-2-KH-APO-Waldesrand-01").size());
    assertDoesNotThrow(communicationBundle::toString);

    val com = communicationBundle.getCommunications().get(2);
    assertEquals(CommunicationType.REPLY, com.getType());
    assertTrue(com.getMessage().contains("how much is the fish"));
  }
}
