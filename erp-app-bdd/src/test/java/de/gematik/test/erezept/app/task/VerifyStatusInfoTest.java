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

package de.gematik.test.erezept.app.task;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.hl7.fhir.r4.model.Task.TaskStatus.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VerifyStatusInfoTest {
  private String userName;

  @BeforeEach
  void init() {
    OnStage.setTheStage(new Cast() {});

    val appAbility = mock(UseIOSApp.class);
    when(appAbility.getPlatformType()).thenReturn(PlatformType.IOS);

    // assemble the screenplay
    userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(userName);
    givenThat(theAppUser).can(appAbility);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldHaveTheCorrectStatusInfoForReady() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val task = mock(ErxTask.class);
    val kbvBundle = mock(KbvErpBundle.class);
    val medicationRequest = mock(KbvErpMedicationRequest.class);

    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(task.getStatus()).thenReturn(READY);

    when(app.getText(PrescriptionDetails.PRESCRIPTION_STATUS_TEXT))
        .thenReturn("Noch 2 Tage einlösbar");
    when(task.getAcceptDate())
        .thenReturn(DateConverter.getInstance().localDateToDate(LocalDate.now().plusDays(3)));
    when(task.getAuthoredOn()).thenReturn(new Date());
    when(kbvBundle.getMedicationRequest()).thenReturn(medicationRequest);
    when(medicationRequest.isMultiple()).thenReturn(false);

    val verifyStatusInfoTask = VerifyStatusInfo.forInput(prescriptionBundle);
    assertDoesNotThrow(() -> actor.attemptsTo(verifyStatusInfoTask));
  }

  @ParameterizedTest
  @ValueSource(strings = {"Angenommen vor 1 Sekunde", "Angenommen vor 2 Sekunden"})
  void shouldHaveTheCorrectStatusInfoForInProgress(String status) {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val task = mock(ErxTask.class);
    val kbvBundle = mock(KbvErpBundle.class);

    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(task.getStatus()).thenReturn(INPROGRESS);
    when(app.getText(PrescriptionDetails.PRESCRIPTION_STATUS_INFO)).thenReturn(status);

    val verifyStatusInfoTask = VerifyStatusInfo.forInput(prescriptionBundle);
    assertDoesNotThrow(() -> actor.attemptsTo(verifyStatusInfoTask));
  }

  @Test
  void shouldThrowExceptionForInvalidTaskStatus() {
    val actor = OnStage.theActorCalled(userName);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val task = mock(ErxTask.class);
    val kbvBundle = mock(KbvErpBundle.class);

    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(task.getStatus()).thenReturn(COMPLETED);

    val verifyStatusInfoTask = VerifyStatusInfo.forInput(prescriptionBundle);
    assertThrows(IllegalStateException.class, () -> actor.attemptsTo(verifyStatusInfoTask));
  }
}
