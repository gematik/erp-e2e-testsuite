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

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.AuditEventGetByIdCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.questions.FhirResponseQuestion;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;

@Slf4j
public class GetAuditEventBundle extends FhirResponseQuestion<ErxAuditEventBundle> {
  private final PrescriptionId prescriptionId;
  private final Actor patient;

  protected GetAuditEventBundle(PrescriptionId prescriptionId, Actor patient) {
    super("Get /AuditEvent");
    this.prescriptionId = prescriptionId;
    this.patient = patient;
  }

  public static Builder forPatient(Actor patient) {
    return new Builder(patient);
  }

  @Override
  @Step("{0} fragt beim Fachdienst, nach Protokolleinträgen zur Prescription #prescriptionId .")
  public ErpResponse<ErxAuditEventBundle> answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(patient, UseTheErpClient.class);

    val auditEvent = erpClient.request(new AuditEventGetByIdCommand(prescriptionId));
    log.info(
        format(
            "Actor {0} received AuditEvent with for PrescriptionId {1}",
            patient.getName(), prescriptionId));

    return auditEvent;
  }

  public static class Builder {
    private final Actor patient;
    private PrescriptionId prescriptionId;

    public Builder(Actor patient) {
      this.patient = patient;
    }

    public Builder forPrescription(PrescriptionId prescriptionId) {
      this.prescriptionId = prescriptionId;
      return this;
    }

    public GetAuditEventBundle build() {
      return new GetAuditEventBundle(prescriptionId, patient);
    }
  }
}
