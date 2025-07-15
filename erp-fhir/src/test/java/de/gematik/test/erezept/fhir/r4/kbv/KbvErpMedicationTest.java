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

package de.gematik.test.erezept.fhir.r4.kbv;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.coding.exceptions.FhirVersionException;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationCompoundingFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import java.util.Collections;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvErpMedicationTest extends ErpFhirParsingTest {

  @Test
  void shouldDetectVersionCorrectly() {
    val version = KbvItaErpVersion.V1_1_0;
    val medication = KbvErpMedicationPZNFaker.builder().withVersion(version).fake();
    assertEquals(version, medication.getVersion());
  }

  @Test
  void shouldThrowIfMedicationDoesNotContainAProfile() {
    val medication = new KbvErpMedication();
    assertThrows(FhirVersionException.class, medication::getVersion);
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
              val withoutVersion = profile.asStringValue().split("\\|")[0];
              profile.setValueAsString(withoutVersion);
            });
    assertEquals(KbvItaErpVersion.getDefaultVersion(), medication.getVersion());
  }

  @Test
  void getPznOptionalShouldWorkForMedicationCompound() {
    PZN pzn = PZN.random();
    val medComp =
        KbvErpMedicationCompoundingFaker.builder().withMedicationIngredient(pzn, "", "").fake();
    assertTrue(medComp.getPznOptional().isPresent());
    assertEquals(pzn.getValue(), medComp.getPznOptional().get().getValue());
  }

  @Test
  void getPznOptionalShouldWorkForMedicationPZN() {
    String pzn = PZN.random().getValue();
    val medComp =
        KbvErpMedicationPZNFaker.builder().withPznMedication(pzn, "Useless Medicine").fake();
    assertTrue(medComp.getPznOptional().isPresent());
    assertEquals(pzn, medComp.getPznOptional().get().getValue());
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
