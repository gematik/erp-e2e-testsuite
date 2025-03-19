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
 */

package de.gematik.test.core.eml.tasks;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.eml.tasks.CheckEpaOpCancelPrescriptionWithTask;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpCancelPrescription;
import de.gematik.test.erezept.eml.fhir.values.RxPrescriptionId;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CheckEpaOpCancelPrescriptionWithTaskTest extends ErpFhirParsingTest {
  private static KbvErpBundle kbvErpBundle;
  private static final Date testDate_22_01_2025 =
      DateConverter.getInstance().localDateToDate(LocalDate.of(2025, Month.JANUARY, 22));

  @BeforeAll
  static void setup() {
    OnStage.setTheStage(new Cast() {});
    CoverageReporter.getInstance().startTestcase("not needed");

    kbvErpBundle =
        KbvErpBundleFaker.builder()
            .withPrescriptionId("160.153.303.257.459")
            .withAuthorDate(testDate_22_01_2025)
            .fake();
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldThrowWhileCheckEpaOpCancelPrescription() {
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    when(useEpaMockClient.downloadCancelPrescriptionBy(any())).thenReturn(List.of());

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);

    val step = CheckEpaOpCancelPrescriptionWithTask.forCancelPrescription(kbvErpBundle);

    assertThrows(AssertionError.class, () -> step.performAs(epaFhirChecker));
  }

  @Test
  void shouldPassWhenValidEpaOpCancelPrescriptionFound() {
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    val validPrescription =
        EpaFhirFactory.create()
            .decode(
                EpaOpCancelPrescription.class,
                ResourceLoader.readFileFromResource(
                    "fhir/valid/medication/Parameters-example-epa-op-cancel-prescription-erp-input-parameters-1.json"));
    when(useEpaMockClient.downloadCancelPrescriptionBy(any()))
        .thenReturn(List.of(validPrescription));

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);

    val step = CheckEpaOpCancelPrescriptionWithTask.forCancelPrescription(kbvErpBundle);

    assertDoesNotThrow(() -> step.performAs(epaFhirChecker));
  }

  @Test
  void shouldThrowWhenNoEpaOpCancelPrescriptionFound() {
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    when(useEpaMockClient.downloadCancelPrescriptionBy(any())).thenReturn(List.of());

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);

    val step = CheckEpaOpCancelPrescriptionWithTask.forCancelPrescription(kbvErpBundle);

    val exception = assertThrows(AssertionError.class, () -> step.performAs(epaFhirChecker));
    assertTrue(
        exception.getMessage().contains("No EpaOpCancelPrescription found for prescriptionId"));
  }

  @Test
  void shouldVerifyEpaOpCancelPrescriptionSuccessfully() {
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    val mockedPrescription = mock(EpaOpCancelPrescription.class);

    when(mockedPrescription.getEpaPrescriptionId())
        .thenReturn(RxPrescriptionId.from("160.153.303.257.459"));
    when(mockedPrescription.getEpaAuthoredOn()).thenReturn(testDate_22_01_2025);
    when(useEpaMockClient.downloadCancelPrescriptionBy(any()))
        .thenReturn(List.of(mockedPrescription));

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);

    val step = CheckEpaOpCancelPrescriptionWithTask.forCancelPrescription(kbvErpBundle);

    assertDoesNotThrow(() -> step.performAs(epaFhirChecker));

    verify(useEpaMockClient).downloadCancelPrescriptionBy(kbvErpBundle.getPrescriptionId());
    verify(mockedPrescription, times(2)).getEpaPrescriptionId();
  }
}
