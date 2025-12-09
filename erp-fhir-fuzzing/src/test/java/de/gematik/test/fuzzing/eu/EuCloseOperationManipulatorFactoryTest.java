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

package de.gematik.test.fuzzing.eu;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.builder.eu.*;
import de.gematik.test.erezept.fhir.r4.eu.*;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import de.gematik.test.erezept.fhir.valuesets.eu.EuRequestType;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class EuCloseOperationManipulatorFactoryTest extends ErpFhirParsingTest {

  private EuCloseOperationInputBuilder getCloseBuilder() {
    val kvnr = KVNR.random();
    EuMedication euMedication =
        EuMedicationBuilder.builder().pzn(PZN.from("pzn-Code"), "Test Name").build();
    EuMedicationDispense medicationDispense =
        EuMedicationDispenseFaker.builder()
            .withPrescriptionId("169.123.456.789.122")
            .withKvnr(kvnr)
            .withMedication(euMedication)
            .fake();

    val euGetPrescriptionBundle =
        EuGetPrescriptionInputBuilder.forRequestType(EuRequestType.DEMOGRAPHICS)
            .kvnr(kvnr)
            .accessCode(EuAccessCode.random())
            .countryCode(IsoCountryCode.AT)
            .practitionerName("Practitioners Name")
            .practitionerRole(EuOrganizationProfession.getDefaultPharmacist())
            .pointOfCare("carePoint")
            .healthcareFacilityType(EuHealthcareFacilityType.getDefault())
            .build();

    return EuCloseOperationInputBuilder.builder(medicationDispense, euMedication)
        .requestDataFromGetPrescriptionInput(euGetPrescriptionBundle)
        .organization(EuOrganizationFaker.faker().fake())
        .practitioner(EuPractitionerBuilder.buildSimplePractitioner())
        .practitionerRole(EuPractitionerRoleBuilder.getSimplePractitionerRole());
  }

  @Test
  void shouldManipulateParameterParts() {

    for (val mutator : EuCloseOperationManipulatorFactory.getParametersManipulator()) {
      var closeOperationInput = getCloseBuilder().build();

      applyMutators(List.of(mutator), closeOperationInput);
      val res = ValidatorUtil.encodeAndValidate(parser, closeOperationInput);
      assertFalse(res.isSuccessful());
    }
  }

  @Test
  void shouldManipulateMedDispenses() {
    for (val mutator : EuCloseOperationManipulatorFactory.getDispensationManipulator()) {
      var closeOperationInput = getCloseBuilder().build();

      applyMutators(List.of(mutator), closeOperationInput);
      val res = ValidatorUtil.encodeAndValidate(parser, closeOperationInput);
      assertFalse(res.isSuccessful());
    }
  }

  @Test
  void shouldManipulateRequestData() {
    for (val mutator : EuCloseOperationManipulatorFactory.getRequestDataManipulator()) {
      var closeOperationInput = getCloseBuilder().build();
      applyMutators(List.of(mutator), closeOperationInput);
      val res = ValidatorUtil.encodeAndValidate(parser, closeOperationInput);
      assertFalse(res.isSuccessful());
    }
  }

  @Test
  void shouldManipulatePractitionerData() {
    for (val mutator : EuCloseOperationManipulatorFactory.getPractitionerDataManipulator()) {
      var closeOperationInput = getCloseBuilder().build();
      applyMutators(List.of(mutator), closeOperationInput);
      val res = ValidatorUtil.encodeAndValidate(parser, closeOperationInput);
      assertFalse(res.isSuccessful());
    }
  }

  @Test
  void shouldManipulateOrganizationData() {
    for (val mutator : EuCloseOperationManipulatorFactory.getOrganizationDataManipulator()) {
      var closeOperationInput = getCloseBuilder().build();
      applyMutators(List.of(mutator), closeOperationInput);
      val res = ValidatorUtil.encodeAndValidate(parser, closeOperationInput);
      assertFalse(res.isSuccessful());
    }
  }

  @Test
  void shouldManipulatePractitionerRoleData() {
    for (val mutator : EuCloseOperationManipulatorFactory.getPractitionerRoleDataManipulator()) {
      var closeOperationInput = getCloseBuilder().build();
      applyMutators(List.of(mutator), closeOperationInput);
      val res = ValidatorUtil.encodeAndValidate(parser, closeOperationInput);
      assertFalse(res.isSuccessful());
    }
  }

  @Test
  void shouldUseAllManipulators() {
    val manipulators = EuCloseOperationManipulatorFactory.getAllEuCloseOperationManipulators();
    assertEquals(34, manipulators.size(), "There should be 34 different manipulators");
  }

  void applyMutators(
      List<NamedEnvelope<FuzzingMutator<EuCloseOperationInput>>> mutators,
      EuCloseOperationInput euCloseOperationInput) {
    mutators.forEach(
        manipulator -> {
          manipulator.getParameter().accept(euCloseOperationInput);
        });
  }
}
