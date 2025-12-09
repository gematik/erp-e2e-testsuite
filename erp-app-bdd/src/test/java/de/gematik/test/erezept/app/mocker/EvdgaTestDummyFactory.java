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

package de.gematik.test.erezept.app.mocker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.smartcards.SmartcardOwnerData;
import de.gematik.bbriccs.smartcards.SmcB;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.questions.MovingToPrescription;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;

public class EvdgaTestDummyFactory {

  public static Actor createTestActor(ManageDataMatrixCodes dmcList) {
    // assemble the screenplay
    val userName = GemFaker.fakerName();
    val theAppUser = spy(OnStage.theActorCalled(userName));

    val appAbility = mock(UseIOSApp.class);
    when(appAbility.getPlatformType()).thenReturn(PlatformType.IOS);
    when(theAppUser.abilityTo(UseIOSApp.class)).thenReturn(appAbility);

    when(theAppUser.abilityTo(ManageDataMatrixCodes.class)).thenReturn(dmcList);

    val erpClient = mock(ErpClient.class);
    val erpClientAbility = mock(UseTheErpClient.class);
    when(theAppUser.abilityTo(UseTheErpClient.class)).thenReturn(erpClientAbility);
    when(erpClientAbility.getClient()).thenReturn(erpClient);

    val egkAbility = mock(ProvideEGK.class);
    when(theAppUser.abilityTo(ProvideEGK.class)).thenReturn(egkAbility);
    when(egkAbility.getKvnr()).thenReturn(mock(KVNR.class));

    return theAppUser;
  }

  public static ErxPrescriptionBundle setupTestBundle(
      ManageDataMatrixCodes dmcList, Actor theAppUser) {
    val evdgaBundle = mock(ErxPrescriptionBundle.class);
    val prescriptionId = PrescriptionId.random();
    val accessCode = AccessCode.random();
    val taskId = TaskId.from(prescriptionId);
    val task = mock(ErxTask.class);

    when(evdgaBundle.getTask()).thenReturn(task);
    when(evdgaBundle.getTask().getTaskId()).thenReturn(taskId);
    when(evdgaBundle.getTask().getAccessCode()).thenReturn(accessCode);

    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    // Return the evdgaBundle immediately, instead of mocking for all of MovingToPrescription
    doReturn(Optional.of(evdgaBundle)).when(theAppUser).asksFor(any(MovingToPrescription.class));

    return evdgaBundle;
  }

  public static Actor createTestInsurance() {
    val ktrName = GemFaker.fakerName();
    val ktr = spy(OnStage.theActorCalled(ktrName));
    val commsAbility = mock(ManageCommunications.class);
    val smcbAbility = mock(UseSMCB.class);
    when(ktr.abilityTo(ManageCommunications.class)).thenReturn(commsAbility);
    when(ktr.abilityTo(UseSMCB.class)).thenReturn(smcbAbility);

    when(smcbAbility.getSmcB()).thenReturn(mock(SmcB.class));
    when(smcbAbility.getSmcB().getOwnerData()).thenReturn(mock(SmartcardOwnerData.class));
    when(smcbAbility.getSmcB().getOwnerData().getCommonName()).thenReturn(ktrName);

    return ktr;
  }
}
