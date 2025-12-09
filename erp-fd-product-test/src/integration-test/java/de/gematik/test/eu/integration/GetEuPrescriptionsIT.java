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

package de.gematik.test.eu.integration;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErxEuPrescriptionVerifier.*;
import static de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer.*;
import static org.hl7.fhir.r4.model.Task.TaskStatus.INPROGRESS;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.ErxEuPrescriptionVerifier;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.TaskAbort;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.eu.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.EuPharmacyActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.r4.eu.EuGetPrescriptionInput;
import de.gematik.test.erezept.fhir.r4.eu.EuPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.eu.EuPartNaming;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import groovy.util.logging.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("EU Prescription Retrieval")
@Tag("ErpEu")
class GetEuPrescriptionsIT extends ErpTest {

  @Actor(name = "Dr. Schraßer")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Fridolin Straßer")
  private PatientActor otherPatient;

  @Actor(name = "Hannes Vogt")
  private EuPharmacyActor hannesVogt;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor germanWaldApo;

  private static Stream<Arguments> allPrescriptionTypesProvider() {
    return Stream.of(
        Arguments.of(PrescriptionFlowType.FLOW_TYPE_160, true),
        Arguments.of(PrescriptionFlowType.FLOW_TYPE_169, false),
        Arguments.of(PrescriptionFlowType.FLOW_TYPE_200, true),
        Arguments.of(PrescriptionFlowType.FLOW_TYPE_209, false));
  }

  static Stream<Arguments> allPrescriptionTypesWithMvoProvider() {
    List<Arguments> combined = new ArrayList<>();

    NamedEnvelope<MultiplePrescriptionExtension> mvo1 =
        NamedEnvelope.of(
            "1 von 2 ab sofort",
            MultiplePrescriptionExtension.asMultiple(2, 2).validThrough(0, 365));

    NamedEnvelope<MultiplePrescriptionExtension> mvo2 =
        NamedEnvelope.of(
            "2 von 2 erst in 3 Monaten",
            MultiplePrescriptionExtension.asMultiple(2, 2).validThrough(90, 365));

    combined.add(Arguments.of(PrescriptionFlowType.FLOW_TYPE_160, mvo1, true, 1));
    combined.add(Arguments.of(PrescriptionFlowType.FLOW_TYPE_160, mvo2, false, 0));
    combined.add(Arguments.of(PrescriptionFlowType.FLOW_TYPE_200, mvo1, true, 1));
    combined.add(Arguments.of(PrescriptionFlowType.FLOW_TYPE_200, mvo2, false, 0));

    return combined.stream();
  }

  private static Consumer<EuGetPrescriptionInput> removePart(EuPartNaming part) {
    return input ->
        input.getRequestData().getPart().stream()
            .filter(p -> p.getName().equals(part.getCode()))
            .forEach(p -> p.setValue(null));
  }

  @Test
  @TestcaseId("ERP_GET_EU_01")
  @DisplayName("Abfrage des aktuellsten E-Rezepts im EU-Ausland")
  void shouldReturnLatestRedeemablePrescription() {
    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withRandomKbvBundle());

    val task = activation.getExpectedResponse();

    sina.attemptsTo(EnsureEuConsent.shouldBePresent());

    sina.performs(PatchPrescriptionForEuRedemption.of(task.getTaskId()));

    val accessCode = EuAccessCode.random();
    sina.performs(GrantEuAccessPermission.withAccessCode(accessCode).forCountryOf(hannesVogt));

    val demographicData =
        hannesVogt.performs(GetDemographicData.forPatient(sina).withAccessCode(accessCode));
    hannesVogt.attemptsTo(
        Verify.that(demographicData)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(bundleHasPrescriptionCount(1))
            .isCorrect());
    val euPrescriptions =
        hannesVogt.performs(GetEuPrescriptions.forPatient(sina).withAccessCode(accessCode));
    hannesVogt.attemptsTo(
        Verify.that(euPrescriptions)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .isCorrect());

