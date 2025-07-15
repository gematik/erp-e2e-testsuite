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

package de.gematik.test.core.eml.tasks;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.eml.tasks.CheckErpDoesNotProvideDispensationToEpa;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvideDispensation;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CheckErpDoesNotProvideDispensationToEpaTest extends ErpFhirParsingTest {

  @BeforeAll
  static void setup() {
    OnStage.setTheStage(new Cast() {});
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldCheckErpDoesNotProvideDispensationToEpaTask() {
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    when(useEpaMockClient.downloadProvideDispensationBy(any())).thenReturn(List.of());
    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);

    val step =
        CheckErpDoesNotProvideDispensationToEpa.forPrescription(PrescriptionId.from("1234567890"));

    assertDoesNotThrow(() -> epaFhirChecker.attemptsTo(step));
  }

  @Test
  void shouldFailWhenDispensationIsProvided() {
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    val mockDispensation = mock(EpaOpProvideDispensation.class);
    when(useEpaMockClient.downloadProvideDispensationBy(any()))
        .thenReturn(List.of(mockDispensation));

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);

    val step =
        CheckErpDoesNotProvideDispensationToEpa.forPrescription(PrescriptionId.from("1234567890"));

    assertThrows(
        AssertionError.class,
        () -> step.performAs(epaFhirChecker)); // Expect failure as dispensation exists
  }

  @Test
  void shouldSupportForDispensationStaticMethod() {
    val mockErpInteraction = mock(ErpInteraction.class);
    val mockBundle = mock(ErxMedicationDispenseBundle.class);
    val mockDispense = mock(ErxMedicationDispense.class);
    when(mockBundle.getMedicationDispenses()).thenReturn(List.of(mockDispense));
    when(mockDispense.getPrescriptionId()).thenReturn(PrescriptionId.from("1234567890"));
    when(mockErpInteraction.getExpectedResponse()).thenReturn(mockBundle);

    val step = CheckErpDoesNotProvideDispensationToEpa.forDispensation(mockErpInteraction);

    assertThat(step).isNotNull();
  }
}
