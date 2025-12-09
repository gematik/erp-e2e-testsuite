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

package de.gematik.test.erezept.fhir.r4.eu;

import static de.gematik.test.erezept.fhir.valuesets.eu.EuPartNaming.*;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;

@ResourceDef(name = "Parameters")
public class EuCloseOperationInput extends Parameters {

  public Optional<PrescriptionId> getPrescriptionId() {
    return this.getParameter().stream()
        .filter(p -> p.getName().equals(RX_DISPENSATION.getCode()))
        .findFirst()
        .map(
            param ->
                param.getPart().stream()
                    .filter(part -> part.getName().equals(MED_DISPENSE.getCode()))
                    .map(ParametersParameterComponent::getResource)
                    .map(EuMedicationDispense.class::cast)
                    .findFirst())
        .orElseThrow()
        .orElseThrow()
        .getIdentifier()
        .stream()
        .filter(ErpWorkflowNamingSystem.PRESCRIPTION_ID::matches)
        .findFirst()
        .map(Identifier::getValue)
        .map(PrescriptionId::from);
  }

  public Optional<ParametersParameterComponent> getFirstRxDispension() {
    return this.getParameter().stream()
        .filter(p -> p.getName().equals(RX_DISPENSATION.getCode()))
        .findFirst();
  }

  public List<ParametersParameterComponent> getRxDispensions() {
    return this.getParameter().stream()
        .filter(p -> p.getName().equals(RX_DISPENSATION.getCode()))
        .toList();
  }

  public EuPractitioner getPractitionerData() {
    return (EuPractitioner)
        this.getParameter().stream()
            .filter(para -> para.getName().equals(PRACTITIONER_DATA.getCode()))
            .findFirst()
            .orElseThrow()
            .getResource();
  }

  public Optional<ParametersParameterComponent> getRequestData() {
    return this.getParameter().stream()
        .filter(para -> para.getName().equals(REQUEST_DATA.getCode()))
        .findFirst();
  }

  public EuOrganization getOrganizationData() {
    return (EuOrganization)
        this.getParameter().stream()
            .filter(para -> para.getName().equals(ORGANIZATION_DATA.getCode()))
            .findFirst()
            .orElseThrow()
            .getResource();
  }

  public EuPractitionerRole getPractitionerRoleData() {
    return (EuPractitionerRole)
        this.getParameter().stream()
            .filter(para -> para.getName().equals(PRACTITIONER_ROLE_DATA.getCode()))
            .findFirst()
            .orElseThrow()
            .getResource();
  }
}
