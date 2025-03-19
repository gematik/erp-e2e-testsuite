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

package de.gematik.test.erezept.screenplay.strategy.prescription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.builder.kbv.KbvMedicalOrganizationFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPractitionerFaker;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class PrescriptionDataMapperTest extends ErpFhirParsingTest {

  static Stream<Arguments> shouldGenerateRandomKbvBundle() {
    return Stream.of(
        Arguments.of(
            InsuranceTypeDe.GKV,
            PrescriptionAssignmentKind.PHARMACY_ONLY,
            PrescriptionFlowType.FLOW_TYPE_160),
        Arguments.of(
            InsuranceTypeDe.PKV,
            PrescriptionAssignmentKind.PHARMACY_ONLY,
            PrescriptionFlowType.FLOW_TYPE_200),
        Arguments.of(
            InsuranceTypeDe.GKV,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            PrescriptionFlowType.FLOW_TYPE_169),
        Arguments.of(
            InsuranceTypeDe.PKV,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            PrescriptionFlowType.FLOW_TYPE_209));
  }

  @ParameterizedTest
  @MethodSource
  @ClearSystemProperty(key = "erp.fhir.profile")
  void shouldGenerateRandomKbvBundle(
      InsuranceTypeDe InsuranceTypeDe,
      PrescriptionAssignmentKind prescriptionAssignmentKind,
      PrescriptionFlowType expectedFlowType) {

    // TODO: make parametrizeable
    System.setProperty("erp.fhir.profile", "1.3.0");
    val patient = new Actor("Marty");
    patient.can(ProvidePatientBaseData.forPatient(KVNR.random(), "Marty McFly", InsuranceTypeDe));

    val practitioner = KbvPractitionerFaker.builder().fake();
    val medOrganization = KbvMedicalOrganizationFaker.builder().fake();
    val medications = List.of(Map.of("key", "value"));
    val prescriptionDataMapper =
        new PrescriptionDataMapperPZN(patient, prescriptionAssignmentKind, medications);

    val result = prescriptionDataMapper.createKbvBundles(practitioner, medOrganization);

    assertEquals(1, result.size());

    val elem = result.get(0);
    val kbvBundleBuilder = elem.getLeft();
    val flowtype = elem.getRight();
    val kbvBundle = kbvBundleBuilder.prescriptionId(PrescriptionId.random(flowtype)).build();

    val vr = ValidatorUtil.encodeAndValidate(parser, kbvBundle);
    assertTrue(vr.isSuccessful());
    assertEquals(expectedFlowType, flowtype);
    assertFalse(kbvBundle.getMedicationRequest().isMultiple());
  }

  @Test
  void shouldCheckMVO() {
    val patient = new Actor("Marty");
    patient.can(
        ProvidePatientBaseData.forPatient(KVNR.random(), "Marty McFly", InsuranceTypeDe.GKV));

    val mvoId = "497c760a-0460-4862-93a0-6f8491f83328";
    val medications =
        List.of(
            Map.of("MVO", "true", "MVO-ID", mvoId, "FreiText", "first prescription"),
            Map.of("MVO", "true", "MVO-ID", "", "FreiText", "second prescription"),
            Map.of("MVO", "true", "MVO-ID", "     ", "FreiText", "third prescription"),
            Map.of("MVO", "true", "MVO-ID", "\t\n", "FreiText", "fourth prescription"),
            Map.of("MVO", "true", "FreiText", "fifth prescription"));
    val practitioner = KbvPractitionerFaker.builder().fake();
    val medOrganization = KbvMedicalOrganizationFaker.builder().fake();

    val prescriptionDataMapper =
        new PrescriptionDataMapperFreitext(
            patient, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT, medications);
    val result = prescriptionDataMapper.createKbvBundles(practitioner, medOrganization);

    assertNotNull(result);
    assertEquals(medications.size(), result.size());
    val bundles =
        result.stream()
            .map(r -> r.getLeft().prescriptionId(PrescriptionId.random()).build())
            .toList();
    bundles.forEach(b -> assertTrue(b.getMedicationRequest().isMultiple()));

    // check if all generated MVO ids are equal
    val distinctIds =
        bundles.stream().map(b -> b.getMedicationRequest().getMvoId()).distinct().count();
    assertEquals(1, distinctIds);
  }

  @Test
  void shouldCheckMVOGueltigkeit() {
    val patient = new Actor("Marty");
    patient.can(
        ProvidePatientBaseData.forPatient(KVNR.random(), "Marty McFly", InsuranceTypeDe.GKV));

    val medications =
        List.of(
            Map.of(
                "MVO",
                "true",
                "FreiText",
                "first prescription",
                "Gueltigkeitsstart",
                "1",
                "Gueltigkeitsende",
                "2"));
    val practitioner = KbvPractitionerFaker.builder().fake();
    val medOrganization = KbvMedicalOrganizationFaker.builder().fake();

    val prescriptionDataMapper =
        new PrescriptionDataMapperFreitext(
            patient, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT, medications);
    val result =
        prescriptionDataMapper.createKbvBundles(practitioner, medOrganization).get(0).getLeft();

    assertNotNull(result);
  }
}
