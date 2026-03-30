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

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.abilities.UseTheTRegisterMockClient;
import de.gematik.test.erezept.actions.AcceptPrescription;
import de.gematik.test.erezept.actions.ClosePrescription;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.GemaTestActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationRequestFaker;
import de.gematik.test.erezept.fhir.extensions.kbv.TeratogenicExtension;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.erezept.trezept.TRegisterMockDownloadRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Hello World Testscenario")
@Tag("Demo")
@Disabled("For Demonstration purpose only!")
class HelloWorldFeatureIT extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafen;

  @SneakyThrows
  @Test
  @TestcaseId("ERP_RC_HELLOWORLD_03")
  @DisplayName("T-Rezept")
  @Disabled("For Demonstration purpose only!")
  void testTRezeptHelloWorldTestCase() {
    val tRezeptFhirChecker = new GemaTestActor("tRezeptFhirChecker");
    this.config.equipWithTPrescriptionMockClient(tRezeptFhirChecker);

    sina.changePatientInsuranceType(InsuranceTypeDe.GKV);

    val kbvBundleNew =
        KbvErpBundleFaker.builder()
            .withMedication(KbvErpMedicationPZNFaker.asTPrescription())
            .withKvnr(sina.getKvnr());

    val task =
        doctor
            .performs(IssuePrescription.forPatient(sina).asTPrescription(kbvBundleNew.toBuilder()))
            .getExpectedResponse();
    // T-Rezepte werden an das Aktensystem übertragen; hier provide-prescription / EMILIA Testfall

    // Zuweisung an die Apotheke
    val acceptation = flughafen.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();
    // Dispensierung
    val dispensation = flughafen.performs(ClosePrescription.acceptedWith(acceptation));
    sina.attemptsTo(
        Verify.that(dispensation).withExpectedType().hasResponseWith(returnCode(200)).isCorrect());

    // ab jetzt wird die Übertragung an das Bfarm-Register eingeleitet; Die Übertragung ist wie bei
    // der EPA asynchron
    // folgende Schritte können jetzt ausgeführt werden
    // hole den digitalen Durchschlag vom Bfarm-Mocks

    // Auslagern als Screenplay Task oder Action
    val client = SafeAbility.getAbility(tRezeptFhirChecker, UseTheTRegisterMockClient.class);
    val request = new TRegisterMockDownloadRequest(task.getTaskId().getValue());

    Thread.currentThread().sleep(1000 * 30);

    val resp = client.downloadRequest(request);
    Assertions.assertTrue(!resp.isEmpty());

    // prüfe, ob der digitalen Durchschlag Fhir valide ist
    // prüfe, ob bestimmte Werte korrekt gesetzt sind
  }

  private KbvErpBundle generateKbvBundle(ErxTask task) {
    // Verordnungsdatensatz
    // Medication.category muss gesetzt sein

    val medication =
        KbvErpMedicationPZNFaker.builder()
            .withCategory(MedicationCategory.C_02)
            // Bundle -> -erp-angabeImpfstoffBtMT-RezeptFalse
            .withVaccine(false)
            .fake();

    val medicationRequest =
        KbvErpMedicationRequestFaker.builder().withMedication(medication).toBuilder()
            .isSER(false)
            .build();
    medicationRequest.addExtension(new TeratogenicExtension().asExtension());

    // -erp-angabeReichdauerT-RezeptPflicht
    // -erp-angabeReichdauerT-RezeptEinheit
    medicationRequest
        .getDispenseRequest()
        .getExpectedSupplyDuration()
        .setUnit("Woche(n)")
        .setValue(1);

    val dosageInstruction = medicationRequest.getDosageInstructionFirstRep();
    // see -erp-angabeDosierungKennzeichenFalse
    dosageInstruction.removeChild("text", dosageInstruction.getTextElement());
    dosageInstruction.removeExtension(KbvItaErpStructDef.DOSAGE_FLAG.getCanonicalUrl());

    // die Versionen sollten später über die Konfiguration gesetzt werden
    val kbvBundleBuilder =
        KbvErpBundleFaker.builder(KbvItaErpVersion.V1_4_0, KbvItaForVersion.V1_3_0)
            .withPrescriptionId(task.getPrescriptionId())
            .withMedication(medication)
            .toBuilder();
    kbvBundleBuilder.medicationRequest(medicationRequest);
    return kbvBundleBuilder.build();
  }
}
