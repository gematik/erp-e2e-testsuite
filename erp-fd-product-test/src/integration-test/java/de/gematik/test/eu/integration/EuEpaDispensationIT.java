/*
 * Copyright (Change Date see Readme), gematik GmbH
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

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIs;
import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.emlMedicationDispenseStatusIsCompleted;
import static de.gematik.test.core.expectations.verifier.emlverifier.EpaOpProvideDispensationVerifier.emlOrganizationCountryCodeMapsTo;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.eml.tasks.CheckEpaOpProvideDispensation;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.eu.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.EuPharmacyActor;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("EU ePA Dispensation")
@Tag("ERP_EPA_EU")
@Tag("ErpEu")
class EuEpaDispensationIT extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor germanDoctor;

  @Actor(name = "Hanna Bäcker")
  private PatientActor patientAtJourney;

  @Actor(name = "Hannes Vogt")
  private EuPharmacyActor euPharmacist;

  private final EuAccessCode accessCode = EuAccessCode.random();
  private GemaTestActor epaFhirChecker;

  @BeforeEach
  void setup() {
    patientAtJourney.attemptsTo(EnsureEuConsent.shouldBePresent());

    epaFhirChecker = new GemaTestActor("epaFhirChecker");
    this.config.equipWithEpaMockClient(epaFhirChecker);
  }

  private PrescriptionId preparePrescription() {
    val task = germanDoctor.prescribeFor(patientAtJourney);
    patientAtJourney.performs(PatchPrescriptionForEuRedemption.of(task.getTaskId()));

    patientAtJourney.performs(
        GrantEuAccessPermission.withAccessCode(accessCode).forCountryOf(euPharmacist));

    val demographicData =
        euPharmacist.performs(
            GetDemographicData.forPatient(patientAtJourney).withAccessCode(accessCode));
    euPharmacist.attemptsTo(
        Verify.that(demographicData)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());

    val euPrescriptionsInteraction =
        euPharmacist.performs(
            GetEuPrescriptions.forPatient(patientAtJourney).withAccessCode(accessCode));
    euPharmacist.attemptsTo(
        Verify.that(euPrescriptionsInteraction)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());

    return euPrescriptionsInteraction.getExpectedResponse().getPrescriptionIds().getFirst();
  }

  @Test
  @TestcaseId("ERP_EU_EPA_01")
  @Tag("MissingFdFeature")
  @DisplayName(
      "Übermittlung des countryCode und Bereitstellung der Dispensierinformation mit"
          + " status=completed an den ePA Medication Service nach $eu-close")
  void shouldProvideCompletedMedicationDispenseWithCountryCodeToEpaOnEuClose() {
    val euPrescriptionId = preparePrescription();

    val acceptedPrescriptionsInteraction =
        euPharmacist.performs(
            RetrievalEuPrescriptions.forPatient(patientAtJourney)
                .withPrescriptionIds(List.of(euPrescriptionId))
                .withAccessCode(accessCode));
    euPharmacist.attemptsTo(
        Verify.that(acceptedPrescriptionsInteraction)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());

    val prescriptionToDispense =
        acceptedPrescriptionsInteraction.getExpectedResponse().getKbvErpBundles().getFirst();

    val closeResponse =
        euPharmacist.performs(
            CloseEuPrescription.with(accessCode, patientAtJourney.getKvnr())
                .withAccepted(prescriptionToDispense));
    euPharmacist.attemptsTo(
        Verify.that(closeResponse)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());

    val euMedicationDispenses =
        patientAtJourney.performs(
            GetEuMedicationDispenses.forPrescription(prescriptionToDispense.getPrescriptionId()));
    patientAtJourney.attemptsTo(
        Verify.that(euMedicationDispenses)
            .withExpectedType()
            .hasResponseWith(returnCodeIs(200))
            .isCorrect());
    val erxMedicationDispenseBundle = euMedicationDispenses.getExpectedResponse();

    epaFhirChecker.attemptsTo(
        CheckEpaOpProvideDispensation.forDispensationWithAdditionalVerifier(
            erxMedicationDispenseBundle,
            euPharmacist.getTelematikId(),
            prescriptionToDispense.getPrescriptionId(),
            List.of(
                emlOrganizationCountryCodeMapsTo(euPharmacist.getCountryCode().asCoding()),
                emlMedicationDispenseStatusIsCompleted())));
  }
}
