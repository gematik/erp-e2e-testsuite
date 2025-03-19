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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.headerContentContains;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDetailText;

import de.gematik.bbriccs.fhir.de.valueset.Country;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.FhirRequirements;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvAssignerOrganizationBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationRequestFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvMedicalOrganizationBuilder;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvNamingSystem;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.valuesets.StatusKennzeichen;
import de.gematik.test.erezept.screenplay.abilities.ProvideDoctorBaseData;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.erezept.toggle.AnrValidationConfigurationIsErrorToggle;
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
@DisplayName("E-Rezept mit invalider ANR ausstellen")
public class ActivateInvalidPractitionerAnrAndZanr extends ErpTest {

  /**
   * Warning ResponseHeader ist in A_24033 genauer spezifiziert und wir nach RFC-2616 "Warning":
   * https://datatracker.ietf.org/doc/html/rfc2616#section-14.46
   *
   * <p>konstruiert. Der folgende String hat demnach die Key-Value-Paare: warn-code: 252 warn-agent:
   * erp-server warn-text: "Ungültige Arztnummer (LANR oder ZANR): Die übergebene Arztnummer
   * entspricht nicht den Prüfziffer-Validierungsregeln"
   *
   * <p>Die Validierungsregeln besagen: Arztnummer: 1.-6. Stelle, Prüfziffer: 7.Stelle,
   * Fachgruppennummer: 8.-9.Stelle "Die Prüfziffer wird mittels des Modulo 10-Verfahrens der
   * Stellen 1-6 der Arztnummer ermittelt. Bei diesem Verfahren werden die Ziffern 1-6 von links
   * nach rechts abwechselnd mit 4 und 9 multipliziert. Die Summe dieser Produkte wird Modulo 10
   * berechnet. Die Prüfziffer ergibt sich aus der Differenz dieser Zahl zu 10 (ist die Differenz
   * 10, so ist die Prüfziffer 0)."
   */
  private static final Boolean EXPECTED_ANR_VALIDATION_CONFIG_ERROR =
      featureConf.getToggle(new AnrValidationConfigurationIsErrorToggle());

  public static final String WARNING_RESPONSE_HEADER =
      "252 erp-server \"Ungültige Arztnummer (LANR oder ZANR): Die übergebene Arztnummer entspricht"
          + " nicht den Prüfziffer-Validierungsregeln.\"";

  @Actor(name = "Leonie Hütter")
  private PatientActor patient;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafen;

  private static Stream<Arguments> afoInvalidANRComposer() {
    return ArgumentComposer.composeWith()
        .arguments("123456700", "die 7. Stelle: 6 statt 7 ist")
        .arguments("456456700", "die 7. Stelle: 5 statt 7 ist")
        .multiply(0, List.of(InsuranceTypeDe.BG, InsuranceTypeDe.PKV, InsuranceTypeDe.GKV))
        .multiply(1, PrescriptionAssignmentKind.class)
        .multiply(2, List.of("Gündüla Gunther", "Adelheid Ulmenwald"))
        .create();
  }

  private static Stream<Arguments> fhirInvalidANRComposer() {
    return ArgumentComposer.composeWith()
        .arguments("1234567", "sie 2 Stellen zu kurz ist")
        .arguments("1234567891", "sie eine Stelle zu lang ist")
        .arguments("12345668", "sie 1 Stellen zu kurz ist")
        .arguments("1234L6789", "sie einen Buchstaben enthält")
        .multiply(0, List.of(InsuranceTypeDe.BG, InsuranceTypeDe.PKV, InsuranceTypeDe.GKV))
        .multiply(1, PrescriptionAssignmentKind.class)
        .multiply(2, List.of("Gündüla Gunther", "Adelheid Ulmenwald"))
        .create();
  }

  private static Stream<Arguments> exceptionAnrComposer() {
    return ArgumentComposer.composeWith()
        .arguments(
            "555555", "A_23891-01 Ausnahmeregelung für Fachgruppennummern mit Ordnungszahl 1-9")
        .arguments(
            "5555550", "A_23891-01 Ausnahmeregelung für Fachgruppennummern mit Ordnungszahl 0")
        .multiply(0, List.of(InsuranceTypeDe.BG, InsuranceTypeDe.PKV, InsuranceTypeDe.GKV))
        .multiply(1, PrescriptionAssignmentKind.class)
        .multiply(2, List.of("Gündüla Gunther", "Adelheid Ulmenwald"))
        .create();
  }

