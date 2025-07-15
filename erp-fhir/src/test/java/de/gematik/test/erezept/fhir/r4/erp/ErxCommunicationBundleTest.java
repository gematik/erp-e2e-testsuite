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

package de.gematik.test.erezept.fhir.r4.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErxCommunicationBundleTest extends ErpFhirParsingTest {

  private final String BASE_PATH = "fhir/valid/erp/1.4.0/communicationbundle/";

  @Test
  void shouldEncodeSingleCommunicationBundle() {
    val fileName = "CommunicationBundle_01.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val communicationBundle = parser.decode(ErxCommunicationBundle.class, content);
    assertNotNull(communicationBundle, "Valid ErxCommunicationBundle must be parseable");

    assertEquals(38, communicationBundle.getCommunications().size());
    assertEquals(18, communicationBundle.getCommunicationsFromSender("X110614233").size());
    assertEquals(20, communicationBundle.getCommunicationsForReceiver("X110614233").size());

    assertEquals(
        4, communicationBundle.getCommunicationsFromSender("5-2-KH-APO-Waldesrand-01").size());
    assertEquals(
        4, communicationBundle.getCommunicationsForReceiver("5-2-KH-APO-Waldesrand-01").size());

    val com = communicationBundle.getCommunications().get(17);
    assertEquals(CommunicationType.DISP_REQ, com.getType());
    assertTrue(com.getMessage().contains("nope, we´ll get SMOK!"));
  }
}
