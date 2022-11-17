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

package de.gematik.test.erezept.fhir.resources.erp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

public class ErxCommunicationBundleTest {

  private final String BASE_PATH = "fhir/valid/erp/1.1.1/";

  private FhirParser parser;

  @Before
  public void setUp() {
    this.parser = new FhirParser();
  }

  @Test
  public void shouldEncodeSingleCommunicationBundle() {
    val fileName = "CommunicationReplyBundle_01.json";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val communicationBundle = parser.decode(ErxCommunicationBundle.class, content);
    assertNotNull("Valid ErxCommunicationBundle must be parseable", communicationBundle);

    assertEquals(1, communicationBundle.getCommunications().size());

    val com = communicationBundle.getCommunications().get(0);
    assertEquals(CommunicationType.REPLY, com.getType());

    assertEquals(1, communicationBundle.getCommunicationsFromSender("606358757").size());
    assertEquals(1, communicationBundle.getCommunicationsForReceiver("X234567890").size());
  }
}
