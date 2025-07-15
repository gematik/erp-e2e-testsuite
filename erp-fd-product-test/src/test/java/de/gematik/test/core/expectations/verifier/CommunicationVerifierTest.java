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

package de.gematik.test.core.expectations.verifier;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class CommunicationVerifierTest extends ErpFhirParsingTest {
  private static final String COM_PATH =
      "fhir/valid/erp/1.4.0/communication/8ca3c379-ac86-470f-bc12-178c9008f5c9.json";

  private static final String COM_PATH_WITHOUT_RECEIVED =
      "fhir/invalid/erp/1.4.0/communication/withReceivedElem.xml";

  private static ErxCommunication getDecodedCommunication(String path) {
    return parser.decode(ErxCommunication.class, ResourceLoader.readFileFromResource(path));
  }

  @BeforeEach
  void init() {
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldNotInstantiateUtilityClass() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(CommunicationVerifier.class));
  }

  @Test
  void shouldFindId() {
    val validComBundle = getDecodedCommunication(COM_PATH);
    val step =
        CommunicationVerifier.matchId("8ca3c379-ac86-470f-bc12-178c9008f5c9", ErpAfos.A_19520);
    step.apply(validComBundle);
  }

  @Test
  void shouldThrowWhileMissingId() {
    val validComBundle = getDecodedCommunication(COM_PATH);
    val step =
        CommunicationVerifier.matchId("01ebbe1d-b718-ec48-f02d-73a051be0000", ErpAfos.A_19520);
    assertThrows(AssertionError.class, () -> step.apply(validComBundle));
  }

  @Test
  void shouldNotFindId() {
    val validComBundle = getDecodedCommunication(COM_PATH);
    val step =
        CommunicationVerifier.doesNotMatchThatId(
            "8ca3c379-ac86-470f-bc12-178c9000000", ErpAfos.A_19520);
    step.apply(validComBundle);
  }

  @Test
  void shouldThrowWhileFindId() {
    val validComBundle = getDecodedCommunication(COM_PATH);
    val step =
        CommunicationVerifier.doesNotMatchThatId(
            "8ca3c379-ac86-470f-bc12-178c9008f5c9", ErpAfos.A_19520);
    assertThrows(AssertionError.class, () -> step.apply(validComBundle));
  }

  @Test
  void shouldNotHaveReceivedEntry() {
    val validComBundle = getDecodedCommunication(COM_PATH);
    val step = CommunicationVerifier.emptyReceivedElement();
    step.apply(validComBundle);
  }

  @Test
  void shouldThrowWhileFindReceivedEntry() {
    val validComBundle = getDecodedCommunication(COM_PATH_WITHOUT_RECEIVED);
    val step = CommunicationVerifier.emptyReceivedElement();
    assertThrows(AssertionError.class, () -> step.apply(validComBundle));
  }

  @Test
  void shouldHaveReceivedEntry() {
    val validComBundle = getDecodedCommunication(COM_PATH_WITHOUT_RECEIVED);
    val step = CommunicationVerifier.presentReceivedElement();
    step.apply(validComBundle);
  }

  @Test
  void shouldThrowWhileFindNoReceivedEntry() {
    val validComBundle = getDecodedCommunication(COM_PATH);
    val step = CommunicationVerifier.presentReceivedElement();
    assertThrows(AssertionError.class, () -> step.apply(validComBundle));
  }
}
