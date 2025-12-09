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
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import de.gematik.test.erezept.fhir.valuesets.eu.EuRequestType;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Parameters;

@ResourceDef(name = "Parameters")
public class EuGetPrescriptionInput extends Parameters {

  public IsoCountryCode getIsoCountyCode() {
    return getRequestData().getPart().stream()
        .filter(p -> p.getName().equals(COUNTRY_CODE.getCode()))
        .map(
            para ->
                IsoCountryCode.fromCode(para.getValue().castToCoding(para.getValue()).getCode()))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), COUNTRY_CODE.getCode()));
  }

  public KVNR getKvnr() {
    return getRequestData().getPart().stream()
        .filter(p -> p.getName().equals(EU_KVNR.getCode()))
        .map(parameter -> parameter.getValue().castToIdentifier(parameter.getValue()).getValue())
        .map(KVNR::from)
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), EU_KVNR.getCode()));
  }

  public String getAccessCode() {
    return getRequestData().getPart().stream()
        .filter(p -> p.getName().equals(ACCESS_CODE.getCode()))
        .map(parameter -> parameter.getValue().castToIdentifier(parameter.getValue()).getValue())
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), ACCESS_CODE.getCode()));
  }

  public EuRequestType getEuRequestType() {
    return getRequestData().getPart().stream()
        .filter(p -> p.getName().equals(REQUEST_TYPE.getCode()))
        .map(
            parameter ->
                EuRequestType.fromCode(
                    parameter.getValue().castToCoding(parameter.getValue()).getCode()))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), REQUEST_TYPE.getCode()));
  }

  public String getPointOfCare() {
    return getRequestData().getPart().stream()
        .filter(p -> p.getName().equals(POINT_OF_CARE.getCode()))
        .map(parameter -> parameter.getValue().castToString(parameter.getValue()).getValue())
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), POINT_OF_CARE.getCode()));
  }

  public EuOrganizationProfession getPractitionerRole() {
    return getRequestData().getPart().stream()
        .filter(p -> p.getName().equals(PRACTITIONER_ROLE.getCode()))
        .map(
            prRole -> {
              val prRoleCoding = prRole.getValue().castToCoding(prRole.getValue());
              return new EuOrganizationProfession(
                  prRoleCoding.getSystem(), prRoleCoding.getCode(), prRoleCoding.getDisplay());
            })
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), PRACTITIONER_ROLE.getCode()));
  }

  public EuHealthcareFacilityType getHealthcareFacilityType() {
    return getRequestData().getPart().stream()
        .filter(p -> p.getName().equals(HEALTHCARE_FACILITY_TYPE.getCode()))
        .map(
            para -> {
              val hFT = para.getValue().castToCoding(para.getValue());
              return new EuHealthcareFacilityType(hFT.getCode(), hFT.getDisplay());
            })
        .findFirst()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), HEALTHCARE_FACILITY_TYPE.getCode()));
  }

  public PrescriptionId getFirstPrescriptionId() {
    return getRequestData().getPart().stream()
        .filter(p -> p.getName().equals(PRESCRIPTION_ID.getCode()))
        .findFirst()
        .map(parameter -> parameter.getValue().castToIdentifier(parameter.getValue()).getValue())
        .map(PrescriptionId::from)
        .orElseThrow(() -> new MissingFieldException(this.getClass(), PRESCRIPTION_ID.getCode()));
  }

  public List<PrescriptionId> getPrescriptionIds() {
    return this.getRequestData().getPart().stream()
        .filter(pC -> pC.getName().equals(PRESCRIPTION_ID.getCode()))
        .map(prId -> PrescriptionId.from(prId.castToIdentifier(prId.getValue()).getValue()))
        .toList();
  }

  public String getPractitionerName() {
    return getRequestData().getPart().stream()
        .filter(p -> p.getName().equals(PRACTITIONER_NAME.getCode()))
        .map(parameter -> parameter.getValue().castToString(parameter.getValue()).getValue())
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), PRACTITIONER_NAME.getCode()));
  }

  public ParametersParameterComponent getRequestData() {
    return this.getParameter().stream()
        .filter(p -> p.getName().equals(REQUEST_DATA.getCode()))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), REQUEST_DATA.getCode()));
  }
}