  private static Stream<Arguments> exceptionNoAnrComposer() {
    return ArgumentComposer.composeWith()
        // Ausnahmen folgend A_23891 -> Hinweis: Keine Prüfziffer, da Fachgruppennummer in separatem
        // Element
        .arguments("A_23891 Hinweis: Keine Prüfziffer, da Fachgruppennummer in separatem Element")
        .multiply(0, List.of(InsuranceTypeDe.BG, InsuranceTypeDe.PKV, InsuranceTypeDe.GKV))
        .multiply(1, PrescriptionAssignmentKind.class)
        .create();
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_ANR_ZANR_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt {2} stellt ein E-Rezept mit invalider ANR: {3} für den"
              + " Kostenträger {0} als Darreichungsform {1} aus, da {4}!")
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst die ANR in Practitioner entsprechen A_24032"
          + " validiert und mit Error-Configuration und OperationOutcome antwortet oder"
          + " entsprechend A_24033 einen Warning Header zurück gibt")
  @MethodSource("afoInvalidANRComposer")
  void activateInvalidAnrZanrInPractitioner(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      DoctorActor doctors,
      String anr,
      String reason) {
    val doc = this.getDoctorNamed(doctors.getName());
    patient.changePatientInsuranceType(insuranceType);
    val activation = doc.performs(getIssuePrescription(assignmentKind, anr, doc));
    /**
     * nach: Technische Anlage zum Vertrag über den Datenaustausch (Anlage 6 BMV-Ä)
     * https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/aerzte/technische_anlagen___aktuell/20230522_TA1_21.pdf
     * "Aufbau der lebenslangen Arztnummer – LANR Die Arztnummer setzt sich aus insgesamt neun
     * Ziffern zusammen:" A_23891 -> bei Auffälligkeiten und Konfiguration 'Warnung' greift A_24033
     * A_23891 -> bei Auffälligkeiten und Konfiguration 'Error' greift A_24032
     */
    if (EXPECTED_ANR_VALIDATION_CONFIG_ERROR) {
      val requirementsSet = ErpAfos.A_24032;

      doc.attemptsTo(
          Verify.that(activation)
              .withOperationOutcome(requirementsSet)
              .hasResponseWith(returnCode(400, requirementsSet))
              .and(
                  operationOutcomeContainsInDetailText(
                      "Ungültige Arztnummer (LANR oder ZANR): Die übergebene Arztnummer entspricht"
                          + " nicht den Prüfziffer-Validierungsregeln.",
                      requirementsSet))
              .isCorrect());

    } else {
      val requirementsSet = ErpAfos.A_24033;
      doc.attemptsTo(
          Verify.that(activation)
              .withExpectedType(requirementsSet)
              .hasResponseWith(returnCode(252, requirementsSet))
              .andResponse(
                  headerContentContains("Warning", WARNING_RESPONSE_HEADER, requirementsSet))
              .isCorrect());
    }
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_ANR_ZANR_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt {2} stellt ein E-Rezept mit invalider ANR: {3} für den"
              + " Kostenträger {0} als Darreichungsform {1} aus, da {4}!")
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst die ANR in Practitioner FHIR-Konform validiert")
  @MethodSource("fhirInvalidANRComposer")
  void activateFhirInvalidAnrZanrInPractitioner(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      DoctorActor doctors,
      String anr,
      String reason) {
    RequirementsSet requirementsSet = FhirRequirements.FHIR_VALIDATION_ERROR;
    String detailedText =
        "Ungültige Arztnummer (LANR oder ZANR): Die übergebene Arztnummer entspricht nicht den"
            + " Prüfziffer-FHIR-Profilen.";
    val doc = this.getDoctorNamed(doctors.getName());
    patient.changePatientInsuranceType(insuranceType);

    val activation = doc.performs(getIssuePrescription(assignmentKind, anr, doc));
    /**
     * nach: Technische Anlage zum Vertrag über den Datenaustausch (Anlage 6 BMV-Ä)
     * https://www.gkv-datenaustausch.de/media/dokumente/leistungserbringer_1/aerzte/technische_anlagen___aktuell/20230522_TA1_21.pdf
     * "Aufbau der lebenslangen Arztnummer – LANR Die Arztnummer setzt sich aus insgesamt neun
     * Ziffern zusammen:" A_23891
     */
    doc.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(requirementsSet)
            .hasResponseWith(returnCode(400, requirementsSet))
            .and(operationOutcomeContainsInDetailText("FHIR-Validation error", requirementsSet))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_ANR_ZANR_03")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt {2} stellt ein E-Rezept mit ANR / ZANR: {3} + xy für den"
              + " Kostenträger {0} als Darreichungsform {1} aus, da {4}!")
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst die ANR 555555 + xy in Practitioner als Ausnahme"
          + " berücksichtigt")
  @MethodSource("exceptionAnrComposer")
  void activateValidAnrZanrInPractitioner(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      DoctorActor doctors,
      String anr,
      String reason) {
    int anrRandomPart;
    if (anr.length() < 7) {
      anrRandomPart = GemFaker.fakerAmount(100, 999);
    } else {
      anrRandomPart = GemFaker.fakerAmount(10, 99);
    }
    anr = anr.concat(String.valueOf(anrRandomPart));
    val doc = this.getDoctorNamed(doctors.getName());
    patient.changePatientInsuranceType(insuranceType);

    val activation = doc.performs(getIssuePrescription(assignmentKind, anr, doc));

    doc.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_23891)
            .hasResponseWith(returnCode(200))
            .isCorrect());
  }

  /**
   * A_23891 beschreibt u.a. einen Sonderfall, der ein Practitioner ohne ANR enthält. Bei dieser
   * Variante ist eine Fachgruppennummer in einem gesonderten Element hinterlgt:
   * Practitioner.qualification:ASV-Fachgruppennummer
   */
  @TestcaseId("ERP_TASK_ACTIVATE_VALID_ANR_ZANR_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt Adelheid Ulmenwald stellt ein E-Rezept mit invalider ANR"
              + " für den Kostenträger {0} als Darreichungsform {1} aus, da {2}!")
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst die ANR in Practitioner korrekt validiert und"
          + " die Ausnahme keine ANR durchleitet, da ASV berücksichtigt wird")
  @MethodSource("exceptionNoAnrComposer")
  void activatePractitionerWithoutANR(
      InsuranceTypeDe insuranceType, PrescriptionAssignmentKind assignmentKind, String reason) {

    val doc = this.getDoctorNamed("Adelheid Ulmenwald");
    SafeAbility.getAbility(doc, ProvideDoctorBaseData.class).setAsv(true);
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    KbvErpMedicationRequest medicationRequest;
    patient.changePatientInsuranceType(insuranceType);

    AccidentExtension accident = null;
    if (patient.getPatientInsuranceType().equals(InsuranceTypeDe.BG))
      accident = AccidentExtension.accidentAtWork().atWorkplace();

    medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withPatient(patient.getPatientData())
            .withInsurance(patient.getInsuranceCoverage())
            .withRequester(doc.getPractitioner())
            .withAccident(accident)
            .withMedication(medication)
            .fake();

    val medicalOrganization =
        KbvMedicalOrganizationBuilder.builder()
            .name("Arztpraxis Meyer")
            .bsnr("757299999")
            .phone("+490309876543")
            .email("info@praxis.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val assignerOrganization =
        KbvAssignerOrganizationBuilder.builder()
            .name(patient.getInsuranceCoverage().getName())
            .iknr(patient.getInsuranceCoverage().getIknr())
            .phone("0301111111")
            .build();

    val prescriptionId = "160.100.000.000.011.09";
    val kbvBundleBuilder =
        KbvErpBundleBuilder.forPrescription(prescriptionId)
            .practitioner(doc.getPractitioner())
            .statusKennzeichen(
                StatusKennzeichen
                    .ASV) // @ ASV-FachgruppenNummer in Practitioner.Qualifier: only Value "01" &
            // "10" allowed
            .patient(patient.getPatientData())
            .insurance(patient.getInsuranceCoverage())
            .medication(medication)
            .medicationRequest(medicationRequest)
            .assigner(assignerOrganization)
            .medicalOrganization(medicalOrganization);

    val issuePrescription =
        IssuePrescription.forPatient(patient)
            .ofAssignmentKind(assignmentKind)
            .withKbvBundleFrom(kbvBundleBuilder);

    val activation = doc.performs(issuePrescription);

    doc.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_23891)
            .hasResponseWith(returnCode(200))
            .isCorrect());
    SafeAbility.getAbility(doc, ProvideDoctorBaseData.class).setAsv(false);
  }

  private IssuePrescription getIssuePrescription(
      PrescriptionAssignmentKind assignmentKind, String anr, DoctorActor doctorActor) {
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    AccidentExtension accident = null;
    if (patient.getPatientInsuranceType().equals(InsuranceTypeDe.BG))
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
                            KbvNamingSystem.BASE_ANR.matches(it)
                                || KbvNamingSystem.ZAHNARZTNUMMER.matches(it))
                    .findFirst()
                    .orElseThrow()
                    .setValue(anr))
        .withKbvBundleFrom(kbvBundleBuilder);
  }
}
