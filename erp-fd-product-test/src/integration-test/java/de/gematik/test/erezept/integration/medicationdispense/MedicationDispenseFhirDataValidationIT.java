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

package de.gematik.test.erezept.integration.medicationdispense;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIs;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.FhirRequirements;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.AcceptPrescription;
import de.gematik.test.erezept.actions.ClosePrescription;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.fuzzing.erx.ErxMedicationDispenseManipulatorFactory;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("MedicationDispense FHIR Data Validation")
@Tag("InvalidMedicationDispense")
public class MedicationDispenseFhirDataValidationIT extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Hanna Bäcker")
  private PatientActor patient;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor pharmacy;

  @TestcaseId("ERP_DISPENSE_MEDICATION_WITH_INVALID_DATE_PATTERN")
  @Test
  @DisplayName(
      "Prüfung, ob der Fachdienst die MedicationDispense mit einem ungültigen Datumsformat nicht"
          + " akzeptiert")
  void shouldCheckMedicationDispenseWithInvalidDatePattern() {
    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task));
    val manipulators =
        ErxMedicationDispenseManipulatorFactory.getAllMedicationDispenseManipulators();

    // The shuffle method is used here to have different orders of manipulators
    Collections.shuffle(manipulators);

    // We assume that the FD will not close with any of the manipulators
    // (ErxMedicationDispenseManipulatorFactory)
    for (val manipulator : manipulators) {
      log.info(manipulator.getName());
      val closeResponse =
          pharmacy.performs(
              ClosePrescription.alternative()
                  .withResourceManipulator(manipulator)
                  .acceptedWith(acceptation));

      pharmacy.attemptsTo(
          Verify.that(closeResponse)
              .withOperationOutcome(FhirRequirements.DATE_TIME_CONSTRAINT)
              .responseWith(returnCodeIs(400)) // Expecting 400 for invalid data
              .isCorrect());
    }
  }
}
