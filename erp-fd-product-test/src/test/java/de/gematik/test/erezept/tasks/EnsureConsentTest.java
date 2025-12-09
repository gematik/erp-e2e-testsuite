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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ConsentDeleteCommand;
import de.gematik.test.erezept.client.usecases.ConsentGetCommand;
import de.gematik.test.erezept.client.usecases.ConsentPostCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxConsent;
import de.gematik.test.erezept.fhir.r4.erp.ErxConsentBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.util.Map;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnsureConsentTest extends ErpFhirBuildingTest {

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
    val patient = new PatientActor("MocPatient");
    patient.can(useErpClient);
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    val getConsentResponse =
        ErpResponse.forPayload(createOperationOutcome(), ErxConsentBundle.class)
            .withStatusCode(400)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(ConsentGetCommand.class))).thenReturn(getConsentResponse);

    val task = EnsureConsent.shouldBePresent();
    assertThrows(UnexpectedResponseResourceError.class, () -> task.performAs(patient));
    verify(useErpClient, times(1)).request(any(ConsentGetCommand.class));
    verify(useErpClient, times(0)).request(any(ConsentPostCommand.class));
    verify(useErpClient, times(0)).request(any(ConsentDeleteCommand.class));
  }

  @Test
  void shouldEnsureConsentIsAlreadySet() {
    val useErpClient = mock(UseTheErpClient.class);
    val patient = new PatientActor("MocPatient");
    patient.can(useErpClient);
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    val consentBundle = mock(ErxConsentBundle.class);
    when(consentBundle.hasConsent()).thenReturn(true); // consent is already set
    val getConsentResponse =
        ErpResponse.forPayload(consentBundle, ErxConsentBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(ConsentGetCommand.class))).thenReturn(getConsentResponse);

    assertDoesNotThrow(() -> patient.attemptsTo(EnsureConsent.shouldBePresent()));
    verify(useErpClient, times(1)).request(any(ConsentGetCommand.class));
    verify(useErpClient, times(0)).request(any(ConsentPostCommand.class));
    verify(useErpClient, times(0)).request(any(ConsentDeleteCommand.class));
  }

  @Test
  void shouldEnsureConsentIsSet() {
    val useErpClient = mock(UseTheErpClient.class);
    val patient = new PatientActor("MocPatient");
    patient.can(useErpClient);
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    val consentBundle = mock(ErxConsentBundle.class);
    when(consentBundle.hasConsent()).thenReturn(false); // consent is not set yet
    val getConsentResponse =
        ErpResponse.forPayload(consentBundle, ErxConsentBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(ConsentGetCommand.class))).thenReturn(getConsentResponse);

    val postConsentResponse =
        ErpResponse.forPayload(mock(ErxConsent.class), ErxConsent.class)
            .withStatusCode(201)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(ConsentPostCommand.class))).thenReturn(postConsentResponse);

    assertDoesNotThrow(() -> patient.attemptsTo(EnsureConsent.shouldBePresent()));
    verify(useErpClient, times(1)).request(any(ConsentGetCommand.class));
    verify(useErpClient, times(1)).request(any(ConsentPostCommand.class));
    verify(useErpClient, times(0)).request(any(ConsentDeleteCommand.class));
  }

  @Test
  void shouldEnsureConsentIsAlreadyUnset() {
    val useErpClient = mock(UseTheErpClient.class);
    val patient = new PatientActor("MocPatient");
    patient.can(useErpClient);
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    val consentBundle = mock(ErxConsentBundle.class);
    when(consentBundle.hasConsent()).thenReturn(false); // consent is already unset
    val getConsentResponse =
        ErpResponse.forPayload(consentBundle, ErxConsentBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(ConsentGetCommand.class))).thenReturn(getConsentResponse);

    assertDoesNotThrow(() -> patient.attemptsTo(EnsureConsent.shouldBeUnset()));
    verify(useErpClient, times(1)).request(any(ConsentGetCommand.class));
    verify(useErpClient, times(0)).request(any(ConsentPostCommand.class));
    verify(useErpClient, times(0)).request(any(ConsentDeleteCommand.class));
  }

  @Test
  void shouldEnsureConsentIsUnset() {
    val useErpClient = mock(UseTheErpClient.class);
    val patient = new PatientActor("MocPatient");
    patient.can(useErpClient);
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));

    val consentBundle = mock(ErxConsentBundle.class);
    when(consentBundle.hasConsent()).thenReturn(true); // consent is already set
    val getConsentResponse =
        ErpResponse.forPayload(consentBundle, ErxConsentBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(ConsentGetCommand.class))).thenReturn(getConsentResponse);

    val postConsentResponse =
        ErpResponse.forPayload(mock(Resource.class), EmptyResource.class)
            .withStatusCode(201)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(useErpClient.request(any(ConsentDeleteCommand.class))).thenReturn(postConsentResponse);

    assertDoesNotThrow(() -> patient.attemptsTo(EnsureConsent.shouldBeUnset()));
    verify(useErpClient, times(1)).request(any(ConsentGetCommand.class));
    verify(useErpClient, times(0)).request(any(ConsentPostCommand.class));
    verify(useErpClient, times(1)).request(any(ConsentDeleteCommand.class));
  }
}
