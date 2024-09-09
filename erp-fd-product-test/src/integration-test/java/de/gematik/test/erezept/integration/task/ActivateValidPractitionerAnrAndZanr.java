/*
 *  Copyright 2023 gematik GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.*;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvNamingSystem;
import de.gematik.test.erezept.fhir.values.LANR;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept mit valider ANR ausstellen")
public class ActivateValidPractitionerAnrAndZanr extends ErpTest {

  /**
   * Die Validierungsregeln besagen: Arztnummer: 1.-6. Stelle, Prüfziffer: 7. Stelle,
   * Fachgruppennummer: 8.-9. Stelle "Die Prüfziffer wird mittels des Modulo 10-Verfahrens der
   * Stellen 1-6 der Arztnummer ermittelt. Bei diesem Verfahren werden die Ziffern 1-6 von links
   * nach rechts abwechselnd mit 4 und 9 multipliziert. Die Summe dieser Produkte wird Modulo 10
   * berechnet. Die Prüfziffer ergibt sich aus der Differenz dieser Zahl zu 10 (ist die Differenz
   * 10, so ist die Prüfziffer 0)."
   */
  @Actor(name = "Leonie Hütter")
  private PatientActor patient;

  private static Stream<Arguments> validAnrComposer() {
    return ArgumentComposer.composeWith()
        .arguments(
            "4444444", "A_23891-01 Validierung der ANR-Prüfziffer in KBV_PR_FOR_Practitioner")
        .arguments(
            "999999900", "A_23891-01 Validierung der ANR-Prüfziffer in KBV_PR_FOR_Practitioner")
        .arguments("random", "A_23891-01 Validierung der ANR-Prüfziffer in KBV_PR_FOR_Practitioner")
        .arguments(
            "000000000", "A_23891-01 Validierung der ANR-Prüfziffer in KBV_PR_FOR_Practitioner")
        .arguments(
            "999999991", "A_23891-01 Validierung der ANR-Prüfziffer in KBV_PR_FOR_Practitioner")
        .arguments(
            "333333300", "A_23891-01 Validierung der ANR-Prüfziffer in KBV_PR_FOR_Practitioner")
        .multiply(
            0,
            List.of(
                VersicherungsArtDeBasis.BG,
                VersicherungsArtDeBasis.PKV,
                VersicherungsArtDeBasis.GKV))
        .multiply(1, PrescriptionAssignmentKind.class)
        .multiply(2, List.of("Gündüla Gunther", "Adelheid Ulmenwald")) // Arzt und Zahnarzt
        .create();
  }

  @TestcaseId("ERP_TASK_ACTIVATE_VALID_ANR_ZANR_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt {2} stellt ein E-Rezept mit ANR / ZANR: {3} für den Kostenträger {0} als Darreichungsform {1} aus. ")
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst die ANR in Practitioner korrekt validiert und zulässige Nummern akzeptiert")
  @MethodSource("validAnrComposer")
  void activateValidAnrZanrInPractitioner(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      DoctorActor doctors,
      String anr) {

    if (anr.length() == 7) anr = anr.concat(String.valueOf(GemFaker.fakerAmount(10, 99)));
    if (anr.length() < 7) // matches anr.equals("random")
    anr = LANR.random().getValue();

    val doc = this.getDoctorNamed(doctors.getName());
    patient.changePatientInsuranceType(insuranceType);

    val activation = doc.performs(getIssuePrescription(assignmentKind, anr, doc));

    doc.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_23891)
            .hasResponseWith(returnCode(200))
            .isCorrect());
  }

  private IssuePrescription getIssuePrescription(
      PrescriptionAssignmentKind assignmentKind, String anr, DoctorActor doctorActor) {
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    AccidentExtension accident = null;
    if (patient.getPatientInsuranceType().equals(VersicherungsArtDeBasis.BG))
      accident = AccidentExtension.accidentAtWork().atWorkplace();

    val kbvBundleBuilder =
        KbvErpBundleFaker.builder()
            .withKvnr(patient.getKvnr())
            .withPractitioner(doctorActor.getPractitioner())
            .withMedication(medication)
            .withInsurance(patient.getInsuranceCoverage(), patient.getPatientData())
            .withAccident(accident)
            .toBuilder();
    return IssuePrescription.forPatient(patient)
        .ofAssignmentKind(assignmentKind)
        .withResourceManipulator(
            kbvBundle ->
                kbvBundle.getPractitioner().getIdentifier().stream()
                    .filter(
                        it ->
                            KbvNamingSystem.BASE_ANR.match(it)
                                || KbvNamingSystem.ZAHNARZTNUMMER.match(it))
                    .findFirst()
                    .orElseThrow()
                    .setValue(anr))
        .withKbvBundleFrom(kbvBundleBuilder);
  }
}
