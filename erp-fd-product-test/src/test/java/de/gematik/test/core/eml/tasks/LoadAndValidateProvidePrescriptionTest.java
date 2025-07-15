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

import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvidePrescriptionVerifier.emlPractitionerHasHbaTelematikId;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.eml.tasks.LoadAndValidateProvidePrescription;
import de.gematik.test.erezept.abilities.UseTheEpaMockClient;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvidePrescription;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LoadAndValidateProvidePrescriptionTest {

  private static EpaOpProvidePrescription epaOpProvidePrescriptionWithPzn;

  @BeforeAll
  static void setup() {
    OnStage.setTheStage(new Cast() {});
    CoverageReporter.getInstance().startTestcase("not needed");
    val fhir = EpaFhirFactory.create();
    epaOpProvidePrescriptionWithPzn =
        fhir.decode(
            EpaOpProvidePrescription.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/parameters/ForTestOnly-Parameters-example-epa-op-provide-prescription-erp-input-parameters-2.json"));
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldCheckLoadAndValidateProvidePrescription() {
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    when(useEpaMockClient.downloadProvidePrescriptionBy(any()))
        .thenReturn(List.of(epaOpProvidePrescriptionWithPzn));
    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);
    val validatorList =
        List.of(emlPractitionerHasHbaTelematikId(TelematikID.from("1-1.58.00000040")));

    val step =
        LoadAndValidateProvidePrescription.withValidator(validatorList)
            .forPrescription(PrescriptionId.from("160.153.303.257.459"));

    assertDoesNotThrow(() -> epaFhirChecker.attemptsTo(step));
  }

  @Test
  void shouldThrowWhileCheckLoadAndValidateProvidePrescription() {
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    when(useEpaMockClient.downloadProvidePrescriptionBy(any()))
        .thenReturn(List.of(epaOpProvidePrescriptionWithPzn));
    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);
    val validatorList = List.of(emlPractitionerHasHbaTelematikId(TelematikID.from("wrong Id")));

    val step =
        LoadAndValidateProvidePrescription.withValidator(validatorList)
            .forPrescription(PrescriptionId.from("160.153.303.257.459"));

    assertThrows(AssertionError.class, () -> step.performAs(epaFhirChecker));
  }

  @Test
  void shouldThrowWhileCheckLoadAndValidateProvidePrescriptionIsEmpty() {
    val useEpaMockClient = mock(UseTheEpaMockClient.class);
    when(useEpaMockClient.downloadProvidePrescriptionBy(any())).thenReturn(List.of());
    val epaFhirChecker = new GemaTestActor("epaFhirChecker");
    epaFhirChecker.can(useEpaMockClient);
    val validatorList = List.of(emlPractitionerHasHbaTelematikId(TelematikID.from("wrong Id")));

    val step =
        LoadAndValidateProvidePrescription.withValidator(validatorList)
            .forPrescription(PrescriptionId.from("160.153.303.257.459"));

    assertThrows(AssertionError.class, () -> step.performAs(epaFhirChecker));
  }
}
