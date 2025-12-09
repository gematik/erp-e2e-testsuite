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

import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.bundleContainsLog;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeBetween;
import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.*;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.*;
import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.TaskVerifier;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.DownloadAuditEvent;
import de.gematik.test.erezept.actions.GetPrescriptionById;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.eu.EuGrantConsent;
import de.gematik.test.erezept.actions.eu.PatchPrescriptionForEuRedemption;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationIngredientFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ManageDoctorsPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ManagePatientPrescriptions;
import de.gematik.test.erezept.screenplay.task.IssueDiGAPrescription;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
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
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept durch den Versicherten zur Einlösung im EU-Ausland markieren")
@Tag("ErpEu")
class EuPrescriptionRedemptionMarkerIT extends ErpTest {

  @Actor(name = "Dr. Schraßer")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Leonie Hütter")
  private PatientActor leonie;

  static Stream<Arguments> mvoPrescriptionTypesProvider() {
    val composer =
        ArgumentComposer.composeWith()
            .arguments(
                NamedEnvelope.of(
                    "1 von 2 ab sofort für ein Jahr gültig",
                    MultiplePrescriptionExtension.asMultiple(1, 2).validThrough(0, 365)))
            .arguments(
                NamedEnvelope.of(
                    "2 von 2 in eine Woche für ein Jahr gültig",
                    MultiplePrescriptionExtension.asMultiple(2, 2).validThrough(7, 365)))
            .arguments(
                NamedEnvelope.of(
                    "Als einmalige Verordnung", MultiplePrescriptionExtension.asNonMultiple()));

    return multiplyWithFlowTypes(composer);
  }

  private static Stream<Arguments> multiplyWithFlowTypes(ArgumentComposer composer) {
    val insuranceArguments = List.of(InsuranceTypeDe.GKV, InsuranceTypeDe.PKV);
    composer.multiply(insuranceArguments);
    return composer.create();
  }

  private static Stream<Arguments> nonEuEligiblePrescriptionsProvider() {
    return Stream.of(
        Arguments.of(
            PrescriptionFlowType.FLOW_TYPE_169,
            (Supplier<KbvErpMedication>)
                () ->
                    KbvErpMedicationIngredientFaker.builder()
                        .withDrugName("Ibuprofen")
                        .withAmount("20", "Stk")
                        .withIngredientComponent(400, 1, "mg")
                        .withStandardSize(StandardSize.N2)
                        .fake()),
        Arguments.of(
            PrescriptionFlowType.FLOW_TYPE_209,
            (Supplier<KbvErpMedication>)
                () ->
                    KbvErpMedicationIngredientFaker.builder()
                        .withDrugName("Paracetamol")
                        .withAmount("10", "Stk")
                        .withIngredientComponent(500, 1, "mg")
                        .withStandardSize(StandardSize.N1)
                        .fake()));
  }

  @TestcaseId("ERP_EU_PATCH_PRESCRIPTION_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} aus "
              + "und markiert es für EU-Ausland")
  @DisplayName("E-Rezept für Einlösung im EU-Ausland markieren")
  @MethodSource("mvoPrescriptionTypesProvider")
  void shouldPatchMvoPrescriptionForEuRedemption(
      InsuranceTypeDe insuranceType, NamedEnvelope<MultiplePrescriptionExtension> mvo) {

    sina.changePatientInsuranceType(insuranceType);

    val medication =
        KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_00).fake();

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

    val expiryDateIsCorrect =
        mvo.getParameter()
            .getEnd()
            .map(TaskVerifier::hasCorrectMvoExpiryDate)
            .orElseGet(TaskVerifier::hasCorrectExpiryDate);

    val acceptDate =
        mvo.getParameter()
            .getEnd()
            .map(TaskVerifier::hasCorrectMvoAcceptDate)
            .orElseGet(
                () -> hasCorrectAcceptDate(PrescriptionFlowType.fromInsuranceKind(insuranceType)));

