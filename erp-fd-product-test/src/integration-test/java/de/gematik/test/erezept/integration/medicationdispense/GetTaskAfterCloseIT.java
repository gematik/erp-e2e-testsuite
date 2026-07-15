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

package de.gematik.test.erezept.integration.medicationdispense;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIs;
import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.ownerIdInPrescriptionEquals;
import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.prescriptionInStatus;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Abruf des Task nach Close durch Apotheke")
@Tag("GetTaskAfterClose")
class GetTaskAfterCloseIT extends ErpTest {

  @Actor(name = "Sina Hüllmann")
  private PatientActor patient;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor pharmacy;

  @Actor(name = "Am Flughafen")
  private PharmacyActor airportPharmacy;

  @TestcaseId("ERP_GET_TASK_AFTER_CLOSE_01")
  @Test
  @DisplayName("Get-Task mit richtigem AccessCode nach dem Abschluss ($close) noch möglich")
  void getTaskAfterClose() {
    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val activation = pharmacy.performs(AcceptPrescription.forTheTask(task));

    pharmacy.performs(ClosePrescription.acceptedWith(activation));
    val response =
        pharmacy.performs(
            GetPrescriptionById.withTaskId(task.getTaskId()).withAccessCode(task.getAccessCode()));

    pharmacy.attemptsTo(
        Verify.that(response)
            .withExpectedType(ErpAfos.A_24178)
            .responseWith(returnCodeIs(200))
            .isCorrect());
  }

  @TestcaseId("ERP_GET_TASK_AFTER_CLOSE_02")
  @Test
  @DisplayName(
      "Get-Task mit richtigem Secret für alternative Apotheke nach accept immer noch möglich")
  void getTaskAfterCloseByAlternativePharmacy() {
    val task = doctor.prescribeFor(patient);
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task));

    val medication1 =
        GemErpMedicationFaker.forPznMedication()
            .withAmount(666)
            .withPzn(PZN.from("17377588"), "Comirnaty von BioNTech/Pfizer")
            .fake();

    val medDisp1 =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(patient.getKvnr())
            .withPrescriptionId(task.getPrescriptionId())
            .withMedication(medication1)
            .withPerformer(pharmacy.getTelematikId().getValue())
            .fake();

    val dispOpParams =
        GemOperationInputParameterBuilder.forDispensingPharmaceuticals()
            .with(medDisp1, medication1)
            .build();
    pharmacy
        .performs(
            DispensePrescriptionNew.withCredentials(
                    task.getTaskId(), acceptation.getExpectedResponse().getSecret())
                .withParameters(dispOpParams))
        .getExpectedResponse();
    val taskCalledByWaldApo =
        pharmacy.performs(
            GetPrescriptionById.withTaskId(task.getTaskId())
                .withSecret(acceptation.getExpectedResponse().getSecret()));
    pharmacy.attemptsTo(
        Verify.that(taskCalledByWaldApo)
            .withExpectedType()
            .and(ownerIdInPrescriptionEquals(pharmacy.getTelematikId(), ErpAfos.A_28410))
            .and(prescriptionInStatus(Task.TaskStatus.INPROGRESS))
            .isCorrect());

    airportPharmacy
        .performs(ClosePrescription.alternative().acceptedWith(acceptation))
        .getExpectedResponse();
    val taskCalledByAirportApo =
        airportPharmacy.performs(
            GetPrescriptionById.withTaskId(task.getTaskId())
                .withSecret(acceptation.getExpectedResponse().getSecret()));
    airportPharmacy.attemptsTo(
        Verify.that(taskCalledByAirportApo)
            .withExpectedType()
            .and(ownerIdInPrescriptionEquals(airportPharmacy.getTelematikId(), ErpAfos.A_28411))
            .and(prescriptionInStatus(Task.TaskStatus.COMPLETED))
            .isCorrect());
  }
}
