/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.core.expectations.verifier;

import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.*;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.ErpFdTestsuiteFactory;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent.Representation;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEventBundle;
import de.gematik.test.erezept.fhir.testutil.ErxFhirTestResourceUtil;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidenceResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class AuditEventVerifierTest extends ParsingTest {

  private static final String AUDIT_EVENT_BUNDLE_PATH_FROM_XML =
      "fhir/valid/erp/1.2.0/auditeventbundle/41f94920-14b5-426a-8859-d045270e63a2.xml";
  private static final String AUDIT_EVENT_BUNDLE_PATH_FROM_JSON =
      "fhir/valid/erp/1.2.0/auditeventbundle/2d4ba5fa-d0ff-4b59-b1c4-8849bd83a971.json";
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

  @Test
  void shouldDetectAllRepresentations() {
    val checksum =
        VsdmExamEvidence.asOnlineTestMode(patient.getEgk())
            .generate(VsdmExamEvidenceResult.NO_UPDATES)
            .getChecksum()
            .orElseThrow();

    val verifier = AuditEventVerifier.forPharmacy(pharmacy).withChecksum(checksum).build();
    val testData =
        ErxFhirTestResourceUtil.createErxAuditEventBundle(
            pharmacy.getTelematikId(), pharmacy.getCommonName());
    for (Representation rep : Representation.values()) {
      verifier.contains(rep).apply(testData);
    }
    verifier.firstElementCorrespondsTo(Representation.PHARMACY_GET_TASK_SUCCESSFUL).apply(testData);
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
}