    doctor.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_27551)
            .hasResponseWith(returnCode(200))
            .and(isInReadyStatus())
            .and(expiryDateIsCorrect)
            .and(acceptDate)
            .isCorrect());

    sina.performs(EuGrantConsent.forOneSelf().withDefaultConsent());

    val taskId = activation.getExpectedResponse().getTaskId();
    val patchResponse = sina.performs(PatchPrescriptionForEuRedemption.of(taskId));

    sina.attemptsTo(
        Verify.that(patchResponse)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(hasRedeemableByProperties(true))
            .and(hasRedeemableByPatientAuthorization(true))
            .isCorrect());

    val searchParams =
        IQueryParameter.search()
            .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
            .sortedBy("date", SortOrder.DESCENDING)
            .createParameter();

    val auditEvents = sina.performs(DownloadAuditEvent.withQueryParams(searchParams));

    leonie.attemptsTo(
        Verify.that(auditEvents)
            .withExpectedType()
            .and(
                bundleContainsLog(
                    format(
                        "{0} {1} hat Markierung zu Einlösung im EU Ausland gespeichert",
                        sina.getEgk().getOwnerData().getGivenName(),
                        sina.getEgk().getOwnerData().getSurname())))
            .isCorrect());

    val getTaskResponse =
        sina.performs(GetPrescriptionById.withTaskId(taskId).withoutAuthentication());

    sina.attemptsTo(
        Verify.that(getTaskResponse)
            .withExpectedType()
            .and(hasRedeemableByPropertiesForBundlePrescription(true))
            .isCorrect());
  }

  @TestcaseId("ERP_EU_PATCH_PRESCRIPTION_02")
  @ParameterizedTest(
      name = "[{index}] → Das Markieren des nicht EU-konformen E-Rezepts ({0}) ist fehlgeschlagen")
  @DisplayName("Markieren eines nicht EU-konformen E-Rezepts schlägt fehl")
  @MethodSource("nonEuEligiblePrescriptionsProvider")
  void shouldRejectPatchForNonEuEligiblePrescription(
      PrescriptionFlowType flowType, Supplier<KbvErpMedication> kbvErpMedicationSupplier) {

    val m = kbvErpMedicationSupplier.get();

    InsuranceTypeDe insuranceType =
        flowType.isPkvType() ? InsuranceTypeDe.PKV : InsuranceTypeDe.GKV;

    PrescriptionAssignmentKind assignmentKind =
        flowType.isDirectAssignment()
            ? PrescriptionAssignmentKind.DIRECT_ASSIGNMENT
            : PrescriptionAssignmentKind.PHARMACY_ONLY;

    sina.changePatientInsuranceType(insuranceType);

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(assignmentKind)
                .withKbvBundleFrom(KbvErpBundleFaker.builder().withMedication(m).toBuilder()));

    val taskId = activation.getExpectedResponse().getTaskId();
    val patchResponse = sina.performs(PatchPrescriptionForEuRedemption.of(taskId));

    sina.attemptsTo(
        Verify.that(patchResponse)
            .withOperationOutcome(ErpAfos.A_27550)
            .hasResponseWith(returnCode(403))
            .isCorrect());
  }

  @Test
  @TestcaseId("ERP_EU_PATCH_PRESCRIPTION_03")
  @DisplayName("Markieren eines DiGA E-Rezepts im EU-Ausland schlägt fehl")
  void shouldRejectPatchForDiGAPrescription() {
    sina.can(ManageDataMatrixCodes.heGetsPrescribed());
    sina.can(ManagePatientPrescriptions.heReceived());

    doctor.can(ManageDoctorsPrescriptions.heIssued());
    doctor.attemptsTo(IssueDiGAPrescription.forPatient(sina));

    val lastPrescription =
        SafeAbility.getAbility(doctor, ManageDoctorsPrescriptions.class).getLast();
    val taskId = lastPrescription.getTaskId();

    val patchResponse = sina.performs(PatchPrescriptionForEuRedemption.of(taskId));

    sina.attemptsTo(
        Verify.that(patchResponse)
            .withOperationOutcome()
            .hasResponseWith(returnCodeBetween(400, 429))
            .isCorrect());
  }

  @Test
  @TestcaseId("ERP_EU_PATCH_PRESCRIPTION_04")
  @DisplayName("Als Patient versuche ich, ein fremdes EU E-Rezept zu markieren")
  void shouldRejectPatchIfPrescriptionNotOwnedByPatient() {
    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withRandomKbvBundle());

    val taskId = activation.getExpectedResponse().getTaskId();

    val patchResponse = leonie.performs(PatchPrescriptionForEuRedemption.of(taskId));

    Verify.that(patchResponse)
        .withOperationOutcome(ErpAfos.A_27550)
        .hasResponseWith(returnCode(403))
        .isCorrect();
  }
}
