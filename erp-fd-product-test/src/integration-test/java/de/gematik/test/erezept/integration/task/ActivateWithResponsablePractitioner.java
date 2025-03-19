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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind.PHARMACY_ONLY;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept Verordnung mit verantwortlichem Arzt")
@Tag("Feature:ResponsibleDoctor")
class ActivateWithResponsablePractitioner extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor responsibleDoctor;

  @Actor(name = "Gündüla Gunther")
  private DoctorActor prescribingDoctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @TestcaseId("ERP_TASK_ACTIVATE_RESPONSIBLE_DOC_01")
  @ParameterizedTest(
      name =
          "[{index}] verordnender Arzt in Weiterbildung stellt für einen {0} Patienten ein E-Rezept"
              + " mit PayorType {1} und einem verantwortlichen Arzt der Profession {2} aus.")
  @DisplayName(
      "E-Rezept als verordnender Arzt in Weiterbildung mit verantwortlichen Arzt ausstellen")
  @MethodSource("responsibleDoctor")
  void activateWithDoctorInTraining(
      InsuranceTypeDe insuranceType, PayorType payorType, QualificationType responsibleDoctorType) {
    sina.changePatientInsuranceType(insuranceType);
    sina.setPayorType(payorType);
    prescribingDoctor.changeQualificationType(QualificationType.DOCTOR_IN_TRAINING);
    responsibleDoctor.changeQualificationType(responsibleDoctorType);

    val issuePrescription =
        IssuePrescription.forPatient(sina)
            .withResponsibleDoctor(responsibleDoctor)
            .ofAssignmentKind(PHARMACY_ONLY)
            .withRandomKbvBundle();
    val activation = prescribingDoctor.performs(issuePrescription);

    responsibleDoctor.attemptsTo(
        Verify.that(activation).withExpectedType().hasResponseWith(returnCode(200)).isCorrect());
  }

  private static Stream<Arguments> responsibleDoctor() {
    // Note 05.12.23: Skip PayorType.SKT because currently not possible to build a valid
    // prescription with SKT
    val payorTypes = new LinkedList<>(List.of(PayorType.UK));
    payorTypes.add(null); // required to have no payor type!
    return ArgumentComposer.composeWith()
        .arguments(QualificationType.DOCTOR)
        .arguments(QualificationType.DENTIST)
        .arguments(QualificationType.DOCTOR_AS_REPLACEMENT)
        .multiply(List.of(InsuranceTypeDe.BG, InsuranceTypeDe.GKV, InsuranceTypeDe.PKV))
        .multiply(1, payorTypes)
        .create();
  }
}
