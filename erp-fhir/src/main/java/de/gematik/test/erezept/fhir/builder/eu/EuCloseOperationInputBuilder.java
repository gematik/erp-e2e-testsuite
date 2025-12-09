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

package de.gematik.test.erezept.fhir.builder.eu;

import static de.gematik.test.erezept.fhir.valuesets.eu.EuPartNaming.*;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.r4.eu.*;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Parameters;

@Slf4j
public class EuCloseOperationInputBuilder
    extends ResourceBuilder<EuCloseOperationInput, EuCloseOperationInputBuilder> {

  private final List<Pair<EuMedicationDispense, GemErpMedication>> pharmaceuticalDispensations =
      new LinkedList<>();
  private EuVersion version = EuVersion.getDefaultVersion();

  private Parameters.ParametersParameterComponent requestData;
  private EuPractitionerRole practitionerRole;
  private EuOrganization organization;
  private EuPractitioner practitioner;

  public EuCloseOperationInputBuilder withOptionalRxDispensation(
      EuMedicationDispense medicationDispense, GemErpMedication medication) {
    this.pharmaceuticalDispensations.add(Pair.of(medicationDispense, medication));
    return this;
  }

  public EuCloseOperationInputBuilder practitioner(EuPractitioner practitioner) {
    this.practitioner = practitioner;
    return this;
  }

  public EuCloseOperationInputBuilder organization(EuOrganization organization) {
    this.organization = organization;
    return this;
  }

  public EuCloseOperationInputBuilder version(EuVersion version) {
    this.version = version;
    return this;
  }

  public EuCloseOperationInputBuilder practitionerRole(EuPractitionerRole practitionerRole) {
    this.practitionerRole = practitionerRole;
    return this;
  }

  public EuCloseOperationInputBuilder requestData(
      Parameters.ParametersParameterComponent requestData) {
    this.requestData = requestData;
    return this;
  }

  public EuCloseOperationInputBuilder requestDataFromGetPrescriptionInput(
      EuGetPrescriptionInput getPrescriptionInput) {
    val reqData = getPrescriptionInput.getRequestData();
    reqData.getPart().removeIf(p -> REQUEST_TYPE.getCode().equals(p.getName()));
    this.requestData = reqData;
    return this;
  }

  public static EuCloseOperationInputBuilder builder() {
    return new EuCloseOperationInputBuilder();
  }

  public static EuCloseOperationInputBuilder builder(
      EuMedicationDispense medicationDispense, GemErpMedication medication) {
    val builder = builder();
    builder.pharmaceuticalDispensations.add(Pair.of(medicationDispense, medication));
    return builder;
  }

  @Override
  public EuCloseOperationInput build() {
    checkRequired();
    val resource =
        this.createResource(
            EuCloseOperationInput::new, GemErpEuStructDef.EU_MED_DSP_CLOSE_INPUT, version);

    this.pharmaceuticalDispensations.forEach(
        disp -> {
          val rxDispensation = resource.addParameter().setName(RX_DISPENSATION.getCode());
          val md = disp.getLeft();
          val medication = disp.getRight();

          rxDispensation.addPart().setName(MED_DISPENSE.getCode()).setResource(md);
          rxDispensation.addPart().setName("medication").setResource(medication);
        });

    resource.addParameter(requestData);
    resource.addParameter().setName(PRACTITIONER_DATA.getCode()).setResource(practitioner);
    resource.addParameter().setName(PRACTITIONER_ROLE_DATA.getCode()).setResource(practitionerRole);
    resource.addParameter().setName(ORGANIZATION_DATA.getCode()).setResource(organization);

    return resource;
  }

  private void checkRequired() {
    checkRequired(requestData, "requestData is required in EuDspCloseOperationInputBuilder");
    checkRequired(practitioner, "practitioner is required in EuDspCloseOperationInputBuilder");
    checkRequired(
        practitionerRole, "practitionerRole is required in EuDspCloseOperationInputBuilder");
    checkRequired(organization, "organization is required in EuDspCloseOperationInputBuilder");
    checkRequiredList(
        this.pharmaceuticalDispensations,
        1,
        "At least one pair of MedicationDispense and Medication is required for dispensing"
            + " pharmaceuticals");
  }
}
