/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.fhir.resources.kbv;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationCompoundingFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.exceptions.FhirValidatorException;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;
import org.junit.jupiter.api.Test;

class KbvErpMedicationTest {

  @Test
  void shouldDetectVersionCorrectly() {
    List.of(KbvItaErpVersion.V1_0_2, KbvItaErpVersion.V1_1_0)
        .forEach(
            version -> {
              val medication = KbvErpMedicationPZNFaker.builder().withVersion(version).fake();
              assertEquals(version, medication.getVersion());
            });
  }

  @Test
  void shouldThrowIfMedicationDoesNotContainAProfile() {
    val medication = new KbvErpMedication();
    assertThrows(FhirValidatorException.class, medication::getVersion);
  }

  @Test
  void shouldProvideDefaultVersionIfMissing() {
    val medication = KbvErpMedicationPZNFaker.builder().fake();

    // remove version from profile
    medication
        .getMeta()
        .getProfile()
        .forEach(
            profile -> {
              val unversioned = profile.asStringValue().split("\\|")[0];
              profile.setValueAsString(unversioned);
            });
    assertEquals(KbvItaErpVersion.getDefaultVersion(), medication.getVersion());
  }

  @Test
  void getPznOptionalShouldWorkForMedicationCompound() {
    PZN pzn = PZN.random();
    val medComp =
        KbvErpMedicationCompoundingFaker.builder().withMedicationIngredient(pzn, "", "").fake();
    assertEquals(pzn.getValue(), medComp.getPznOptional().get().getValue());
  }

  @Test
  void getPznOptionalShouldWorkForMedicationPZN() {
    String pzn = PZN.random().getValue();
    val medComp =
        KbvErpMedicationPZNFaker.builder().withPznMedication(pzn, "Useless Medicine").fake();
    assertEquals(pzn, medComp.getPznOptional().get().getValue());
  }

  @Test
  void shouldReturnIntValueIfNonNull() {
    val medication = mock(KbvErpMedication.class);
    val ratio = mock(Ratio.class);
    val quantity = mock(Quantity.class);
    when(medication.getAmount()).thenReturn(ratio);
    when(ratio.getNumerator()).thenReturn(quantity);
    when(quantity.getValue()).thenReturn(BigDecimal.ONE);
    when(medication.getMedicationAmount()).thenCallRealMethod();
    assertEquals(BigDecimal.ONE.intValue(), medication.getMedicationAmount());
  }

  @Test
  void shouldGetFreeText() {
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    assertDoesNotThrow(medication::getFreeText);
  }

  @Test
  void shouldThrowOnMissingCategory() {
    List<MedicationCategory> categories = Collections.emptyList();
    val medication = mock(KbvErpMedication.class);
    when(medication.getCatagory()).thenReturn(categories);
    when(medication.getCategoryFirstRep()).thenCallRealMethod();
    assertThrows(MissingFieldException.class, medication::getCategoryFirstRep);
  }

  @Test
  void shouldThrowOnMissingPzn() {
    List<String> listOfPzn = Collections.emptyList();
    val medication = mock(KbvErpMedication.class);
    when(medication.getPzn()).thenReturn(listOfPzn);
    when(medication.getPznFirstRep()).thenCallRealMethod();
    assertThrows(MissingFieldException.class, medication::getPznFirstRep);
  }
}
