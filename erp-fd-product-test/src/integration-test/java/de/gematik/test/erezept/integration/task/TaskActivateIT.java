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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIs;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIsBetween;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDiagnostics;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHasDetailsText;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHintsDeviatingAuthoredOnDate;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.hasCorrectAcceptDate;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.hasCorrectExpiryDate;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.hasWorkflowType;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.isInReadyStatus;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.de.valueset.IdentifierTypeDe;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.FhirRequirements;
import de.gematik.test.core.expectations.requirements.KbvProfileRules;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.ActivatePrescription;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.TaskCreate;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.toggle.ErpDarreichungsformAprilActive;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept ausstellen")
@Tag("UseCase:Activate")
class TaskActivateIT extends ErpTest {

  private static final Boolean EXPECT_DARREICHUNGSFORM_APRIL_IS_ACTIVE =
      featureConf.getToggle(new ErpDarreichungsformAprilActive());

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @TestcaseId("ERP_TASK_ACTIVATE_01")
  @Tag("Smoketest")
  @ParameterizedTest(name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} aus")
  @DisplayName("E-Rezept als Verordnender Arzt an eine/n Versicherte/n ausstellen")
  @MethodSource("prescriptionTypesProvider")
  void activatePrescription(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowType) {

    sina.changePatientInsuranceType(insuranceType);

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withRandomKbvBundle());
    doctor.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_19022)
            .hasResponseWith(returnCode(200))
            .and(hasWorkflowType(expectedFlowType))
            .and(isInReadyStatus())
            .and(hasCorrectExpiryDate())
            .and(hasCorrectAcceptDate(expectedFlowType))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit Darreichungsform {2}"
              + " aus")
  @DisplayName(
      "E-Rezept als Verordnender Arzt an eine/n Versicherte/n ausstellen mit definierter"
          + " Darreichungsform")
  @MethodSource("prescriptionTypesProviderDarreichungsformen")
  void activatePrescriptionWithDarreichungsformen(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      Darreichungsform df,
      PrescriptionFlowType expectedFlowType) {

    sina.changePatientInsuranceType(insuranceType);

    val medication =
        KbvErpMedicationPZNFaker.builder()
            .withCategory(MedicationCategory.C_00)
            .withSupplyForm(df)
            .fake();

    val kbvBundleBuilder =
        KbvErpBundleFaker.builder()
            .withKvnr(sina.getKvnr())
            .withMedication(medication) // what is the medication
            .withInsurance(sina.getInsuranceCoverage(), sina.getPatientData())
            .withPractitioner(doctor.getPractitioner())
            .toBuilder();

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(kbvBundleBuilder));

    if (EXPECT_DARREICHUNGSFORM_APRIL_IS_ACTIVE) {
      doctor.attemptsTo(
          Verify.that(activation)
              .withExpectedType(KbvProfileRules.EXTENDED_VALUE_SET_DARREICHUNGSFORMEN)
              .hasResponseWith(returnCode(200))
              .and(hasWorkflowType(expectedFlowType))
              .and(isInReadyStatus())
              .and(hasCorrectExpiryDate())
              .and(hasCorrectAcceptDate(expectedFlowType))
              .isCorrect());
    } else {
      doctor.attemptsTo(
          Verify.that(activation)
              .withOperationOutcome()
              .responseWith(returnCodeIs(400))
              .has(
                  operationOutcomeHasDetailsText(
                      "FHIR-Validation error", FhirRequirements.FHIR_VALIDATION_ERROR))
              .isCorrect());
    }
  }

  static Stream<Arguments> prescriptionTypesProviderInvalidAuthoredOn() {
    val invalidAuthoredOnDates =
        List.of(
            Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
            Date.from(Instant.now().minus(1, ChronoUnit.DAYS)));
    val composer =
        ArgumentComposer.composeWith()
            .arguments(
                InsuranceTypeDe.GKV, // given insurance kind
                PrescriptionAssignmentKind.PHARMACY_ONLY) // expected flow type
            .arguments(InsuranceTypeDe.GKV, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
            .arguments(InsuranceTypeDe.PKV, PrescriptionAssignmentKind.PHARMACY_ONLY)
            .arguments(InsuranceTypeDe.PKV, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT);

    return composer.multiplyAppend(invalidAuthoredOnDates).create();
  }

  static Stream<Arguments> prescriptionTypesProvider() {
    return ArgumentComposer.composeWith()
        .arguments(
            InsuranceTypeDe.GKV, // given insurance kind
            PrescriptionAssignmentKind.PHARMACY_ONLY, // given assignment kind
            PrescriptionFlowType.FLOW_TYPE_160) // expected flow type
        .arguments(
            InsuranceTypeDe.GKV,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            PrescriptionFlowType.FLOW_TYPE_169)
        .arguments(
            InsuranceTypeDe.PKV,
            PrescriptionAssignmentKind.PHARMACY_ONLY,
            PrescriptionFlowType.FLOW_TYPE_200)
        .arguments(
            InsuranceTypeDe.PKV,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            PrescriptionFlowType.FLOW_TYPE_209)
        .multiplyAppend(MedicationCategory.class)
        .create();
  }

  @TestcaseId("ERP_TASK_ACTIVATE_03")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit Ausstellungsdatum {2}"
              + " aus")
  @DisplayName("Ausstellungsdatum und Signaturzeitpunkt müssen taggleich sein")
  @MethodSource("prescriptionTypesProviderInvalidAuthoredOn")
  void activatePrescriptionWithInvalidAuthoredOn(
      InsuranceTypeDe insuranceType, PrescriptionAssignmentKind assignmentKind, Date authoredOn) {

    sina.changePatientInsuranceType(insuranceType);

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withKvnr(sina.getKvnr())
                        .withAuthorDate(authoredOn)
                        .toBuilder()));
    doctor.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(ErpAfos.A_22487)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeHintsDeviatingAuthoredOnDate())
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit zeitlicher Präzision"
              + " {2} aus")
  @DisplayName(
      "E-Rezept mit einem Ausstellungsdatum, dürfen keinen Zeitstempel enthalten und sind begrenzt"
          + " auf 10 Zeichen im Format JJJJ-MM-TT")
  @MethodSource("prescriptionTypesProviderInvalidTemporalPrecision")
  void activatePrescriptionWithInvalidAuthoredOnFormat(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      TemporalPrecisionEnum precision) {

    sina.changePatientInsuranceType(insuranceType);

    val medication =
        KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_00).fake();

    val kbvBundleBuilder =
        KbvErpBundleFaker.builder()
            .withKvnr(sina.getKvnr())
            .withMedication(medication)
            .withInsurance(sina.getInsuranceCoverage(), sina.getPatientData())
            .withPractitioner(doctor.getPractitioner())
            .withAuthorDate(new Date(), precision)
            .toBuilder();

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(kbvBundleBuilder));
    doctor.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(KbvProfileRules.AUTHORED_ON_DATEFORMAT)
            // don't care about exact return code: ensure it's in 400 range
            .hasResponseWith(returnCodeIsBetween(400, 420))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_05")
  @ParameterizedTest(name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} aus")
  @DisplayName("PKV E-Rezept als Verordnender Arzt an eine/n Versicherte/n ausstellen")
  @MethodSource("pkvPrescriptionTypesProvider")
  void activatePkvPrescription(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowType) {

    sina.changePatientInsuranceType(insuranceType);
    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withRandomKbvBundle());

    doctor.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_19022)
            .hasResponseWith(returnCode(200))
            .and(hasWorkflowType(expectedFlowType))
            .and(isInReadyStatus())
            .and(hasCorrectExpiryDate())
            .and(hasCorrectAcceptDate(expectedFlowType))
            .isCorrect());
  }

  static Stream<Arguments> prescriptionTypesProviderDmpKennzeichen() {
    val dmpList =
        List.of(
            DmpKennzeichen.ADIPOSITAS,
            DmpKennzeichen.ASTHMA_UND_DIABETES_TYP_2_UND_KHK,
            DmpKennzeichen.BRUSTKREBS_UND_COPD_UND_DIABETES_TYP_1_UND_KHK);
    return ArgumentComposer.composeWith(prescriptionTypesProvider()).multiply(2, dmpList).create();
  }

  static Stream<Arguments> prescriptionTypesProviderDarreichungsformen() {
    val dfList =
        List.of(
            Darreichungsform.PUE, // From April 2025 Active
            Darreichungsform.LYE); // From April 2025 Active
    return ArgumentComposer.composeWith(prescriptionTypesProvider()).multiply(2, dfList).create();
  }

  static Stream<Arguments> pkvPrescriptionTypesProvider() {
    return ArgumentComposer.composeWith()
        .arguments(
            InsuranceTypeDe.PKV,
            PrescriptionAssignmentKind.PHARMACY_ONLY,
            PrescriptionFlowType.FLOW_TYPE_200)
        .arguments(
            InsuranceTypeDe.PKV,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            PrescriptionFlowType.FLOW_TYPE_209)
        .create();
  }

  static Stream<Arguments> prescriptionTypesProviderInvalidTemporalPrecision() {
    val excludedPrecisions =
        new TemporalPrecisionEnum[] {
          TemporalPrecisionEnum.DAY, // this one is the only valid one!
          TemporalPrecisionEnum.MINUTE // not supported by HAPI DateTimeType
        };
    val composer =
        ArgumentComposer.composeWith()
            .arguments(
                InsuranceTypeDe.GKV, // given insurance kind
                PrescriptionAssignmentKind.PHARMACY_ONLY) // expected flow type
            .arguments(InsuranceTypeDe.GKV, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
            .arguments(InsuranceTypeDe.PKV, PrescriptionAssignmentKind.PHARMACY_ONLY)
            .arguments(InsuranceTypeDe.PKV, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT);

    return composer.multiplyAppend(TemporalPrecisionEnum.class, excludedPrecisions).create();
  }

  @TestcaseId("ERP_TASK_ACTIVATE_06")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein E-Rezept für einen Versicherten mit dem"
              + " Versicherungsart-Identifier {0} aus")
  @DisplayName(
      "Verordnender Arzt stellt ein E-Rezept für einen Versicherten mit ungültigem"
          + " Versicherungsart-Identifier aus")
  @EnumSource(
      value = IdentifierTypeDe.class,
      names = {"GKV", "PKV"},
      mode = EnumSource.Mode.EXCLUDE)
  void invalidInsuranceType(IdentifierTypeDe insuranceIdentifierType) {

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withResourceManipulator(
                    it ->
                        it.getPatient()
                            .getIdentifierFirstRep()
                            .getType()
                            .getCodingFirstRep()
                            .setCode(insuranceIdentifierType.getCode()))
                .withRandomKbvBundle());
    doctor.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(FhirRequirements.FHIR_VALIDATION_ERROR)
            .hasResponseWith(returnCode(400))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "In der Ressource vom Typ Patient ist keine GKV-VersichertenID vorhanden, diese"
                        + " ist aber eine Pflichtangabe beim Kostentraeger des Typs",
                    ErpAfos.A_23936))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_07")
  @DisplayName("Es muss geprüft werden, dass kein Verordnungsdatensatz doppelt eingestellt wird")
  @Test
  void activatePrescriptionCopyIsForbidden() {
    val creation =
        doctor
            .performs(TaskCreate.withFlowType(PrescriptionFlowType.FLOW_TYPE_160))
            .getExpectedResponse();

    val kbvBundleBuilder = getKbvErpBundleBuilder(creation.getPrescriptionId());
    val activation =
        ActivatePrescription.withId(creation.getTaskId())
            .andAccessCode(creation.getAccessCode())
            .withKbvBundle(kbvBundleBuilder.build());

    val activationResult = doctor.performs(activation);
    val activationResult2 = doctor.performs(activation);

    doctor.attemptsTo(
        Verify.that(activationResult)
            .withExpectedType()
            .responseWith(returnCodeIs(200))
            .isCorrect());
    doctor.attemptsTo(
        Verify.that(activationResult2)
            .withOperationOutcome()
            .responseWith(returnCodeIs(403))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_08")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit dem DmpKennzeichen"
              + " Version 1.06 aus")
  @DisplayName(
      "E-Rezept als Verordnender Arzt an eine/n Versicherte/n mit DmpKennzeichen Version 1.06"
          + " ausstellen")
  @MethodSource("prescriptionTypesProviderDmpKennzeichen")
  void activatePrescriptionWithNewDmpKennzeichen(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      DmpKennzeichen dmp,
      PrescriptionFlowType expectedFlowType) {

    sina.changePatientInsuranceType(insuranceType);
    sina.changeDmpKennzeichen(dmp);

    val medication =
        KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_00).fake();

    val kbvBundleBuilder =
        KbvErpBundleFaker.builder()
            .withKvnr(sina.getKvnr())
            .withMedication(medication)
            .withInsurance(sina.getInsuranceCoverage(), sina.getPatientData())
            .withPractitioner(doctor.getPractitioner())
            .toBuilder();

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(kbvBundleBuilder));

    doctor.attemptsTo(
        Verify.that(activation)
            .withExpectedType(KbvProfileRules.EXTENDED_VALUE_SET_DMPKENNZEICHEN)
            .hasResponseWith(returnCode(200))
            .and(hasWorkflowType(expectedFlowType))
            .and(isInReadyStatus())
            .and(hasCorrectExpiryDate())
            .and(hasCorrectAcceptDate(expectedFlowType))
            .isCorrect());
  }

  private KbvErpBundleBuilder getKbvErpBundleBuilder(PrescriptionId prescriptionId) {
    val medication =
        KbvErpMedicationPZNFaker.builder()
            .withCategory(MedicationCategory.C_00)
            .withSupplyForm(Darreichungsform.SCH)
            .fake();

    val kbvBundleBuilder =
        KbvErpBundleFaker.builder()
            .withPrescriptionId(prescriptionId)
            .withKvnr(sina.getKvnr())
            .withMedication(medication)
            .withInsurance(sina.getInsuranceCoverage(), sina.getPatientData())
            .withPractitioner(doctor.getPractitioner())
            .toBuilder();
    return kbvBundleBuilder;
  }

  @TestcaseId("ERP_TASK_ACTIVATE_08")
  @ParameterizedTest(
      name = "[{index}] -> Verordnender Arzt stellt ein E-Rezept mit Arzneimittelkategorie {3} aus")
  @DisplayName(
      "E-Rezept als Verordnender Arzt mit Arzneimittelkategorie = 00, 01 oder 02 im Bundle")
  @MethodSource("prescriptionTypesProvider")
  void activatePrescriptionWithMedicationExtension(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowType,
      MedicationCategory medicationCategory) {

    sina.changePatientInsuranceType(insuranceType);

    val medication = KbvErpMedicationPZNFaker.builder().withCategory(medicationCategory).fake();

    val kbvBundleBuilder =
        KbvErpBundleFaker.builder()
            .withKvnr(sina.getKvnr())
            .withMedication(medication)
            .withInsurance(sina.getInsuranceCoverage(), sina.getPatientData())
            .withPractitioner(doctor.getPractitioner())
            .toBuilder();

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(kbvBundleBuilder));

    if (MedicationCategory.C_00.equals(medicationCategory)) {
      doctor.attemptsTo(
          Verify.that(activation)
              .withExpectedType(KbvProfileRules.SUPPLY_REQUEST_AND_MEDICATION_REQUEST)
              .hasResponseWith(returnCode(200))
              .and(hasWorkflowType(expectedFlowType))
              .and(hasCorrectExpiryDate())
              .isCorrect());
    } else {
      doctor.attemptsTo(
          Verify.that(activation)
              .withOperationOutcome()
              .hasResponseWith(returnCode(400, FhirRequirements.FHIR_PROFILES))
              .isCorrect());
    }
  }
}
