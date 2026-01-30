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
import static de.gematik.test.erezept.fhir.valuesets.eu.EuPartNaming.COUNTRY_CODE;
import static de.gematik.test.erezept.fhir.valuesets.eu.EuPartNaming.HEALTHCARE_FACILITY_TYPE;
import static de.gematik.test.erezept.fhir.valuesets.eu.EuPartNaming.POINT_OF_CARE;
import static de.gematik.test.erezept.fhir.valuesets.eu.EuPartNaming.PRACTITIONER_NAME;
import static de.gematik.test.erezept.fhir.valuesets.eu.EuPartNaming.PRACTITIONER_ROLE;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.eu.*;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import de.gematik.test.erezept.fhir.valuesets.eu.EuRequestType;
import lombok.val;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.Test;

class EuCloseOperationInputBuilderTest extends ErpFhirParsingTest {

  EuMedication euMedication =
      EuMedicationBuilder.builder().pzn(PZN.from("pzn-Code"), "Test Name").build();
  EuMedicationDispense medicationDispense =
      EuMedicationDispenseFaker.builder()
          .withKvnr(KVNR.random())
          .withMedication(euMedication)
          .fake();

  EuCloseOperationInputBuilder closeInputBuilder =
      EuCloseOperationInputBuilder.builder(medicationDispense, euMedication)
          .requestData(getRequestTypeParameter())
          .organization(EuOrganizationFaker.faker().fake())
          .practitioner(EuPractitionerBuilder.buildSimplePractitioner())
          .practitionerRole(EuPractitionerRoleBuilder.getSimplePractitionerRole());

  private static Parameters.ParametersParameterComponent getRequestTypeParameter() {
    Parameters.ParametersParameterComponent parameters =
        new Parameters.ParametersParameterComponent().setName(REQUEST_DATA.getCode());

    parameters.addPart().setName(EU_KVNR.getCode()).setValue(KVNR.randomGkv().asIdentifier(false));
    parameters
        .addPart()
        .setName(ACCESS_CODE.getCode())
        .setValue(EuAccessCode.random().asIdentifier());
    parameters.addPart().setName(COUNTRY_CODE.getCode()).setValue(IsoCountryCode.BG.asCoding());
    parameters
        .addPart()
        .setName(PRACTITIONER_NAME.getCode())
        .setValue(new StringType("this.practitionerName"));
    parameters
        .addPart()
        .setName(PRACTITIONER_ROLE.getCode())
        .setValue(EuOrganizationProfession.getDefaultPharmacist().asCoding());
    parameters
        .addPart()
        .setName(POINT_OF_CARE.getCode())
        .setValue(new StringType("this.pointOfCare"));
    parameters
        .addPart()
        .setName(HEALTHCARE_FACILITY_TYPE.getCode())
        .setValue(EuHealthcareFacilityType.getDefault().asCoding());
    return parameters;
  }

  @Test
  void shouldSetVersionCorrect() {
    val closeInput = closeInputBuilder.version(EuVersion.getDefaultVersion()).build();
    assertTrue(closeInput.getMeta().getProfile().get(0).asStringValue().contains("|1.1"));
  }

  @Test
  void shouldSetPractitionerCorrect() {
    val practitioner = EuPractitionerBuilder.buildSimplePractitioner();
    val closeInput = closeInputBuilder.practitioner(practitioner).build();

    val res = ValidatorUtil.encodeAndValidate(parser, closeInput);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldUseWithRequestDataCorrect() {
    Parameters.ParametersParameterComponent parameters = getRequestTypeParameter();
    val closeInput = closeInputBuilder.requestData(parameters).build();
    val res = ValidatorUtil.encodeAndValidate(parser, closeInput);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldUseWithRequestDataFromPrescriptionInputCorrect() {
    val euGetPrescriptionBundle =
        EuGetPrescriptionInputBuilder.forRequestType(EuRequestType.DEMOGRAPHICS)
            .kvnr(KVNR.randomGkv())
            .accessCode(EuAccessCode.random())
            .countryCode(IsoCountryCode.AT)
            .practitionerName("Practitioners Name")
            .practitionerRole(EuOrganizationProfession.getDefaultPharmacist())
            .pointOfCare("carePoint")
            .healthcareFacilityType(EuHealthcareFacilityType.getDefault())
            .build();
    val closeInput =
        closeInputBuilder.requestDataFromGetPrescriptionInput(euGetPrescriptionBundle).build();
    val res = ValidatorUtil.encodeAndValidate(parser, closeInput);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldBuildValid() {
    val closeInput = closeInputBuilder.build();

    val res = ValidatorUtil.encodeAndValidate(parser, medicationDispense);
    assertTrue(res.isSuccessful());
    val res1 = ValidatorUtil.encodeAndValidate(parser, euMedication);
    assertTrue(res1.isSuccessful());
    val res2 = ValidatorUtil.encodeAndValidate(parser, closeInput);
    assertTrue(res2.isSuccessful());
  }
}
