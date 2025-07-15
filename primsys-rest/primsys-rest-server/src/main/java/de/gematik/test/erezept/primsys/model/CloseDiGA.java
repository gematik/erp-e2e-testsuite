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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.client.usecases.CloseTaskCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseDiGABuilder;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseDiGAFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.primsys.actors.HealthInsurance;
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto;
import de.gematik.test.erezept.primsys.data.DispensedMedicationDto;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import jakarta.ws.rs.core.Response;
import java.util.Date;
import lombok.val;

public class CloseDiGA {

  private final HealthInsurance actor;
  private final ActorContext ctx;

  public CloseDiGA(HealthInsurance actor) {
    this.actor = actor;
    this.ctx = ActorContext.getInstance();
  }

  public Response closePrescription(String prescriptionId, String secret) {
    return closePrescription(PrescriptionId.from(prescriptionId), Secret.from(secret));
  }

  public Response closePrescription(PrescriptionId prescriptionId, Secret secret) {
    val accepted = this.getAcceptedPrescription(prescriptionId);

    // Note: accepted already carries the secret; there is no need to provide this on from outside!!
    val closeCommand = this.createCloseCommand(accepted, secret);
    val closeResponse = actor.erpRequest(closeCommand);
    val body = closeResponse.getExpectedResource();

    val dispensedData =
        this.storeDispensedMedication(prescriptionId, secret, accepted, body.getId());
    return Response.status(closeResponse.getStatusCode()).entity(dispensedData).build();
  }

  public Response declinePrescription(String prescriptionId, String secret) {
    return declinePrescription(PrescriptionId.from(prescriptionId), Secret.from(secret));
  }

  public Response declinePrescription(PrescriptionId prescriptionId, Secret secret) {
    val accepted = this.getAcceptedPrescription(prescriptionId);

    // Note: accepted already carries the secret; there is no need to provide this on from outside!!
    val declineCommand = this.createDeclineCloseCommand(accepted, secret);
    val declineResponse = actor.erpRequest(declineCommand);
    val body = declineResponse.getExpectedResource();

    val dispensedData =
        this.storeDispensedMedication(prescriptionId, secret, accepted, body.getId());
    return Response.status(declineResponse.getStatusCode()).entity(dispensedData).build();
  }

  private AcceptedPrescriptionDto getAcceptedPrescription(PrescriptionId prescriptionId) {
    return ctx.getAcceptedPrescription(prescriptionId.getValue())
        .orElseThrow(
            () ->
                ErrorResponseBuilder.createInternalErrorException(
                    404,
                    format(
                        "no prescription with PrescriptionId {0} was accepted", prescriptionId)));
  }

  private CloseTaskCommand createCloseCommand(AcceptedPrescriptionDto accepted, Secret secret) {
    val prescriptionId = accepted.getPrescriptionId();
    val type = InsuranceTypeDe.fromCode(accepted.getPatientInsuranceType().getCode());
    val kvnr = KVNR.from(accepted.getForKvnr(), type);
    val healthApp = accepted.getDiga();

    val operationBuilder = GemOperationInputParameterBuilder.forClosingDiGA();
    val md =
        ErxMedicationDispenseDiGAFaker.builder()
            .withKvnr(kvnr)
            .withPrescriptionId(prescriptionId)
            .withPerformer(this.actor.getSmcb().getTelematikId())
            .withPzn(healthApp.getPzn(), healthApp.getName())
            .fake();

    val operationParams = operationBuilder.with(md).build();
    return new CloseTaskCommand(TaskId.from(prescriptionId), secret, operationParams);
  }

  private CloseTaskCommand createDeclineCloseCommand(
      AcceptedPrescriptionDto accepted, Secret secret) {
    val prescriptionId = accepted.getPrescriptionId();
    val type = InsuranceTypeDe.fromCode(accepted.getPatientInsuranceType().getCode());
    val kvnr = KVNR.from(accepted.getForKvnr(), type);

    val operationBuilder = GemOperationInputParameterBuilder.forClosingDiGA();
    val md =
        ErxMedicationDispenseDiGABuilder.forKvnr(kvnr)
            .prescriptionId(prescriptionId)
            .performerId(this.actor.getSmcb().getTelematikId())
            .note(GemFaker.getFaker().backToTheFuture().quote())
            .build();

    val operationParams = operationBuilder.with(md).build();
    return new CloseTaskCommand(TaskId.from(prescriptionId), secret, operationParams);
  }

  private DispensedMedicationDto storeDispensedMedication(
      PrescriptionId prescriptionId,
      Secret secret,
      AcceptedPrescriptionDto accepted,
      String receiptId) {

    val dispensedData = new DispensedMedicationDto();
    dispensedData.setPrescriptionId(prescriptionId.getValue());
    dispensedData.setSecret(secret.getValue());
    dispensedData.setAcceptData(accepted);
    dispensedData.setReceipt(receiptId);
    dispensedData.setDispensedDate(new Date());
    // intentionally skip medication, for DiGAs we always close with the info from the original
    // prescription

    this.ctx.addDispensedMedications(dispensedData);
    return dispensedData;
  }
}
