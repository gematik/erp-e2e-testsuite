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

package de.gematik.test.erezept.tasks;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.actions.eu.EnsureEuConsent;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.eu.EuConsentDeleteCommand;
import de.gematik.test.erezept.client.usecases.eu.EuConsentGetCommand;
import de.gematik.test.erezept.client.usecases.eu.EuConsentPostCommand;
import de.gematik.test.erezept.fhir.builder.eu.EuConsentBuilder;
import de.gematik.test.erezept.fhir.r4.eu.EuConsent;
import de.gematik.test.erezept.fhir.r4.eu.EuConsentBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.util.List;
import java.util.Map;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnsureEuConsentTest extends ErpFhirBuildingTest {

  @BeforeEach
  void init() {
    OnStage.setTheStage(new Cast() {});
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldFailOnOperationOutcomeDuringGet() {
    val useErpClient = mock(UseTheErpClient.class);
    val patient = new PatientActor("MockPatient");
    val kvnr = KVNR.random();
    patient.can(useErpClient);
    patient.can(ProvidePatientBaseData.forGkvPatient(kvnr, patient.getName()));

    val getResponse =
        ErpResponse.forPayload(createOperationOutcome(), EuConsentBundle.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(EuConsentGetCommand.class))).thenReturn(getResponse);

    val postResponse =
        ErpResponse.forPayload(EuConsentBuilder.forKvnr(kvnr).build(), EuConsent.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(EuConsentPostCommand.class))).thenReturn(postResponse);

    val task = EnsureEuConsent.shouldBePresent();
    assertDoesNotThrow(() -> task.performAs(patient));

    verify(useErpClient, times(1)).request(any(EuConsentGetCommand.class));
    verify(useErpClient, times(1)).request(any(EuConsentPostCommand.class));
    verify(useErpClient, never()).request(any(EuConsentDeleteCommand.class));
  }

  @Test
  void shouldEnsureConsentIsAlreadySet() {
    val useErpClient = mock(UseTheErpClient.class);
    val patient = new PatientActor("MockPatient");
    patient.can(useErpClient);
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    val consent = EuConsentBuilder.forKvnr(patient.getKvnr()).build();

    val entry = new Bundle.BundleEntryComponent();
    entry.setResource(consent);

    val bundle = new EuConsentBundle();
    bundle.setEntry(List.of(entry));

    val getResponse =
        ErpResponse.forPayload(bundle, EuConsentBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());

    when(useErpClient.request(any(EuConsentGetCommand.class))).thenReturn(getResponse);

    assertDoesNotThrow(() -> patient.attemptsTo(EnsureEuConsent.shouldBePresent()));

    verify(useErpClient, times(1)).request(any(EuConsentGetCommand.class));
    verify(useErpClient, never()).request(any(EuConsentPostCommand.class));
    verify(useErpClient, never()).request(any(EuConsentDeleteCommand.class));
  }

  @Test
  void shouldEnsureConsentIsSet() {
    val useErpClient = mock(UseTheErpClient.class);
    val patient = new PatientActor("MockPatient");
    patient.can(useErpClient);
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    val emptyConsentBundle = new EuConsentBundle();
    emptyConsentBundle.setEntry(List.of());

    val getResponse =
        ErpResponse.forPayload(emptyConsentBundle, EuConsentBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(EuConsentGetCommand.class))).thenReturn(getResponse);

    val consent = EuConsentBuilder.forKvnr(patient.getKvnr()).build();
    val postResponse =
        ErpResponse.forPayload(consent, EuConsent.class)
            .withStatusCode(201)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(EuConsentPostCommand.class))).thenReturn(postResponse);

    assertDoesNotThrow(() -> patient.attemptsTo(EnsureEuConsent.shouldBePresent()));

    verify(useErpClient, times(1)).request(any(EuConsentGetCommand.class));
    verify(useErpClient, times(1)).request(any(EuConsentPostCommand.class));
    verify(useErpClient, never()).request(any(EuConsentDeleteCommand.class));
  }

  @Test
  void shouldEnsureConsentIsAlreadyUnset() {
    val useErpClient = mock(UseTheErpClient.class);
    val patient = new PatientActor("MockPatient");
    patient.can(useErpClient);
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    val emptyConsentBundle = new EuConsentBundle();
    emptyConsentBundle.setEntry(List.of());

    val getResponse =
        ErpResponse.forPayload(emptyConsentBundle, EuConsentBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(EuConsentGetCommand.class))).thenReturn(getResponse);

    val deleteResponse =
        ErpResponse.forPayload(emptyConsentBundle, EmptyResource.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(EuConsentDeleteCommand.class))).thenReturn(deleteResponse);

    assertDoesNotThrow(() -> patient.attemptsTo(EnsureEuConsent.shouldBeSet(false)));

    verify(useErpClient, times(1)).request(any(EuConsentGetCommand.class));
    verify(useErpClient, never()).request(any(EuConsentPostCommand.class));
    verify(useErpClient, never()).request(any(EuConsentDeleteCommand.class));
  }

  @Test
  void shouldEnsureConsentIdIsUnset() {
    val useErpClient = mock(UseTheErpClient.class);
    val patient = new PatientActor("MockPatient");
    patient.can(useErpClient);
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    val consent = EuConsentBuilder.forKvnr(patient.getKvnr()).build();
    consent.setId("Consent.meta");

    val entry = new Bundle.BundleEntryComponent();
    entry.setResource(consent);

    val consentBundle = new EuConsentBundle();
    consentBundle.setEntry(List.of(entry));

    val getResponse =
        ErpResponse.forPayload(consentBundle, EuConsentBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(EuConsentGetCommand.class))).thenReturn(getResponse);

    val deleteResponse =
        ErpResponse.forPayload(mock(Resource.class), EmptyResource.class)
            .withStatusCode(204)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(EuConsentDeleteCommand.class))).thenReturn(deleteResponse);

    assertDoesNotThrow(() -> patient.attemptsTo(EnsureEuConsent.shouldBeSet(false)));

    verify(useErpClient, times(1)).request(any(EuConsentGetCommand.class));
    verify(useErpClient, never()).request(any(EuConsentPostCommand.class));
    verify(useErpClient, times(1)).request(any(EuConsentDeleteCommand.class));
  }
}
