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

package de.gematik.test.erezept.fhir.valuesets.eu;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import lombok.val;
import org.junit.jupiter.api.Test;

class EuRequestTypeTest {

  @Test
  void shouldGetCodeSystemCorrectForDemographics() {
    val expected = "https://gematik.de/fhir/erp-eu/CodeSystem/GEM_ERPEU_CS_RequestType";
    assertEquals(expected, EuRequestType.DEMOGRAPHICS.getCodeSystem().getCanonicalUrl());
  }

  @Test
  void shouldUsingFromCodeCorrectForDemographics() {
    val code = EuRequestType.DEMOGRAPHICS.getCode();
    val result = EuRequestType.fromCode(code);
    assertEquals(EuRequestType.DEMOGRAPHICS, result);
    assertEquals("demographics", result.getCode());
    assertEquals(
        "requesting demographics by one prescription to validate Patients identity",
        result.getDisplay());
  }

  @Test
  void shouldGetCodeSystemCorrectForPrescriptionRetrieval() {
    val expected = "https://gematik.de/fhir/erp-eu/CodeSystem/GEM_ERPEU_CS_RequestType";
    assertEquals(expected, EuRequestType.PRESCRIPTION_RETRIEVAL.getCodeSystem().getCanonicalUrl());
  }

  @Test
  void shouldUsingFromCodeCorrectForPrescriptionRetrieval() {
    val code = EuRequestType.PRESCRIPTION_RETRIEVAL.getCode();
    val result = EuRequestType.fromCode(code);
    assertEquals(EuRequestType.PRESCRIPTION_RETRIEVAL, result);
    assertEquals("e-prescriptions-retrieval", result.getCode());
    assertEquals(
        "Query and accept a specific list of EU redeemable prescriptions in the corresponding EU"
            + " country",
        result.getDisplay());
  }

  @Test
  void shouldGetCodeSystemCorrectForPrescriptionList() {
    val expected = "https://gematik.de/fhir/erp-eu/CodeSystem/GEM_ERPEU_CS_RequestType";
    assertEquals(expected, EuRequestType.PRESCRIPTION_LIST.getCodeSystem().getCanonicalUrl());
  }

  @Test
  void shouldUsingFromCodeCorrectForPrescriptionList() {
    val code = EuRequestType.PRESCRIPTION_LIST.getCode();
    val result = EuRequestType.fromCode(code);
    assertEquals(EuRequestType.PRESCRIPTION_LIST, result);
    assertEquals("e-prescriptions-list", result.getCode());
    assertEquals(
        "Query all redeemable prescriptions in the corresponding EU country", result.getDisplay());
  }

  @Test
  void shouldFailWhileUsingFromCodeWithInvalidCode() {
    assertThrows(InvalidValueSetException.class, () -> EuRequestType.fromCode("invalid"));
    assertThrows(InvalidValueSetException.class, () -> EuRequestType.fromCode(""));
    assertThrows(InvalidValueSetException.class, () -> EuRequestType.fromCode(null));
  }
}
