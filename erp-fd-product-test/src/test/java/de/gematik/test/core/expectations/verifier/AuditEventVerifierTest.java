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

package de.gematik.test.core.expectations.verifier;

import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.bundleContainsLogFor;
import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.bundleDoesNotContainLogFor;
import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.hasAuditEventAtPosition;
import static de.gematik.test.erezept.fhir.testutil.ErxFhirTestResourceUtil.createErxAuditEvent;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.ErpFdTestsuiteFactory;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEvent.Representation;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ErxFhirTestResourceUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidenceResult;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmService;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Bundle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Slf4j
class AuditEventVerifierTest extends ErpFhirParsingTest {

  private static final String AUDIT_EVENT_BUNDLE_PATH_FROM_XML =
      "fhir/valid/erp/1.4.0/auditeventbundle/561f4a7e-0616-4c92-b6d5-91217aea136f.xml";
  private static final String AUDIT_EVENT_BUNDLE_PATH_FROM_JSON =
      "fhir/valid/erp/1.4.0/auditeventbundle/561f4a7e-0616-4c92-b6d5-91217aea136f.json";
  private PatientActor patient;

  private PharmacyActor pharmacy;

  private ErxAuditEventBundle firstErxAuditEventBundle;
  private ErxAuditEventBundle secondErxAuditEventBundle;

  @BeforeEach
  void setupReporter() {
    CoverageReporter.getInstance().startTestcase("not needed");
    StopwatchProvider.init();
    val config = ErpFdTestsuiteFactory.create();
    pharmacy = new PharmacyActor("Am Flughafen");
    val pharmacyConfig = config.getPharmacyConfig(pharmacy.getName());
    val smcb = config.getSmcbByICCSN(pharmacyConfig.getSmcbIccsn());
    val useSmcb = UseSMCB.itHasAccessTo(smcb);
    pharmacy.can(useSmcb);

    patient = new PatientActor("Hanna Bäcker");
    val patientConfig = config.getPatientConfig(patient.getName());
    val egk = config.getEgkByICCSN(patientConfig.getEgkIccsn());
    givenThat(patient).can(ProvideEGK.sheOwns(egk));
    firstErxAuditEventBundle =
        getDecodedFromPath(ErxAuditEventBundle.class, AUDIT_EVENT_BUNDLE_PATH_FROM_XML);
    secondErxAuditEventBundle =
        getDecodedFromPath(ErxAuditEventBundle.class, AUDIT_EVENT_BUNDLE_PATH_FROM_JSON);
  }

  @ParameterizedTest
  @EnumSource(Representation.class)
  void shouldDetectAllRepresentations(Representation rep) {
    val checksum =
        VsdmExamEvidence.asOnlineMode(VsdmService.instantiateWithTestKey(), patient.getEgk())
            .build(VsdmExamEvidenceResult.NO_UPDATES)
            .getCheckDigit()
            .orElseThrow();

    val verifier = AuditEventVerifier.forPharmacy(pharmacy).withChecksum(checksum).build();
    val testData =
        ErxFhirTestResourceUtil.createErxAuditEventBundle(
            pharmacy.getTelematikId(), pharmacy.getCommonName());
    verifier.contains(rep, Instant.now()).apply(testData);
  }

  @Test
  void shouldFailWhileDetectingWithEmptyAuditEventBundle() {
    val checksum =
        VsdmExamEvidence.asOnlineMode(VsdmService.instantiateWithTestKey(), patient.getEgk())
            .build(VsdmExamEvidenceResult.NO_UPDATES)
            .getCheckDigit()
            .orElseThrow();

    val verifier = AuditEventVerifier.forPharmacy(pharmacy).withChecksum(checksum).build();

    val testData = new ErxAuditEventBundle();

    val action = verifier.contains(Representation.PHARMACY_GET_TASK_SUCCESSFUL, Instant.now());
    assertThrows(AssertionError.class, () -> action.apply(testData));
  }

