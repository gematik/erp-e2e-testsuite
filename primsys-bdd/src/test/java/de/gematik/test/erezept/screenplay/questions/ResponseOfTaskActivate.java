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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseOfTaskActivate extends FhirResponseQuestion<ErxTask> {

  private final PrescriptionAssignmentKind prescriptionAssignmentKind;
  private final Actor recipient;

  public static ResponseOfTaskActivate asAssingnementTo(
      PrescriptionAssignmentKind prescriptionAssignmentKind, Actor thePatient) {
    return new ResponseOfTaskActivate(prescriptionAssignmentKind, thePatient);
  }

  @Override
  public ErpResponse<ErxTask> answeredBy(Actor actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val konnektorAbility = SafeAbility.getAbility(actor, UseTheKonnektor.class);
    val flowtype = calculateFlowType();

    // create Task
    val createCmd = new TaskCreateCommand(flowtype);
    val createResponse = erpClientAbility.request(createCmd);
    createResponse.getExpectedResource();

    // activate Task
    val draftTask = createResponse.getExpectedResource();
    val prescriptionId = draftTask.getPrescriptionId();
    val kbvBundle =
        KbvErpBundleFaker.builder()
            .withPrescriptionId(prescriptionId)
            .withKvnr(
                SafeAbility.getAbility(recipient, ProvidePatientBaseData.class)
                    .getPatient()
                    .getKvnr())
            .fake();

    val taskId = draftTask.getTaskId();
    val accessCode = draftTask.getOptionalAccessCode().orElseThrow();
    val kbvXml = erpClientAbility.encode(kbvBundle, EncodingType.XML);
    val signedKbv = konnektorAbility.signDocumentWithHba(kbvXml).getPayload();

    val activate = new TaskActivateCommand(taskId, accessCode, signedKbv);
    return erpClientAbility.request(activate);
  }

  private PrescriptionFlowType calculateFlowType() {
    val insuranceType =
        SafeAbility.getAbility(recipient, ProvidePatientBaseData.class)
            .getPatient()
            .getInsuranceType();

    return PrescriptionFlowType.fromInsuranceKind(
        insuranceType,
        prescriptionAssignmentKind.equals(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT));
  }
}
