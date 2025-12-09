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
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationCompoundingBuilder;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvIngredientComponentBuilder;
import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Quantity;
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
    assertFalse(medication.getNameFromCodingDisplay().isPresent());
    assertTrue(medication.getNameFromCodeText().isPresent());
    assertFalse(medication.getNameFromCodeOreContainedRessource().get().isEmpty());
  }

  @Test
  void shouldFindMedicationNameInContainedMedication() {
    val pzn = PZN.random();
    Medication.MedicationIngredientComponent ingredient =
        KbvIngredientComponentBuilder.builder()
            .pzn(pzn)
            .ask(ASK.from("askCode"))
            .ingredientStrength(new Quantity(3), new Quantity(1), "ml")
            .darreichungsform("im Einer")
            .textInCoding("falls kein Code zur Hand hier was passendes rein schreiben")
            .build();

    val medCompounding =
        GemErpMedicationCompoundingBuilder.forCompounding()
            .codeText("medicineName")
            .ingredientComponent(ingredient)
            .build();
    assertEquals("medicineName", medCompounding.getNameFromCodeText().orElseThrow());
  }

  @Test
  void shouldFindMedicationNameInContainedMedicationInDisplay() {
    val pzn = PZN.random();
    Medication.MedicationIngredientComponent ingredient =
        KbvIngredientComponentBuilder.builder()
            .ask(ASK.from("askCode"))
            .ingredientStrength(new Quantity(3), new Quantity(1), "ml")
            .darreichungsform("im Einer")
            .textInCoding("falls kein Code zur Hand hier was passendes rein schreiben")
            .build();

    ingredient.getItemCodeableConcept().addCoding(pzn.asCoding().setDisplay("displayName"));
    val medCompounding =
        GemErpMedicationCompoundingBuilder.forCompounding().ingredientComponent(ingredient).build();
    assertEquals("displayName", medCompounding.getAnyNameFromContainedMed().orElseThrow());
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

  @Test
  void shouldGetDescription() {
    val medication =
        GemErpMedicationFaker.forPznMedication()
            .withPzn(PZN.from("132456789"), "nameOfPrescription")
            .withDarreichungsform(Darreichungsform.PUE)
            .fake();
    assertNotNull(medication.getDescription());
    assertEquals(
        "Type: GemErpMedication, PZN: 132456789, ASK: not an ASK-Dispensation, als"
            + " nameOfPrescription",
        medication.getDescription());
  }
}
