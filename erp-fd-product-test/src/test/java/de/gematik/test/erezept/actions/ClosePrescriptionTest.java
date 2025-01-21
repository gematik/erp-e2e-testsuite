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

package de.gematik.test.erezept.actions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.createEmptyValidationResult;
import static de.gematik.test.erezept.actions.ClosePrescription.applyMutators;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.CloseTaskCommand;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.resources.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.*;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ClosePrescriptionTest {
  private static PharmacyActor pharmacy;
  private static UseTheErpClient useErpClient;
  private static ErxMedicationDispense erxMedicationDispense;

  @BeforeAll
  static void setup() {
    CoverageReporter.getInstance().startTestcase("don't care");
    // init pharmacy
    pharmacy = new PharmacyActor("Am Flughafen");
    useErpClient = mock(UseTheErpClient.class);

    val useSmcb = mock(UseSMCB.class);
    when(useSmcb.getTelematikID()).thenReturn("Telematik-ID");

    pharmacy.can(useErpClient);
    pharmacy.can(useSmcb);

    erxMedicationDispense = new ErxMedicationDispense();
  }

  @ParameterizedTest
  @ValueSource(strings = {"1.3.0", "1.4.0"})
  @ClearSystemProperty(key = "erp.fhir.profile")
  void shouldPerformClosePrescription(String version) {
    System.setProperty("erp.fhir.profile", version);

    val mockAcceptBundle = mock(ErxAcceptBundle.class);
    val mockResponse = (ErpResponse<ErxAcceptBundle>) mock(ErpResponse.class);
    val mockTask = mock(ErxTask.class);

    when(mockResponse.getExpectedResource()).thenReturn(mockAcceptBundle);
    when(mockAcceptBundle.getTaskId()).thenReturn(TaskId.from("1234567890"));
    when(mockAcceptBundle.getSecret()).thenReturn(new Secret("secret"));
    when(mockAcceptBundle.getKbvBundleAsString()).thenReturn("EMPTY");
    when(mockAcceptBundle.getTask()).thenReturn(mockTask);
    when(mockTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
    when(mockTask.getForKvnr()).thenReturn(Optional.of(KVNR.from("X123456789")));
    when(useErpClient.decode(eq(KbvErpBundle.class), any()))
        .thenReturn(KbvErpBundleFaker.builder().withKvnr(KVNR.from("X123456789")).fake());

    val acceptResponse =
        ErpResponse.forPayload(mockAcceptBundle, ErxAcceptBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    val acceptInteraction = new ErpInteraction<>(acceptResponse);

    val resource = new ErxReceipt();
    val response =
        ErpResponse.forPayload(resource, ErxReceipt.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(CloseTaskCommand.class))).thenReturn(response);

    val closePrescription = ClosePrescription.acceptedWith(acceptInteraction);
    assertDoesNotThrow(() -> pharmacy.performs(closePrescription));
  }

  @ParameterizedTest
  @ValueSource(strings = {"1.3.0", "1.4.0"})
  @ClearSystemProperty(key = "erp.fhir.profile")
  void shouldClosePrescriptionWithAlternativeDates(String version) {
    System.setProperty("erp.fhir.profile", version);

    val mockAcceptBundle = mock(ErxAcceptBundle.class);
    val mockResponse = (ErpResponse<ErxAcceptBundle>) mock(ErpResponse.class);
    val mockTask = mock(ErxTask.class);

    when(mockResponse.getExpectedResource()).thenReturn(mockAcceptBundle);
    when(mockAcceptBundle.getTaskId()).thenReturn(TaskId.from("1234567890"));
    when(mockAcceptBundle.getSecret()).thenReturn(new Secret("secret"));
    when(mockAcceptBundle.getKbvBundleAsString()).thenReturn("EMPTY");
    when(mockAcceptBundle.getTask()).thenReturn(mockTask);
    when(mockTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
    when(mockTask.getForKvnr()).thenReturn(Optional.of(KVNR.from("X123456789")));
    when(useErpClient.decode(eq(KbvErpBundle.class), any()))
        .thenReturn(KbvErpBundleFaker.builder().withKvnr(KVNR.from("X123456789")).fake());

    val acceptResponse =
        ErpResponse.forPayload(mockAcceptBundle, ErxAcceptBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    val acceptInteraction = new ErpInteraction<>(acceptResponse);

    val resource = new ErxReceipt();
    val response =
        ErpResponse.forPayload(resource, ErxReceipt.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(CloseTaskCommand.class))).thenReturn(response);

    val closePrescription =
        ClosePrescription.alternative().acceptedWith(acceptInteraction, new Date(), new Date());
    assertDoesNotThrow(() -> pharmacy.performs(closePrescription));
  }

  @Test
  void shouldApplyAllMutators() {
    FuzzingMutator<ErxMedicationDispense> mutator1 = mock(FuzzingMutator.class);
    FuzzingMutator<ErxMedicationDispense> mutator2 = mock(FuzzingMutator.class);

    NamedEnvelope<FuzzingMutator<ErxMedicationDispense>> envelope1 =
        new NamedEnvelope<>("mutator1", mutator1);
    NamedEnvelope<FuzzingMutator<ErxMedicationDispense>> envelope2 =
        new NamedEnvelope<>("mutator2", mutator2);

    List<NamedEnvelope<FuzzingMutator<ErxMedicationDispense>>> mutators =
        Arrays.asList(envelope1, envelope2);

    ErxMedicationDispense target = new ErxMedicationDispense();

    applyMutators(mutators, target);

    verify(mutator1).accept(target);
    verify(mutator2).accept(target);
  }
}
