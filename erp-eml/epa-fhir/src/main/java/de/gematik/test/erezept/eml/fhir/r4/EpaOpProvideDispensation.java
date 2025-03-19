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
 */

package de.gematik.test.erezept.eml.fhir.r4;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef;
import de.gematik.test.erezept.eml.fhir.profile.GematikDirStrucDef;
import de.gematik.test.erezept.eml.fhir.values.RxPrescriptionId;
import java.util.Date;
import org.hl7.fhir.r4.model.Parameters;

public class EpaOpProvideDispensation extends Parameters {

  public Date getEpaAuthoredOn() {
    return getRxDispensationParameter().getPart().stream()
        .filter(part -> part.getName().equals("authoredOn"))
        .map(p -> p.getValue().castToDate(p.getValue()).getValue())
        .findFirst()
        .orElseThrow();
  }

  public Date getEpaWhenHandedOver() {
    return this.getEpaMedicationDispense().getWhenHandedOver();
  }

  public RxPrescriptionId getEpaPrescriptionId() {
    return getRxDispensationParameter().getPart().stream()
        .filter(part -> part.getName().equals("prescriptionId"))
        .map(p -> p.getValue().castToIdentifier(p.getValue()))
        .map(RxPrescriptionId::from)
        .findFirst()
        .orElseThrow();
  }

  public EpaMedicationDispense getEpaMedicationDispense() {
    return getRxDispensationParameter().getPart().stream()
        .filter(entry -> entry.getName().equals("medicationDispense"))
        .filter(
            entry -> EpaMedicationStructDef.EPA_MEDICATION_DISPENSE.matches(entry.getResource()))
        .map(entry -> (EpaMedicationDispense) entry.getResource())
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(), EpaMedicationStructDef.EPA_MEDICATION_DISPENSE));
  }

  public EpaMedication getEpaMedication() {
    return getRxDispensationParameter().getPart().stream()
        .filter(entry -> entry.getName().equals("medication"))
        .filter(entry -> EpaMedicationStructDef.EPA_MEDICATION.matches(entry.getResource()))
        .map(entry -> (EpaMedication) entry.getResource())
        .findFirst()
        .orElseThrow();
  }

  public EpaOrganisation getEpaOrganisation() {
    return getRxDispensationParameter().getPart().stream()
        .filter(entry -> entry.getName().equals("organization"))
        .filter(entry -> GematikDirStrucDef.ORGANIZATION.matches(entry.getResource()))
        .map(entry -> (EpaOrganisation) entry.getResource())
        .findFirst()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), GematikDirStrucDef.ORGANIZATION));
  }

  private ParametersParameterComponent getRxDispensationParameter() {
    return this.getParameter().stream()
        .filter(p -> p.getName().equals("rxDispensation"))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), "rxDispensation"));
  }
}
