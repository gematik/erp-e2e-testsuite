/*
 * Copyright 2023 gematik GmbH
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

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.ErpFdTestsuiteFactory;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent.Representation;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidenceResult;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
class AuditEventVerifierTest {
  private PatientActor patient;

  private PharmacyActor pharmacy;

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
        FhirTestResourceUtil.createErxAuditEventBundle(
            pharmacy.getTelematikId(), pharmacy.getCommonName());
    for (Representation rep : Representation.values()) {
      verifier.contains(rep).apply(testData);
    }
    verifier.firstElementCorrespondsTo(Representation.PHARMACY_GET_TASK_SUCCESSFUL).apply(testData);
  }
}
