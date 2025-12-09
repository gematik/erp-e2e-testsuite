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

package de.gematik.test.erezept.integration.communication;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.communication.SendMessages;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.KtrActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.task.IssueDiGAPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import groovy.util.logging.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Integrationstest Communication Reply mit DiGA-Profil")
@Tag("DiGACommunication")
class CommunicationDiGAReplyIT extends ErpTest {

  @Actor(name = "Hanna Bäcker")
  private static PatientActor patient;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "AOK Bremen")
  private KtrActor ktr;

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharmacy;

  @TestcaseId("ERP_DIGA_COMMUNICATION_REPLY_01")
  @Test
  @DisplayName("Als Apotheke versuche ich eine Antwort für eine DiGA Verordnung zu senden")
  void shouldRejectDiGAReplyFromPharmacy() {
    doctor.attemptsTo(IssueDiGAPrescription.forPatient(patient));
    val dmc = SafeAbility.getAbility(patient, ManageDataMatrixCodes.class).getDmcs().getFirst();
    val patientBaseData = SafeAbility.getAbility(patient, ProvidePatientBaseData.class);

    val communication =
        ErxCommunicationBuilder.asReply(
                "Die Anfrage zur Ausstellung eines Freischaltcodes für die DiGA wurde abgwiesen, da"
                    + " Sie nicht bei der Gematik-KK versichert sind.")
            .sender(pharmacy.getTelematikId().getValue())
            .receiver(patientBaseData.getKvnr().getValue())
            .basedOn(dmc.getTaskId())
            .flowType(PrescriptionFlowType.FLOW_TYPE_162)
            .build();

    val responseInteraction = pharmacy.performs(SendMessages.withCommunication(communication));

    pharmacy.attemptsTo(
        Verify.that(responseInteraction)
            .withOperationOutcome(ErpAfos.A_19447_05)
            .hasResponseWith(returnCode(400))
            .isCorrect());
  }

  @TestcaseId("ERP_DIGA_COMMUNICATION_REPLY_02")
  @Test
  @DisplayName("Als Kostenträger sende ich eine Antwort für eine DiGA Verordnung")
  void shouldAcceptDiGAReplyFromPharmacy() {
    doctor.attemptsTo(IssueDiGAPrescription.forPatient(patient));
    val dmc = SafeAbility.getAbility(patient, ManageDataMatrixCodes.class).getDmcs().getFirst();
    val patientBaseData = SafeAbility.getAbility(patient, ProvidePatientBaseData.class);

    val communication =
        ErxCommunicationBuilder.asReply(
                "Die Anfrage zur Ausstellung eines Freischaltcodes für die DiGA wurde abgwiesen, da"
                    + " Sie nicht bei der Gematik-KK versichert sind.")
            .sender(ktr.getTelematikId().getValue())
            .receiver(patientBaseData.getKvnr().getValue())
            .basedOn(dmc.getTaskId())
            .flowType(PrescriptionFlowType.FLOW_TYPE_162)
            .build();

    val responseInteraction = ktr.performs(SendMessages.withCommunication(communication));

    ktr.attemptsTo(
        Verify.that(responseInteraction)
            .withExpectedType(ErpAfos.A_19447_05)
            .hasResponseWith(returnCode(201))
            .isCorrect());
  }
}
