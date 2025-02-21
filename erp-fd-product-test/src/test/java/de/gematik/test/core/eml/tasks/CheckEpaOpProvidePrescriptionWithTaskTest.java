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

package de.gematik.test.core.eml.tasks;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.eml.tasks.CheckEpaOpProvideDispensation;
import de.gematik.test.eml.tasks.CheckEpaOpProvidePrescriptionWithTask;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvideDispensation;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvidePrescription;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.TelematikID;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CheckEpaOpProvidePrescriptionWithTaskTest extends ParsingTest {
  private static EpaOpProvidePrescription epaOpProvidePrescriptionWithPzn;
  private static EpaOpProvideDispensation epaOpProvideDispensation;
  private static KbvErpBundle kbvErpBundle;
  private static ErxMedicationDispense medicationDispense;
  private static final Date testDate_22_01_2025 =
      DateConverter.getInstance().localDateToDate(LocalDate.of(2025, Month.JANUARY, 22));

  @BeforeAll
  static void setup() {
    OnStage.setTheStage(new Cast() {});
    CoverageReporter.getInstance().startTestcase("not needed");
    val fhir = EpaFhirFactory.create();
    epaOpProvidePrescriptionWithPzn =
        fhir.decode(
            EpaOpProvidePrescription.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/medication/ForTestOnly-Parameters-example-epa-op-provide-prescription-erp-input-parameters-2.json"));

    kbvErpBundle =
        parser.decode(
            KbvErpBundle.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/kbv/1.1.0/bundle/manipulated/ManipulatedForUnitTestfe9200db-d503-4a1b-bd13-96ab375f9bab.xml"));
    epaOpProvideDispensation =
        fhir.decode(
            EpaOpProvideDispensation.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/medication/Parameters-example-epa-op-provide-dispensation-erp-input-parameters-1.json"));

    medicationDispense =
        ErxMedicationDispenseBuilder.forKvnr(KVNR.from("X110411319"))
            .dosageInstruction("1-0-0-0")
            .whenHandedOver(testDate_22_01_2025)
            .wasSubstituted(false)
            .medication(getMedication())
            .performerId("urn:uuid:151f1697-7512-4e21-9466-1b75207475d8")
            .prescriptionId("160.153.303.257.459")
            .status(MedicationDispense.MedicationDispenseStatus.COMPLETED)
            .build();
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldCheckEpaOpOutPrescriptionTask() {
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    when(useEpaMockClient.downloadProvidePrescriptionBy(any()))
        .thenReturn(List.of(epaOpProvidePrescriptionWithPzn));
    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);
    val step =
        CheckEpaOpProvidePrescriptionWithTask.forPrescription(
            kbvErpBundle, TelematikID.from("9-2.58.00000040"), TelematikID.from("1-1.58.00000040"));
    assertDoesNotThrow(() -> epaFhirChecker.attemptsTo(step));
  }

  @Test
  void shouldThrowWhileCheckEpaOpOutPrescriptionTask() {
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    when(useEpaMockClient.downloadProvidePrescriptionBy(any())).thenReturn(List.of());
    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);
    val step =
        CheckEpaOpProvidePrescriptionWithTask.forPrescription(
            kbvErpBundle, TelematikID.from("9-2.58.00000040"), TelematikID.from("1-1.58.00000040"));
    assertThrows(AssertionError.class, () -> step.performAs(epaFhirChecker));
  }

  @Test
  void shouldCheckEpaOpOutDispensationCorrect() {
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    when(useEpaMockClient.downloadProvideDispensationBy(any()))
        .thenReturn(List.of(epaOpProvideDispensation));
    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);
    val step =
        CheckEpaOpProvideDispensation.forDispensation(
            medicationDispense, TelematikID.from("9-2.58.00000040"));
    assertDoesNotThrow(() -> epaFhirChecker.attemptsTo(step));
  }

  @Test
  void shouldThrowWhileCheckEpaOpOutDispensation() {
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    when(useEpaMockClient.downloadProvidePrescriptionBy(any())).thenReturn(List.of());
    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);
    val step =
        CheckEpaOpProvideDispensation.forDispensation(
            medicationDispense, TelematikID.from("9-2.58.00000040"));
    assertThrows(AssertionError.class, () -> step.performAs(epaFhirChecker));
  }

  private static KbvErpMedication getMedication() {
    val medication = new KbvErpMedication();
    medication
        .getCode()
        .getCoding()
        .add(
            DeBasisProfilCodeSystem.PZN
                .asCoding("10019621")
                .setDisplay("IBU-ratiopharm 400mg akut Schmerztabletten"));
    medication
        .getCode()
        .getCoding()
        .add(new Coding(DeBasisProfilCodeSystem.ATC.getCanonicalUrl(), "M01AE01", "Ibuprofen"));
    return medication;
  }
}
