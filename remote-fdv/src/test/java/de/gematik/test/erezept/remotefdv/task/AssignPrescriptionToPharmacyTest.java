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

package de.gematik.test.erezept.remotefdv.task;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.remotefdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.remotefdv.client.FdVResponse;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.Communication;

class AssignPrescriptionToPharmacyTest {
  private final SmartcardArchive sca = SmartcardArchive.fromResources();
  private String userName;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(userName);
    val useTheMockRemoteFdVClient = mock(UseTheRemoteFdVClient.class);
    givenThat(theAppUser).can(useTheMockRemoteFdVClient);
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
    givenThat(theAppUser).can(ProvideEGK.sheOwns(sca.getEgk(0)));
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldCorrectlyAssignPrescriptionToPharmacy() {
    val actor = OnStage.theActorCalled(userName);
    val pharmacy = OnStage.theActorCalled("Pharmacy");

    pharmacy.can(ManageDataMatrixCodes.sheGetsPrescribed());
    pharmacy.can(ManageCommunications.heExchanges());
    pharmacy.can(UseSMCB.itHasAccessTo(sca.getSmcB(0)));

    val remoteFdV = actor.abilityTo(UseTheRemoteFdVClient.class);

    val prescriptionId = PrescriptionId.random();
    val accessCode = AccessCode.random();

    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);
    dmcList.appendDmc(DmcPrescription.ownerDmc(TaskId.from(prescriptionId), accessCode));

    val response = new FdVResponse<Communication>();
    val communication = new Communication();
    communication.setReference(prescriptionId.getValue());
    response.setExpectedResource(List.of(communication));

    val assignPrescription = AssignPrescriptionToPharmacy.fromStack("erste").toPharmacy(pharmacy);
    when(remoteFdV.sendRequest(
            PatientRequests.assignToPharmacy(prescriptionId.getValue(), any(), "delivery")))
        .thenReturn(response);
    assertDoesNotThrow(() -> assignPrescription.performAs(actor));

    val pharmacyCommunications = SafeAbility.getAbility(pharmacy, ManageCommunications.class);
    assertFalse(pharmacyCommunications.getExpectedCommunications().isEmpty());
  }
}
