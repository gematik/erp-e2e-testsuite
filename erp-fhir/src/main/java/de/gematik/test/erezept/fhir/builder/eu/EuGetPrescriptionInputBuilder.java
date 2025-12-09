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
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.eu.EuGetPrescriptionInput;
import de.gematik.test.erezept.fhir.r4.eu.EuHealthcareFacilityType;
import de.gematik.test.erezept.fhir.r4.eu.EuOrganizationProfession;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import de.gematik.test.erezept.fhir.valuesets.eu.EuRequestType;
import java.util.ArrayList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.StringType;

public class EuGetPrescriptionInputBuilder
    extends ResourceBuilder<EuGetPrescriptionInput, EuGetPrescriptionInputBuilder> {

  private EuVersion version = EuVersion.getDefaultVersion();
  private EuRequestType euRequestType;
  private KVNR kvnr;
  private EuAccessCode euAccessCode;
  private IsoCountryCode isoCountryCode;
  private EuOrganizationProfession euOrganizationProfession =
      EuOrganizationProfession.getDefaultPharmacist();
  private String pointOfCare;
  private EuHealthcareFacilityType healthcareFacilityType;
  private String practitionerName;
  private List<PrescriptionId> prescriptionIds = new ArrayList<>() {};

  public static EuGetPrescriptionInputBuilder forRequestType(EuRequestType euRequestType) {
    val builder = new EuGetPrescriptionInputBuilder();
    builder.euRequestType = euRequestType;
    return builder;
  }

  public EuGetPrescriptionInputBuilder version(EuVersion version) {
    this.version = version;
    return this;
  }

  public EuGetPrescriptionInputBuilder kvnr(KVNR kvnr) {
    this.kvnr = kvnr;
    return this;
  }

  public EuGetPrescriptionInputBuilder practitionerRole(EuOrganizationProfession practitionerRole) {
    this.euOrganizationProfession = practitionerRole;
    return this;
  }

  public EuGetPrescriptionInputBuilder accessCode(EuAccessCode euAccessCode) {
    this.euAccessCode = euAccessCode;
    return this;
  }

  public EuGetPrescriptionInputBuilder countryCode(IsoCountryCode countyCode) {
    this.isoCountryCode = countyCode;
    return this;
  }

  public EuGetPrescriptionInputBuilder pointOfCare(String pointOfCare) {
    this.pointOfCare = pointOfCare;
    return this;
  }

  public EuGetPrescriptionInputBuilder practitionerName(String practitionerName) {
    this.practitionerName = practitionerName;
    return this;
  }

  public EuGetPrescriptionInputBuilder healthcareFacilityType(
      EuHealthcareFacilityType healthcareFacilityType) {
    this.healthcareFacilityType = healthcareFacilityType;
    return this;
  }

  public EuGetPrescriptionInputBuilder prescriptionId(PrescriptionId prescriptionId) {
    this.prescriptionIds.add(prescriptionId);
    return this;
  }

  public EuGetPrescriptionInputBuilder prescriptionIds(List<PrescriptionId> prescriptionIds) {
    this.prescriptionIds = prescriptionIds;
    return this;
  }

  @Override
  public EuGetPrescriptionInput build() {
    checkRequired();

    val euPrescrBundle =
        this.createResource(
            EuGetPrescriptionInput::new, GemErpEuStructDef.PRESCRIPTION_INPUT, version);
    euPrescrBundle.addParameter().setName(REQUEST_DATA.getCode());
    val param = euPrescrBundle.getParameter(REQUEST_DATA.getCode());
    param.addPart().setName(REQUEST_TYPE.getCode()).setValue(this.euRequestType.asCoding());
    param
        .addPart()
        .setName(EU_KVNR.getCode())
        .setValue(this.kvnr.asIdentifier(DeBasisProfilNamingSystem.KVID_GKV_SID, false));
    param.addPart().setName(ACCESS_CODE.getCode()).setValue(this.euAccessCode.asIdentifier());
    param.addPart().setName(COUNTRY_CODE.getCode()).setValue(this.isoCountryCode.asCoding());
    param
        .addPart()
        .setName(PRACTITIONER_NAME.getCode())
        .setValue(new StringType(this.practitionerName));
    param
        .addPart()
        .setName(PRACTITIONER_ROLE.getCode())
        .setValue(this.euOrganizationProfession.asCoding());
    param.addPart().setName(POINT_OF_CARE.getCode()).setValue(new StringType(this.pointOfCare));
    param
        .addPart()
        .setName(HEALTHCARE_FACILITY_TYPE.getCode())
        .setValue(healthcareFacilityType.asCoding());
    if (!prescriptionIds.isEmpty()) {
      prescriptionIds.forEach(
          prescriptionId ->
              param
                  .addPart()
                  .setName(PRESCRIPTION_ID.getCode())
                  .setValue(prescriptionId.asIdentifier()));
    }
    return euPrescrBundle;
  }

  private void checkRequired() {
    this.checkRequired(euRequestType, "getEuPrescriptionInput needs an euRequestType");
    this.checkRequired(kvnr, "getEuPrescriptionInput needs an KVNR");
    this.checkRequired(isoCountryCode, "getEuPrescriptionInput needs an isoCountyCode");
    this.checkRequired(
        euOrganizationProfession, "getEuPrescriptionInput needs an euPractitionerRole");
    this.checkRequired(pointOfCare, "getEuPrescriptionInput needs an pointOfCare");
    this.checkRequired(
        healthcareFacilityType, "getEuPrescriptionInput needs an healthcareFacilityType");
    this.checkRequired(euAccessCode, "getEuPrescriptionInput needs an euAccessCode");
    this.checkRequired(practitionerName, "getEuPrescriptionInput needs an practitionerName");
  }
}
