/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.fdv.questions;

import static de.gematik.test.erezept.fhir.parser.ProfileFhirParserFactory.ERP_FHIR_PROFILES_CONFIG;
import static de.gematik.test.erezept.fhir.parser.ProfileFhirParserFactory.ERP_FHIR_PROFILES_TOGGLE;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.conf.ProfilesConfigurator;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.erezept.remotefdv.api.model.Patient;
import de.gematik.erezept.remotefdv.api.model.Prescription;
import de.gematik.erezept.remotefdv.api.model.WorkFlow;
import de.gematik.test.erezept.common.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.fdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.remotefdv.client.FdVResponse;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import java.util.Collections;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TheLastPrescriptionTest {
  // this loads FHIR profiles in the virtual configuration and is needed for the unit tests
  private static ProfilesConfigurator configuration =
      ProfilesConfigurator.getConfiguration(ERP_FHIR_PROFILES_CONFIG, ERP_FHIR_PROFILES_TOGGLE);

  private String userName;
  private String kvnr;

  @BeforeEach
  void setup() {
    OnStage.setTheStage(new Cast() {});
    userName = GemFaker.fakerName();
    kvnr = KVNR.randomStringValue();
    val theAppUser = OnStage.theActorCalled(userName);
    val useTheMockRemoteFdVClient = mock(UseTheRemoteFdVClient.class);

    givenThat(theAppUser).can(useTheMockRemoteFdVClient);
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
    givenThat(theAppUser).can(ProvidePatientBaseData.forGkvPatient(KVNR.from(kvnr), userName));
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldGetPrescription() {
    val prescriptionId = PrescriptionId.random();
    val accessCode = AccessCode.random();
    val actor = OnStage.theActorCalled(userName);
    val remoteFdV = actor.abilityTo(UseTheRemoteFdVClient.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);
    dmcList.appendDmc(DmcPrescription.ownerDmc(TaskId.from(prescriptionId), accessCode));

    val patient = new Patient();
    patient.setKvnr(kvnr);

    val prescription = new Prescription();
    prescription.setPrescriptionId(prescriptionId.getValue());
    prescription.setAccessCode(accessCode.getValue());
    prescription.setWorkFlow(WorkFlow._160);
    prescription.setPatient(patient);

    val response = new FdVResponse<Prescription>();
    response.setResourcesList(List.of(prescription));
    when(remoteFdV.sendRequest(PatientRequests.getPrescriptionById(any()))).thenReturn(response);

    assertTrue(actor.asksFor(TheLastPrescription.from("ausgestellt").existsInBackend()));
    assertTrue(actor.asksFor(TheLastPrescription.prescribed().existsInBackend()));

    // direct assignment
    val directAssignmentPres = new Prescription();
    directAssignmentPres.setWorkFlow(WorkFlow._169);
    directAssignmentPres.setPrescriptionId(prescriptionId.getValue());
    directAssignmentPres.setPatient(patient);

    val response2 = new FdVResponse<Prescription>();
    response2.setResourcesList(List.of(directAssignmentPres));
    when(remoteFdV.sendRequest(PatientRequests.getPrescriptionById(any()))).thenReturn(response2);

    assertTrue(actor.asksFor(TheLastPrescription.from("ausgestellt").existsInBackend()));
    assertTrue(actor.asksFor(TheLastPrescription.prescribed().existsInBackend()));
  }

  @Test
  void shouldNotGetPrescription() {
    val prescriptionId = PrescriptionId.random();
    val accessCode = AccessCode.random();
    val actor = OnStage.theActorCalled(userName);
    val remoteFdV = actor.abilityTo(UseTheRemoteFdVClient.class);
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);
    dmcList.moveToDeleted(DmcPrescription.ownerDmc(TaskId.from(prescriptionId), accessCode));

    val response = new FdVResponse<Prescription>();
    response.setResourcesList(Collections.emptyList());
    when(remoteFdV.sendRequest(PatientRequests.getPrescriptionById(any()))).thenReturn(response);

    assertFalse(actor.asksFor(TheLastPrescription.from("gelöscht").existsInBackend()));
    assertFalse(actor.asksFor(TheLastPrescription.deleted().existsInBackend()));
  }
}
