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

public class EpaOpProvidePrescription extends Parameters {

  public RxPrescriptionId getEpaPrescriptionId() {
    return getRxPrescriptionParameter().getPart().stream()
        .filter(part -> part.getName().equals("prescriptionId"))
        .map(p -> p.getValue().castToIdentifier(p.getValue()))
        .map(RxPrescriptionId::from)
        .findFirst()
        .orElseThrow();
  }

  public Date getEpaAuthoredOn() {
    return getRxPrescriptionParameter().getPart().stream()
        .filter(part -> part.getName().equals("authoredOn"))
        .map(p -> p.getValue().castToDate(p.getValue()).getValue())
        .findFirst()
        .orElseThrow();
  }

  public EpaMedication getEpaMedication() {
    return getRxPrescriptionParameter().getPart().stream()
        .filter(entry -> entry.getName().equals("medication"))
        .filter(entry -> EpaMedicationStructDef.EPA_MEDICATION.matches(entry.getResource()))
        .map(entry -> (EpaMedication) entry.getResource())
        .findFirst()
        .orElseThrow();
  }

  public EpaMedicationRequest getEpaMedicationRequest() {
    return getRxPrescriptionParameter().getPart().stream()
        .filter(entry -> entry.getName().equals("medicationRequest"))
        .filter(entry -> EpaMedicationStructDef.EPA_MEDICATION_REQUEST.matches(entry.getResource()))
        .map(entry -> (EpaMedicationRequest) entry.getResource())
        .findFirst()
        .orElseThrow();
  }

  public EpaOrganisation getEpaOrganisation() {
    return getRxPrescriptionParameter().getPart().stream()
        .filter(entry -> entry.getName().equals("organization"))
        .filter(entry -> GematikDirStrucDef.ORGANIZATION.matches(entry.getResource()))
        .map(entry -> (EpaOrganisation) entry.getResource())
        .findFirst()
        .orElseThrow();
  }

  public EpaPractitioner getEpaPractitioner() {
    return getRxPrescriptionParameter().getPart().stream()
        .filter(entry -> entry.getName().equals("practitioner"))
        .filter(entry -> GematikDirStrucDef.PRACTITIONER.matches(entry.getResource()))
        .map(entry -> (EpaPractitioner) entry.getResource())
        .findFirst()
        .orElseThrow();
  }

  private ParametersParameterComponent getRxPrescriptionParameter() {
    return this.getParameter().stream()
        .filter(p -> p.getName().equals("rxPrescription"))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), "rxPrescription"));
  }
}
