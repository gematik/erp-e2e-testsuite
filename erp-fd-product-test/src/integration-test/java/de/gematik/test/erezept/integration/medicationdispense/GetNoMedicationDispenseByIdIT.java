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

package de.gematik.test.erezept.integration.medicationdispense;

import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDetailText;

import de.gematik.bbriccs.rest.HttpRequestMethod;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.ErpResponseVerifier;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.AcceptPrescription;
import de.gematik.test.erezept.actions.ClosePrescription;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.usecases.BaseCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Get MedicationDispense by Id should fail")
@Tag("MedicationDispense")
@Tag("GetMedDspByIdShouldFail")
public class GetNoMedicationDispenseByIdIT extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Hanna Bäcker")
  private PatientActor patient;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor pharmacy;

  @TestcaseId("GET_MEDICATION_DISPENSE_BY_ID_SHOULD_FAIL_01")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass der Endpunkt GET / MedicationDispens / <id>  nach dem"
          + " C_11928 die Anfrage eines Patienten mit einer OperationOutcome beantwortet und RC 405"
          + " zurückgibt.")
  public void getMedDispByIdShouldNotWorkForPatients() {

    val task = doctor.prescribeFor(patient);

    val acceptance = pharmacy.performs(AcceptPrescription.forTheTask(task));
    pharmacy.performs(ClosePrescription.acceptedWith(acceptance));

    val resp = patient.performs(new GetMedDspById(task.getPrescriptionId()));
    patient.attemptsTo(
        Verify.that(resp)
            .withOperationOutcome()
            .hasResponseWith(ErpResponseVerifier.returnCode(405))
            .and(
                operationOutcomeContainsInDetailText("no matching handler found.", ErpAfos.A_19514))
            .isCorrect());
  }

  @TestcaseId("GET_MEDICATION_DISPENSE_BY_ID_SHOULD_FAIL_02")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass der Endpunkt GET / MedicationDispens / <id> aufgerufen"
          + " von einer Apotheke mit einer OperationOutcome beantwortet und RC 403 zurückgibt.")
  public void getMedDispByIdShouldNotWorkForPharmacy() {

    val task = doctor.prescribeFor(patient);

    val acceptance = pharmacy.performs(AcceptPrescription.forTheTask(task));
    pharmacy.performs(ClosePrescription.acceptedWith(acceptance));

    val resp = pharmacy.performs(new GetMedDspById(task.getPrescriptionId()));
    pharmacy.attemptsTo(
        Verify.that(resp)
            .withOperationOutcome(ErpAfos.A_19405)
            .hasResponseWith(ErpResponseVerifier.returnCodeIs(403))
            .isCorrect());
  }

  private static class GetMedicationDispenseByIdShouldFailCommand
      extends BaseCommand<ErxMedicationDispense> {

    private GetMedicationDispenseByIdShouldFailCommand(PrescriptionId prescriptionId) {
      super(
          ErxMedicationDispense.class,
          HttpRequestMethod.GET,
          "/MedicationDispense",
          prescriptionId.getValue());
    }

    @Override
    public Optional<Resource> getRequestBody() {
      return Optional.empty();
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  private static class GetMedDspById extends ErpAction<ErxMedicationDispense> {

    private final PrescriptionId prescriptionId;

    @Override
    @Step(
        "{0} läd die Medication Dispense vom Erp-Fd mit Get/Medication Dispense/<taskId> herunter")
    public ErpInteraction<ErxMedicationDispense> answeredBy(
        net.serenitybdd.screenplay.Actor actor) {

      val cmd = new GetMedicationDispenseByIdShouldFailCommand(prescriptionId);
      return performCommandAs(cmd, actor);
    }
  }
}