  @Test
  void shouldFailWhileDetectingWithWrongAuditEventBundle() {
    val checksum =
        VsdmExamEvidence.asOnlineMode(VsdmService.instantiateWithTestKey(), patient.getEgk())
            .build(VsdmExamEvidenceResult.NO_UPDATES)
            .getCheckDigit()
            .orElseThrow();

    val verifier = AuditEventVerifier.forPharmacy(pharmacy).withChecksum(checksum).build();
    val erxAuditEventBundle = new ErxAuditEventBundle();
    erxAuditEventBundle.addEntry(
        new Bundle.BundleEntryComponent()
            .setResource(
                createErxAuditEvent(
                    "TestText",
                    TelematikID.from("telematikId"),
                    "agentName",
                    AuditEvent.AuditEventAction.R)));

    val action = verifier.contains(Representation.PHARMACY_GET_TASK_SUCCESSFUL, Instant.now());
    assertThrows(AssertionError.class, () -> action.apply(erxAuditEventBundle));
  }

  @Test
  void shouldCompareAuditEventAtPositionCorrect() {
    val auditEventNo3 = firstErxAuditEventBundle.getAuditEvents().get(3);
    val step = hasAuditEventAtPosition(auditEventNo3, 3);
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldThrowWhileCompareAuditEventAtPositionCorrect() {
    val auditEventNo2 = firstErxAuditEventBundle.getAuditEvents().get(2);
    val step = hasAuditEventAtPosition(auditEventNo2, 3);
    assertThrows(AssertionError.class, () -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldVerifyContainedText() {
    val step =
        bundleContainsLogFor(
            PrescriptionId.from("160.000.006.746.135.23"),
            "Praxis Blôch-BauerTEST-ONLY hat das Rezept mit der ID\n"
                + "                        160.000.006.746.135.23 eingestellt.");
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldThrowWhileVerifyContainedText() {
    val step =
        bundleContainsLogFor(
            PrescriptionId.from("160.000.023.898.863.48"),
            "Sina Karla Gräfin TestMänn downloaded a prescription");
    assertThrows(AssertionError.class, () -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldNotContainLogForGivenPrescriptionIdAndLogContent() {
    val prescriptionId = PrescriptionId.from("160.000.023.898.864.45");
    val logContent =
        "Die Löschinformation zum E-Rezept konnte nicht in die Patientenakte übermittelt werden.";
    val step = bundleDoesNotContainLogFor(prescriptionId, logContent);

    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldNotContainLogForAnotherPrescriptionId() {
    val prescriptionId = PrescriptionId.from("160.000.023.898.863.48");
    val logContent =
        "Die Löschinformation zum E-Rezept konnte nicht in die Patientenakte übermittelt werden.";
    val step = bundleDoesNotContainLogFor(prescriptionId, logContent);

    assertDoesNotThrow(() -> step.apply(secondErxAuditEventBundle));
  }

  @Test
  void shouldNotContainLogForInvalidContent() {
    val prescriptionId = PrescriptionId.from("160.000.023.898.864.45");
    val logContent = "Some random log content that shouldn't exist in the audit events";
    val step = bundleDoesNotContainLogFor(prescriptionId, logContent);

    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldContainLogContentInAuditEventBundle() {
    val logContent = "Praxis Blôch-BauerTEST-ONLY hat das Rezept mit der ID";
    val step = AuditEventVerifier.bundleContainsLog(logContent);
    assertDoesNotThrow(() -> step.apply(firstErxAuditEventBundle));
  }

  @Test
  void shouldThrowWhenLogContentNotInAuditEventBundle() {
    val logContent = "Nicht existierender Logeintrag";
    val step = AuditEventVerifier.bundleContainsLog(logContent);
    assertThrows(AssertionError.class, () -> step.apply(firstErxAuditEventBundle));
  }
}
