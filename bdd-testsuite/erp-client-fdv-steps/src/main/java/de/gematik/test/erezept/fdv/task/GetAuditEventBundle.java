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

package de.gematik.test.erezept.fdv.task;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.AuditEventGetByIdCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.questions.FhirResponseQuestion;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GetAuditEventBundle extends FhirResponseQuestion<ErxAuditEventBundle> {
  private final PrescriptionId prescriptionId;

  public static GetAuditEventBundle forPrescription(PrescriptionId prescriptionId) {
    return new GetAuditEventBundle(prescriptionId);
  }

  @Override
  @Step("{0} fragt beim Fachdienst, nach Protokolleinträgen zur Prescription #prescriptionId")
  public ErpResponse<ErxAuditEventBundle> answeredBy(Actor actor) {
    log.info(
        "Actor {} fetches AuditEvents for PrescriptionId {}",
        actor.getName(),
        prescriptionId.getValue());
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);

    val cmd = new AuditEventGetByIdCommand(prescriptionId);
    return erpClient.request(cmd);
  }
}
