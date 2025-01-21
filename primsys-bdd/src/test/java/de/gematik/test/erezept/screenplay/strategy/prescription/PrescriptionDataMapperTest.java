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

package de.gematik.test.erezept.screenplay.strategy.prescription;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.kbv.MedicalOrganizationFaker;
import de.gematik.test.erezept.fhir.builder.kbv.PractitionerFaker;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
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

class PrescriptionDataMapperTest {

  static Stream<Arguments> shouldGenerateRandomKbvBundle() {
    return Stream.of(
        Arguments.of(
            VersicherungsArtDeBasis.GKV,
            PrescriptionAssignmentKind.PHARMACY_ONLY,
            PrescriptionFlowType.FLOW_TYPE_160),
        Arguments.of(
            VersicherungsArtDeBasis.PKV,
            PrescriptionAssignmentKind.PHARMACY_ONLY,
            PrescriptionFlowType.FLOW_TYPE_200),
        Arguments.of(
            VersicherungsArtDeBasis.GKV,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            PrescriptionFlowType.FLOW_TYPE_169),
        Arguments.of(
            VersicherungsArtDeBasis.PKV,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            PrescriptionFlowType.FLOW_TYPE_209));
  }

  @ParameterizedTest
  @MethodSource
  void shouldGenerateRandomKbvBundle(
      VersicherungsArtDeBasis versicherungsArtDeBasis,
      PrescriptionAssignmentKind prescriptionAssignmentKind,
      PrescriptionFlowType expectedFlowType) {

    val patient = new Actor("Marty");
    patient.can(
        ProvidePatientBaseData.forPatient(KVNR.random(), "Marty McFly", versicherungsArtDeBasis));

    val practitioner = PractitionerFaker.builder().fake();
    val medOrganization = MedicalOrganizationFaker.builder().fake();
    val medications = List.of(Map.of("key", "value"));
    val prescriptionDataMapper =
        new PrescriptionDataMapperPZN(patient, prescriptionAssignmentKind, medications);

    val result = prescriptionDataMapper.createKbvBundles(practitioner, medOrganization);

    assertEquals(1, result.size());

    val elem = result.get(0);
    val kbvBundle = elem.getLeft();
    val flowtype = elem.getRight();

    assertNotNull(kbvBundle);
    assertNotNull(flowtype);
    assertEquals(expectedFlowType, flowtype);
    assertFalse(
        kbvBundle
            .prescriptionId(PrescriptionId.random())
            .build()
            .getMedicationRequest()
            .isMultiple());
  }

  @Test
  void shouldCheckMVO() {
    val patient = new Actor("Marty");
    patient.can(
        ProvidePatientBaseData.forPatient(
            KVNR.random(), "Marty McFly", VersicherungsArtDeBasis.GKV));

    val mvoId = "497c760a-0460-4862-93a0-6f8491f83328";
    val medications =
        List.of(
            Map.of("MVO", "true", "MVO-ID", mvoId, "FreiText", "first prescription"),
            Map.of("MVO", "true", "MVO-ID", "", "FreiText", "second prescription"),
            Map.of("MVO", "true", "MVO-ID", "     ", "FreiText", "third prescription"),
            Map.of("MVO", "true", "MVO-ID", "\t\n", "FreiText", "fourth prescription"),
            Map.of("MVO", "true", "FreiText", "fifth prescription"));
    val practitioner = PractitionerFaker.builder().fake();
    val medOrganization = MedicalOrganizationFaker.builder().fake();

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
        ProvidePatientBaseData.forPatient(
            KVNR.random(), "Marty McFly", VersicherungsArtDeBasis.GKV));

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
    val practitioner = PractitionerFaker.builder().fake();
    val medOrganization = MedicalOrganizationFaker.builder().fake();

    val prescriptionDataMapper =
        new PrescriptionDataMapperFreitext(
            patient, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT, medications);
    val result =
        prescriptionDataMapper.createKbvBundles(practitioner, medOrganization).get(0).getLeft();

    assertNotNull(result);
  }
}
