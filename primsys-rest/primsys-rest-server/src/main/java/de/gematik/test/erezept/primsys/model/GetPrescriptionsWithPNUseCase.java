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

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.client.usecases.TaskGetByExamEvidenceCommand;
import de.gematik.test.erezept.fhir.values.Value;
import de.gematik.test.erezept.primsys.actors.Pharmacy;
import de.gematik.test.erezept.primsys.data.ShallowPrescriptionDto;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class GetPrescriptionsWithPNUseCase {

  private final Pharmacy pharmacy;

  public GetPrescriptionsWithPNUseCase(Pharmacy pharmacy) {
    this.pharmacy = pharmacy;
  }

  public Response getPrescriptionsByEvidence(String examEvidence) {
    val response = this.pharmacy.erpRequest(new TaskGetByExamEvidenceCommand(examEvidence));
    val prescriptionsAsDto = new ArrayList<ShallowPrescriptionDto>();
    val resource =
        response
            .getResourceOptional()
            .orElseThrow(() -> ErrorResponseBuilder.createFachdienstErrorException(response));

    resource
        .getTasks()
        .forEach(
            erxTask -> {
              val dto = new ShallowPrescriptionDto();
              dto.setPrescriptionId(erxTask.getPrescriptionId().getValue());
              dto.setTaskId(erxTask.getTaskId().getValue());
              dto.setAccessCode(erxTask.getAccessCode().getValue());
              dto.setKvnr(erxTask.getForKvnr().map(Value::getValue).orElse("entry is empty"));

              prescriptionsAsDto.add(dto);
            });
    return Response.status(response.getStatusCode()).entity(prescriptionsAsDto).build();
  }

  public Response getPrescriptionByKvnr(String kvnr) {
    log.warn(
        "GetPrescriptionsWithPNUseCase.getPrescriptionByKvnr({}) was called but is not implemented,"
            + " yet",
        kvnr);
    return ErrorResponseBuilder.createInternalError(400, "not yet implemented");
  }
}
