/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.eml;

import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.provDispensationHasCorrectDosageDgMPComponent;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.eml.tasks.CheckEpaOpProvideDispensationsRenderedDosageInstruction;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.TimingBuilder;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvideDispensation;
import de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationFaker;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CheckEpaOpProvideDispensationsRenderedDosageInstructionTest extends ErpFhirBuildingTest {

  private UseTheEpaMockClient mockClient;
  private Actor actor;
  private PrescriptionId prescriptionId;

  EpaOpProvideDispensation epaOpProvideDispensationFromMock =
      EpaFhirFactory.create()
          .decode(
              EpaOpProvideDispensation.class,
              ResourceLoader.readFileFromResource(
                  "fhir/valid/parameters/1.3.0/epaOpProvDispenseInputFromEpaMock.json"));

  private static ErxMedicationDispenseBundle createMedicationDispenseBundle() {
    val medDispBuilder =
        ErxMedicationDispenseFaker.builder(ErpWorkflowVersion.V1_6)
            .withPrescriptionId(PrescriptionId.from("1"))
            .withDgmp(
                DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.MG)
                    .timing(
                        TimingBuilder.forRepeatComp()
                            .frequency(1)
                            .period(3)
                            .periodUnit(Timing.UnitsOfTime.D)
                            .timeOfDay("08:00:00")
                            .build())
                    .build())
            .toBuilder();
    val med =
        GemErpMedicationFaker.forPznMedication()
            .withPzn(PZN.from("10000001"), "IBU-ratiopharm 400mg akut Schmerztabletten")
            .fake();
    med.getCode()
        .addCoding(DeBasisProfilCodeSystem.ATC.asCoding("M01AE01").setDisplay("Ibuprofen"));
    val medDisp = medDispBuilder.medication(med).build();
    val bundle = new ErxMedicationDispenseBundle();
    bundle.addEntry().setResource(medDisp);
    bundle.addEntry().setResource(med);
    return bundle;
  }

  @BeforeEach
  void setup() {
    CoverageReporter.getInstance().startTestcase("testcase");
    mockClient = mock(UseTheEpaMockClient.class);
    actor = new Actor("TestActor");
    actor.can(mockClient);
    prescriptionId = PrescriptionId.from("1");
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldThrowIfNoDispensationFound() {
    when(mockClient.downloadProvideDispensationBy(any())).thenReturn(List.of());

    CheckEpaOpProvideDispensationsRenderedDosageInstruction task =
        CheckEpaOpProvideDispensationsRenderedDosageInstruction.forDispensation(
            createMedicationDispenseBundle(), prescriptionId);

    AssertionError error = assertThrows(AssertionError.class, () -> task.performAs(actor));
    assertTrue(error.getMessage().contains("No EpaOpProvideDispensation found"));
  }

  @Test
  void shouldApplyDefaultVerifiers() {
    when(mockClient.downloadProvideDispensationBy(any()))
        .thenReturn(List.of(epaOpProvideDispensationFromMock));
    val medDisp = createMedicationDispenseBundle();
    CheckEpaOpProvideDispensationsRenderedDosageInstruction task =
        CheckEpaOpProvideDispensationsRenderedDosageInstruction
            .forDispensationWithAdditionalVerifier(
                medDisp,
                prescriptionId,
                List.of(
                    provDispensationHasCorrectDosageDgMPComponent(
                        createMedicationDispenseBundle()
                            .getDispensePairBy(prescriptionId)
                            .get(0)
                            .getLeft())));

    assertDoesNotThrow(() -> task.performAs(actor));
  }

  @Test
  void shouldApplyAdditionalVerifiers() {
    when(mockClient.downloadProvideDispensationBy(any()))
        .thenReturn(List.of(epaOpProvideDispensationFromMock));

    CheckEpaOpProvideDispensationsRenderedDosageInstruction task =
        CheckEpaOpProvideDispensationsRenderedDosageInstruction.forDispensation(
            createMedicationDispenseBundle(), prescriptionId);

    assertDoesNotThrow(() -> task.performAs(actor));
  }
}
