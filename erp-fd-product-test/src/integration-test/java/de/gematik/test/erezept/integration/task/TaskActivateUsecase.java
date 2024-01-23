/*
 * Copyright 2023 gematik GmbH
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
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIsBetween;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHasDetailsText;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHintsDeviatingAuthoredOnDate;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.*;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.FhirRequirements;
import de.gematik.test.core.expectations.requirements.KbvProfileRules;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.MedicationRequestBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.toggle.E2ECucumberTag;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept ausstellen")
class TaskActivateUsecase extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @TestcaseId("ERP_TASK_ACTIVATE_01")
  @ParameterizedTest(name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} aus")
  @DisplayName("E-Rezept als Verordnender Arzt an eine/n Versicherte/n ausstellen")
  @MethodSource("prescriptionTypesProvider")
  void activatePrescription(
      VersicherungsArtDeBasis insuranceType,
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
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit Darreichungsform {2} aus")
  @DisplayName("E-Rezept als Verordnender Arzt an eine/n Versicherte/n ausstellen")
  @MethodSource("prescriptionTypesProviderDarreichungsformen")
  void activatePrescriptionWithNewDarreichungsformen(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      Darreichungsform df,
      PrescriptionFlowType expectedFlowType) {

    sina.changePatientInsuranceType(insuranceType);

    val medication =
        KbvErpMedicationPZNBuilder.faker()
            .category(MedicationCategory.C_00)
            .darreichungsform(df)
            .build();
    val medicationRequest =
        MedicationRequestBuilder.faker(sina.getPatientData())
            .insurance(sina.getInsuranceCoverage())
            .requester(doctor.getPractitioner())
            .medication(medication)
            .build();

    val kbvBundleBuilder =
        KbvErpBundleBuilder.faker(sina.getKvnr())
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication);

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(kbvBundleBuilder));
    doctor.attemptsTo(
        Verify.that(activation)
            .withExpectedType(KbvProfileRules.EXTENDED_VALUE_SET_DARREICHUNGSFORMEN)
            .hasResponseWith(returnCode(200))
            .and(hasWorkflowType(expectedFlowType))
            .and(isInReadyStatus())
            .and(hasCorrectExpiryDate())
            .and(hasCorrectAcceptDate(expectedFlowType))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_03")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit Ausstellungsdatum {2} aus")
  @DisplayName("Ausstellungsdatum und Signaturzeitpunkt müssen taggleich sein")
  @MethodSource("prescriptionTypesProviderInvalidAuthoredOn")
  void activatePrescriptionWithInvalidAuthoredOn(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      Date authoredOn) {

    sina.changePatientInsuranceType(insuranceType);

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(KbvErpBundleBuilder.faker(sina.getKvnr(), authoredOn)));
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
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit zeitlicher Präzision {2} aus")
  @DisplayName(
      "E-Rezept mit einem Ausstellungsdatum, dürfen keinen Zeitstempel enthalten und sind begrenzt auf 10 Zeichen im Format JJJJ-MM-TT")
  @MethodSource("prescriptionTypesProviderInvalidTemporalPrecision")
  void activatePrescriptionWithInvalidAuthoredOnFormat(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      TemporalPrecisionEnum precision) {

    sina.changePatientInsuranceType(insuranceType);

    val medication = KbvErpMedicationPZNBuilder.faker().category(MedicationCategory.C_00).build();
    val medicationRequest =
        MedicationRequestBuilder.faker(sina.getPatientData())
            .insurance(sina.getInsuranceCoverage())
            .requester(doctor.getPractitioner())
            .authoredOn(new Date(), precision)
            .medication(medication)
            .build();

    val kbvBundleBuilder =
        KbvErpBundleBuilder.faker(sina.getKvnr())
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication);

    // TODO: make sure outgoing Requests are not verified as this one will fail the HAPI validator
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
    // TODO: make sure to reset verification of outgoing Requests to avoid messing up with the
    // configuration!
  }

  @TestcaseId("ERP_TASK_ACTIVATE_05")
  @ParameterizedTest(name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} aus")
  @DisplayName("PKV E-Rezept als Verordnender Arzt an eine/n Versicherte/n ausstellen")
  @MethodSource("pkvPrescriptionTypesProvider")
  void activatePkvPrescription(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowType) {

    val isOldKbvProfile =
        KbvItaErpVersion.getDefaultVersion().compareTo(KbvItaErpVersion.V1_0_2) == 0;
    val pkvActivated = cucumberFeatures.isFeatureActive(E2ECucumberTag.INSURANCE_PKV);

    sina.changePatientInsuranceType(insuranceType);
    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withRandomKbvBundle());

    if (isOldKbvProfile || !pkvActivated) {
      // reject PKV prescription always on old profiles!
      // alternatively reject PKV prescriptions if PKV is deactivated
      doctor.attemptsTo(
          Verify.that(activation)
              .withOperationOutcome(FhirRequirements.FHIR_PROFILES)
              .hasResponseWith(returnCode(400))
              .isCorrect());
    } else {
      // new profiles + pkv activated: accept PKV prescriptions
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
  }

  @TestcaseId("ERP_TASK_ACTIVATE_06")
  @ParameterizedTest(name = "[{index}] -> Verordnender Arzt stellt ein E-Rezept für einen Versicherten mit dem Versicherungsart-Identifier {0} aus")
  @DisplayName("Verordnender Arzt stellt ein E-Rezept für einen Versicherten mit ungültigem Versicherungsart-Identifier aus")
  @EnumSource(value = IdentifierTypeDe.class, names = {"GKV", "PKV"}, mode = EnumSource.Mode.EXCLUDE)
  void invalidInsuranceType(
          IdentifierTypeDe insuranceIdentifierType) {

    val activation =
            doctor.performs(
                    IssuePrescription.forPatient(sina)
                            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                            .withResourceManipulator( it ->
                                    it.getPatient().getIdentifierFirstRep().getType().getCodingFirstRep().setCode(insuranceIdentifierType.getCode()))
                            .withRandomKbvBundle());
    doctor.attemptsTo(
            Verify.that(activation)
                    .withOperationOutcome(ErpAfos.A_23936)
                    .hasResponseWith(returnCode(400))
                    .and(operationOutcomeHasDetailsText("Als Identifier für den Patienten muss eine Versichertennummer angegeben werden.", ErpAfos.A_23936))
                    .isCorrect());

  }

  static Stream<Arguments> prescriptionTypesProvider() {
    val composer =
        ArgumentComposer.composeWith()
            .arguments(
                VersicherungsArtDeBasis.GKV, // given insurance kind
                PrescriptionAssignmentKind.PHARMACY_ONLY, // given assignment kind
                PrescriptionFlowType.FLOW_TYPE_160) // expected flow type
            .arguments(
                VersicherungsArtDeBasis.GKV,
                PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
                PrescriptionFlowType.FLOW_TYPE_169);

    if (cucumberFeatures.isFeatureActive(E2ECucumberTag.INSURANCE_PKV)) {
      composer
          .arguments(
              VersicherungsArtDeBasis.PKV,
              PrescriptionAssignmentKind.PHARMACY_ONLY,
              PrescriptionFlowType.FLOW_TYPE_200)
          .arguments(
              VersicherungsArtDeBasis.PKV,
              PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
              PrescriptionFlowType.FLOW_TYPE_209);
    }

    return composer.create();
  }

  static Stream<Arguments> prescriptionTypesProviderDarreichungsformen() {
    val dfList = List.of(Darreichungsform.IJD, Darreichungsform.PLD, Darreichungsform.SUI);
    return ArgumentComposer.composeWith(prescriptionTypesProvider()).multiply(2, dfList).create();
  }

  static Stream<Arguments> pkvPrescriptionTypesProvider() {
    return ArgumentComposer.composeWith()
        .arguments(
            VersicherungsArtDeBasis.PKV,
            PrescriptionAssignmentKind.PHARMACY_ONLY,
            PrescriptionFlowType.FLOW_TYPE_200)
        .arguments(
            VersicherungsArtDeBasis.PKV,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            PrescriptionFlowType.FLOW_TYPE_209)
        .create();
  }

  static Stream<Arguments> prescriptionTypesProviderInvalidAuthoredOn() {
    val invalidAuthoredOnDates =
        List.of(
            Date.from(Instant.now().plus(1, ChronoUnit.DAYS)),
            Date.from(Instant.now().minus(1, ChronoUnit.DAYS)));
    val composer =
        ArgumentComposer.composeWith()
            .arguments(
                VersicherungsArtDeBasis.GKV, // given insurance kind
                PrescriptionAssignmentKind.PHARMACY_ONLY) // expected flow type
            .arguments(VersicherungsArtDeBasis.GKV, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT);

    if (cucumberFeatures.isFeatureActive(E2ECucumberTag.INSURANCE_PKV)) {
      composer
          .arguments(VersicherungsArtDeBasis.PKV, PrescriptionAssignmentKind.PHARMACY_ONLY)
          .arguments(VersicherungsArtDeBasis.PKV, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT);
    }

    return composer.multiplyAppend(invalidAuthoredOnDates).create();
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
                VersicherungsArtDeBasis.GKV, // given insurance kind
                PrescriptionAssignmentKind.PHARMACY_ONLY) // expected flow type
            .arguments(VersicherungsArtDeBasis.GKV, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT);

    if (cucumberFeatures.isFeatureActive(E2ECucumberTag.INSURANCE_PKV)) {
      composer
          .arguments(VersicherungsArtDeBasis.PKV, PrescriptionAssignmentKind.PHARMACY_ONLY)
          .arguments(VersicherungsArtDeBasis.PKV, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT);
    }

    return composer.multiplyAppend(TemporalPrecisionEnum.class, excludedPrecisions).create();
  }
}
