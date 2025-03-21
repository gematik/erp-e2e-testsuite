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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.eml.tasks.CheckErpDoesNotProvidePrescriptionCancellationToEpa;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpCancelPrescription;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.util.List;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CheckErpDoesNotProvidePrescriptionCancellationToEpaTest extends ErpFhirParsingTest {
  private static KbvErpBundle kbvErpBundle;

  @BeforeAll
  static void setup() {
    OnStage.setTheStage(new Cast() {});
    CoverageReporter.getInstance().startTestcase("not needed");
    val fhir = EpaFhirFactory.create();

    kbvErpBundle =
        parser.decode(
            KbvErpBundle.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/kbv/1.1.0/bundle/manipulated/ManipulatedForUnitTestfe9200db-d503-4a1b-bd13-96ab375f9bab.xml"));
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldCheckErpDoesNotCancelPrescriptionToEpaTask() {
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    when(useEpaMockClient.downloadCancelPrescriptionBy(any())).thenReturn(List.of());
    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);
    val step =
        CheckErpDoesNotProvidePrescriptionCancellationToEpa.forCancelPrescription(kbvErpBundle);
    assertDoesNotThrow(() -> epaFhirChecker.attemptsTo(step));
  }

  @Test
  void shouldCheckErpDoesNotCancelPrescriptionToEpaWithPrescriptions() {
    val epaOpCancelPrescription = mock(EpaOpCancelPrescription.class);
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    when(useEpaMockClient.downloadCancelPrescriptionBy(any()))
        .thenReturn(List.of(epaOpCancelPrescription));

    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);
    val step =
        CheckErpDoesNotProvidePrescriptionCancellationToEpa.forCancelPrescription(kbvErpBundle);
    assertThrows(
        AssertionError.class,
        () -> step.performAs(epaFhirChecker)); // Expect failure as prescription is cancelled
  }

  @Test
  void shouldCreateCancelTaskFromErpInteraction() {
    val expectedKbvBundle = mock(KbvErpBundle.class);
    val expectedResponse = mock(ErxPrescriptionBundle.class);
    val erpInteraction = mock(ErpInteraction.class);

    when(expectedResponse.getKbvBundle()).thenReturn(Optional.of(expectedKbvBundle));
    when(erpInteraction.getExpectedResponse()).thenReturn(expectedResponse);

    val task =
        CheckErpDoesNotProvidePrescriptionCancellationToEpa.forCancelPrescription(erpInteraction);

    assertNotNull(task);
  }
}