    // houseKeeping
    sina.performs(TaskAbort.asPatient(task));
  }

  @Test
  @TestcaseId("ERP_GET_EU_02")
  @DisplayName("Abfrage aller offenen, einlösbaren E-Rezepte im EU-Ausland")
  void shouldReturnAllOpenRedeemablePrescriptions() {
    val activation1 =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withRandomKbvBundle());
    val activation2 =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withRandomKbvBundle());

    val taskId1 = activation1.getExpectedResponse().getTaskId();
    val taskId2 = activation2.getExpectedResponse().getTaskId();

    sina.attemptsTo(EnsureEuConsent.shouldBePresent());
    sina.performs(PatchPrescriptionForEuRedemption.of(taskId1));
    sina.performs(PatchPrescriptionForEuRedemption.of(taskId2));

    val accessCode = EuAccessCode.random();
    sina.performs(GrantEuAccessPermission.withAccessCode(accessCode).forCountryOf(hannesVogt));

    val euPrescriptions =
        hannesVogt.performs(
            GetEuPrescriptions.forPatient(sina).count(2).withAccessCode(accessCode));

    hannesVogt.attemptsTo(
        Verify.that(euPrescriptions)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(bundleHasPrescriptionCount(2))
            .isCorrect());

    // houseKeeping
    sina.performs(TaskAbort.asPatient(activation1.getExpectedResponse()));
    sina.performs(TaskAbort.asPatient(activation2.getExpectedResponse()));
  }

  @Test
  @TestcaseId("ERP_GET_EU_03")
  @DisplayName("Abfrage einer definierten Liste von E-Rezepten im EU-Ausland")
  void shouldReturnPrescriptionsForGivenList() {
    val activation1 =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withRandomKbvBundle());
    val activation2 =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withRandomKbvBundle());

    val taskId1 = activation1.getExpectedResponse().getTaskId();
    val taskId2 = activation2.getExpectedResponse().getTaskId();

    sina.attemptsTo(EnsureEuConsent.shouldBePresent());
    sina.performs(PatchPrescriptionForEuRedemption.of(taskId1));
    sina.performs(PatchPrescriptionForEuRedemption.of(taskId2));

    val accessCode = EuAccessCode.random();
    sina.performs(GrantEuAccessPermission.withAccessCode(accessCode).forCountryOf(hannesVogt));

    val prescriptionIds =
        List.of(
            activation1.getExpectedResponse().getPrescriptionId(),
            activation2.getExpectedResponse().getPrescriptionId());

    val retrievedPrescriptions =
        hannesVogt.performs(
            RetrievalEuPrescriptions.forPatient(sina)
                .withPrescriptionIds(prescriptionIds)
                .withAccessCode(accessCode));

    hannesVogt.attemptsTo(
        Verify.that(retrievedPrescriptions)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(tasksHaveStatus(INPROGRESS))
            .and(tasksHaveNewSecretIdentifier())
            .and(tasksHaveOwnerSet())
            .isCorrect());
    // houseKeeping
    sina.performs(TaskAbort.asPatient(activation1.getExpectedResponse()));
    sina.performs(TaskAbort.asPatient(activation2.getExpectedResponse()));
  }

  @Test
  @TestcaseId("ERP_GET_EU_04")
  @DisplayName("Abfrage von teilweise bereits abgefragten E-Rezepten im EU-Ausland")
  void shouldRetrievePartiallyClaimedPrescriptions() {
    val activation1 =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withRandomKbvBundle());
    val activation2 =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withRandomKbvBundle());

    val taskId1 = activation1.getExpectedResponse().getTaskId();
    val taskId2 = activation2.getExpectedResponse().getTaskId();

    sina.attemptsTo(EnsureEuConsent.shouldBePresent());
    sina.performs(PatchPrescriptionForEuRedemption.of(taskId1));
    sina.performs(PatchPrescriptionForEuRedemption.of(taskId2));

    val accessCode = EuAccessCode.random();
    sina.performs(GrantEuAccessPermission.withAccessCode(accessCode).forCountryOf(hannesVogt));

    val prescriptionId1 = activation1.getExpectedResponse().getPrescriptionId();
    val retrieved1 =
        hannesVogt.performs(
            RetrievalEuPrescriptions.forPatient(sina)
                .withPrescriptionIds(prescriptionId1)
                .withAccessCode(accessCode));

    hannesVogt.attemptsTo(
        Verify.that(retrieved1)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(tasksHaveStatus(INPROGRESS))
            .and(tasksHaveOwnerSet())
            .and(tasksHaveNewSecretIdentifier())
            .isCorrect());

    val prescriptionIds2 =
        List.of(
            activation1.getExpectedResponse().getPrescriptionId(),
            activation2.getExpectedResponse().getPrescriptionId());
    val retrieved2 =
        hannesVogt.performs(
            RetrievalEuPrescriptions.forPatient(sina)
                .withPrescriptionIds(prescriptionIds2)
                .withAccessCode(accessCode));

    hannesVogt.attemptsTo(
        Verify.that(retrieved2)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(ErxEuPrescriptionVerifier.tasksHaveStatus(INPROGRESS))
            .and(ErxEuPrescriptionVerifier.tasksHaveOwnerSet())
            .and(tasksHaveNewSecretIdentifier())
            .isCorrect());
    // houseKeeping
    sina.performs(TaskAbort.asPatient(activation1.getExpectedResponse()));
    sina.performs(TaskAbort.asPatient(activation2.getExpectedResponse()));
  }

  @ParameterizedTest(name = "[{index}] Abfrage der E-Rezept {0} ({1}) in der EU")
  @MethodSource("allPrescriptionTypesWithMvoProvider")
  @TestcaseId("ERP_GET_EU_05")
  @DisplayName("MVO-Verordnungen: Die erste Teilverordnung ist EU-eligible")
  void shouldRetrieveOnlyEligiblePrescriptionsAsMVO(
      PrescriptionFlowType flowType,
      NamedEnvelope<MultiplePrescriptionExtension> mvo,
      boolean expectedInResult,
      int availableMedications) {
    InsuranceTypeDe insurance = flowType.isPkvType() ? InsuranceTypeDe.PKV : InsuranceTypeDe.GKV;

    sina.changePatientInsuranceType(insurance);

    val medication = KbvErpMedicationPZNFaker.builder().fake();

    val patientCoverage = sina.getPatientCoverage();
    val kbvBundleBuilder =
        KbvErpBundleFaker.builder()
            .withMedication(medication)
            .withInsurance(patientCoverage.second, patientCoverage.first)
            .withPractitioner(doctor.getPractitioner())
            .withMvo(mvo.getParameter())
            .toBuilder();

    val activation =
        doctor.performs(IssuePrescription.forPatient(sina).withKbvBundleFrom(kbvBundleBuilder));

    val taskId = activation.getExpectedResponse().getTaskId();
    val prescriptionId = activation.getExpectedResponse().getPrescriptionId();

    sina.attemptsTo(EnsureEuConsent.shouldBePresent());
    sina.performs(PatchPrescriptionForEuRedemption.of(taskId));

    val accessCode = EuAccessCode.random();
    sina.performs(GrantEuAccessPermission.withAccessCode(accessCode).forCountryOf(hannesVogt));

    val euPrescriptions =
        hannesVogt.performs(GetEuPrescriptions.forPatient(sina).withAccessCode(accessCode));

    VerificationStep<EuPrescriptionBundle> bundleBehavior;
    if (expectedInResult) {
      bundleBehavior = bundleContainsPrescription(prescriptionId, availableMedications);
    } else {
      bundleBehavior = bundleNotContainsPrescription(prescriptionId);
    }

    hannesVogt.attemptsTo(
        Verify.that(euPrescriptions)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(bundleBehavior)
            .isCorrect());

    // houseKeeping
    sina.performs(TaskAbort.asPatient(activation.getExpectedResponse()));
  }

  @ParameterizedTest(name = "[{index}] Abfrage der {0} E-Rezept in der EU")
  @MethodSource("allPrescriptionTypesProvider")
  @TestcaseId("ERP_GET_EU_06")
  @DisplayName("Abfrage von berechtigte Verordnungen in der EU")
  void shouldRetrieveOnlyEligiblePrescriptions(
      PrescriptionFlowType flowType, boolean expectedInResult) {

    InsuranceTypeDe insurance = flowType.isPkvType() ? InsuranceTypeDe.PKV : InsuranceTypeDe.GKV;
    PrescriptionAssignmentKind assignment =
        flowType.isDirectAssignment()
            ? PrescriptionAssignmentKind.DIRECT_ASSIGNMENT
            : PrescriptionAssignmentKind.PHARMACY_ONLY;

    sina.changePatientInsuranceType(insurance);

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina).ofAssignmentKind(assignment).withRandomKbvBundle());

    val taskId = activation.getExpectedResponse().getTaskId();
    val prescriptionId = activation.getExpectedResponse().getPrescriptionId();

    sina.attemptsTo(EnsureEuConsent.shouldBePresent());
    sina.performs(PatchPrescriptionForEuRedemption.of(taskId));

    val accessCode = EuAccessCode.random();
    sina.performs(GrantEuAccessPermission.withAccessCode(accessCode).forCountryOf(hannesVogt));

    val euPrescriptions =
        hannesVogt.performs(GetEuPrescriptions.forPatient(sina).withAccessCode(accessCode));

    val bundleContains =
        expectedInResult
            ? bundleContainsPrescription(prescriptionId, 1)
            : bundleNotContainsPrescription(prescriptionId);

    hannesVogt.attemptsTo(
        Verify.that(euPrescriptions)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(bundleContains)
            .isCorrect());
    sina.performs(TaskAbort.asPatient(activation.getExpectedResponse()));
  }

  @Test
  @TestcaseId("ERP_GET_EU_07")
  @DisplayName("Abfrage einer e-Rezeptur mit fehlendem Accesscode und CountryCode")
  void shouldFailWhenAccessCodeOrCountryMissing() {
    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withRandomKbvBundle());

    val taskId = activation.getExpectedResponse().getTaskId();
    sina.attemptsTo(EnsureEuConsent.shouldBePresent());
    sina.performs(PatchPrescriptionForEuRedemption.of(taskId));

    val invalidRequest =
        GetEuPrescriptions.forPatient(sina)
            .with(removePart(EuPartNaming.COUNTRY_CODE))
            .with(removePart(EuPartNaming.ACCESS_CODE))
            .withRandomAccessCode();

    val response = hannesVogt.performs(invalidRequest);

    hannesVogt.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_27062)
            .hasResponseWith(returnCode(400))
            .isCorrect());
    sina.performs(TaskAbort.asPatient(activation.getExpectedResponse()));
  }

  @TestcaseId("ERP_GET_EU_08")
  @ParameterizedTest(
      name =
          "[{index}] Abfrage eines {0} {2} E-Rezeptes als {1} im EU-Ausland sollte nicht"
              + " herausgegeben werden")
  @MethodSource(
      "de.gematik.test.erezept.arguments.WorkflowAndMedicationComposer#workflowPharmacyOnlyAndAlternativeMedicationComposer")
  @DisplayName("Abfrage einer e-Rezeptur für Compounding-, Freitext- und Rezepturverordnung")
  void shouldFailWhileTryingToGetUnsupportedPrescriptionsVariationsInOtherEuropeanCounties(
      InsuranceTypeDe insuranceType,
      PrescriptionFlowType expectedFlowTypeForDescription,
      String medicationType) {

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(getMedication(medicationType))
                        .toBuilder()));

    sina.changePatientInsuranceType(insuranceType);
    sina.attemptsTo(EnsureEuConsent.shouldBePresent());
    sina.performs(
        PatchPrescriptionForEuRedemption.of(activation.getExpectedResponse().getTaskId()));

    val acessPermission =
        sina.performs(GrantEuAccessPermission.withRandomAccessCode().forCountryOf(hannesVogt));

    val euAccessCode = acessPermission.getExpectedResponse().getAccessCode();
    val euPrescriptions =
        hannesVogt.performs(GetEuPrescriptions.forPatient(sina).withAccessCode(euAccessCode));

    hannesVogt.attemptsTo(
        Verify.that(euPrescriptions)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(
                bundleNotContainsPrescription(activation.getExpectedResponse().getPrescriptionId()))
            .isCorrect());
    sina.performs(TaskAbort.asPatient(activation.getExpectedResponse()));
  }

  private KbvErpMedication getMedication(String medicationType) {
    return switch (medicationType) {
      case MEDICATION_INGREDIENT -> KbvErpMedicationIngredientFaker.builder().fake();
      case MEDICATION_COMPOUNDING -> KbvErpMedicationCompoundingFaker.builder().fake();
      case MEDICATION_FREITEXT -> KbvErpMedicationFreeTextBuilder.builder()
          .freeText(
              "Diese Rezeptart ist nur für den deutschen gebrauch. (Speziell für Zahnärzte die sich"
                  + " PZN-Gebühren sparen möchten ;-)")
          .build();
      default -> throw new IllegalArgumentException("Unknown medication type: " + medicationType);
    };
  }

  @Test
  @TestcaseId("ERP_GET_EU_09")
  @DisplayName("Abfrage eines E-Rezeptes am NCPeH-Endpunkt als normale Apotheke ")
  void shouldFailWhenNormalPharmacistUsesNCPeHEndpoint() {
    val tsk = doctor.prescribeFor(sina);
    sina.attemptsTo(EnsureEuConsent.shouldBePresent());
    sina.performs(PatchPrescriptionForEuRedemption.of(tsk.getPrescriptionId().toTaskId()));

    val accessPermission =
        sina.performs(GrantEuAccessPermission.withRandomAccessCode().forCountryOf(hannesVogt));
    EuAccessCode accessCode = accessPermission.getExpectedResponse().getAccessCode();
    sina.performs(GrantEuAccessPermission.withAccessCode(accessCode).forCountryOf(hannesVogt));

    val demographicData =
        germanWaldApo.performs(
            GetDemographicData.forKvnr(sina.getKvnr()).withAccessCode(accessCode));
    germanWaldApo.attemptsTo(
        Verify.that(demographicData)
            .withOperationOutcome()
            .hasResponseWith(returnCode(403))
            .isCorrect());

    val euPrescriptions =
        germanWaldApo.performs(GetEuPrescriptions.forPatient(sina).withAccessCode(accessCode));

    germanWaldApo.attemptsTo(
        Verify.that(euPrescriptions)
            .withOperationOutcome()
            .hasResponseWith(returnCode(403))
            .isCorrect());
    // HouseKeeping
    sina.performs(TaskAbort.asPatient(tsk));
  }

  @Test
  @TestcaseId("ERP_GET_EU_10")
  @DisplayName(
      "Fehlerhafte Abfrage eines E-Rezeptes am NCPeH-Endpunkt beim Patientenabgleich ohne Access-"
          + " oder CountryCode ")
  void shouldFailWhenGetDemographicsMissesCountryAndAccessCode() {
    val tsk = doctor.prescribeFor(sina);
    sina.attemptsTo(EnsureEuConsent.shouldBePresent());
    sina.performs(PatchPrescriptionForEuRedemption.of(tsk.getPrescriptionId().toTaskId()));

    val accessPermission =
        sina.performs(GrantEuAccessPermission.withRandomAccessCode().forCountryOf(hannesVogt));
    val euAccessCode = accessPermission.getExpectedResponse().getAccessCode();
    sina.performs(GrantEuAccessPermission.withAccessCode(euAccessCode).forCountryOf(hannesVogt));
    val invalidRequest =
        GetDemographicData.forKvnr(sina.getKvnr())
            .with(removePart(EuPartNaming.COUNTRY_CODE))
            .with(removePart(EuPartNaming.ACCESS_CODE))
            .withRandomAccessCode();

    val demographicData = hannesVogt.performs(invalidRequest);

    hannesVogt.attemptsTo(
        Verify.that(demographicData)
            .withOperationOutcome(ErpAfos.A_27091)
            .hasResponseWith(returnCode(400))
            .isCorrect());
    // HouseKeeping
    sina.performs(TaskAbort.asPatient(tsk));
  }

  @Test
  @TestcaseId("ERP_GET_EU_11")
  @DisplayName(
      "Fehlerhafte Abfrage eines E-Rezeptes am NCPeH-Endpunkt beim Claimen der Prescriptions")
  void shouldFailWhenGetEuPrescriptionsRetrievalMissesCountryAndAccessCode() {
    val tsk = doctor.prescribeFor(sina);
    sina.attemptsTo(EnsureEuConsent.shouldBePresent());
    sina.performs(PatchPrescriptionForEuRedemption.of(tsk.getPrescriptionId().toTaskId()));
    sina.performs(GrantEuAccessPermission.withRandomAccessCode().forCountryOf(hannesVogt));
    sina.performs(GrantEuAccessPermission.withRandomAccessCode().forCountryOf(hannesVogt));
    val prescriptionIds = List.of(tsk.getPrescriptionId());
    val invalidRetrievalQuestion =
        RetrievalEuPrescriptions.forPatient(sina)
            .with(removePart(EuPartNaming.COUNTRY_CODE))
            .with(removePart(EuPartNaming.ACCESS_CODE))
            .prescriptionIds(prescriptionIds)
            .withRandomAccessCode();
    val retrievedPrescriptions = hannesVogt.performs(invalidRetrievalQuestion);
    hannesVogt.attemptsTo(
        Verify.that(retrievedPrescriptions)
            .withOperationOutcome(ErpAfos.A_27091)
            .hasResponseWith(returnCode(400))
            .isCorrect());
    // HouseKeeping
    sina.performs(TaskAbort.asPatient(tsk));
  }

  @Test
  @TestcaseId("ERP_GET_EU_11")
  @DisplayName(
      "Fehlerhafte Abfrage eines E-Rezeptes am NCPeH-Endpunkt beim Patientenabgleich ohne Access-"
          + " oder CountryCode ")
  void shouldGetEmptyResponseWhileForgettingToTransferPrescriptionIds() {

    sina.attemptsTo(EnsureEuConsent.shouldBePresent());
    val accessPermission =
        sina.performs(GrantEuAccessPermission.withRandomAccessCode().forCountryOf(hannesVogt));
    sina.performs(
        GrantEuAccessPermission.withAccessCode(EuAccessCode.random()).forCountryOf(hannesVogt));
    val prescriptionIds = List.of(PrescriptionId.random());
    val invalidRetrivalQuestion =
        RetrievalEuPrescriptions.forPatient(sina)
            .with(removePart(EuPartNaming.PRESCRIPTION_ID))
            .prescriptionIds(prescriptionIds)
            .withAccessCode(accessPermission.getExpectedResponse().getAccessCode());
    val retrievedPrescriptions = hannesVogt.performs(invalidRetrivalQuestion);
    hannesVogt.attemptsTo(
        Verify.that(retrievedPrescriptions)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400))
            .isCorrect());
  }

  @Test
  @TestcaseId("ERP_GET_EU_12")
  @DisplayName("Abfrage eines E-Rezeptes am NCPeH-Endpunkt mit falscher KVNR")
  void shouldFailWhilesApothecaryUsesWrongKVNR() {
    // first patientJourney
    val tsk = doctor.prescribeFor(sina);
    sina.attemptsTo(EnsureEuConsent.shouldBePresent());
    sina.performs(PatchPrescriptionForEuRedemption.of(tsk.getPrescriptionId().toTaskId()));

    val sinasAccessPermission =
        sina.performs(GrantEuAccessPermission.withRandomAccessCode().forCountryOf(hannesVogt));

    // second patientJourney in LI
    val tsk2 = doctor.prescribeFor(otherPatient);
    otherPatient.attemptsTo(EnsureEuConsent.shouldBePresent());
    otherPatient.performs(PatchPrescriptionForEuRedemption.of(tsk2.getPrescriptionId().toTaskId()));
    otherPatient.performs(
        GrantEuAccessPermission.withAccessCode(EuAccessCode.random()).forCountryOf(hannesVogt));

    val euAccessCode = sinasAccessPermission.getExpectedResponse().getAccessCode();
    val demographicData =
        hannesVogt.performs(
            GetDemographicData.forPatient(otherPatient).withAccessCode(euAccessCode));
    hannesVogt.attemptsTo(
        Verify.that(demographicData)
            .withOperationOutcome(ErpAfos.A_27061)
            .hasResponseWith(returnCode(403))
            .isCorrect());

    val euPrescriptions =
        hannesVogt.performs(GetEuPrescriptions.forPatient(sina).withAccessCode(euAccessCode));

    hannesVogt.attemptsTo(
        Verify.that(euPrescriptions)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(bundleContainsOnlyKvnr(sina.getKvnr(), ErpAfos.A_27063))
            .isCorrect());
    // HouseKeeping
    sina.performs(TaskAbort.asPatient(tsk));
  }
}
