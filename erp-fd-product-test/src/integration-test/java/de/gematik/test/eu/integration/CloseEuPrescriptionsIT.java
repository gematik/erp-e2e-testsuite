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
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIs;
import static de.gematik.test.core.expectations.verifier.MedicationDispenseBundleVerifier.*;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHasDetailsText;
import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.*;
import static de.gematik.test.erezept.fhir.valuesets.eu.EuPartNaming.MED_DISPENSE;
import static java.text.MessageFormat.format;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.abilities.ProvidePharmacyBaseData;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actions.eu.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.EuPharmacyActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.fhir.builder.eu.*;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.eu.*;
import de.gematik.test.erezept.fhir.r4.kbv.KbvBaseBundle;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.fuzzing.eu.EuCloseOperationManipulatorFactory;
import groovy.util.logging.Slf4j;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.Task;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("EU Close Prescription")
@Tag("ERP_CLOSE_EU")
@Tag("ErpEu")
class CloseEuPrescriptionsIT extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor germanDoctor;

  @Actor(name = "Hanna Bäcker")
  private PatientActor patientAtJourney;

  @Actor(name = "Hannes Vogt")
  private EuPharmacyActor euPharmacist;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor germanPharmacy;

  private final EuAccessCode accessCode = EuAccessCode.random();

  @BeforeEach
  void ensureConsent() {
    patientAtJourney.attemptsTo(EnsureEuConsent.shouldBePresent());
  }

  private List<PrescriptionId> preparePrescriptions() {
    return preparePrescriptions(1);
  }

  private List<PrescriptionId> preparePrescriptions(int amount) {
    // generate Prescription
    val scenarioTasks =
        IntStream.range(0, amount)
            .mapToObj(it -> germanDoctor.prescribeFor(patientAtJourney))
            .map(ErxTask::getTaskId)
            .toList();

    // ensure tasks are patched for EuRedemption
    scenarioTasks.forEach(it -> patientAtJourney.performs(PatchPrescriptionForEuRedemption.of(it)));

    patientAtJourney.performs(
        GrantEuAccessPermission.withAccessCode(accessCode).forCountryOf(euPharmacist));

    // patientValidation in EU abroad
    val demographicData =
        euPharmacist.performs(
            GetDemographicData.forPatient(patientAtJourney).withAccessCode(accessCode));
    euPharmacist.attemptsTo(
        Verify.that(demographicData)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());
    // check available prescriptions
    val euPrescriptionsInteraction =
        euPharmacist.performs(
            GetEuPrescriptions.forPatient(patientAtJourney).withAccessCode(accessCode));

    euPharmacist.attemptsTo(
        Verify.that(euPrescriptionsInteraction)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());
    val euPrescriptions = euPrescriptionsInteraction.getExpectedResponse();
    return euPrescriptions.getPrescriptionIds();
  }

  @TestcaseId("ERP_EU_CLOSE_001")
  @ParameterizedTest(
      name = "[{index}] -> Erfolgreiche Übermittlung von Abgabeinformationen für {0} E-Rezepte")
  @DisplayName("Erfolgreiche Übermittlung von Abgabeinformationen durch den Fachdienst")
  @ValueSource(ints = {1, 2})
  void shouldSuccessfullyCloseEuPrescription(int prescriptionAmount) {
    val euPrescriptionIds = preparePrescriptions(prescriptionAmount);

    // accept Prescription
    val acceptedPrescriptionsInteraction =
        euPharmacist.performs(
            RetrievalEuPrescriptions.forPatient(patientAtJourney)
                .withPrescriptionIds(euPrescriptionIds)
                .withAccessCode(accessCode));
    euPharmacist.attemptsTo(
        Verify.that(acceptedPrescriptionsInteraction)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());

    // Dispense- & Close-Prescription
    val prescriptionToDispense =
        acceptedPrescriptionsInteraction.getExpectedResponse().getKbvErpBundles();
    prescriptionToDispense.forEach(
        prescription -> {
          val closeResponse =
              euPharmacist.performs(
                  CloseEuPrescription.with(accessCode, patientAtJourney.getKvnr())
                      .withAccepted(prescription));
          euPharmacist.attemptsTo(
              Verify.that(closeResponse)
                  .withExpectedType()
                  .hasResponseWith(returnCodeIs(200))
                  .isCorrect());
        });

    val audit = patientAtJourney.performs(DownloadAuditEvent.orderByDateDesc());

    patientAtJourney.attemptsTo(
        Verify.that(audit)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .and(bundleContainsLog(getAuditEventComparison()))
            .isCorrect());

    prescriptionToDispense.stream()
        .map(KbvBaseBundle::getPrescriptionId)
        .forEach(
            pid -> {
              val euMD = patientAtJourney.performs(GetEuMedicationDispenses.forPrescription(pid));
              patientAtJourney.attemptsTo(
                  Verify.that(euMD)
                      .withExpectedType()
                      .hasResponseWith(returnCodeIs(200))
                      .and(containsEuPrescriptionAndDispensation(pid, ErpAfos.A_27068))
                      .isCorrect());
            });
  }

  private @NotNull String getAuditEventComparison() {
    val pharmacyBaseData = SafeAbility.getAbility(euPharmacist, ProvidePharmacyBaseData.class);
    return format(
        "Der {0} {1} hat in {2} {3} in Land {4} Ihr E-Rezept eingelöst.",
        EuPractitionerRoleBuilder.getSimplePractitionerRole()
            .getCodeFirstRep()
            .getCodingFirstRep()
            .getCode(),
        pharmacyBaseData.getPractitionerIdentifier().getValue(),
        EuHealthcareFacilityType.getDefault().getCode(),
        pharmacyBaseData.getOrganizationIdentifier().getValue(),
        pharmacyBaseData.getCountryCode());
  }

  @Test
  @TestcaseId("ERP_EU_CLOSE_002")
  @DisplayName("Erfolglose Übermittlung von Abgabeinformationen an den Fachdienst ohne Accept")
  void shouldDenySendingEuCloseInputParamWithoutAccept() {
    preparePrescriptions();

    // check available prescriptions
    val prescriptionToDispense =
        euPharmacist
            .performs(GetEuPrescriptions.forPatient(patientAtJourney).withAccessCode(accessCode))
            .getExpectedResponse()
            .getKbvErpBundles();

    // Note: here accept should be performed, but forgotten by the EU Pharmacist

    // Dispense- & Close-Prescription
    prescriptionToDispense.forEach(
        prescription -> {
          val closeResponse =
              euPharmacist.performs(
                  CloseEuPrescription.with(accessCode, patientAtJourney.getKvnr())
                      .withAccepted(prescription));
          euPharmacist.attemptsTo(
              Verify.that(closeResponse)
                  .withOperationOutcome()
                  .hasResponseWith(returnCodeIs(403))
                  .isCorrect());
        });
  }

  @Test
  @TestcaseId("ERP_EU_CLOSE_003")
  @DisplayName("Erfolglose Übermittlung von Abgabeinformationen an den Fachdienst ohne EuConsent")
  void shouldDenySendingEuCloseInputParamWithoutConsent() {
    val euPrescriptionIds = preparePrescriptions();

    // accept Prescription
    val acceptedPrescriptionsInteraction =
        euPharmacist.performs(
            RetrievalEuPrescriptions.forPatient(patientAtJourney)
                .withPrescriptionIds(euPrescriptionIds)
                .withAccessCode(accessCode));
    euPharmacist.attemptsTo(
        Verify.that(acceptedPrescriptionsInteraction)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());

    // set EuConsent and prepare for EuRedemption
    patientAtJourney.performs(EuRejectConsent.forOneSelf().build());

    // Dispense- & Close-Prescription
    val prescriptionToDispense =
        acceptedPrescriptionsInteraction.getExpectedResponse().getKbvErpBundles().stream()
            .findFirst()
            .orElseThrow();
    val closeResponse =
        euPharmacist.performs(
            CloseEuPrescription.with(accessCode, patientAtJourney.getKvnr())
                .withAccepted(prescriptionToDispense));

    euPharmacist.attemptsTo(
        Verify.that(closeResponse)
            .withOperationOutcome()
            .and(
                operationOutcomeHasDetailsText(
                    "Patienteneinwilligung nicht vorhanden", ErpAfos.A_27070))
            .hasResponseWith(returnCodeIs(403))
            .isCorrect());
  }

  @Test
  @TestcaseId("ERP_EU_CLOSE_004")
  @DisplayName("Zugriffsberechtigung nicht Korrekt, da revoke-eu-access-permission für das Land LI")
  void shouldFailWhileCloseEuPrescriptionWithoutAccessPermission() {
    val euPrescriptionIds = preparePrescriptions();

    // accept Prescription
    val acceptedPrescriptionsInteraction =
        euPharmacist.performs(
            RetrievalEuPrescriptions.forPatient(patientAtJourney)
                .withPrescriptionIds(euPrescriptionIds)
                .withAccessCode(accessCode));
    euPharmacist.attemptsTo(
        Verify.that(acceptedPrescriptionsInteraction)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());

    // revoke-eu-access-permission
    val revokeAccess = patientAtJourney.performs(DeleteEuAccessPermission.forOneSelf());
    patientAtJourney.attemptsTo(
        Verify.that(revokeAccess).withoutBody().hasResponseWith(returnCodeIs(204)).isCorrect());

    // Dispense- & Close-Prescription
    val prescriptionToDispense =
        acceptedPrescriptionsInteraction.getExpectedResponse().getKbvErpBundles().stream()
            .findFirst()
            .orElseThrow();
    val closeResponse =
        euPharmacist.performs(
            CloseEuPrescription.with(accessCode, patientAtJourney.getKvnr())
                .withAccepted(prescriptionToDispense));
    euPharmacist.attemptsTo(
        Verify.that(closeResponse)
            .withOperationOutcome(ErpAfos.A_27071)
            .hasResponseWith(returnCodeIs(403))
            .isCorrect());
  }

  // Prio-2

  @Test
  @TestcaseId("ERP_EU_CLOSE_005")
  @DisplayName("Deutscher LEI darf den Endpunkt $eu-close nicht benutzen")
  void shouldFailWhileClosEuWithWrongOid() {
    val euPrescriptionIds = preparePrescriptions();

    // accept Prescription
    val acceptedPrescriptionsInteraction =
        euPharmacist.performs(
            RetrievalEuPrescriptions.forPatient(patientAtJourney)
                .withPrescriptionIds(euPrescriptionIds)
                .withAccessCode(accessCode));
    euPharmacist.attemptsTo(
        Verify.that(acceptedPrescriptionsInteraction)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());
    // Dispense- & Close-Prescription
    val prescriptionToDispense =
        acceptedPrescriptionsInteraction.getExpectedResponse().getKbvErpBundles().stream()
            .findFirst()
            .orElseThrow();
    val closeResponse =
        germanPharmacy.performs(
            CloseEuPrescription.with(accessCode, patientAtJourney.getKvnr())
                .withAccepted(prescriptionToDispense));
    germanPharmacy.attemptsTo(
        Verify.that(closeResponse)
            .withOperationOutcome(ErpAfos.A_27069)
            .hasResponseWith(returnCodeIs(403))
            .isCorrect());
  }

  @Test
  @TestcaseId("ERP_EU_CLOSE_006")
  @DisplayName(
      "Fehlgeschlagene Übermittlung von Abgabeinformationen durch fehlerhaften request body")
  void shouldFailClosingEuPrescriptionCausedByInvalidRequestBody() {
    val euPrescriptionIds = preparePrescriptions();

    val manipulators = EuCloseOperationManipulatorFactory.getAllEuCloseOperationManipulators();
    // accept Prescription
    val acceptedPrescriptionsInteraction =
        euPharmacist.performs(
            RetrievalEuPrescriptions.forPatient(patientAtJourney)
                .withPrescriptionIds(euPrescriptionIds)
                .withAccessCode(accessCode));
    euPharmacist.attemptsTo(
        Verify.that(acceptedPrescriptionsInteraction)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());
    // Dispense- & Close-Prescription
    val prescriptionToDispense =
        acceptedPrescriptionsInteraction.getExpectedResponse().getKbvErpBundles().stream()
            .findFirst()
            .orElseThrow();

    for (val manipulator : manipulators) {
      val closeResponse =
          euPharmacist.performs(
              CloseEuPrescription.with(accessCode, patientAtJourney.getKvnr())
                  .withResourceManipulator(manipulator)
                  .withAccepted(prescriptionToDispense));
      euPharmacist.attemptsTo(
          Verify.that(closeResponse)
              .withOperationOutcome(ErpAfos.A_27072)
              .hasResponseWith(returnCodeIs(400))
              .isCorrect());
    }
  }

  @Test
  @TestcaseId("ERP_EU_CLOSE_007")
  @DisplayName(
      "Nachtest B_FD-1349, Erfolgreiche Übermittlung von Abgabeinformationen nach einstellen einer"
          + " deutschen und einer europäischen Dispensiereung und Herausgabe an den Patienten")
  void shouldSuccessfullyResponseDispenseInformation() {
    val euPrescriptionIds = preparePrescriptions();

    // german dispensation
    val task = germanDoctor.prescribeFor(patientAtJourney);
    val accepted = germanPharmacy.performs(AcceptPrescription.forTheTask(task));
    val germanClose =
        germanPharmacy.performs(ClosePrescription.alternative().acceptedWith(accepted, new Date()));

    // accept EuPrescription
    val euAcceptedPrescriptionsInteraction =
        euPharmacist.performs(
            RetrievalEuPrescriptions.forPatient(patientAtJourney)
                .withPrescriptionIds(euPrescriptionIds)
                .withAccessCode(accessCode));
    euPharmacist.attemptsTo(
        Verify.that(euAcceptedPrescriptionsInteraction)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());
    // Dispense- & Close-Prescription
    val prescriptionToDispense =
        euAcceptedPrescriptionsInteraction.getExpectedResponse().getKbvErpBundles().stream()
            .findFirst()
            .orElseThrow();
    CloseEuPrescription closeEuPrescription =
        CloseEuPrescription.with(accessCode, patientAtJourney.getKvnr())
            .withAccepted(prescriptionToDispense);
    val closeResponse = euPharmacist.performs(closeEuPrescription);
    val euMedicationDispense =
        (EuMedicationDispense)
            closeEuPrescription
                .getEuCloseOperationInput()
                .getFirstRxDispension()
                .orElseThrow()
                .getPart()
                .stream()
                .filter(p -> p.getName().equals(MED_DISPENSE.getCode()))
                .findFirst()
                .orElseThrow()
                .getResource();
    euPharmacist.attemptsTo(
        Verify.that(closeResponse)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());

    val euMD =
        patientAtJourney.performs(
            GetEuMedicationDispenses.whenHandedOver(SearchPrefix.EQ, LocalDate.now()));
    val euMedicationDispenseResponse =
        euMD
            .getExpectedResponse()
            .getEuDispensePairBy(euMedicationDispense.getPrescriptionId())
            .stream()
            .map(p -> p.getLeft())
            .findFirst()
            .orElseThrow();
    patientAtJourney.attemptsTo(
        Verify.that(euMD)
            .withExpectedType()
            .and(
                containsEuPrescriptionAndDispensation(
                    prescriptionToDispense.getPrescriptionId(), ErpAfos.A_27068))
            .and(containsEuPractitionerData(prescriptionToDispense.getPrescriptionId()))
            .and(containsEuPractitionerRoleRelateTo(euMedicationDispenseResponse))
            .and(containsEuOrganisationDataRelatesTo(euMedicationDispenseResponse))
            .and(
                containsErxMedicationAndGemMedDispense(
                    germanClose.getExpectedResponse().getPrescriptionId()))
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());
  }

  @Test
  @TestcaseId("ERP_EU_CLOSE_008")
  @DisplayName(
      "Erfolgreiche Übermittlung von Abgabeinformationen und überprüfung des Systemzeitstempels"
          + " beim Dispensieren im EuAusland")
  void shouldCheckTimestampWhileClosing() {
    val euPrescriptionIds = preparePrescriptions();

    // accept Prescription
    val acceptedPrescriptionsInteraction =
        euPharmacist.performs(
            RetrievalEuPrescriptions.forPatient(patientAtJourney)
                .withPrescriptionIds(euPrescriptionIds)
                .withAccessCode(accessCode));
    euPharmacist.attemptsTo(
        Verify.that(acceptedPrescriptionsInteraction)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());

    // fetch the accepted tasks to get the lastModified timestamps from
    val acceptedTaskModificationDates =
        euPrescriptionIds.stream()
            .collect(
                Collectors.toMap(
                    pid -> pid,
                    pid ->
                        patientAtJourney
                            .performs(TheTask.withId(pid.toTaskId()))
                            .getExpectedResponse()
                            .getTask()
                            .getLastModified()));

    // Dispense- & Close-Prescription
    val prescriptionToDispense =
        acceptedPrescriptionsInteraction.getExpectedResponse().getKbvErpBundles().stream()
            .findFirst()
            .orElseThrow();
    val closeAction =
        CloseEuPrescription.with(accessCode, patientAtJourney.getKvnr())
            .withAccepted(prescriptionToDispense);
    euPharmacist.performs(closeAction);

    euPrescriptionIds.forEach(
        pid -> {
          val previousLastModified = acceptedTaskModificationDates.get(pid);
          val closedTask = patientAtJourney.performs(TheTask.withId(pid.toTaskId()));
          patientAtJourney.attemptsTo(
              Verify.that(closedTask)
                  .withExpectedType()
                  .hasResponseWith(returnCodeIs(200))
                  .and(lastModifiedIsAfter(previousLastModified))
                  .and(hasLastMedDspTimestampEq(closeAction.getTimeStamp(), ErpAfos.A_27074))
                  .and(prescriptionHasStatus(Task.TaskStatus.COMPLETED, ErpAfos.A_27072))
                  .isCorrect());
        });
  }
}
