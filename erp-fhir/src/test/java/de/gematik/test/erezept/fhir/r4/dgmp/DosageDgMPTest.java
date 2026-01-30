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

package de.gematik.test.erezept.fhir.r4.dgmp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.systems.KbvCodeSystem;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.io.File;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class DosageDgMPTest extends ErpFhirParsingTest {

  private static final String BASE_PATH = "fhir/valid/kbv/1.4.0/dgmp/";

  @Test
  void shouldCreateGeneratorMetaExtension() {
    val ex = RenderedDosageInstructionUtil.createGeneratorExtension();
    assertNotNull(ex);
    assertNotNull(ex.getExtensionByUrl("algorithmVersion"));
    assertNotNull(ex.getExtensionByUrl("language"));
  }

  @ParameterizedTest
  @MethodSource
  void shouldNotThrowOnUnsupportedDosageInstruction(KbvErpMedicationRequest mr) {
    val dis = mr.getDosageInstructionDgMPs();
    val rendered = assertDoesNotThrow(() -> RenderedDosageInstructionUtil.render(dis));
    log.debug(rendered);
  }

  @ParameterizedTest
  @MethodSource
  void shouldReadAndRenderDosageInstruction(File input) {
    val content = ResourceLoader.readString(input);
    val mr = parser.decode(KbvErpMedicationRequest.class, content);

    val di = mr.getRenderedDosageInstruction();
    assertTrue(di.isPresent());
    assertFalse(mr.getDosageInstructionDgMPs().isEmpty());

    val original = mr.getRenderedDosageInstruction().orElseThrow();
    val rendered = RenderedDosageInstructionUtil.render(mr.getDosageInstructionDgMPs());
    log.info(rendered);
    assertEquals(original, rendered, "Calculate DosageInstruction MUST match the original one");
  }

  static Stream<Arguments> shouldReadAndRenderDosageInstruction() {
    // examples from https://ig.fhir.de/igs/medication/artifacts.html
    return ResourceLoader.getResourceFilesInDirectory(BASE_PATH).stream().map(Arguments::of);
  }

  static Stream<Arguments> shouldNotThrowOnUnsupportedDosageInstruction() {
    return Stream.of(
            new KbvErpMedicationRequest(), // empty MR without dosage instruction
            withUnsupportedDosage01of20(newMedicationRequest()),
            withUnsupportedDosage02of20(newMedicationRequest()),
            withRandomDosageInstructions(newMedicationRequest()))
        .map(Arguments::of);
  }

  private static KbvErpMedicationRequest newMedicationRequest() {
    return new KbvErpMedicationRequest();
  }

  private static KbvErpMedicationRequest withUnsupportedDosage01of20(KbvErpMedicationRequest mr) {
    // https://ig.fhir.de/igs/medication/MedicationRequest-MR-Unsupported-Dosage-01-of-20-Count.xml.html
    mr.setId("MR-Unsupported-Dosage-01-of-20-Count");
    mr.addDosageInstruction()
        .setText("count")
        .getTiming()
        .getRepeat()
        .setCount(5)
        .setFrequency(1)
        .setPeriod(1)
        .setPeriodUnit(Timing.UnitsOfTime.D);
    return mr;
  }

  private static KbvErpMedicationRequest withUnsupportedDosage02of20(KbvErpMedicationRequest mr) {
    // https://ig.fhir.de/igs/medication/MedicationRequest-MR-Unsupported-Dosage-02-of-20-asNeededBoolean.xml.html
    mr.setId("MR-Unsupported-Dosage-02-of-20-asNeededBoolean");
    mr.addDosageInstruction().setText("asNeededBoolean").setAsNeeded(new BooleanType(true));
    return mr;
  }

  private static KbvErpMedicationRequest withRandomDosageInstructions(KbvErpMedicationRequest mr) {
    mr.setId("MR-Random-Dosage-Instructions");

    val rnd = GemFaker.getFaker().random();
    val di = mr.addDosageInstruction();

    if (GemFaker.fakerBool()) {
      di.getTiming()
          .getRepeat()
          .setFrequency(rnd.nextInt(0, 4))
          .setPeriod(rnd.nextInt(0, 10))
          .setPeriodUnit(GemFaker.randomElement(Timing.UnitsOfTime.values()));
    }

    val iterations = GemFaker.getFaker().random().nextInt(1, 3);
    for (int i = 0; i < iterations; i++) {
      if (GemFaker.fakerBool()) {
        // https://simplifier.net/kbvschluesseltabellen/KBV-CS-SFHIR-BMP-DOSIEREINHEIT
        val unit = GemFaker.randomElement("mg", "ml", "g", "Stück", "Tabletten");
        di.addDoseAndRate()
            .getDoseQuantity()
            .setValue(rnd.nextInt(1, 500))
            .setUnit(unit)
            .setCode(String.valueOf(rnd.nextInt(1, 10)))
            .setSystem(KbvCodeSystem.DOSIEREINHEIT.getCanonicalUrl());
      }
    }

    return mr;
  }
}
