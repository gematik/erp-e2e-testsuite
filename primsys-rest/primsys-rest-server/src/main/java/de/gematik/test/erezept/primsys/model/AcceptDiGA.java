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

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.client.usecases.TaskAcceptCommand;
import de.gematik.test.erezept.fhir.r4.kbv.KbvEvdgaBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.primsys.actors.HealthInsurance;
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto;
import de.gematik.test.erezept.primsys.data.HealthAppRequestDto;
import de.gematik.test.erezept.primsys.data.valuesets.PatientInsuranceTypeDto;
import de.gematik.test.erezept.primsys.mapping.CoverageDataMapper;
import de.gematik.test.konnektor.soap.mock.LocalVerifier;
import jakarta.ws.rs.core.Response;
import lombok.val;

public class AcceptDiGA {

  private final HealthInsurance actor;

  public AcceptDiGA(HealthInsurance ktr) {
    this.actor = ktr;
  }

  public Response acceptPrescription(String taskId, String accessCode) {
    return acceptPrescription(TaskId.from(taskId), AccessCode.from(accessCode));
  }

  public Response acceptPrescription(TaskId taskId, AccessCode accessCode) {
    val acceptCommand = new TaskAcceptCommand(taskId, accessCode);
    val acceptResponse = actor.erpRequest(acceptCommand);
    val acceptedTask = acceptResponse.getExpectedResource();

    val evdgaBundle =
        actor.decode(
            KbvEvdgaBundle.class,
            LocalVerifier.parse(acceptedTask.getSignedKbvBundle()).getDocument());

    val patient = evdgaBundle.getPatient();
    val patientInsuranceType =
        PatientInsuranceTypeDto.fromCode(patient.getInsuranceType().getCode());
    val healthAppRequest = evdgaBundle.getHealthAppRequest();
    val diga =
        HealthAppRequestDto.builder()
            .pzn(healthAppRequest.getPzn().getValue())
            .name(healthAppRequest.getName())
            .ser(healthAppRequest.relatesToSocialCompensationLaw())
            .build();

    val acceptData =
        AcceptedPrescriptionDto.withPrescriptionId(
                acceptedTask.getTask().getPrescriptionId().getValue())
            .forKvnr(patient.getKvnr().getValue(), patientInsuranceType)
            .withAccessCode(acceptedTask.getTask().getAccessCode().getValue())
            .withSecret(acceptedTask.getSecret().getValue())
            .coveredBy(
                CoverageDataMapper.from(evdgaBundle.getCoverage(), evdgaBundle.getPatient())
                    .getDto())
            .prescriptionReference(evdgaBundle.getLogicalId())
            .andDiGA(diga);

    ActorContext.getInstance().addAcceptedPrescription(acceptData);
    return Response.status(acceptResponse.getStatusCode()).entity(acceptData).build();
  }
}
