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

package de.gematik.test.trezept.integration;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDetailText;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.communication.SendMessages;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.KtrActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Send Communication T-Rezept")
@Tag("TRezept")
class SendCommunicationTRezeptIT extends ErpTest {

  @Actor(name = "Sina Hüllmann")
  private PatientActor patient;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafen;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "AOK Bremen")
  private KtrActor healthInsurance;

  @Test
  @TestcaseId("ERP_COMMUNICATION_TREZEPT_01")
  @DisplayName("Schicken einer T-Rezept Communication mit supplyOptionsType SHIPMENT nicht erlaubt")
  void shouldRejectTRezeptCommunication() {

    val kbvBundleNew =
        KbvErpBundleFaker.builder().withMedication(KbvErpMedicationPZNFaker.asTPrescription());

    val task =
        doctor
            .performs(IssuePrescription.forPatient(sina).asTPrescription(kbvBundleNew.toBuilder()))
            .getExpectedResponse();

    val communication =
        patient.performs(
            SendMessages.to(flughafen)
                .forTask(task)
                .asDispenseRequest(
                    new CommunicationDisReqMessage(
                        SupplyOptionsType.SHIPMENT, GemFaker.getFaker().buffy().quotes())));

    patient.attemptsTo(
        Verify.that(communication)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400))
            .and(
                operationOutcomeContainsInDetailText(
                    "for flowType 166 only onPremise and delivery are allowed", ErpAfos.A_23878_01))
            .isCorrect());
  }

  @ParameterizedTest(
      name = "Schicken einer T-Rezept Communication mit supplyOptionsType {0} erlaubt")
  @EnumSource(
      value = SupplyOptionsType.class,
      names = {"ON_PREMISE", "DELIVERY"})
  @TestcaseId("ERP_COMMUNICATION_TREZEPT_02")
  void shouldSendSuccessfullyTRezeptCommunication(SupplyOptionsType supplyOptionsType) {

    val kbvBundleNew =
        KbvErpBundleFaker.builder().withMedication(KbvErpMedicationPZNFaker.asTPrescription());

    val task =
        doctor
            .performs(IssuePrescription.forPatient(sina).asTPrescription(kbvBundleNew.toBuilder()))
            .getExpectedResponse();

    val comm =
        patient.performs(
            SendMessages.to(flughafen)
                .forTask(task)
                .asDispenseRequest(
                    new CommunicationDisReqMessage(
                        supplyOptionsType, GemFaker.getFaker().buffy().quotes())));

    patient.attemptsTo(
        Verify.that(comm).withExpectedType().hasResponseWith(returnCode(201)).isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_TREZEPT_03")
  @ParameterizedTest(
      name = "[{index}] -> Zuweisung von T-Rezepten an Kostenträger fuehrt zu StatusCode 403 ({0})")
  @DisplayName("Zuweisung von T-Rezepten an Kostenträger fuehrt zu StatusCode 403")
  @EnumSource(
      value = InsuranceTypeDe.class,
      mode = INCLUDE,
      names = {"GKV", "PKV"})
  void shouldRejectDispRequestWithAssignmentToHealthInsurance(InsuranceTypeDe insuranceTypeDe) {
    patient.changePatientInsuranceType(insuranceTypeDe);
    val kbvBundleNew =
        KbvErpBundleFaker.builder().withMedication(KbvErpMedicationPZNFaker.asTPrescription());

    // activate
    val task =
        doctor
            .performs(IssuePrescription.forPatient(sina).asTPrescription(kbvBundleNew.toBuilder()))
            .getExpectedResponse();

    val communication =
        patient.performs(
            SendMessages.to(healthInsurance)
                .forTask(task)
                .asDispenseRequest(
                    new CommunicationDisReqMessage(
                        SupplyOptionsType.ON_PREMISE, GemFaker.getFaker().buffy().quotes())));

    patient.attemptsTo(
        Verify.that(communication)
            .withOperationOutcome(ErpAfos.A_27767)
            .hasResponseWith(returnCode(403))
            .isCorrect());
  }
}
