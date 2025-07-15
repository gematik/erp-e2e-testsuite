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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class GemErpMedicationTest extends ErpFhirParsingTest {

  @Test
  void shouldCopyFromBaseMedication() {
    Resource medication = new Medication();
    val gemErpMedication = assertDoesNotThrow(() -> GemErpMedication.fromMedication(medication));
    assertNotEquals(medication, gemErpMedication);
  }

  @Test
  void shouldReadNameFromContainedMedication() {
    val content =
        ResourceLoader.readFileFromResource("fhir/valid/erp/1.4.0/medication/Rezeptur.json");
    val medication = parser.decode(GemErpMedication.class, content);
    assertTrue(medication.getNameFromCodeOreContainedRessource().isPresent());
    assertFalse(medication.getNameFromCodeOreContainedRessource().get().isEmpty());
  }

  @Test
  void shouldReturnAskIfPresent() {
    val medication = new GemErpMedication();
    val askCode = "TestASK";

    medication.getCode().addCoding(ASK.from(askCode).asCoding());

    val result = medication.getAsk();
    assertTrue(result.isPresent());
    assertEquals(askCode, result.get().getValue());
  }

  @Test
  void shouldReturnAtcIfPresent() {
    val medication = new GemErpMedication();
    val atcCode = "TestATC";
    medication.getCode().addCoding(ATC.from(atcCode).asCoding());

    val result = medication.getAtc();
    assertTrue(result.isPresent());
    assertEquals(atcCode, result.get().getValue());
  }

  @Test
  void shouldReturnSnomedIfPresent() {
    val medication = new GemErpMedication();
    val snomedCode = "TestSNOMED";
    val coding =
        new Coding().setSystem(CommonCodeSystem.SNOMED_SCT.getCanonicalUrl()).setCode(snomedCode);
    medication.getCode().addCoding(coding);

    val result = medication.getSnomed();
    assertTrue(result.isPresent());
    assertEquals(snomedCode, result.get());
  }

  @Test
  void shouldFailwWhileReturnAsk() {
    val medication = new GemErpMedication();
    assertEquals(Optional.empty(), medication.getAsk());
  }

  @Test
  void shouldFailWhileReturnAtc() {
    val medication = new GemErpMedication();
    assertEquals(Optional.empty(), medication.getAtc());
  }

  @Test
  void shouldThrowWileReturnSnomed() {
    val medication = new GemErpMedication();

    assertEquals(Optional.empty(), medication.getSnomed());
  }
}
