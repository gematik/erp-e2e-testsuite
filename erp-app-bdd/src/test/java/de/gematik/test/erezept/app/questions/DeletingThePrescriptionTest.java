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

package de.gematik.test.erezept.app.questions;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionTechnicalInformation;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;

class DeletingThePrescriptionTest {
  private String userName;
  private KbvErpBundle kbvBundle;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(userName);
    val erpClientAbility = mock(UseTheErpClient.class);
    givenThat(theAppUser).can(erpClientAbility);
    val mdmc = ManageDataMatrixCodes.sheGetsPrescribed();
    givenThat(theAppUser).can(mdmc);

    kbvBundle = KbvErpBundleFaker.builder().fake();
    mdmc.appendDmc(
        DmcPrescription.ownerDmc(
            TaskId.from(kbvBundle.getPrescriptionId().getValue()), AccessCode.random()));

    val mockPrescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(mockPrescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    val mockResponse =
        ErpResponse.forPayload(mockPrescriptionBundle, ErxPrescriptionBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(erpClientAbility.request(any(TaskGetByIdCommand.class))).thenReturn(mockResponse);

    // make sure the teardown does not run into an NPE
    val tearDownResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), Resource.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(erpClientAbility.request(any(TaskAbortCommand.class))).thenReturn(tearDownResponse);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldCheckPrescriptionCannotBeDeleted() {
    val useMockAppAbility = mock(UseIOSApp.class);
    val mockElement = mock(WebElement.class);
    when(useMockAppAbility.getWebElement(any())).thenReturn(mockElement); // pretend to find one
    when(useMockAppAbility.getWebElementListLen(any())).thenReturn(1);

    when(useMockAppAbility.getText(PrescriptionTechnicalInformation.TASKID))
        .thenReturn(kbvBundle.getPrescriptionId().getValue());
    when(useMockAppAbility.isPresent(PrescriptionDetails.PRESCRIPTION_CANNOT_BE_DELETED_INFO))
        .thenReturn(true);
    val actor = OnStage.theActor(userName).can(useMockAppAbility);
    assertTrue(actor.asksFor(DeletingThePrescription.canNotBePerformedFor("letzte")));
  }
}
