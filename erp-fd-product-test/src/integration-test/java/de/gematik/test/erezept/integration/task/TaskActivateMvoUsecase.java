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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.hasCorrectMvoAcceptDate;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.hasCorrectMvoExpiryDate;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.isInReadyStatus;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionIdExtension;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StatusKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.toggle.InvalidMvoIDAcceptToogle;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import de.gematik.test.fuzzing.kbv.MvoExtensionManipulatorFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
@DisplayName("E-Rezept als Mehrfachverordnung ausstellen")
@Tag("Feature:MVO")
class TaskActivateMvoUsecase extends ErpTest {

  private static final Boolean invalidMvoIdAccept =
      featureConf.getToggle(new InvalidMvoIDAcceptToogle());

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @TestcaseId("ERP_TASK_ACTIVATE_MVO_01")
  @ParameterizedTest(
      name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} als MVO ({2}) aus")
  @DisplayName("Mehrfachverordnung als Verordnender Arzt an eine/n Versicherte/n ausstellen")
  @MethodSource("mvoPrescriptionTypesProvider")
  void activateMvoPrescription(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<MultiplePrescriptionExtension> mvo) {

    sina.changePatientInsuranceType(insuranceType);

    val medication =
        KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_00).fake();

    val kbvBundleBuilder =
        KbvErpBundleFaker.builder()
            .withMedication(medication)
            .withInsurance(sina.getInsuranceCoverage(), sina.getPatientData())
            .withPractitioner(doctor.getPractitioner())
            .withMvo(mvo.getParameter())
            .toBuilder();
    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(kbvBundleBuilder));

    val mvoEndDate = mvo.getParameter().getEnd().orElse(null);
    doctor.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_22627)
            .hasResponseWith(returnCode(200))
            .and(isInReadyStatus())
            .and(hasCorrectMvoExpiryDate(mvoEndDate))
            .and(hasCorrectMvoAcceptDate(mvoEndDate))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_MVO_02")
  @ParameterizedTest(
      name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} als MVO ({2}) aus")
  @DisplayName(
      "Mehrfachverordnung mit ungültigem Verhältnis als Verordnender Arzt an eine/n Versicherte/n"
          + " ausstellen")
  @MethodSource("invalidRatioMvoPrescriptionTypesProvider")
  void activateMvoPrescriptionWithInvalidRatio(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<MultiplePrescriptionExtension> mvo,
      RequirementsSet req) {

    sina.changePatientInsuranceType(insuranceType);

    val medication =
        KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_00).fake();

    val kbvBundleBuilder =
        KbvErpBundleFaker.builder()
            .withMedication(medication)
            .withInsurance(sina.getInsuranceCoverage(), sina.getPatientData())
            .withPractitioner(doctor.getPractitioner())
            .withMvo(mvo.getParameter())
            .toBuilder();

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(kbvBundleBuilder));
    doctor.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(req)
            .hasResponseWith(returnCode(400))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_MVO_03")
  @ParameterizedTest(
      name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} als MVO ({2}) aus")
  @DisplayName(
      "Mehrfachverordnung mit unzulässigen Angaben der Extension als Verordnender Arzt an eine/n"
          + " Versicherte/n ausstellen")
  @MethodSource("mvoExtensionManipulator")
  void activateMvoPrescriptionWithManipulatedExtension(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<Consumer<KbvErpBundle>> kbvBundleMutator) {

    sina.changePatientInsuranceType(insuranceType);

    val medication =
        KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_00).fake();

    val kbvBundleBuilder =
        KbvErpBundleFaker.builder()
            .withMedication(medication)
            .withInsurance(sina.getInsuranceCoverage(), sina.getPatientData())
            .withPractitioner(doctor.getPractitioner())
            .withMvo(MultiplePrescriptionExtension.asMultiple(1, 4).validThrough(0, 365))
            .toBuilder();

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withResourceManipulator(kbvBundleMutator.getParameter())
                .withKbvBundleFrom(kbvBundleBuilder));
    doctor.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(ErpAfos.A_22631)
            .hasResponseWith(returnCode(400))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_MVO_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} als MVO mit"
              + " Statuskennzeichen {2} aus")
  @DisplayName(
      "Mehrfachverordnung mit unzulässigem Statuskennzeichen als Verordnender Arzt an eine/n"
          + " Versicherte/n ausstellen")
  @MethodSource("mvoWithInvalidStatuskennzeichen")
  void activateMvoPrescriptionWithInvalidStatuskennzeichen(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      StatusKennzeichen statusKennzeichen,
      RequirementsSet req) {

    sina.changePatientInsuranceType(insuranceType);

    val medication =
        KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_00).fake();

    val kbvBundleBuilder =
        KbvErpBundleFaker.builder()
            .withMedication(medication)
            .withStatusKennzeichen(statusKennzeichen)
            .withInsurance(sina.getInsuranceCoverage(), sina.getPatientData())
            .withPractitioner(doctor.getPractitioner())
            .withMvo(MultiplePrescriptionExtension.asMultiple(1, 4).validThrough(0, 365))
            .toBuilder();
    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(kbvBundleBuilder));
    doctor.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(req)
            .hasResponseWith(returnCode(400))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_MVO_05")
  @ParameterizedTest(
      name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} als MVO mit {2} aus")
  @DisplayName(
      "Mehrfachverordnung mit unzulässiger oder zulässiger MVO-ID als Verordnender Arzt an eine/n"
          + " Versicherte/n ausstellen")
  @MethodSource("mvoIdExtension")
  void activateMvoPrescriptionWithInvalidIdExtension(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      NamedEnvelope<MultiplePrescriptionIdExtension> idExt,
      boolean expectSuccess) {

    sina.changePatientInsuranceType(insuranceType);

    val medication =
        KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_00).fake();

    val kbvBundleBuilder =
        KbvErpBundleFaker.builder()
            .withMedication(medication)
            .withInsurance(sina.getInsuranceCoverage(), sina.getPatientData())
            .withPractitioner(doctor.getPractitioner())
            .withMvo(
                MultiplePrescriptionExtension.asMultiple(1, 4)
                    .withId(idExt.getParameter())
                    .validThrough(0, 365))
            .toBuilder();
    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(kbvBundleBuilder));
    val verify =
        expectSuccess || invalidMvoIdAccept
            ? Verify.that(activation)
                .withExpectedType(ErpAfos.A_24901)
                .hasResponseWith(returnCode(200))
            : Verify.that(activation)
                .withOperationOutcome(ErpAfos.A_24901)
                .hasResponseWith(returnCode(400));
    doctor.attemptsTo(verify.isCorrect());
  }

  static Stream<Arguments> mvoPrescriptionTypesProvider() {
    val composer =
        ArgumentComposer.composeWith()
            .arguments(
                NamedEnvelope.of(
                    "1 von 4 ab sofort für ein Jahr gültig",
                    MultiplePrescriptionExtension.asMultiple(1, 4).validThrough(0, 365)))
            .arguments(
                NamedEnvelope.of(
                    "3 von 4 ab sofort für ein Jahr gültig",
                    MultiplePrescriptionExtension.asMultiple(3, 4).validThrough(0, 365)))
            .arguments(
                NamedEnvelope.of(
                    "4 von 4 ab sofort ohne EndDatum",
                    MultiplePrescriptionExtension.asMultiple(4, 4).fromNow().withoutEndDate()));

    return multiplyWithFlowTypes(composer);
  }

  static Stream<Arguments> invalidRatioMvoPrescriptionTypesProvider() {
    val composer =
        ArgumentComposer.composeWith()
            .arguments(
                NamedEnvelope.of(
                    "1 von 5 ab sofort für ein Jahr gültig",
                    MultiplePrescriptionExtension.asMultiple(1, 5).validThrough(0, 365)),
                ErpAfos.A_22628)
            .arguments(
                NamedEnvelope.of(
                    "5 von 5 ab sofort für ein Jahr gültig",
                    MultiplePrescriptionExtension.asMultiple(5, 5).validThrough(0, 365)),
                ErpAfos.A_22628)
            .arguments(
                NamedEnvelope.of(
                    "0 von 4 ab sofort für ein Jahr gültig",
                    MultiplePrescriptionExtension.asMultiple(0, 4).validThrough(0, 365)),
                ErpAfos.A_22704)
            .arguments(
                NamedEnvelope.of(
                    "1 von 1 ab sofort für ein Jahr gültig",
                    MultiplePrescriptionExtension.asMultiple(1, 1).validThrough(0, 365)),
                ErpAfos.A_22629)
            .arguments(
                NamedEnvelope.of(
                    "3 von 2 ab sofort für ein Jahr gültig",
                    MultiplePrescriptionExtension.asMultiple(3, 2).validThrough(0, 365)),
                ErpAfos.A_22630)
            .arguments(
                NamedEnvelope.of(
                    "1 von 4 ohne Beginn Einlösefrist",
                    MultiplePrescriptionExtension.asMultiple(1, 4).validForDays(365, false)),
                ErpAfos.A_22634)
            .arguments(
                NamedEnvelope.of(
                    "1 von 4 ohne Einlösefristen",
                    MultiplePrescriptionExtension.asMultiple(1, 4).withoutEndDate(false)),
                ErpAfos.A_22634);

    return multiplyWithFlowTypes(composer);
  }

  static Stream<Arguments> mvoIdExtension() {
    val composer =
        ArgumentComposer.composeWith()
            .arguments(
                NamedEnvelope.of(
                    "MVO-ID mit bekanntem Schema urn:ietf:rfc:3986",
                    MultiplePrescriptionIdExtension.randomId()),
                true)
            .arguments(
                NamedEnvelope.of(
                    "ungültiger MVO-ID und bekanntem Schema urn:ietf:rfc:3986",
                    MultiplePrescriptionIdExtension.invalidId()),
                false)
            .arguments(
                NamedEnvelope.of(
                    "MOV-ID mit unbekanntem Schema",
                    MultiplePrescriptionIdExtension.with("abc", "def")),
                false);

    return multiplyWithFlowTypes(composer);
  }

  /**
   * Manipulating the MVO Extension to check for A_22631
   *
   * @return Stream of Arguments
   */
  static Stream<Arguments> mvoExtensionManipulator() {
    val composer = ArgumentComposer.composeWith();
    MvoExtensionManipulatorFactory.getMvoExtensionKennzeichenFalsifier()
        .forEach(composer::arguments);

    return multiplyWithFlowTypes(composer);
  }

  static Stream<Arguments> mvoWithInvalidStatuskennzeichen() {
    val composer =
        ArgumentComposer.composeWith()
            .arguments(StatusKennzeichen.RELEASE_MGMT, ErpAfos.A_22632)
            .arguments(StatusKennzeichen.RELEASE_SUBSTITUTE, ErpAfos.A_22632)
            .arguments(StatusKennzeichen.SUBSTITUTE, ErpAfos.A_22633)
            .arguments(StatusKennzeichen.ASV_SUBSTITUTE, ErpAfos.A_22633)
            .arguments(StatusKennzeichen.TSS_SUBSTITUTE, ErpAfos.A_22633);

    return multiplyWithFlowTypes(composer);
  }

  private static Stream<Arguments> multiplyWithFlowTypes(ArgumentComposer composer) {
    val insuranceArguments = new ArrayList<VersicherungsArtDeBasis>(2);
    insuranceArguments.add(VersicherungsArtDeBasis.GKV);
    insuranceArguments.add(VersicherungsArtDeBasis.PKV);
    composer.multiply(
        List.of(
            PrescriptionAssignmentKind.PHARMACY_ONLY,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT));
    composer.multiply(insuranceArguments);
    return composer.create();
  }
}
