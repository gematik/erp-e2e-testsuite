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

package de.gematik.test.erezept.actors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.smartcards.SmcB;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.ErpFdTestsuiteFactory;
import de.gematik.test.erezept.actions.MockActorsUtils;
import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.screenplay.abilities.ProvideDoctorBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DoctorActorTest extends ErpFhirBuildingTest {

  @BeforeAll
  static void setup() {
    StopwatchProvider.init();
    CoverageReporter.getInstance().startTestcase("don't care");
  }

  private DoctorActor createDoc(String name) {
    val config = ErpFdTestsuiteFactory.create();
    val doctor = new DoctorActor(name);
    val docConfig = config.getDoctorConfig(doctor.getName());
    val provideBaseData = ProvideDoctorBaseData.fromConfiguration(docConfig, "UselessTestId");
    doctor.can(provideBaseData);

    val smcbMock = mock(SmcB.class);
    when(smcbMock.getTelematikId()).thenReturn("Telematik-ID");
    doctor.can(UseSMCB.itHasAccessTo(smcbMock));

    return doctor;
  }

  @Test
  void shouldProvideCorrectData() {
    val doctor = createDoc("Adelheid Ulmenwald");
    assertNotNull(doctor.getPractitioner());
    assertNotNull(doctor.getMedicalOrganization());
    assertNotNull(doctor.getMedicalOrganization());
  }

  @ParameterizedTest
  @EnumSource(value = QualificationType.class)
  void shouldProvideCorrectDataAfterChange(QualificationType type) {
    val doctor = createDoc("Adelheid Ulmenwald");
    doctor.changeQualificationType(type);
    val bd = SafeAbility.getAbility(doctor, ProvideDoctorBaseData.class);
    assertEquals(type, bd.getPractitioner().getQualificationType());
  }

  @Test
  void shouldHaveTelematikIds() {
    val doctor = createDoc("Adelheid Ulmenwald");
    assertNotNull(doctor.getHbaTelematikId());
    assertNotNull(doctor.getSmcbTelematikId());
  }

  @Test
  void shouldPrescribe() {
    val mockUtil = new MockActorsUtils();
    val prescId = PrescriptionId.random();
    val taskId = TaskId.from(prescId);
    val draftTask = spy(new ErxTask());
    doReturn(taskId).when(draftTask).getTaskId();
    doReturn(prescId).when(draftTask).getPrescriptionId();
    doReturn(AccessCode.random()).when(draftTask).getAccessCode();
    doReturn(Task.TaskStatus.DRAFT).when(draftTask).getStatus();
    val createResponse = mockUtil.createErpResponse(draftTask, ErxTask.class, 201);

    val doc = mockUtil.actorStage.getDoctorNamed("Adelheid Ulmenwald");
    val patient = mockUtil.actorStage.getPatientNamed("Sina Hüllmann");
    when(mockUtil.erpClientMock.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
    when(mockUtil.erpClientMock.request(any(TaskActivateCommand.class))).thenReturn(createResponse);
    val resp = doc.prescribeFor(patient);

    assertEquals(prescId, resp.getPrescriptionId());
  }
}
