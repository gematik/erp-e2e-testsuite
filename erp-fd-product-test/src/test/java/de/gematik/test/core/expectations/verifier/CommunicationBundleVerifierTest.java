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

package de.gematik.test.core.expectations.verifier;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunicationBundle;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.TelematikID;
import de.gematik.test.erezept.testutil.PrivateConstructorsUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class CommunicationBundleVerifierTest extends ParsingTest {
  private static final String DIS_REQ_BUNDLE_PATH =
      "fhir/valid/erp/1.2.0/communicationbundle/2c565050-eefb-4d14-b00d-640a6e0cc677.xml";

  private static final String REPLY_BUNDLE_PATH =
      "fhir/valid/erp/1.2.0/communicationbundle/b8031217-57ca-4ab8-8283-dadf28421e1f.json";

  private static ErxCommunicationBundle getDecodedDipReqBundle() {
    return parser.decode(
        ErxCommunicationBundle.class, ResourceUtils.readFileFromResource(DIS_REQ_BUNDLE_PATH));
  }

  private static ErxCommunicationBundle getDecodedReplyBundle() {
    return parser.decode(
        ErxCommunicationBundle.class, ResourceUtils.readFileFromResource(REPLY_BUNDLE_PATH));
  }

  @BeforeEach
  void init() {
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldNotInstantiateUtilityClass() {
    assertTrue(
        PrivateConstructorsUtil.throwsInvocationTargetException(CommunicationBundleVerifier.class));
  }

  @Test
  void shouldFindId() {
    val validComBundle = getDecodedDipReqBundle();
    val step =
        CommunicationBundleVerifier.containsCommunicationWithId(
            "01ebbe1d-b718-ec48-f02d-73a051be9e23", ErpAfos.A_19520_01);
    step.apply(validComBundle);
  }

  @Test
  void shouldThrowWhileMissingId() {
    val validComBundle = getDecodedDipReqBundle();
    val step =
        CommunicationBundleVerifier.containsCommunicationWithId(
            "01ebbe1d-b718-ec48-f02d-73a051be0000", ErpAfos.A_19520_01);
    assertThrows(AssertionError.class, () -> step.apply(validComBundle));
  }

  @Test
  void shouldFindNoId() {
    val validComBundle = getDecodedDipReqBundle();
    val step =
        CommunicationBundleVerifier.containsNoCommunicationWithId(
            "01ebbe1d-b718-ec48-f02d-73a051b0000", ErpAfos.A_19520_01);
    step.apply(validComBundle);
  }

  @Test
  void shouldThrowWhileMissingNoId() {
    val validComBundle = getDecodedDipReqBundle();
    val step =
        CommunicationBundleVerifier.containsNoCommunicationWithId(
            "01ebbe1d-b718-ec48-f02d-73a051be9e23", ErpAfos.A_19520_01);
    assertThrows(AssertionError.class, () -> step.apply(validComBundle));
  }

  @Test
  void shouldCountCorrect() {
    val validComBundle = getDecodedDipReqBundle();
    val step = CommunicationBundleVerifier.containsCountOfCommunication(4, ErpAfos.A_19521);
    step.apply(validComBundle);
  }

  @Test
  void shoulThrowWhileCounting() {
    val validComBundle = getDecodedDipReqBundle();
    val step = CommunicationBundleVerifier.containsCountOfCommunication(5, ErpAfos.A_19521);
    assertThrows(AssertionError.class, () -> step.apply(validComBundle));
  }

  @Test
  void shouldCompareRecipientCorrect() {
    val validComBundle = getDecodedDipReqBundle();
    val step =
        CommunicationBundleVerifier.containsOnlyRecipientWith(
            "5-2-KH-APO-Waldesrand-01", ErpAfos.A_19521);
    step.apply(validComBundle);
  }

  @Test
  void shoulThrowWhileCompareRecipient() {
    val validComBundle = getDecodedDipReqBundle();
    val step = CommunicationBundleVerifier.containsOnlyRecipientWith("X110499478", ErpAfos.A_19521);
    assertThrows(AssertionError.class, () -> step.apply(validComBundle));
  }

  @Test
  void shouldCompareSenderAsKvnrCorrect() {
    val validComBundle = getDecodedDipReqBundle();
    val step = CommunicationBundleVerifier.onlySenderWith(KVNR.from("X110499478"));
    step.apply(validComBundle);
  }

  @Test
  void shouldCompareSenderAsTelematikIdCorrect() {
    val validComBundle = getDecodedReplyBundle();
    val step =
        CommunicationBundleVerifier.onlySenderWith(TelematikID.from("5-2-KH-APO-Waldesrand-01"));
    step.apply(validComBundle);
  }

  @Test
  void shouldCompareSenderCorrect() {
    val validComBundle = getDecodedDipReqBundle();
    val step = CommunicationBundleVerifier.onlySenderWith("X110499478", ErpAfos.A_19521);
    step.apply(validComBundle);
  }

  @Test
  void shouldThrowWhileCompareSender() {
    val validComBundle = getDecodedDipReqBundle();
    val step =
        CommunicationBundleVerifier.onlySenderWith("5-2-KH-APO-Waldesrand-01", ErpAfos.A_19521);
    assertThrows(AssertionError.class, () -> step.apply(validComBundle));
  }
}
